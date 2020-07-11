package com.example.uber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PassengersActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button btnRequest;

    private boolean isRequestCancelled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passengers);
        setTitle("Passenger");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequest = findViewById(R.id.btnRequest);
        btnRequest.setOnClickListener(this);

        ParseQuery<ParseObject> cabRequestQuery = ParseQuery.getQuery("RequestCab");
        cabRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        cabRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() > 0 && e == null){
                    isRequestCancelled = false;
                    btnRequest.setText("Cancel your request");
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateCameraPassengerLocation(location);
            }
        };

        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission
                        (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(PassengersActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentPassengerLocation != null) {
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.checkSelfPermission(PassengersActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (currentPassengerLocation != null) {
                    updateCameraPassengerLocation(currentPassengerLocation);
                }
            }
        }
    }

    private void updateCameraPassengerLocation(Location plocation) {
        LatLng passengersLocation = new LatLng(plocation.getLatitude(), plocation.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengersLocation, 17));
        mMap.addMarker(new MarkerOptions().position(passengersLocation).title("You are here!!"));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRequest:

                if (isRequestCancelled == true){
                if (ActivityCompat.checkSelfPermission(PassengersActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location passengerscurrentlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (passengerscurrentlocation != null){
                        ParseObject requestCab = new ParseObject("RequestCab");
                        requestCab.put("username", ParseUser.getCurrentUser().getUsername());

                        ParseGeoPoint passengerLocation = new ParseGeoPoint(passengerscurrentlocation.getLatitude(), passengerscurrentlocation.getLongitude());
                        requestCab.put("passengersLocation", passengerLocation);

                        requestCab.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null){
                                    Toast.makeText(PassengersActivity.this, "A cab request is sent", Toast.LENGTH_LONG).show();

                                    btnRequest.setText("Cancel your request");
                                    isRequestCancelled = false;
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show();
                    }
                }
                }
                else {
                    ParseQuery<ParseObject> cabRequestQuery = ParseQuery.getQuery("RequestCab");
                    cabRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                    cabRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> requestList, ParseException e) {
                            if (requestList.size() > 0 && e == null){
                                isRequestCancelled = true;
                                btnRequest.setText("Request a new cab");

                                for (ParseObject cabRequest : requestList){
                                    cabRequest.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null){
                                                Toast.makeText(PassengersActivity.this, "Request deleted:(", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }

                break;
        }
    }
}
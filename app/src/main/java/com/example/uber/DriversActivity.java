package com.example.uber;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriversActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private Button btnConfirmRide, btnCancelRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers);
        setTitle("Driver");

        btnConfirmRide = findViewById(R.id.btnConfirmRide);
        btnConfirmRide.setOnClickListener(this);
        btnCancelRide = findViewById(R.id.btnCancelRide);
        btnCancelRide.setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

       @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLatitude", 0),
                getIntent().getDoubleExtra("dLongitude", 0));

        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLatitude", 0),
                getIntent().getDoubleExtra("pLongitude", 0));

           LatLngBounds.Builder builder = new LatLngBounds.Builder();
           Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver"));
           Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger"));

           ArrayList<Marker> myMarkers = new ArrayList<>();
           myMarkers.add(driverMarker);
           myMarkers.add(passengerMarker);

           for (Marker marker : myMarkers) {
               builder.include(marker.getPosition());
           }

           LatLngBounds bounds = builder.build();

           CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 150);
           mMap.animateCamera(cameraUpdate);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btnConfirmRide:
                ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCab");
                carRequestQuery.whereEqualTo("username", getIntent().getStringExtra("rUsername"));
                carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() > 0 && e == null){
                            for (ParseObject cabRequest : objects){

                                cabRequest.put("requestAccepted", true);
                                cabRequest.put("driverOfMe", ParseUser.getCurrentUser().getUsername());

                                cabRequest.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null){
                                            Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                                    "http://maps.google.com/maps?saddr=" + getIntent().getDoubleExtra("dLatitude", 0)
                                                            + "," + getIntent().getDoubleExtra("dLongitude", 0) + "&" +
                                                    "daddr=" + getIntent().getDoubleExtra("pLatitude", 0) + "," +
                                                    getIntent().getDoubleExtra("pLongitude", 0)));
                                            startActivity(googleIntent);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
                break;

            case R.id.btnCancelRide:
                Intent intent = new Intent(this, DriverRequestList.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
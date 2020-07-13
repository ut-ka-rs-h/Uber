package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PassengersActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button btnRequest, btnPasLogout, btnBeep;

    private boolean isRequestCancelled = true;

    private boolean isCabReady = false;

    private Timer timer;

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
        btnPasLogout = findViewById(R.id.btnPasLogout);
        btnBeep = findViewById(R.id.btnBeep);
        btnRequest.setOnClickListener(this);
        btnPasLogout.setOnClickListener(this);
        btnBeep.setOnClickListener(this);

        ParseQuery<ParseObject> cabRequestQuery = ParseQuery.getQuery("RequestCab");
        cabRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        cabRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() > 0 && e == null) {
                    isRequestCancelled = false;
                    btnRequest.setText("Cancel your request");
                    btnRequest.setBackgroundColor(Color.RED);

                    getDriverUpdate();
                }
            }
        });
    }


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

            ActivityCompat.requestPermissions(PassengersActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
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

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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
        if (isCabReady == false){
            LatLng passengersLocation = new LatLng(plocation.getLatitude(), plocation.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengersLocation, 17));
            mMap.addMarker(new MarkerOptions().position(passengersLocation).title("You are here!!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRequest:

                if (isRequestCancelled == true) {
                    if (ActivityCompat.checkSelfPermission(PassengersActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        Location passengerscurrentlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (passengerscurrentlocation != null) {
                            ParseObject requestCab = new ParseObject("RequestCab");
                            requestCab.put("username", ParseUser.getCurrentUser().getUsername());

                            ParseGeoPoint passengerLocation = new ParseGeoPoint(passengerscurrentlocation.getLatitude(), passengerscurrentlocation.getLongitude());
                            requestCab.put("passengersLocation", passengerLocation);

                            requestCab.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
//                                    Toast.makeText(PassengersActivity.this, "A cab request is sent", Toast.LENGTH_LONG).show();

                                        btnRequest.setText("Cancel your request");
                                        btnRequest.setBackgroundColor(Color.RED);
                                        isRequestCancelled = false;
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    ParseQuery<ParseObject> cabRequestQuery = ParseQuery.getQuery("RequestCab");
                    cabRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                    cabRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> requestList, ParseException e) {
                            if (requestList.size() > 0 && e == null) {
                                isRequestCancelled = true;
                                btnRequest.setText("Request a new cab");
                                btnRequest.setBackgroundColor(Color.parseColor("#009000"));

                                for (ParseObject cabRequest : requestList) {
                                    cabRequest.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
//                                                Toast.makeText(PassengersActivity.this, "Request deleted:(", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }

                break;

            case R.id.btnPasLogout:
//                Toast.makeText(PassengersActivity.this, "Logging Out", Toast.LENGTH_SHORT).show();
                final AlertDialog.Builder builder = new AlertDialog.Builder(PassengersActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ParseUser.logOutInBackground(new LogOutCallback() {
                            @Override
                            public void done(ParseException e) {
                                Intent intent = new Intent(PassengersActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.create();
                builder.show();
                break;

            case R.id.btnBeep:
                getDriverUpdate();
                break;
        }
    }

    private void getDriverUpdate() {

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ParseQuery<ParseObject> cabRequestQuery = ParseQuery.getQuery("RequestCab");
                cabRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                cabRequestQuery.whereEqualTo("requestAccepted", true);
                cabRequestQuery.whereExists("driverOfMe");

                cabRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() > 0 && e == null) {

                            isCabReady = true;
                            for (final ParseObject requestObject : objects) {

                                ParseQuery<ParseUser> driver = ParseUser.getQuery();
                                driver.whereEqualTo("username", requestObject.getString("driverOfMe"));
                                driver.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> drivers, ParseException e) {
                                        if (drivers.size() > 0 && e == null) {
                                            for (ParseUser driverOfRequest : drivers) {
                                                ParseGeoPoint driverOfRequestLocation = driverOfRequest.getParseGeoPoint("driverLocation");
                                                if (ActivityCompat.checkSelfPermission(PassengersActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PassengersActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                    Location passengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                    ParseGeoPoint pLocationAsParseGeoPoint = new ParseGeoPoint(passengerLocation.getLatitude(), passengerLocation.getLongitude());

                                                    double kmsDistance = driverOfRequestLocation.distanceInKilometersTo(pLocationAsParseGeoPoint);

                                                    if (kmsDistance < 0.5){
                                                        Toast.makeText(PassengersActivity.this, "Your cab is at your location :)", Toast.LENGTH_SHORT).show();

                                                        requestObject.deleteInBackground(new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                isCabReady = false;
                                                                isRequestCancelled = true;
                                                                btnRequest.setText("Request a new cab");
                                                                btnRequest.setBackgroundColor(Color.parseColor("#009000"));
                                                            }
                                                        });
                                                    }
                                                    else {
                                                        float roundedDistance = Math.round(kmsDistance * 10);
                                                        Toast.makeText(PassengersActivity.this, requestObject.getString("driverOfMe") + " is " + roundedDistance / 10 + "Kms away from your location", Toast.LENGTH_SHORT).show();


                                                        LatLng dLocation = new LatLng(driverOfRequestLocation.getLatitude(),
                                                                driverOfRequestLocation.getLongitude());

                                                        LatLng pLocation = new LatLng(pLocationAsParseGeoPoint.getLatitude(),
                                                                pLocationAsParseGeoPoint.getLongitude());

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

                                                }

                                            }
                                        }
                                    }
                                });
                            }
                        }
                        else {isCabReady = false;
                        }
                    }
                });
            }
        }, 0, 3000);


    }

}



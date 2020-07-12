package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestList extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private ArrayList<String> nearbyDriverRequests;
    private ArrayAdapter adapter;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private SwipeRefreshLayout swipeRefresh;

    private ArrayList<Double> passengersLat;
    private ArrayList<Double> passengersLong;

    private ArrayList<String> requestCabUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        listView = findViewById(R.id.requestListView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        nearbyDriverRequests = new ArrayList<>();
        passengersLat = new ArrayList<>();
        passengersLong = new ArrayList<>();

        requestCabUserName = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearbyDriverRequests);

        listView.setAdapter(adapter);

        nearbyDriverRequests.clear();


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            updateRequestListView(location);
                        }
                    };

                    permissionCheckAndUpdate();

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission
                (DriverRequestList.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try {locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void permissionCheckAndUpdate() {
        if (ActivityCompat.checkSelfPermission
                (DriverRequestList.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission
                        (DriverRequestList.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DriverRequestList.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

        }
        else {
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentDriverLocation != null) {
                updateRequestListView(currentDriverLocation);
            }
        }

        listView.setOnItemClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drivers_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Intent intent = new Intent(DriverRequestList.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        return super.onOptionsItemSelected(item);
    }


    private void updateRequestListView(Location driverLocation) {

        if (driverLocation != null){


            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());

            final ParseQuery<ParseObject> requestCabQuery = ParseQuery.getQuery("RequestCab");
            requestCabQuery.whereNear("passengersLocation", driverCurrentLocation);
            requestCabQuery.whereDoesNotExist("driverOfMe");
            requestCabQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null){
                        if (objects.size() > 0){

                            if (nearbyDriverRequests.size() > 0){
                                nearbyDriverRequests.clear();
                            }
                            if (passengersLat.size() > 0){
                                passengersLat.clear();
                            }
                            if (passengersLong.size() > 0){
                                passengersLong.clear();
                            }
                            if (requestCabUserName.size() > 0){
                                requestCabUserName.clear();
                            }
                            for (ParseObject nearRequest : objects){

                                ParseGeoPoint plocation = (ParseGeoPoint) nearRequest.get("passengersLocation");

                                Double kmsDistanceToPassenger = driverCurrentLocation.distanceInKilometersTo(plocation);
                                float roundedDistance = Math.round(kmsDistanceToPassenger * 10);

                                nearbyDriverRequests.add("There are " + roundedDistance / 10 + "Kms to " + nearRequest.get("username"));

                                passengersLat.add(plocation.getLatitude());
                                passengersLong.add(plocation.getLongitude());
                                requestCabUserName.add(nearRequest.get("username") + "");
                            }
                        }
                        else {
                            Toast.makeText(DriverRequestList.this, "There are no request yet:(", Toast.LENGTH_LONG).show();
                        }
                        adapter.notifyDataSetChanged();
                        if (swipeRefresh.isRefreshing()){
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.checkSelfPermission(DriverRequestList.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateRequestListView(currentDriverLocation);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (cdLocation != null){
                Intent intent = new Intent(DriverRequestList.this, DriversActivity.class);
                intent.putExtra("dLatitude", cdLocation.getLatitude());
                intent.putExtra("dLongitude", cdLocation.getLongitude());
                intent.putExtra("pLatitude", passengersLat.get(position));
                intent.putExtra("pLongitude", passengersLong.get(position));
                intent.putExtra("rUsername", requestCabUserName.get(position));

                startActivity(intent);
            }
        }
    }
}
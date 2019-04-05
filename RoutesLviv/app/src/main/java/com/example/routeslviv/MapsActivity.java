package com.example.routeslviv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.example.routeslviv.DirectionsAPI.FetchURL;
import com.example.routeslviv.DirectionsAPI.TaskLoadedCallback;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private StatusSensors statusSensors;
    private Polyline currentPolyline;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private LatLng fromLatLng;
    private LatLng toLatLng;
    private String house;
    private float DEFAULT_ZOOM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle argumets = getIntent().getExtras();
        house = argumets.getString("house");
        toLatLng = new LatLng(argumets.getFloat("latitude"), argumets.getFloat("longitude"));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(getLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setSmallestDisplacement(3);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        statusSensors = new StatusSensors(this, mMap, fusedLocationProviderClient, locationRequest, getLocationCallback);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocationUI();
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, getLocationCallback, Looper.myLooper());
        }

        mMap.addMarker(new MarkerOptions().position(toLatLng).title(house));
    }

    private String getURL(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null) currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    private void getLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    public void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted || statusSensors.GPSEnabled) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    LocationCallback getLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            try {
                List<Location> locationList = locationResult.getLocations();
                if (locationPermissionGranted || (statusSensors.GPSEnabled && statusSensors.NetworkEnabled)) {
                    if (locationList.size() > 0) {
                        if (DEFAULT_ZOOM == 0) {
                            DEFAULT_ZOOM = 10;
                        } else {
                            DEFAULT_ZOOM = mMap.getCameraPosition().zoom;
                        }
                        Location location = locationList.get(locationList.size() - 1);
                        fromLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fromLatLng, DEFAULT_ZOOM));
                        new FetchURL(MapsActivity.this).execute(getURL(fromLatLng, toLatLng, "walking"), "walking");
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(49.840821, 24.026642), DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            } catch(SecurityException e)  {
                Log.e("Exception: %s", e.getMessage());
            }
        }
    };
}
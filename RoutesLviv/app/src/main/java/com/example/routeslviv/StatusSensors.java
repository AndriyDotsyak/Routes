package com.example.routeslviv;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;

public class StatusSensors implements LocationListener {
    boolean GPSEnabled;
    boolean NetworkEnabled;

    private Context context;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private LocationManager locationManager;
    private MapsActivity mapsActivity = new MapsActivity();

    StatusSensors(Context context, GoogleMap mMap, FusedLocationProviderClient fusedLocationProviderClient, LocationRequest locationRequest, LocationCallback locationCallback) {
        this.context = context;
        this.mMap = mMap;
        this.fusedLocationProviderClient = fusedLocationProviderClient;
        this.locationRequest = locationRequest;
        this.locationCallback = locationCallback;

        statusSensors();

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        GPSEnabled = true;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocationUI();
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        GPSEnabled = false;
        Toast.makeText(context.getApplicationContext(), "Turn on GPS", Toast.LENGTH_LONG).show();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (GPSEnabled) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void statusSensors() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            NetworkEnabled = activeNetwork.isConnectedOrConnecting();
        } else {
            NetworkEnabled = false;
        }

        if (!NetworkEnabled && !GPSEnabled) {
            Toast.makeText(context.getApplicationContext(), "Turn on internet and GPS", Toast.LENGTH_LONG).show();
        } else {
            if (!NetworkEnabled) {
                Toast.makeText(context.getApplicationContext(), "Turn on internet", Toast.LENGTH_LONG).show();
            }
            if (!GPSEnabled) {
                Toast.makeText(context.getApplicationContext(), "Turn on GPS", Toast.LENGTH_LONG).show();
            }
        }
    }
}
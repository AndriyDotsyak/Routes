package com.example.routeslviv;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, TaskLoadedCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Polyline currentPolyline;
    private LatLng fromLatLng;
    private LatLng toLatLng;
    private String house;

    private boolean GPSEnabled;
    private boolean NetworkEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle argumets = getIntent().getExtras();
        house = argumets.getString("house");
        toLatLng = new LatLng(argumets.getFloat("latitude"), argumets.getFloat("longitude"));
        providerStatus();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        Log.d("main", String.valueOf(toLatLng));
        mMap.addMarker(new MarkerOptions().position(toLatLng).title(house));
    }

    @Override
    public void onLocationChanged(Location location) {
        fromLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(fromLatLng, 11);
        mMap.animateCamera(cameraUpdate);
        new FetchURL(MapsActivity.this).execute(getURL(fromLatLng, toLatLng, "walking"), "walking");
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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

    private void providerStatus() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        NetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (NetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, MapsActivity.this);
            } else if (GPSEnabled){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, MapsActivity.this);
            }
            return;
        }
    }
}
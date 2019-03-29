package com.example.routeslviv;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SwitchHouses extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_houses);
        ButterKnife.bind(this);

        checkLocationPermission();
    }

    @OnClick({R.id.btn_makeIT, R.id.btn_avtoStation, R.id.btn_railwayStation})
    void onSaveClick(View view) {
        switch (view.getId()) {
            case R.id.btn_makeIT:
                Intent intent = new Intent("android.intent.action.maps");
                intent.putExtra("latitude", 49.809123f);
                intent.putExtra("longitude", 24.017877f);
                intent.putExtra("house", "MakeIT");
                startActivity(intent);
                break;
            case R.id.btn_avtoStation:
                intent = new Intent("android.intent.action.maps");
                intent.putExtra("latitude", 49.786774f);
                intent.putExtra("longitude", 24.016282f);
                intent.putExtra("house", "Автовокзал");
                startActivity(intent);
                break;
            case R.id.btn_railwayStation:
                intent = new Intent("android.intent.action.maps");
                intent.putExtra("latitude", 49.839750f);
                intent.putExtra("longitude", 23.994366f);
                intent.putExtra("house", "Залізничний вокзал");
                startActivity(intent);
                break;
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(SwitchHouses.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            return false;
        } else {
            return true;
        }
    }
}

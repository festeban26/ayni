package com.festeban26.ayni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.festeban26.ayni.R;
import com.festeban26.ayni.utils.ResultCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

public class SelectMapLocationActivity extends AppCompatActivity{

    private GoogleMap mMap;
    private String mCity;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_map_location);

        Toolbar toolbar = findViewById(R.id.Toolbar_SelectMapLocationActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {
            mCity = getIntent().getStringExtra("city");
            setView_Map();
            setView_SelectButton();
        }
    }

    private void setView_Map(){

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Fragment_SelectMapLocationActivity_Map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    // left, top, right, bottom
                    mMap.setPadding(10, 120, 10, 100);
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    mMap.getUiSettings().setTiltGesturesEnabled(false);

                    LatLng latLng = new LatLng(-1.160171, -78.432915);
                    float zoom = 6.5f;

                    if (mCity != null) {
                        switch (mCity) {
                            case "Ambato":
                                latLng = new LatLng(-1.241667, -78.619720);
                                zoom = 13;
                                break;
                            case "Ibarra":
                                latLng = new LatLng(0.35171, -78.12233);
                                zoom = 13;
                                break;
                            case "Quito":
                                latLng = new LatLng(-0.180653, -78.467834);
                                zoom = 12;
                                break;
                            case "Tena":
                                latLng = new LatLng(-0.996297, -77.813606);
                                zoom = 15;
                                break;
                        }
                    }

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(zoom)
                            .build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                    mMap.moveCamera(cameraUpdate);
                }
            });
        }
    }

    private void setView_SelectButton(){
        mButton = findViewById(R.id.Button_SelectMapLocationActivity);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    LatLng selectedLatLng = mMap.getCameraPosition().target;
                    Intent intent = new Intent();
                    intent.putExtra("latitude", selectedLatLng.latitude);
                    intent.putExtra("longitude", selectedLatLng.longitude);
                    setResult(ResultCodes.SUCCESS, intent);
                    finish();
                }
            }
        });
    }

    /**
     * Back arrow button pressed
     *
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(ResultCodes.CANCELED);
        finish();
    }
}

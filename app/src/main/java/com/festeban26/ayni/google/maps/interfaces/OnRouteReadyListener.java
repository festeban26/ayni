package com.festeban26.ayni.google.maps.interfaces;

import com.festeban26.ayni.google.maps.helpers.MapsHelper;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface OnRouteReadyListener {

    void onSuccess(List<LatLng> polylinePath, long distanceInMeters, long durationInSeconds, MapsHelper mapsHelper);


    void onFailure();
}

package com.festeban26.ayni.google.maps;

import com.festeban26.ayni.google.maps.model.Route;
import com.google.maps.model.DirectionsResult;

public interface OnRouteResponseListener {

    void onRouteResults(Route result, DirectionsResult directionsResult);
}

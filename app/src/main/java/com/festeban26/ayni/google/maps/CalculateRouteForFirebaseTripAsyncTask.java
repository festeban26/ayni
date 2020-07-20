package com.festeban26.ayni.google.maps;

import android.os.AsyncTask;

import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.Location;
import com.festeban26.ayni.google.maps.helpers.MapsHelper;
import com.festeban26.ayni.google.maps.interfaces.OnRouteReadyListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CalculateRouteForFirebaseTripAsyncTask extends AsyncTask<Void, Void, DirectionsResult> {

    private final static int TIMEOUT = 1;

    FirebaseTrip mFirebaseTrip;
    private String mGoogleMapsKey;
    private OnRouteReadyListener mDelegate;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private MapsHelper mMapsHelper;

    public CalculateRouteForFirebaseTripAsyncTask(FirebaseTrip firebaseTrip, String googleMapsKey, OnRouteReadyListener delegate) {
        mFirebaseTrip = firebaseTrip;
        mGoogleMapsKey = googleMapsKey;
        mDelegate = delegate;

        Location originLocation = mFirebaseTrip.getOrigin();
        Location destinationLocation = mFirebaseTrip.getDestination();

        mOriginLatLng = new LatLng(originLocation.getLat(), originLocation.getLng());
        mDestinationLatLng = new LatLng(destinationLocation.getLat(), destinationLocation.getLng());

        mMapsHelper = new MapsHelper(firebaseTrip);
    }

    @Override
    protected DirectionsResult doInBackground(Void... voids) {
        GeoApiContext geoApiContext = new GeoApiContext();
        geoApiContext.setApiKey(mGoogleMapsKey)
                .setConnectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .setReadTimeout(TIMEOUT, TimeUnit.SECONDS)
                .setWriteTimeout(TIMEOUT, TimeUnit.SECONDS);

        com.google.maps.model.LatLng compatOrigin
                = new com.google.maps.model.LatLng(mOriginLatLng.latitude, mOriginLatLng.longitude);
        com.google.maps.model.LatLng compatDestination
                = new com.google.maps.model.LatLng(mDestinationLatLng.latitude, mDestinationLatLng.longitude);

        try {
            // If passengers, no waypoints
            if (!mMapsHelper.tripHasWaypoints()) {
                return DirectionsApi
                        .newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(compatOrigin)
                        .destination(compatDestination)
                        .await();
            } else {
                return DirectionsApi
                        .newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(compatOrigin)
                        .destination(compatDestination)
                        .waypoints(mMapsHelper.getWaypointsAsCompatLatLng())
                        .optimizeWaypoints(true)
                        .await();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(DirectionsResult directionsResult) {
        super.onPostExecute(directionsResult);

        boolean onSuccess = false;

        if (directionsResult != null) {
            DirectionsRoute directionsRoute = directionsResult.routes[0];
            if (directionsRoute != null) {
                DirectionsLeg[] legs = directionsRoute.legs;
                if (legs != null) {
                    long distance = calculateTotalDistanceInMeters(legs);
                    long duration = calculateTotalDurationInSeconds(legs);
                    List<com.google.android.gms.maps.model.LatLng> decodedPath
                            = PolyUtil.decode(directionsRoute.overviewPolyline.getEncodedPath());
                    mDelegate.onSuccess(decodedPath, distance, duration, mMapsHelper);
                    onSuccess = true;
                }
            }
        }
        // If got no result from API
        if(!onSuccess)
            mDelegate.onFailure();
    }

    private static long calculateTotalDistanceInMeters(DirectionsLeg[] legs) {
        long distanceInMeters = 0;

        for (DirectionsLeg leg : legs)
            distanceInMeters += leg.distance.inMeters;

        return distanceInMeters;
    }

    private static long calculateTotalDurationInSeconds(DirectionsLeg[] legs) {
        long durationInSeconds = 0;

        for (DirectionsLeg leg : legs)
            durationInSeconds += leg.duration.inSeconds;

        return durationInSeconds;
    }
}

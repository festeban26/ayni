package com.festeban26.ayni.google.maps;

import android.os.AsyncTask;

import com.festeban26.ayni.firebase.model.Location;
import com.festeban26.ayni.google.maps.model.Route;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CalculateRouteAsyncTask extends AsyncTask<Void, Void, DirectionsResult> {

    private final static int TIMEOUT = 1;

    private LatLng mOrigin;
    private LatLng mDestination;
    private List<LatLng> mWaypoints;
    private boolean mOptimizeWaypoints;
    private String mGoogleMapsKey;
    private OnRouteResponseListener mDelegate;

    public CalculateRouteAsyncTask(LatLng origin, LatLng destination, String googleMapsKey,
                                   OnRouteResponseListener delegate) {
        mOrigin = origin;
        mDestination = destination;
        mDelegate = delegate;
        mGoogleMapsKey = googleMapsKey;
    }

    public CalculateRouteAsyncTask(LatLng origin, LatLng destination, @Nullable List<LatLng> waypoints,
                                   boolean optimizeWaypoints, String googleMapsKey, OnRouteResponseListener delegate) {
        mOrigin = origin;
        mDestination = destination;
        mWaypoints = waypoints;
        mOptimizeWaypoints = optimizeWaypoints;
        mDelegate = delegate;
        mGoogleMapsKey = googleMapsKey;
    }


    @Override
    protected DirectionsResult doInBackground(Void... voids) {

        GeoApiContext geoApiContext = new GeoApiContext();
        geoApiContext.setApiKey(mGoogleMapsKey)
                .setConnectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .setReadTimeout(TIMEOUT, TimeUnit.SECONDS)
                .setWriteTimeout(TIMEOUT, TimeUnit.SECONDS);

        com.google.maps.model.LatLng compatOrigin
                = new com.google.maps.model.LatLng(mOrigin.latitude, mOrigin.longitude);
        com.google.maps.model.LatLng compatDestination
                = new com.google.maps.model.LatLng(mDestination.latitude, mDestination.longitude);

        try {
            if (mWaypoints == null) {

                return DirectionsApi
                        .newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(compatOrigin)
                        .destination(compatDestination)
                        .await();
            } else {

                List<com.google.maps.model.LatLng> compatWaypoints = new ArrayList<>();
                for (LatLng waypoint : mWaypoints) {
                    com.google.maps.model.LatLng compatWaypoint
                            = new com.google.maps.model.LatLng(waypoint.latitude, waypoint.longitude);
                    compatWaypoints.add(compatWaypoint);
                }

                return DirectionsApi
                        .newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(compatOrigin)
                        .destination(compatDestination)
                        .waypoints(compatWaypoints.toArray(new com.google.maps.model.LatLng[0]))
                        .optimizeWaypoints(mOptimizeWaypoints)
                        .await();
            }

        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(DirectionsResult directionsResult) {
        super.onPostExecute(directionsResult);

        if (directionsResult != null) {
            DirectionsRoute directionsRoute = directionsResult.routes[0];
            if (directionsRoute != null) {

                // If no waypoints were provided
                if (mWaypoints == null) {
                    DirectionsLeg leg = directionsRoute.legs[0];
                    if (leg != null) {
                        String originAddress = leg.startAddress;
                        String destinationAddress = leg.endAddress;
                        Location originLocation = new Location(originAddress, mOrigin.latitude, mOrigin.longitude);
                        Location destinationLocation = new Location(destinationAddress, mDestination.latitude, mDestination.longitude);
                        long distance = leg.distance.inMeters;
                        long duration = leg.duration.inSeconds;

                        Route route = new Route(originLocation, destinationLocation, distance, duration);
                        mDelegate.onRouteResults(route, directionsResult);
                    }
                }
                // If waypoints were provided but no waypoints optimization needed
                else if (!mOptimizeWaypoints) {

                    DirectionsLeg[] legs = directionsRoute.legs;
                    if (legs != null) {
                        String originAddress = legs[0].startAddress;
                        String destinationAddress = legs[legs.length - 1].endAddress;
                        Location originLocation = new Location(originAddress, mOrigin.latitude, mOrigin.longitude);
                        Location destinationLocation = new Location(destinationAddress, mDestination.latitude, mDestination.longitude);
                        long distance = calculateTotalDistanceInMeters(legs);
                        long duration = calculateTotalDurationInSeconds(legs);

                        List<Location> waypoints = new ArrayList<>();

                        // < -1 because the last one endAddress is the original trip end address
                        // not the last waypoint address
                        for(int i = 0; i < legs.length - 1; i++ ){
                            LatLng waypointLatLng = mWaypoints.get(i);
                            String waypointAddress = legs[i].endAddress;
                            double lat = waypointLatLng.latitude;
                            double lng = waypointLatLng.longitude;
                            Location waypoint = new Location(waypointAddress, lat, lng);
                            waypoints.add(waypoint);
                        }

                        Route route = new Route(originLocation, destinationLocation, distance, duration, waypoints);
                        mDelegate.onRouteResults(route, directionsResult);
                    }

                }
                else{

                    DirectionsLeg[] legs = directionsRoute.legs;
                    if (legs != null) {
                        String originAddress = legs[0].startAddress;
                        String destinationAddress = legs[legs.length - 1].endAddress;
                        Location originLocation = new Location(originAddress, mOrigin.latitude, mOrigin.longitude);
                        Location destinationLocation = new Location(destinationAddress, mDestination.latitude, mDestination.longitude);
                        long distance = calculateTotalDistanceInMeters(legs);
                        long duration = calculateTotalDurationInSeconds(legs);

                        List<Location> waypoints = new ArrayList<>();

                        for(int i = 0; i < legs.length; i++ ){
                            LatLng waypointLatLng = mWaypoints.get(i);
                            String waypointAddress = legs[i].endAddress;
                            double lat = waypointLatLng.latitude;
                            double lng = waypointLatLng.longitude;
                            Location waypoint = new Location(waypointAddress, lat, lng);
                            waypoints.add(waypoint);
                        }

                        Route route = new Route(originLocation, destinationLocation, distance, duration, waypoints);
                        mDelegate.onRouteResults(route, directionsResult);
                    }
                }

            }
        } else {
            mDelegate.onRouteResults(null, null);
        }
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

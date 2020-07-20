package com.festeban26.ayni.google.maps.helpers;

import com.festeban26.ayni.firebase.model.FirebaseD2DService;
import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.FirebaseTripPassenger;
import com.festeban26.ayni.firebase.model.Location;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsHelper {

    // Parallel arrays
    private List<FirebaseTripPassenger> mWaypointsPassengers;
    private List<LatLng> mWaypoints;
    private boolean hasWaypoints = false;
    private FirebaseTrip mFirebaseTrip;
    private LatLng mFirebaseTripOriginLatLng;
    private LatLng mFirebaseTripDestinationLatLng;

    public MapsHelper(FirebaseTrip firebaseTrip) {

        mFirebaseTrip = firebaseTrip;
        Location originLocation = mFirebaseTrip.getOrigin();
        Location destinationLocation = mFirebaseTrip.getDestination();
        mFirebaseTripOriginLatLng = new LatLng(originLocation.getLat(), originLocation.getLng());
        mFirebaseTripDestinationLatLng = new LatLng(destinationLocation.getLat(), destinationLocation.getLng());

        Map<String, FirebaseTripPassenger> passengersMap = firebaseTrip.getPassengers();

        if (passengersMap != null) {

            List<LatLng> waypoints = new ArrayList<>();
            List<FirebaseTripPassenger> passengers = new ArrayList<>();
            List<FirebaseTripPassenger> passengersMapAsArray = new ArrayList<>(passengersMap.values());

            boolean hasAtLeastOneD2DService = false;

            for (FirebaseTripPassenger passenger : passengersMapAsArray) {

                FirebaseD2DService service = passenger.getD2DService();

                if (service != null) {
                    hasAtLeastOneD2DService = true;

                    Location origin = service.getOrigin();
                    Location destination = service.getDestination();
                    LatLng originLatLng = new LatLng(origin.getLat(), origin.getLng());
                    LatLng destinationLatLng = new LatLng(destination.getLat(), destination.getLng());

                    waypoints.add(originLatLng);
                    passengers.add(passenger);
                    waypoints.add(destinationLatLng);
                    passengers.add(passenger);
                }
            }

            mWaypointsPassengers = passengers;
            mWaypoints = waypoints;

            if (hasAtLeastOneD2DService)
                hasWaypoints = true;
        }
    }

    public List<LatLng> getWaypoints() {
        return mWaypoints;
    }

    public com.google.maps.model.LatLng[] getWaypointsAsCompatLatLng() {

        List<com.google.maps.model.LatLng> compatWaypoints = new ArrayList<>();
        for (LatLng waypoint : mWaypoints) {
            com.google.maps.model.LatLng compatWaypoint
                    = new com.google.maps.model.LatLng(waypoint.latitude, waypoint.longitude);
            compatWaypoints.add(compatWaypoint);
        }
        return compatWaypoints.toArray(new com.google.maps.model.LatLng[0]);
    }

    public boolean tripHasWaypoints() {
        return hasWaypoints;
    }

    // offset from edges of the map in pixels
    public CameraUpdate getCameraUpdate(int padding) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        // Include origin and destination

        builder.include(mFirebaseTripOriginLatLng);
        builder.include(mFirebaseTripDestinationLatLng);

        // If the trip has waypoints, add them
        if (hasWaypoints) {

            for (LatLng waypoint : mWaypoints)
                builder.include(waypoint);
        }

        LatLngBounds bounds = builder.build();
        return CameraUpdateFactory.newLatLngBounds(bounds, padding);
    }

    public List<MarkerOptions> getMarkers(String originMarkerTitle, String destinationMarkerTitle) {
        List<MarkerOptions> markers = new ArrayList<>();

        MarkerOptions originMarker
                = new MarkerOptions()
                .position(mFirebaseTripOriginLatLng)
                .title(originMarkerTitle)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        markers.add(originMarker);
        MarkerOptions destinationMarker
                = new MarkerOptions()
                .position(mFirebaseTripDestinationLatLng)
                .title(destinationMarkerTitle)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        markers.add(destinationMarker);

        if (hasWaypoints) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

            for(int i = 0; i < mWaypoints.size(); i ++){

                String passengerName = mWaypointsPassengers.get(i).getFirstName();
                MarkerOptions marker = new MarkerOptions().position(mWaypoints.get(i))
                        .icon(icon)
                        .title(passengerName);
                markers.add(marker);
            }
        }
        return markers;
    }
}









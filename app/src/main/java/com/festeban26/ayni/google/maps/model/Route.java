package com.festeban26.ayni.google.maps.model;

import com.festeban26.ayni.firebase.model.Location;

import java.util.List;

public class Route {

    private Location mOrigin;
    private Location mDestination;
    private long mDistanceInMeters;
    private long mDurationInSeconds;
    private List<Location> mWaypoints;

    public Route(Location origin, Location destination, long distanceInMeters, long durationInSeconds) {
        mOrigin = origin;
        mDestination = destination;
        mDistanceInMeters = distanceInMeters;
        mDurationInSeconds = durationInSeconds;
    }

    public Route(Location origin, Location destination, long distanceInMeters, long durationInSeconds, List<Location> waypoints) {
        mOrigin = origin;
        mDestination = destination;
        mDistanceInMeters = distanceInMeters;
        mDurationInSeconds = durationInSeconds;
        mWaypoints = waypoints;
    }


    public Location getOrigin() {
        return mOrigin;
    }

    public Location getDestination() {
        return mDestination;
    }

    public long getDistanceInMeters() {
        return mDistanceInMeters;
    }

    public long getDurationInSeconds() {
        return mDurationInSeconds;
    }

    public List<Location> getWaypoints(){
        return mWaypoints;
    }

    public void setOrigin(Location origin) {
        mOrigin = origin;
    }

    public void setDestination(Location destination) {
        mDestination = destination;
    }

    public void setDistance(long distanceInMeters) {
        mDistanceInMeters = distanceInMeters;
    }

    public void setDuration(long durationInSeconds) {
        mDurationInSeconds = durationInSeconds;
    }

    public void serWaypoints(List<Location> waypoints){
        mWaypoints = waypoints;
    }
}

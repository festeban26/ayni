package com.festeban26.ayni.firebase.model;

// TODO
// Rename to door to door services
public class FirebaseD2DService {

    private Location mOrigin;
    private Location mDestination;
    private Double mFee;
    private Long mDistanceInMeters;
    private Long mDurationInSeconds;

    public Location getOrigin() {
        return this.mOrigin;
    }

    public Location getDestination() {
        return this.mDestination;
    }

    public Double getFee(){
        return mFee;
    }

    public Long getDistance(){
        return mDistanceInMeters;
    }

    public Long getDuration(){
        return mDurationInSeconds;
    }

    public void setOrigin(Location origin) {
        this.mOrigin = origin;
    }

    public void setDestination(Location destination) {
        this.mDestination = destination;
    }

    public void setFee(Double fee){
        mFee = fee;
    }

    public void setDistance(Long distanceInMeters){
        mDistanceInMeters = distanceInMeters;
    }

    public void setDuration(Long durationInSeconds){
        mDurationInSeconds = durationInSeconds;
    }

}

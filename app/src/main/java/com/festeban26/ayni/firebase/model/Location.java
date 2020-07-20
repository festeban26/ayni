package com.festeban26.ayni.firebase.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

public class Location {

    private String mAddress;
    private String mCity;
    private Float mLat;
    private Float mLng;


    public Location() {
    }

    public Location(String address, Float lat, Float lng) {
        mLat = lat;
        mLng = lng;
        mAddress = address;
    }

    public Location(String address, double lat, double lng) {
        mLat = (float) lat;
        mLng = (float) lng;
        mAddress = address;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getCity() {
        return mCity;
    }

    public Float getLat() {
        return mLat;
    }

    public Float getLng() {
        return mLng;
    }


    public void setLat(Float lat) {
        mLat = lat;
    }

    public void setLng(Float lng) {
        mLng = lng;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public void setCity(String city) {
        mCity = city;
    }

    @Exclude
    public LatLng getLocation_asLatLng() {
        return new LatLng(getLat(), getLng());
    }
}

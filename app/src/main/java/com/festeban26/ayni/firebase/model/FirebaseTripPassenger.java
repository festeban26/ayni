package com.festeban26.ayni.firebase.model;

public class FirebaseTripPassenger {

    private String mId;
    private String mFirstName;
    private String mProfilePictureUrl;
    private int mNumberOfFriendsInCommonWithTheDriver;
    private FirebaseD2DService mD2DService;

    public FirebaseTripPassenger() {

    }

    public FirebaseTripPassenger(String id, String firstName, String profilePictureUrl, int numberOfFriendsInCommonWithTheDriver) {
        mId = id;
        mFirstName = firstName;
        mProfilePictureUrl = profilePictureUrl;
        mNumberOfFriendsInCommonWithTheDriver = numberOfFriendsInCommonWithTheDriver;
    }

    public FirebaseTripPassenger(String id, String firstName, String profilePictureUrl,
                                 int numberOfFriendsInCommonWithTheDriver, FirebaseD2DService d2DService) {
        mId = id;
        mFirstName = firstName;
        mProfilePictureUrl = profilePictureUrl;
        mNumberOfFriendsInCommonWithTheDriver = numberOfFriendsInCommonWithTheDriver;
        mD2DService = d2DService;
    }

    public String getId() {
        return mId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getProfilePictureUrl() {
        return mProfilePictureUrl;
    }

    public int getFriendsInCommonWithDriver() {
        return mNumberOfFriendsInCommonWithTheDriver;
    }

    public FirebaseD2DService getD2DService(){
        return mD2DService;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        mProfilePictureUrl = profilePictureUrl;
    }

    public void setFriendsInCommonWithDriver(int numberOfFriendsInCommonWithTheDriver) {
        mNumberOfFriendsInCommonWithTheDriver = numberOfFriendsInCommonWithTheDriver;
    }

    public void setD2DService(FirebaseD2DService d2DService){
        mD2DService = d2DService;
    }
}

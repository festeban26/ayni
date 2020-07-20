package com.festeban26.ayni.firebase.model;

import com.festeban26.ayni.facebook.model.FacebookUser;
import com.google.firebase.database.Exclude;

import java.util.Map;

public class FirebaseUser {

    private String mId;
    private String mEmail;
    private String mFullName;
    private String mFirstName;
    private String mProfilePictureUrl;
    private Map<String, Boolean> friends;

    public FirebaseUser() {
    }

    public FirebaseUser(String id, String email, String fullName, String firstName, String profilePictureUrl,
                        Map<String, Boolean> friends) {
        mId = id;
        mEmail = email;
        mFullName = fullName;
        mFirstName = firstName;
        mProfilePictureUrl = profilePictureUrl;
        this.friends = friends;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        mFullName = fullName;
    }

    public String getProfilePictureUrl() {
        return mProfilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        mProfilePictureUrl = profilePictureUrl;
    }

    public Map<String, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Boolean> friends) {
        this.friends = friends;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    /**
     * It is always compared to me (a Facebook User)
     *
     * @param me
     * @return
     */
    @Exclude
    public int getNumberOfFriendsInCommonWithMe(FacebookUser me) {

        int counter = 0;
        for (String friendId : me.getFriendsIds()) {
            if (getFriends().containsKey(friendId))
                counter++;
        }
        return counter;
    }

    @Exclude
    public static String getImageUrlFromId(String id){
        return  "https://graph.facebook.com/" + id + "/picture?type=large&width=480&height=480";
    }
}

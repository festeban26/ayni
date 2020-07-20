package com.festeban26.ayni.facebook.model;

import android.util.Log;

import com.facebook.AccessToken;
import com.festeban26.ayni.firebase.model.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacebookUser {

    private static final String sFACEBOOK_STRING_ID = "id";
    private static final String sFACEBOOK_STRING_NAME = "name";
    private static final String sFACEBOOK_STRING_FIRST_NAME = "first_name";
    private static final String sFACEBOOK_STRING_EMAIL = "email";
    private static final String sFACEBOOK_STRING_FRIENDS = "friends";

    public static final List<String> sREAD_PERMISSIONS = Arrays.asList("user_friends", "email");

    // The requested fields
    public static final String sFACEBOOK_STRING_FIELDS = "fields";
    public static final String sFACEBOOK_STRING_FIELDS_LIST
            = sFACEBOOK_STRING_ID + ","
            + sFACEBOOK_STRING_NAME + ","
            + sFACEBOOK_STRING_FIRST_NAME + ","
            + sFACEBOOK_STRING_EMAIL + ","
            + sFACEBOOK_STRING_FRIENDS;

    private String mId;
    private String mEmail;
    private String mFullName;
    private String mFirstName;
    private String mUserImageUrl;
    private List<String> mFriendsIds;
    private AccessToken mAccessToken;

    public FacebookUser() {

    }

    private FacebookUser(String id, String email, String fullName, String firstName, String userImageUrl, List<String> friendsIds, AccessToken accessToken) {
        setId(id);
        setEmail(email);
        setFullName(fullName);
        setFirstName(firstName);
        setUserImageUrl(userImageUrl);
        setFriendsIds(friendsIds);
        setAccessToken(accessToken);
    }

    public FirebaseUser getAsFirebaseUser(){
        Map<String, Boolean> friendsIds = new HashMap<>();

        for(String id : mFriendsIds)
            friendsIds.put(id, true);

        return new FirebaseUser(mId, mEmail, mFullName, mFirstName, mUserImageUrl, friendsIds);
    }

    public static FacebookUser parseFacebookResponseIntoFacebookUser(JSONObject jsonObject, AccessToken token) {
        try {
            String id = null;
            String email = null;
            String fullName = null;
            String firstName = null;
            String userImageUrl = null;
            List<String> friendsIds = null;

            if (jsonObject.has(sFACEBOOK_STRING_ID)) {
                String graphObjectId = jsonObject.getString(sFACEBOOK_STRING_ID);

                id = graphObjectId;
                userImageUrl = "https://graph.facebook.com/" + graphObjectId + "/picture?type=large&width=480&height=480";
            }

            if (jsonObject.has(sFACEBOOK_STRING_EMAIL))
                email = jsonObject.getString(sFACEBOOK_STRING_EMAIL);

            if (jsonObject.has(sFACEBOOK_STRING_NAME))
                fullName = jsonObject.getString(sFACEBOOK_STRING_NAME);

            if (jsonObject.has(sFACEBOOK_STRING_FIRST_NAME))
                firstName = jsonObject.getString(sFACEBOOK_STRING_FIRST_NAME);

            if (jsonObject.has(sFACEBOOK_STRING_FRIENDS)) {
                JSONObject friends_raw = jsonObject.getJSONObject(sFACEBOOK_STRING_FRIENDS);
                JSONArray friendsArray_raw = friends_raw.getJSONArray("data");
                friendsIds = new ArrayList<>();

                for (int i = 0; i < friendsArray_raw.length(); i++) {
                    JSONObject friendObject = friendsArray_raw.getJSONObject(i);
                    String friendId = friendObject.getString(sFACEBOOK_STRING_ID);
                    friendsIds.add(friendId);
                }
            }

            return new FacebookUser(id, email, fullName, firstName, userImageUrl, friendsIds, token);

        } catch (JSONException e) {
            Log.d("ERROR", e.toString());
            return null;
        }
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String name) {
        this.mFullName = name;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public String getUserImageUrl() {
        return mUserImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.mUserImageUrl = userImageUrl;
    }

    public List<String> getFriendsIds() {
        return mFriendsIds;
    }

    public void setFriendsIds(List<String> friendsIds) {
        mFriendsIds = friendsIds;
    }

    public AccessToken getAccessToken(){
        return mAccessToken;
    }

    public void setAccessToken(AccessToken token){
        mAccessToken = token;
    }
}




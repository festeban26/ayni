package com.festeban26.ayni;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.firebase.utility.DbPath;
import com.festeban26.ayni.interfaces.AppAuthListener;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.utils.Preferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Java Helper class: authentication and login.
 * Singleton class
 */
public class AppAuth {

    private static AppAuth sInstance;

    public static synchronized AppAuth getInstance() {

        if (sInstance == null) {
            sInstance = new AppAuth();
        }
        return sInstance;
    }

    private AppAuthListener mAppAuthListener;

    //private constructor to avoid client applications to use constructor
    private AppAuth() {
    }

    public void addListenerForLoginStatus(AppAuthListener appAuthListener) {
        this.mAppAuthListener = appAuthListener;
    }


    public boolean isUserLoggedIn(final Context applicationContext) {

        SharedPreferences preferences
                = applicationContext.getSharedPreferences(ApplicationConstants.sSTRING_SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        if (preferences.contains(ApplicationConstants.sSTRING_SHARED_PREFERENCES_IS_USER_LOGGED_IN))
            return preferences.getBoolean(ApplicationConstants.sSTRING_SHARED_PREFERENCES_IS_USER_LOGGED_IN, false);
        else
            return false;
    }

    private void setLoginState(boolean loginState, Context applicationContext) {

        SharedPreferences preferences
                = applicationContext.getSharedPreferences(ApplicationConstants.sSTRING_SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ApplicationConstants.sSTRING_SHARED_PREFERENCES_IS_USER_LOGGED_IN, loginState);
        editor.apply();
    }

    public FacebookUser getCurrentFacebookUser(final Context applicationContext) {
        SharedPreferences preferences
                = applicationContext.getSharedPreferences(ApplicationConstants.sSTRING_SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        if (preferences.contains(ApplicationConstants.sSTRING_SHARED_PREFERENCES_FACEBOOK_USER)) {

            String facebookUserAsJsonString = preferences
                    .getString(ApplicationConstants.sSTRING_SHARED_PREFERENCES_FACEBOOK_USER, "");

            if (!TextUtils.isEmpty(facebookUserAsJsonString)) {
                return new Gson().fromJson(facebookUserAsJsonString, FacebookUser.class);
            } else
                return null;
        } else
            return null;
    }

    public void logout(final Context applicationContext) {

        // Log out from Facebook
        LoginManager.getInstance().logOut();

        SharedPreferences preferences
                = applicationContext.getSharedPreferences(ApplicationConstants.sSTRING_SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        // Clear SharedPreferences
        preferences.edit().clear().apply();

        setLoginState(false, applicationContext);

    }

    public AppAuth login(final Context applicationContext, final AccessToken facebookAccessToken) {

        GraphRequest graphRequest = GraphRequest.newMeRequest(facebookAccessToken, new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                if(object != null){

                    final FacebookUser facebookUser = FacebookUser.parseFacebookResponseIntoFacebookUser(object, facebookAccessToken);

                    if (facebookUser != null) {

                        FirebaseDatabase.getInstance().getReference()
                                .child(DbPath.USERS)
                                .child(facebookUser.getId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                                        // If user does not exist on database
                                        if (!dataSnapshot.exists()) {

                                            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                                    .getReference(DbPath.USERS)
                                                    .child(facebookUser.getId());

                                            FirebaseUser firebaseUser = facebookUser.getAsFirebaseUser();


                                            databaseReference.setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        // Save user data on Shared Preferences
                                                        saveUserDataOnSharedPreferences(facebookUser, applicationContext);
                                                        setLoginState(true, applicationContext);

                                                        if (mAppAuthListener != null)
                                                            mAppAuthListener.onSuccess();
                                                    }
                                                }
                                            });

                                            // Notification  token

                                            SharedPreferences preferences = applicationContext.getSharedPreferences(Preferences.FILENAME, Context.MODE_PRIVATE);

                                            if(preferences.contains(Preferences.NOTIFICATIONS_TOKEN)){
                                                String token = preferences.getString(Preferences.NOTIFICATIONS_TOKEN, null);

                                                if(token != null){
                                                    Map<String, String> map = new HashMap<>();
                                                    map.put("token", token);
                                                    String userId = facebookUser.getId();
                                                    FirebaseDatabase.getInstance().getReference(DbPath.NOTIFICATION_TOKENS)
                                                            .child(userId).setValue(map);
                                                }
                                            }
                                        }
                                        // If user does exist on database
                                        else {
                                            saveUserDataOnSharedPreferences(facebookUser, applicationContext);
                                            setLoginState(true, applicationContext);

                                            if (mAppAuthListener != null)
                                                mAppAuthListener.onSuccess();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NotNull DatabaseError error) {
                                        // Failed to read value
                                        setLoginState(false, applicationContext);

                                        if (mAppAuthListener != null)
                                            mAppAuthListener.onError();
                                    }
                                });

                    } else {
                        setLoginState(false, applicationContext);

                        if (mAppAuthListener != null)
                            mAppAuthListener.onError();
                    }
                }
            }
        });

        Bundle parameters = new Bundle();
        // TODO FUTURE download the string from server. Improve modularity
        parameters.putString(FacebookUser.sFACEBOOK_STRING_FIELDS, FacebookUser.sFACEBOOK_STRING_FIELDS_LIST);
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync(); // When completed GOTO onCompleted() above
        return this;
    }

    private void saveUserDataOnSharedPreferences(FacebookUser facebookUser, Context applicationContext) {

        SharedPreferences preferences
                = applicationContext.getSharedPreferences(ApplicationConstants.sSTRING_SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        String facebookUserAsJson = new Gson().toJson(facebookUser);
        editor.putBoolean(ApplicationConstants.sSTRING_SHARED_PREFERENCES_IS_USER_LOGGED_IN, true);
        editor.putString(ApplicationConstants.sSTRING_SHARED_PREFERENCES_FACEBOOK_USER, facebookUserAsJson);
        editor.apply();
    }
}
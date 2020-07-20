package com.festeban26.ayni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.interfaces.AppAuthListener;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.utils.ResultCodes;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    // Facebook login button
    private LoginButton loginButton;
    // Handles Facebook login
    private CallbackManager callbackManager;

    // Access content warning
    private TextView mAccessContentMessage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Next line is required to get Facebook tabbed activity result
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Go back arrow behaviour.
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(ResultCodes.CANCELED);
                onBackPressed();
            }
        });

        // Facebook LOG IN
        callbackManager = CallbackManager.Factory.create();

        // Initialize Facebook login button
        loginButton = findViewById(R.id.LoginButton_RegistrationActivity_FacebookLogin);
        loginButton.setReadPermissions(FacebookUser.sREAD_PERMISSIONS);
        mAccessContentMessage = findViewById(R.id.TextView_RegistrationOrLoginActivity_AccessContentMessage);

        TextView loginOrRegisterWithFacebookTextView = findViewById(R.id.TextView_SignInActivity_LoginOrRegisterWithFacebook);
        loginOrRegisterWithFacebookTextView.setText(getString(R.string.String_SignInActivity_LoginOrRegisterWithFacebook));

        mAccessContentMessage.setText(getString(R.string.String_SignInActivity_ContentRestrictionMessage));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
            }

            @Override
            public void onCancel() {
                // ERROR HANDLING
                Toast.makeText(SignInActivity.this,
                        "Login cancelled.", Toast.LENGTH_SHORT).show();
                setResult(ResultCodes.CANCELED);
                finish();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(SignInActivity.this,
                        "Login failed. Please try again later", Toast.LENGTH_SHORT).show();
                setResult(ResultCodes.FAILURE);
                finish();
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("E", "test");
                    }

                    @Override
                    public void onCancel() {
                        Log.d("E", "test");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("E", "test");
                    }
                });


        if(getIntent().getBooleanExtra("SIGN_IN_DUE_TO_CONTENT_RESTRICTION", false)){
            mAccessContentMessage.setVisibility(View.VISIBLE);
        }
        else {
            mAccessContentMessage.setVisibility(View.GONE);
        }
    }


    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken != null) {
                // TODO
                AppAuth.getInstance().login(getApplicationContext(), currentAccessToken).addListenerForLoginStatus(new AppAuthListener() {
                    @Override
                    public void onSuccess() {
                        setResult(ResultCodes.SUCCESS);
                        finish();
                    }

                    @Override
                    public void onError() {
                    }
                });
            }
        }
    };
}

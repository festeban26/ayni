package com.festeban26.ayni.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.utils.IntentNames;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private Button mSendMessageButton;
    private ImageView mPhoto;
    private TextView mName;
    private TextView mNumOfCommonFriends;
    private GridView mGridView;

    private FirebaseUser mFirebaseUser;
    private String mUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.Toolbar_UserProfileActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().getExtras() != null) {

            String userId = getIntent().getExtras().getString("userId");

            if (userId != null) {
                mUserId = userId;
                setViews();
            }
        }
    }

    public void setViews() {

        mPhoto = findViewById(R.id.ImageView_UserProfileActivity_Photo);
        mName = findViewById(R.id.TextView_UserProfileActivity_Name);
        mSendMessageButton = findViewById(R.id.Button_UserProfileActivity_SendMessage);
        mGridView = findViewById(R.id.GridView_UserProfileActivity);
        mNumOfCommonFriends = findViewById(R.id.TextView_UserProfileActivity_NumOfCommonFriends);

        setView_SendMessageButton();

        FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(mUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                        if (user != null) {
                            mFirebaseUser = user;
                            setViews_OnDownloadedFirebaseUser();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setViews_OnDownloadedFirebaseUser() {

        Glide.with(UserProfileActivity.this)
                .load(mFirebaseUser.getProfilePictureUrl())
                .apply(RequestOptions.centerInsideTransform())
                .into(mPhoto);

        mName.setText(mFirebaseUser.getFullName());

        FacebookUser me = AppAuth.getInstance().getCurrentFacebookUser(this);
        List<String> myFriendsIds = me.getFriendsIds();

        if (mFirebaseUser.getFriends() != null) {

            Map<String, Boolean> hisFriends = mFirebaseUser.getFriends();

            List<String> urls = new ArrayList<>();

            for (String mFriendId : myFriendsIds) {
                if (hisFriends.containsKey(mFriendId)) {
                    urls.add(FirebaseUser.getImageUrlFromId(mFriendId));
                }
            }

            String counter = Integer.toString(urls.size());
            mNumOfCommonFriends.setText(counter);
            mGridView.setVerticalScrollBarEnabled(false);
            mGridView.setAdapter(new FacebookSimpleFriendAdapter(this, urls));
        }

    }

    private void setView_SendMessageButton() {

        String myUserId = AppAuth.getInstance().getCurrentFacebookUser(this).getId();
        String targetUserProfileId = mUserId;

        if(myUserId.equalsIgnoreCase(targetUserProfileId)){
            mSendMessageButton.setVisibility(View.GONE);
        }
        else
            mSendMessageButton.setVisibility(View.VISIBLE);

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFirebaseUser != null) {
                    Intent intent = new Intent(UserProfileActivity.this, MessageActivity.class);
                    intent.putExtra(IntentNames.MESSAGES_ACTIVITY__USER_ID, mFirebaseUser.getId());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private class FacebookSimpleFriendAdapter extends BaseAdapter {

        private Context mContext;
        private List<String> mUrls;

        public FacebookSimpleFriendAdapter(Context context, List<String> urls) {
            mContext = context;
            mUrls = urls;
        }

        @Override
        public int getCount() {
            return mUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            String url = mUrls.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.item_simple_facebook_friend, null);
            }

            ImageView photo = convertView.findViewById(R.id.ImageView_SimpleFacebookFriendItem_Photo);

            Glide.with(mContext)
                    .load(url)
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(5)))
                    .into(photo);

            return convertView;
        }
    }

}







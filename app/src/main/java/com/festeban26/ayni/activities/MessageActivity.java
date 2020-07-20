package com.festeban26.ayni.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.messaging.adapters.MessageAdapter;
import com.festeban26.ayni.messaging.model.Chat;
import com.festeban26.ayni.utils.IntentNames;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/*
TODO
Actualmente se descargan todos los mensajes.
Seria bueno agregar una fecha a los mensajes y descargar los mensajes enviados desde esa fecha
(o ultima revision)
 */

public class MessageActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mUsername;
    private RecyclerView mRecyclerView;

    private ImageButton mSendButton;
    private EditText mTextToSend;

    private String mMyCurrentUserId;
    private String mTheOtherUserId;
    private String mDbPath;
    private DatabaseReference mDatabaseReference;

    private MessageAdapter mMessageAdapter;
    private List<Chat> mChats;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.Toolbar_MessageActivity);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().getExtras() != null) {
            mTheOtherUserId = getIntent().getStringExtra(IntentNames.MESSAGES_ACTIVITY__USER_ID);
            mMyCurrentUserId = AppAuth.getInstance().getCurrentFacebookUser(MessageActivity.this).getId();
            mDbPath = MessageActivity.getDbPath(mMyCurrentUserId, mTheOtherUserId);
            setViews();
        }
    }

    private void setViews() {

        mRecyclerView = findViewById(R.id.RecyclerView_MessageActivity);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mProfileImage = findViewById(R.id.ImageView_MessageActivity_ProfileImage);
        mUsername = findViewById(R.id.TextView_MessageActivity_Username);
        mSendButton = findViewById(R.id.ImageButton_MessageActivity_Send);
        mTextToSend = findViewById(R.id.EditText_MessageActivity_Text);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mTextToSend.getText().toString();
                if (!message.isEmpty()) {
                    sendMessage(mMyCurrentUserId, mTheOtherUserId, message);
                }
                mTextToSend.setText("");
            }
        });


        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mTheOtherUserId);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                if (user != null) {
                    mUsername.setText(user.getFirstName());

                    Glide.with(MessageActivity.this)
                            .load(user.getProfilePictureUrl())
                            .apply(RequestOptions.circleCropTransform())
                            .into(mProfileImage);

                    readMessages(mMyCurrentUserId, mTheOtherUserId, user.getProfilePictureUrl());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void sendMessage(String sender, String receiver, String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        databaseReference.child("Chats").child(mDbPath).push().setValue(hashMap);
    }

    private void readMessages(final String myId, final String userId, final String imageUrl) {
        mChats = new ArrayList<>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Chats").child(mDbPath);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        //if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        mChats.add(chat);
                    }
                    mMessageAdapter = new MessageAdapter(MessageActivity.this, mChats, imageUrl);
                    mRecyclerView.setAdapter(mMessageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private static String getDbPath(String s1, String s2) {
        int comparator = s1.compareTo(s2);

        // Lexicographically
        // If s1 = s1
        if (comparator == 0) {
            return s1 + s2;
        }
        // If s1 < s2
        else if (comparator < 0) {
            return s1 + s2;
        }
        // If s2 < s1
        else {
            return s2 + s1;
        }
    }
}

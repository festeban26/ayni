package com.festeban26.ayni.fragments.MessagesFragmentFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.festeban26.ayni.messaging.adapters.UserAdapter;
import com.festeban26.ayni.messaging.model.User;
import com.festeban26.ayni.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    private RecyclerView mRecyclerView;

    private UserAdapter mUserAdapter;
    private List<User> mUsers;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        mRecyclerView = view.findViewById(R.id.RecyclerView_UserFragment);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();

        readUsers();

        return view;
    }

    private void readUsers(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for( DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    if (user != null && firebaseUser != null)
                        if(!user.getId().equals(firebaseUser.getUid()))
                            mUsers.add(user);

                }

                mUserAdapter = new UserAdapter(getContext(), mUsers);
                mRecyclerView.setAdapter(mUserAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
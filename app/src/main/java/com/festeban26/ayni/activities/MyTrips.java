package com.festeban26.ayni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.FirebaseTripPassenger;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.utils.CurrencyHelper;
import com.festeban26.ayni.utils.RequestCodes;
import com.festeban26.ayni.utils.ResultCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.joda.money.Money;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyTrips extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private FacebookUser mCurrentUser;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == RequestCodes.SIGN_IN) {
            if (resultCode != ResultCodes.SUCCESS) {
                finish();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (!AppAuth.getInstance().isUserLoggedIn(this)) {
            Intent intent = new Intent(MyTrips.this, SignInActivity.class);
            intent.putExtra("SIGN_IN_DUE_TO_CONTENT_RESTRICTION", true);
            startActivityForResult(intent, RequestCodes.SIGN_IN);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);

        Toolbar toolbar = findViewById(R.id.Toolbar_MyTripsActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.RecyclerView_MyTrips);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MyTrips.this));

        readMyTrips();
    }

    private void readMyTrips() {

        DatabaseReference myTripsDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = AppAuth.getInstance().getCurrentFacebookUser(MyTrips.this);

        if (mCurrentUser != null) {

            String currentUserId = mCurrentUser.getId();
            myTripsDatabaseReference.child("UserTrips").child(currentUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<FirebaseTrip> firebaseTrips = new ArrayList<>();

                            if (dataSnapshot.exists()) {

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    FirebaseTrip firebaseTrip = snapshot.getValue(FirebaseTrip.class);
                                    if (firebaseTrip != null) {
                                        firebaseTrips.add(firebaseTrip);
                                    }
                                }

                                MyTripsAdapter adapter = new MyTripsAdapter(firebaseTrips);
                                mRecyclerView.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    class MyTripsAdapter extends RecyclerView.Adapter<MyTripsAdapter.ViewHolder> {

        private List<FirebaseTrip> firebaseTrips;

        public MyTripsAdapter(List<FirebaseTrip> firebaseTrips) {
            this.firebaseTrips = firebaseTrips;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(MyTrips.this).inflate(R.layout.item_my_trip, viewGroup, false);
            return new MyTripsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            final FirebaseTrip firebaseTrip = firebaseTrips.get(i);

            String date = firebaseTrip.getDate_asString();
            String time = firebaseTrip.getTime_asString();
            String originCity = firebaseTrip.getOrigin().getCity();
            String destinationCity = firebaseTrip.getDestination().getCity();

            String numberOfEmptySeats = Integer.toString(firebaseTrip.getNumberOfEmptySeats());
            String numberOfEmptySeatsDescription = firebaseTrip.getNumberOfEmptySeats() == 1 ?
                    getString(R.string.String_TripItem_EmptySeatsSingular)
                    : getString(R.string.String_TripItem_EmptySeatsPlural);

            viewHolder.getDate().setText(date);
            viewHolder.getTime().setText(time);
            viewHolder.getOriginCity().setText(originCity);
            viewHolder.getDestinationCity().setText(destinationCity);
            viewHolder.getEmptySeats().setText(numberOfEmptySeats);
            viewHolder.getEmptySeatsDescription().setText(numberOfEmptySeatsDescription);

            final FirebaseUser driver = firebaseTrip.getDriver();
            String driverName = driver.getFirstName();
            String driversPhotoUrl = driver.getProfilePictureUrl();

            // If the current user is the driver
            if (driver.getId().equals(mCurrentUser.getId())) {

                // Price displaySettings
                // I'm the driver so display original price
                String price = CurrencyHelper.getAsString(firebaseTrip.getPrice());
                viewHolder.getPrice().setText(price);

                String driverNameToBeDisplayed = getString(R.string.String_SearchResultsActivity_DriverMe);
                viewHolder.getDriverName().setText(driverNameToBeDisplayed);
                viewHolder.getFacebookInformationLayout().setVisibility(View.INVISIBLE);

                int color = ContextCompat.getColor(MyTrips.this, R.color.Color_Post);
                String userRole = getString(R.string.String_MyTripsItem_UserRoleDriver);

                viewHolder.getUserRoleLayout().setBackgroundColor(color);
                viewHolder.getUserRoleTx().setText(userRole);
                viewHolder.getUserRoleIv().setImageResource(R.drawable.ic_driver);

            } else {

                // I am a passenger, so trip has passengers
                // Price displaySettings
                FirebaseTripPassenger me = firebaseTrip.getPassengers().get(mCurrentUser.getId());
                if(me != null){
                    if (me.getD2DService() == null) {
                        String price = CurrencyHelper.getAsString(firebaseTrip.getPrice());
                        viewHolder.getPrice().setText(price);
                    } else {

                        // Display total price
                        Money money = CurrencyHelper.getAsMoney(firebaseTrip.getPrice())
                                .plus(me.getD2DService().getFee());
                        String fee = CurrencyHelper.getAsString(money);
                        viewHolder.getPrice().setText(fee);
                    }
                }

                int color = ContextCompat.getColor(MyTrips.this, R.color.Color_Search);
                viewHolder.getVerticalLine().setBackgroundColor(color);
                viewHolder.getOriginDescription().setTextColor(color);
                viewHolder.getDestinationDescription().setTextColor(color);

                viewHolder.getDriverName().setText(driverName);
                viewHolder.getFacebookInformationLayout().setVisibility(View.VISIBLE);

                int friendsInCommonWithMe = driver.getNumberOfFriendsInCommonWithMe(mCurrentUser);
                String facebookFriendsInCommon = Integer.toString(friendsInCommonWithMe);
                String facebookFriendsInCommonDescription = friendsInCommonWithMe == 1 ?
                        getString(R.string.String_TripItem_CommonFriendsSingular)
                        : getString(R.string.String_TripItem_CommonFriendsPlural);

                viewHolder.getDriverFacebookFriendsInCommon().setText(facebookFriendsInCommon);
                viewHolder.getDriverFacebookFriendsInCommonDescription().setText(facebookFriendsInCommonDescription);
            }

            Glide.with(MyTrips.this)
                    .load(driversPhotoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(viewHolder.getDriverPhoto());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String currentUserId = mCurrentUser.getId();
                    String tripDriverId = driver.getId();

                    // If I'm the driver
                    if (currentUserId.equalsIgnoreCase(tripDriverId)) {
                        Intent intent = new Intent(MyTrips.this, MyTripAsDriverDetailsActivity.class);
                        intent.putExtra("firebaseTrip", new Gson().toJson(firebaseTrip));
                        startActivity(intent);
                    }
                    // If I'm a passenger
                    else {
                        Intent intent = new Intent(MyTrips.this, MyTripAsPassengerDetailsActivity.class);
                        intent.putExtra("firebaseTrip", new Gson().toJson(firebaseTrip));
                        startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return firebaseTrips.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mDate, mTime, mOriginCity, mDestinationCity, mPrice, mEmptySeats, mEmptySeatsDescription, mDriverName,
                    mDriverFacebookFriendsInCommon, mDriverFacebookFriendsInCommonDescription, mDriverRating;

            private ImageView mDriversPhoto;

            private LinearLayout mUserRoleLayout;
            private LinearLayout mFacebookInformationLayout;
            private TextView mUserRoleTx;
            private ImageView mUserRoleIv;
            private TextView mOriginDescription;
            private TextView mDestinationDescription;
            private View mVerticalLine;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mDate = itemView.findViewById(R.id.TextView_TripItem_Date);
                this.mTime = itemView.findViewById(R.id.TextView_TripItem_Time);
                this.mOriginCity = itemView.findViewById(R.id.TextView_TripItem_OriginCity);
                this.mDestinationCity = itemView.findViewById(R.id.TextView_TripItem_DestinationCity);
                this.mPrice = itemView.findViewById(R.id.TextView_TripItem_Price);
                this.mEmptySeats = itemView.findViewById(R.id.TextView_TripItem_NumberOfEmptySeats);
                this.mEmptySeatsDescription = itemView.findViewById(R.id.TextView_TripItem_NumberOfEmptySeatsDescription);

                this.mDriverName = itemView.findViewById(R.id.TextView_TripItem_DriverName);
                this.mDriverFacebookFriendsInCommon = itemView.findViewById(R.id.TextView_TripItem_DriverNumberOfFacebookFriendsInCommon);
                this.mDriverFacebookFriendsInCommonDescription = itemView.findViewById(R.id.TextView_TripItem_DriverNumberOfFacebookFriendsInCommonDescription);
                this.mDriverRating = itemView.findViewById(R.id.TextView_TripItem_DriverRating);
                this.mDriversPhoto = itemView.findViewById(R.id.ImageView_TripItem_DriverPhoto);

                this.mFacebookInformationLayout = itemView.findViewById(R.id.Layout_TripItem_FacebookInformation);

                this.mUserRoleLayout = itemView.findViewById(R.id.Layout_MyTripsItem_UserRole);
                this.mUserRoleTx = itemView.findViewById(R.id.TextView_MyTripsItem_UserRole);
                this.mUserRoleIv = itemView.findViewById(R.id.ImageView_MyTripsItem_UserRole);
                this.mOriginDescription = itemView.findViewById(R.id.TextView_TripItem_OriginDescription);
                this.mDestinationDescription = itemView.findViewById(R.id.TextView_TripItem_DestinationDescription);
                this.mVerticalLine = itemView.findViewById(R.id.View_TripItem_VerticalLineLeftOfOriginAndDestination);
            }

            private TextView getDate() {
                return this.mDate;
            }

            private TextView getTime() {
                return this.mTime;
            }

            private TextView getPrice() {
                return this.mPrice;
            }

            private TextView getEmptySeats() {
                return this.mEmptySeats;
            }

            private TextView getEmptySeatsDescription() {
                return this.mEmptySeatsDescription;
            }

            private TextView getDriverName() {
                return this.mDriverName;
            }

            private TextView getDriverFacebookFriendsInCommon() {
                return this.mDriverFacebookFriendsInCommon;
            }

            private TextView getDriverFacebookFriendsInCommonDescription() {
                return this.mDriverFacebookFriendsInCommonDescription;
            }

            private TextView getDriverRating() {
                return this.mDriverRating;
            }

            private TextView getOriginCity() {
                return this.mOriginCity;
            }

            private TextView getDestinationCity() {
                return this.mDestinationCity;
            }

            private ImageView getDriverPhoto() {
                return this.mDriversPhoto;
            }

            private LinearLayout getFacebookInformationLayout() {
                return this.mFacebookInformationLayout;
            }

            private LinearLayout getUserRoleLayout() {
                return this.mUserRoleLayout;
            }

            private TextView getUserRoleTx() {
                return this.mUserRoleTx;
            }

            private ImageView getUserRoleIv() {
                return this.mUserRoleIv;
            }

            private TextView getOriginDescription() {
                return this.mOriginDescription;
            }

            private TextView getDestinationDescription() {
                return this.mDestinationDescription;
            }

            private View getVerticalLine() {
                return this.mVerticalLine;
            }
        }
    }
}
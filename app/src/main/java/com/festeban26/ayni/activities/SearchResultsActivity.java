package com.festeban26.ayni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.utils.CurrencyHelper;
import com.festeban26.ayni.utils.RequestCodes;
import com.festeban26.ayni.utils.ResultCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class SearchResultsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private FacebookUser mCurrentUser;
    private FirebaseTrip mSelectedFireBaseTrip;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.BOOK_TRIP) {
            if (resultCode == ResultCodes.SUCCESS) {
                setResult(ResultCodes.SUCCESS);
                finish();
            }
        } else if (requestCode == RequestCodes.SIGN_IN) {
            if (resultCode == ResultCodes.SUCCESS) {
                startTripDetailsActivity(mSelectedFireBaseTrip);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Toolbar toolbar = findViewById(R.id.Toolbar_SearchResultsActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.RecyclerView_SearchResultsActivity);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(SearchResultsActivity.this));

        if (getIntent().getExtras() != null) {

            if (AppAuth.getInstance().isUserLoggedIn(SearchResultsActivity.this))
                mCurrentUser = AppAuth.getInstance().getCurrentFacebookUser(SearchResultsActivity.this);

            String originCity = getIntent().getStringExtra("originCity");
            String destinationCity = getIntent().getStringExtra("destinationCity");
            int year = getIntent().getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR));
            int month = getIntent().getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH) + 1); // Plus 1 to set Jan to 1 instead of 0
            int day = getIntent().getIntExtra("day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

            searchTrips(originCity, destinationCity, year, month, day);
        }
    }

    private void startTripDetailsActivity(FirebaseTrip firebaseTrip) {
        Intent intent = new Intent(SearchResultsActivity.this, TripDetailsActivity.class);
        intent.putExtra("firebaseTrip", new Gson().toJson(firebaseTrip));
        startActivityForResult(intent, RequestCodes.BOOK_TRIP);
    }

    private void searchTrips(String originCity, String destinationCity, int year, int month, int day) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        NumberFormat formatter = new DecimalFormat("00");
        String year_toFormattedString = formatter.format(year);
        String month_toFormattedString = formatter.format(month + 1); // Plus 1 to set Jan to 1 instead of 0
        String day_toFormattedString = formatter.format(day);

        databaseReference.child("Trips")
                .child(originCity + "-" + destinationCity)
                .child(year_toFormattedString)
                .child(month_toFormattedString)
                .child(day_toFormattedString).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FirebaseTrip> firebaseTrips = new ArrayList<>();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        FirebaseTrip firebaseTrip = snapshot.getValue(FirebaseTrip.class);
                        firebaseTrips.add(firebaseTrip);
                    }

                    SearchResultsActivity.SearchResultsAdapter adapter
                            = new SearchResultsActivity.SearchResultsAdapter(firebaseTrips);

                    mRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsActivity.SearchResultsAdapter.ViewHolder> {

        private List<FirebaseTrip> mFirebaseTrips;

        public SearchResultsAdapter(List<FirebaseTrip> firebaseTrips) {
            this.mFirebaseTrips = firebaseTrips;
        }

        @NonNull
        @Override
        public SearchResultsActivity.SearchResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(SearchResultsActivity.this).inflate(R.layout.item_search_results, viewGroup, false);
            return new SearchResultsActivity.SearchResultsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultsActivity.SearchResultsAdapter.ViewHolder viewHolder, final int i) {
            final FirebaseTrip firebaseTrip = this.mFirebaseTrips.get(i);

            String date = firebaseTrip.getDate_asString();
            String time = firebaseTrip.getTime_asString();
            String originCity = firebaseTrip.getOrigin().getCity();
            String destinationCity = firebaseTrip.getDestination().getCity();
            String price = CurrencyHelper.getAsString(firebaseTrip.getPrice());

            String numberOfEmptySeats = Integer.toString(firebaseTrip.getNumberOfEmptySeats());
            String numberOfEmptySeatsDescription = firebaseTrip.getNumberOfEmptySeats() == 1 ?
                    getString(R.string.String_TripItem_EmptySeatsSingular)
                    : getString(R.string.String_TripItem_EmptySeatsPlural);

            viewHolder.getDate().setText(date);
            viewHolder.getTime().setText(time);
            viewHolder.getOriginCity().setText(originCity);
            viewHolder.getDestinationCity().setText(destinationCity);
            viewHolder.getPrice().setText(price);

            viewHolder.getEmptySeats().setText(numberOfEmptySeats);
            viewHolder.getEmptySeatsDescription().setText(numberOfEmptySeatsDescription);

            FirebaseUser driver = firebaseTrip.getDriver();
            String driverName = driver.getFirstName();
            String driverPhotoUrl = driver.getProfilePictureUrl();

            // If user is not logged in
            if (mCurrentUser == null) {
                viewHolder.getDriverName().setText(driverName);
                viewHolder.getFacebookInformationLayout().setVisibility(View.INVISIBLE);
            }
            // If the current user is the driver
            else if (driver.getId().equals(mCurrentUser.getId())) {

                String driverNameToBeDisplayed = getString(R.string.String_SearchResultsActivity_DriverMe);
                viewHolder.getDriverName().setText(driverNameToBeDisplayed);
                viewHolder.getFacebookInformationLayout().setVisibility(View.INVISIBLE);
            } else {

                viewHolder.getDriverName().setText(driverName);
                viewHolder.getFacebookInformationLayout().setVisibility(View.VISIBLE);

                int friendsInCommonWithMe = driver.getNumberOfFriendsInCommonWithMe(mCurrentUser);
                String numberOfFacebookFriendsInCommon = Integer.toString(friendsInCommonWithMe);
                viewHolder.getDriverFacebookFriendsInCommon().setText(numberOfFacebookFriendsInCommon);

                String facebookFriendsInCommonDescription = friendsInCommonWithMe == 1 ?
                        getString(R.string.String_TripItem_CommonFriendsSingular)
                        : getString(R.string.String_TripItem_CommonFriendsPlural);
                viewHolder.getDriverFacebookFriendsInCommonDescription().setText(facebookFriendsInCommonDescription);
            }


            Glide.with(SearchResultsActivity.this)
                    .load(driverPhotoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(viewHolder.getDriverPhoto());


            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedFireBaseTrip = firebaseTrip;

                    // If user is not logged in
                    if (!AppAuth.getInstance().isUserLoggedIn(SearchResultsActivity.this)) {
                        Intent intent = new Intent(SearchResultsActivity.this, SignInActivity.class);
                        intent.putExtra("SIGN_IN_DUE_TO_CONTENT_RESTRICTION", true);
                        startActivityForResult(intent, RequestCodes.SIGN_IN);
                    } else {
                        startTripDetailsActivity(firebaseTrip);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.mFirebaseTrips.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mDate, mTime, mOriginCity, mDestinationCity, mPrice, mEmptySeats, mEmptySeatsDescription, mDriverName,
                    mDriverFacebookFriendsInCommon, mDriverFacebookFriendsInCommonDescription, mDriverRating;

            private ImageView mDriversPhoto;

            private LinearLayout mFacebookInformationLayout;

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
        }
    }
}

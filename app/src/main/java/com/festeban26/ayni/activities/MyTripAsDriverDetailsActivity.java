package com.festeban26.ayni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.festeban26.ayni.R;
import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.FirebaseTripPassenger;
import com.festeban26.ayni.google.maps.CalculateRouteForFirebaseTripAsyncTask;
import com.festeban26.ayni.google.maps.WorkaroundMapFragment;
import com.festeban26.ayni.google.maps.helpers.MapsHelper;
import com.festeban26.ayni.google.maps.interfaces.OnRouteReadyListener;
import com.festeban26.ayni.utils.CurrencyHelper;
import com.festeban26.ayni.utils.IntentNames;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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

public class MyTripAsDriverDetailsActivity extends AppCompatActivity {

    private ScrollView mScrollView;

    private GoogleMap mRoutePreviewMap;

    private FirebaseTrip mFirebaseTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trip_as_driver_details);

        Toolbar toolbar = findViewById(R.id.Toolbar_MyTripAsDriverDetailsActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {

            if(getIntent().hasExtra("firebaseTrip")){
                String firebaseTripAsJsonString = getIntent().getStringExtra("firebaseTrip");
                FirebaseTrip firebaseTrip = new Gson().fromJson(firebaseTripAsJsonString, FirebaseTrip.class);

                if (firebaseTrip != null) {
                    mFirebaseTrip = firebaseTrip;
                    setViews();
                }
            }
            else if(getIntent().hasExtra(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__BUNDLE)){
                Bundle bundle = getIntent().getBundleExtra(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__BUNDLE);

                String originCity = bundle.getString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__ORIGIN_CITY);
                String destinationCity = bundle.getString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__DESTINATION_CITY);
                String year = bundle.getString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__YEAR);
                String month = bundle.getString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__MONTH);
                String day = bundle.getString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__DAY);
                String id = bundle.getString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__ID);

                if (originCity != null && destinationCity != null && year != null && month != null && day != null && id != null) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Trips")
                            .child(originCity + "-" + destinationCity)
                            .child(year)
                            .child(month)
                            .child(day)
                            .child(id);

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                FirebaseTrip firebaseTrip = dataSnapshot.getValue(FirebaseTrip.class);

                                if (firebaseTrip != null) {

                                    mFirebaseTrip = firebaseTrip;
                                    setViews();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        }
    }

    private void setViews() {

        mScrollView = findViewById(R.id.ScrollView_MyTripAsDriverDetailsActivity);

        TextView dateTextView = findViewById(R.id.TextView_TripItem_Date);
        TextView timeTextView = findViewById(R.id.TextView_TripItem_Time);
        TextView mOriginCityTextView = findViewById(R.id.TextView_TripItem_OriginCity);
        TextView destinationCityTextView = findViewById(R.id.TextView_TripItem_DestinationCity);
        TextView priceTextView = findViewById(R.id.TextView_TripItem_Price);
        TextView numberOfEmptySeatsTextView = findViewById(R.id.TextView_TripItem_NumberOfEmptySeats);
        TextView emptySeatsDescription = findViewById(R.id.TextView_TripItem_NumberOfEmptySeatsDescription);

        View mPassengersView = findViewById(R.id.View_MyTripAsDriverDetailsActivity_Passengers);
        View mRouteView = findViewById(R.id.View_MyTripAsDriverDetailsActivity_Route);

        final FirebaseTrip firebaseTrip = mFirebaseTrip;

        String date = firebaseTrip.getDate_asString();
        String time = firebaseTrip.getTime_asString();
        String originCity = firebaseTrip.getOrigin().getCity();
        String destinationCity = firebaseTrip.getDestination().getCity();
        String price = CurrencyHelper.getAsString(firebaseTrip.getPrice());

        String numberOfEmptySeats = Integer.toString(firebaseTrip.getNumberOfEmptySeats());
        String numberOfEmptySeatsDescription = firebaseTrip.getNumberOfEmptySeats() == 1 ?
                getString(R.string.String_TripItem_EmptySeatsSingular)
                : getString(R.string.String_TripItem_EmptySeatsPlural);

        dateTextView.setText(date);
        timeTextView.setText(time);
        mOriginCityTextView.setText(originCity);
        destinationCityTextView.setText(destinationCity);
        priceTextView.setText(price);
        numberOfEmptySeatsTextView.setText(numberOfEmptySeats);
        emptySeatsDescription.setText(numberOfEmptySeatsDescription);

        // Check if the trip has passengers
        // If has no passengers
        if (firebaseTrip.getPassengers() == null) {
            mPassengersView.setVisibility(View.GONE);
        } else {
            mPassengersView.setVisibility(View.VISIBLE);
            setView_PassengersView();
        }

        // Check if the trip has D2D service
        // If it does not provides the service
        if (!firebaseTrip.getOffersD2DService()) {
            mRouteView.setVisibility(View.GONE);
        } else {
            mRouteView.setVisibility(View.VISIBLE);
            setView_RoutePreviewView();
        }
    }


    private void setView_PassengersView() {
        RecyclerView mPassengersRecyclerView = findViewById(R.id.RecyclerView_MyTripAsDriverPassengers);
        mPassengersRecyclerView.setHasFixedSize(true);
        mPassengersRecyclerView.setLayoutManager(new LinearLayoutManager(MyTripAsDriverDetailsActivity.this));

        List<FirebaseTripPassenger> passengers = new ArrayList<>(mFirebaseTrip.getPassengers().values());

        PassengersAdapter adapter = new PassengersAdapter(mFirebaseTrip, passengers);
        mPassengersRecyclerView.setAdapter(adapter);
    }

    private void setView_RoutePreviewView() {

        if (mRoutePreviewMap == null) {

            Fragment routePreviewMapFragment
                    = getSupportFragmentManager().findFragmentById(R.id.MapFragment_MyTripRouteCardview_RoutePreview);
            WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) routePreviewMapFragment;

            if (mapFragment != null) {
                mapFragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        mScrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mRoutePreviewMap = googleMap;
                        mRoutePreviewMap.getUiSettings().setZoomControlsEnabled(true);
                        mRoutePreviewMap.getUiSettings().setTiltGesturesEnabled(false);
                        mRoutePreviewMap.setPadding(10, 10, 10, 10); // left, top, right, bottom

                        CalculateRouteForFirebaseTripAsyncTask asyncTask = new CalculateRouteForFirebaseTripAsyncTask(
                                mFirebaseTrip,
                                getString(R.string.google_maps_key),
                                new OnRouteReadyListener() {
                                    @Override
                                    public void onSuccess(List<LatLng> polylinePath, long distanceInMeters,
                                                          long durationInSeconds, MapsHelper mapsHelper) {

                                        mRoutePreviewMap.moveCamera(mapsHelper.getCameraUpdate(150));

                                        PolylineOptions polylineOptions = new PolylineOptions()
                                                .addAll(polylinePath)
                                                .color(ContextCompat.getColor(MyTripAsDriverDetailsActivity.this, R.color.Color_Post));
                                        mRoutePreviewMap.addPolyline(polylineOptions);

                                        List<MarkerOptions> markers = mapsHelper.getMarkers(
                                                getString(R.string.String_General_Origin),
                                                getString(R.string.String_General_Destination));

                                        for(MarkerOptions marker : markers)
                                            mRoutePreviewMap.addMarker(marker);
                                    }

                                    @Override
                                    public void onFailure() {

                                    }
                                }
                        );
                        asyncTask.execute();
                    }
                });
            }
        }
    }

    private class PassengersAdapter extends RecyclerView.Adapter<PassengersAdapter.ViewHolder> {

        private List<FirebaseTripPassenger> mPassengers;
        private FirebaseTrip mFirebaseTrip;

        public PassengersAdapter(FirebaseTrip firebaseTrip, List<FirebaseTripPassenger> passengers) {
            mFirebaseTrip = firebaseTrip;
            mPassengers = passengers;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(MyTripAsDriverDetailsActivity.this).inflate(R.layout.item_passenger_in_my_trip, viewGroup, false);
            return new PassengersAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            final FirebaseTripPassenger passenger = mPassengers.get(i);

            String name = passenger.getFirstName();
            int friendsInCommon = passenger.getFriendsInCommonWithDriver();
            String friendsInCommon_asString = Integer.toString(friendsInCommon);
            String friendsInCommonDescription = friendsInCommon == 1 ?
                    getString(R.string.String_TripItem_CommonFriendsSingular)
                    : getString(R.string.String_TripItem_CommonFriendsPlural);
            // Fee
            String fee;
            // If the passenger has no registered D2D service
            if (passenger.getD2DService() == null) {
                viewHolder.getD2DServiceDescription().setVisibility(View.GONE);
                fee = CurrencyHelper.getAsString(mFirebaseTrip.getPrice());
            } else {
                viewHolder.getD2DServiceDescription().setVisibility(View.VISIBLE);
                Money money = CurrencyHelper.getAsMoney(mFirebaseTrip.getPrice())
                        .plus(passenger.getD2DService().getFee());
                fee = CurrencyHelper.getAsString(money);
            }
            String imageUrl = passenger.getProfilePictureUrl();

            viewHolder.getName().setText(name);
            viewHolder.getNumberOfFacebookCommonFriendsWithTheDriver().setText(friendsInCommon_asString);
            viewHolder.getDescriptionOfNumberOfFacebookCommonFriendsWithTheDriver().setText(friendsInCommonDescription);
            viewHolder.getFee().setText(fee);

            Glide.with(MyTripAsDriverDetailsActivity.this)
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(viewHolder.getPhoto());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MyTripAsDriverDetailsActivity.this, UserProfileActivity.class);
                    intent.putExtra("userId", passenger.getId());
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mPassengers.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView mPhoto;
            private TextView mName;
            private TextView mFee;
            private TextView mD2DServiceDescription;
            private TextView mNumberOfFacebookCommonFriendsWithTheDriver;
            private TextView mDescriptionOfNumberOfFacebookCommonFriendsWithTheDriver;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                mPhoto = itemView.findViewById(R.id.ImageView_PassengerInMyTripItem_DriverPhoto);
                mName = itemView.findViewById(R.id.TextView_PassengerInMyTripItem_PassengerName);
                mFee = itemView.findViewById(R.id.TextView_PassengerInMyTripItem_Fee);
                mD2DServiceDescription = itemView.findViewById(R.id.TextView_PassengerInMyTripItem_D2DServiceDescription);
                mNumberOfFacebookCommonFriendsWithTheDriver = itemView.findViewById(R.id.TextView_PassengerInMyTripItem_NumberOfFacebookCommonFriendsWithTheDriver);
                mDescriptionOfNumberOfFacebookCommonFriendsWithTheDriver = itemView.findViewById(R.id.TextView_PassengerInMyTripItem_DescriptionOfNumberOfFacebookCommonFriendsWithTheDriver);
            }

            public ImageView getPhoto() {
                return mPhoto;
            }

            public TextView getName() {
                return mName;
            }

            public TextView getFee() {
                return mFee;
            }

            public TextView getD2DServiceDescription() {
                return mD2DServiceDescription;
            }

            public TextView getNumberOfFacebookCommonFriendsWithTheDriver() {
                return mNumberOfFacebookCommonFriendsWithTheDriver;
            }

            public TextView getDescriptionOfNumberOfFacebookCommonFriendsWithTheDriver() {
                return mDescriptionOfNumberOfFacebookCommonFriendsWithTheDriver;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

package com.festeban26.ayni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.firebase.model.FirebaseD2DService;
import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.FirebaseTripPassenger;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.firebase.model.Location;
import com.festeban26.ayni.google.maps.WorkaroundMapFragment;
import com.festeban26.ayni.utils.CurrencyHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.joda.money.Money;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyTripAsPassengerDetailsActivity extends AppCompatActivity {

    private ScrollView mScrollView;

    private FirebaseTrip mFirebaseTrip;
    private GoogleMap mPickupLocationPreviewMap;
    private GoogleMap mDestinationLocationMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trip_as_passenger_details);

        Toolbar toolbar = findViewById(R.id.Toolbar_MyTripAsPassengerDetailsActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {

            String firebaseTripAsJsonString = getIntent().getStringExtra("firebaseTrip");
            FirebaseTrip firebaseTrip = new Gson().fromJson(firebaseTripAsJsonString, FirebaseTrip.class);

            if (firebaseTrip != null) {
                mFirebaseTrip = firebaseTrip;
                setViews();
            }
        }
    }

    private void setViews() {

        mScrollView = findViewById(R.id.ScrollView_MyTripAsPassengerDetailsActivity);

        TextView dateTextView = findViewById(R.id.TextView_TripItem_Date);
        TextView timeTextView = findViewById(R.id.TextView_TripItem_Time);
        TextView mOriginCityTextView = findViewById(R.id.TextView_TripItem_OriginCity);
        TextView destinationCityTextView = findViewById(R.id.TextView_TripItem_DestinationCity);
        TextView priceTextView = findViewById(R.id.TextView_TripItem_Price);
        TextView numberOfEmptySeatsTextView = findViewById(R.id.TextView_TripItem_NumberOfEmptySeats);
        TextView emptySeatsDescription = findViewById(R.id.TextView_TripItem_NumberOfEmptySeatsDescription);

        TextView driverNameTextView = findViewById(R.id.TextView_TripItem_DriverName);
        TextView driverFacebookFriendsInCommonTextView = findViewById(R.id.TextView_TripItem_DriverNumberOfFacebookFriendsInCommon);
        TextView driverFacebookFriendsInCommonDescription = findViewById(R.id.TextView_TripItem_DriverNumberOfFacebookFriendsInCommonDescription);
        ImageView driverPhotoImageView = findViewById(R.id.ImageView_TripItem_DriverPhoto);
        LinearLayout driverProfileLayout = findViewById(R.id.Layout_TripItem_DriverInformation);
        ImageView viewDriverProfileImageView = findViewById(R.id.ImageView_TripItem_ViewDriverProfile);
        LinearLayout driverFacebookInformationLayout = findViewById(R.id.Layout_TripItem_FacebookInformation);

        View mPassengersView = findViewById(R.id.View_MyTripAsPassengerDetailsActivity_Passengers);
        View mOriginAndDestinationView = findViewById(R.id.View_MyTripAsPassengerDetailsActivity_D2DOriginAndDestinationPreview);
        TextView myPickupAddressTextView = findViewById(R.id.TextView_OriginAndDestinationPreviewCardview_OriginAddress);
        TextView myDestinationAddressTextView = findViewById(R.id.TextView_OriginAndDestinationPreviewCardview_DestinationAddress);

        final FirebaseTrip firebaseTrip = mFirebaseTrip;
        final FacebookUser mCurrentUser = AppAuth.getInstance().getCurrentFacebookUser(MyTripAsPassengerDetailsActivity.this);

        String date = firebaseTrip.getDate_asString();
        String time = firebaseTrip.getTime_asString();
        String originCity = firebaseTrip.getOrigin().getCity();
        String destinationCity = firebaseTrip.getDestination().getCity();

        String numberOfEmptySeats = Integer.toString(firebaseTrip.getNumberOfEmptySeats());
        String numberOfEmptySeatsDescription = firebaseTrip.getNumberOfEmptySeats() == 1 ?
                getString(R.string.String_TripItem_EmptySeatsSingular)
                : getString(R.string.String_TripItem_EmptySeatsPlural);

        dateTextView.setText(date);
        timeTextView.setText(time);
        mOriginCityTextView.setText(originCity);
        destinationCityTextView.setText(destinationCity);
        numberOfEmptySeatsTextView.setText(numberOfEmptySeats);
        emptySeatsDescription.setText(numberOfEmptySeatsDescription);

        FirebaseUser driver = firebaseTrip.getDriver();
        String driverName = driver.getFirstName();
        String driverPhotoUrl = driver.getProfilePictureUrl();

        viewDriverProfileImageView.setVisibility(View.VISIBLE);

        driverNameTextView.setText(driverName);
        driverFacebookInformationLayout.setVisibility(View.VISIBLE);

        int friendsInCommonWithMe = driver.getNumberOfFriendsInCommonWithMe(mCurrentUser);
        String numberOfFacebookFriendsInCommon = Integer.toString(friendsInCommonWithMe);
        driverFacebookFriendsInCommonTextView.setText(numberOfFacebookFriendsInCommon);

        String facebookFriendsInCommonDescription = friendsInCommonWithMe == 1 ?
                getString(R.string.String_TripItem_CommonFriendsSingular)
                : getString(R.string.String_TripItem_CommonFriendsPlural);
        driverFacebookFriendsInCommonDescription.setText(facebookFriendsInCommonDescription);

        Glide.with(MyTripAsPassengerDetailsActivity.this)
                .load(driverPhotoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(driverPhotoImageView);

        driverProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser driver = firebaseTrip.getDriver();
                Intent intent = new Intent(MyTripAsPassengerDetailsActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", driver.getId());
                startActivity(intent);
            }
        });

        // Check if the trip has passengers
        // If has no passengers
        if (firebaseTrip.getPassengers() == null) {
            mPassengersView.setVisibility(View.GONE);
        } else {
            mPassengersView.setVisibility(View.VISIBLE);
            setView_PassengersView();
        }

        // Check if I had a D2D service
        FirebaseTripPassenger me = firebaseTrip.getPassengers().get(mCurrentUser.getId());
        if (me != null) {
            // If i don't have any d2d registered services
            if (me.getD2DService() == null) {
                mOriginAndDestinationView.setVisibility(View.GONE);
                String price = CurrencyHelper.getAsString(firebaseTrip.getPrice());
                priceTextView.setText(price);
            }
            // If I have a D2D service
            else {

                // Display total price
                Money money = CurrencyHelper.getAsMoney(firebaseTrip.getPrice())
                        .plus(me.getD2DService().getFee());
                String fee = CurrencyHelper.getAsString(money);
                priceTextView.setText(fee);

                mOriginAndDestinationView.setVisibility(View.VISIBLE);

                FirebaseD2DService myD2DService = me.getD2DService();
                myPickupAddressTextView.setText(myD2DService.getOrigin().getAddress());
                myDestinationAddressTextView.setText(myD2DService.getDestination().getAddress());
                setView_OriginAndDestinationPreviewMap(myD2DService.getOrigin(), myD2DService.getDestination());
            }
        }
    }

    private void setView_PassengersView() {

        RecyclerView mPassengersRecyclerView = findViewById(R.id.RecyclerView_MyTripAsPassengerPassengers);
        mPassengersRecyclerView.setHasFixedSize(true);
        mPassengersRecyclerView.setLayoutManager(new LinearLayoutManager(MyTripAsPassengerDetailsActivity.this,
                LinearLayoutManager.HORIZONTAL, false));

        List<FirebaseTripPassenger> passengers = new ArrayList<>(mFirebaseTrip.getPassengers().values());

        PassengersAdapter adapter = new PassengersAdapter(passengers);
        mPassengersRecyclerView.setAdapter(adapter);

    }

    private void setView_OriginAndDestinationPreviewMap(final Location origin, final Location destination) {

        if (mPickupLocationPreviewMap == null) {

            Fragment originPreviewMapFragment
                    = getSupportFragmentManager().findFragmentById(R.id.MapFragment_OriginAndDestinationPreviewCardview_Origin);
            WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) originPreviewMapFragment;

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
                        mPickupLocationPreviewMap = googleMap;
                        mPickupLocationPreviewMap.getUiSettings().setZoomControlsEnabled(true);
                        mPickupLocationPreviewMap.getUiSettings().setTiltGesturesEnabled(false);
                        mPickupLocationPreviewMap.setPadding(5, 5, 5, 5); // left, top, right, bottom
                        mPickupLocationPreviewMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                LatLng originMarkerCoordinates = new LatLng(origin.getLat(), origin.getLng());
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(originMarkerCoordinates)
                                        .zoom(16f) // 15f to set zoom to street level
                                        .build();

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                                mPickupLocationPreviewMap.moveCamera(cameraUpdate);

                                mPickupLocationPreviewMap.addMarker(new MarkerOptions()
                                        .position(originMarkerCoordinates)
                                        .title(getString(R.string.String_General_Origin)));
                            }
                        });
                    }
                });
            }
        }

        if (mDestinationLocationMap != null) {
            mDestinationLocationMap.clear();
        } else {
            Fragment originPreviewMapFragment = getSupportFragmentManager().findFragmentById(R.id.MapFragment_OriginAndDestinationPreviewCardview_Destination);
            WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) originPreviewMapFragment;

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
                        mDestinationLocationMap = googleMap;
                        mDestinationLocationMap.getUiSettings().setZoomControlsEnabled(true);
                        mDestinationLocationMap.getUiSettings().setTiltGesturesEnabled(false);
                        mDestinationLocationMap.setPadding(5, 5, 5, 5); // left, top, right, bottom
                        mDestinationLocationMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                LatLng destinationMarkerCoordinates = new LatLng(destination.getLat(), destination.getLng());
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(destinationMarkerCoordinates)
                                        .zoom(16f) // 15f to set zoom to street level
                                        .build();

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                                mDestinationLocationMap.moveCamera(cameraUpdate);

                                mDestinationLocationMap.addMarker(new MarkerOptions()
                                        .position(destinationMarkerCoordinates)
                                        .title(getString(R.string.String_General_Destination)));
                            }
                        });
                    }
                });
            }
        }

    }

    private class PassengersAdapter extends RecyclerView.Adapter<PassengersAdapter.ViewHolder> {

        private List<FirebaseTripPassenger> mPassengers;

        public PassengersAdapter(List<FirebaseTripPassenger> passengers) {
            mPassengers = passengers;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(MyTripAsPassengerDetailsActivity.this).inflate(R.layout.item_simple_passenger, viewGroup, false);
            return new PassengersAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            final FirebaseTripPassenger passenger = mPassengers.get(i);

            String name = passenger.getFirstName();
            String imageUrl = passenger.getProfilePictureUrl();

            viewHolder.getName().setText(name);
            Glide.with(MyTripAsPassengerDetailsActivity.this)
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(viewHolder.getPhoto());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MyTripAsPassengerDetailsActivity.this, UserProfileActivity.class);
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

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                mPhoto = itemView.findViewById(R.id.ImageView_SimplePassengerItem_Photo);
                mName = itemView.findViewById(R.id.TextView_SimplePassengerItem_Name);
            }

            public ImageView getPhoto() {
                return mPhoto;
            }

            public TextView getName() {
                return mName;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

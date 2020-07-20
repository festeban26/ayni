package com.festeban26.ayni.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.firebase.model.FirebaseTripPassenger;
import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.firebase.model.Location;
import com.festeban26.ayni.google.maps.WorkaroundMapFragment;
import com.festeban26.ayni.utils.CurrencyHelper;
import com.festeban26.ayni.utils.RequestCodes;
import com.festeban26.ayni.utils.ResultCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

public class TripDetailsActivity extends AppCompatActivity {

    private ScrollView mScrollView;

    private Button mBookingButton;

    private FacebookUser mCurrentUser;
    private FirebaseTrip mFirebaseTrip;

    private GoogleMap mOriginPreviewMap;
    private GoogleMap mDestinationPreviewMap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.BOOK_TRIP) {
            if (resultCode == ResultCodes.SUCCESS) {
                setResult(ResultCodes.SUCCESS);
                finish();
            } else if (resultCode == ResultCodes.ANOTHER_USER_BOOKED_FIRST) {
                if (data != null) {
                    String updatedFirebaseTripAsJsonString = data.getStringExtra("updatedFirebaseTrip");
                    FirebaseTrip updatedFirebaseTrip = new Gson().fromJson(updatedFirebaseTripAsJsonString, FirebaseTrip.class);

                    if (updatedFirebaseTrip != null) {
                        mFirebaseTrip = updatedFirebaseTrip;

                        setViews();

                        AlertDialog alertDialog = new AlertDialog.Builder(TripDetailsActivity.this).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage(getString(R.string.String_ToastMessage_SomeoneElseJustBookedThisTrip));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        // TODO
                        //mReloadSearchResults = true;
                    }
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        Toolbar toolbar = findViewById(R.id.Toolbar_JourneyDetailsActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {

            String firebaseTripAsJsonString = getIntent().getStringExtra("firebaseTrip");
            FirebaseTrip firebaseTrip = new Gson().fromJson(firebaseTripAsJsonString, FirebaseTrip.class);

            if (firebaseTrip != null) {
                mCurrentUser = AppAuth.getInstance().getCurrentFacebookUser(TripDetailsActivity.this);
                mFirebaseTrip = firebaseTrip;
                setViews();
            }
        }
    }

    private void setViews() {

        mScrollView = findViewById(R.id.ScrollView_TripDetailsActivity);

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

        LinearLayout doorToDoorServiceInformationLayout = findViewById(R.id.LinearLayout_TripDetailsActivity_DoorToDoorService);
        TextView originAddressOfDoorToDoorService = findViewById(R.id.TextView_TripDetailsActivity_OriginAddressOfDoorToDoorService);
        TextView destinationAddressOfDoorToDoorService = findViewById(R.id.TextView_TripDetailsActivity_DestinationAddressOfDoorToDoorService);

        mBookingButton = findViewById(R.id.Button_TripDetailsActivity_Book);

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

        final FirebaseUser driver = firebaseTrip.getDriver();
        String driverName = driver.getFirstName();
        String driverPhotoUrl = driver.getProfilePictureUrl();

        viewDriverProfileImageView.setVisibility(View.VISIBLE);

        // If the current user is the driver
        if (driver.getId().equals(mCurrentUser.getId())) {
            String driverNameToBeDisplayed = getString(R.string.String_SearchResultsActivity_DriverMe);
            driverNameTextView.setText(driverNameToBeDisplayed);
            driverFacebookInformationLayout.setVisibility(View.INVISIBLE);
            mBookingButton.setEnabled(false);
        } else {

            driverNameTextView.setText(driverName);
            driverFacebookInformationLayout.setVisibility(View.VISIBLE);

            int friendsInCommonWithMe = driver.getNumberOfFriendsInCommonWithMe(mCurrentUser);
            String numberOfFacebookFriendsInCommon = Integer.toString(friendsInCommonWithMe);
            driverFacebookFriendsInCommonTextView.setText(numberOfFacebookFriendsInCommon);

            String facebookFriendsInCommonDescription = friendsInCommonWithMe == 1 ?
                    getString(R.string.String_TripItem_CommonFriendsSingular)
                    : getString(R.string.String_TripItem_CommonFriendsPlural);
            driverFacebookFriendsInCommonDescription.setText(facebookFriendsInCommonDescription);
        }

        Glide.with(TripDetailsActivity.this)
                .load(driverPhotoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(driverPhotoImageView);

        driverProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetailsActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", driver.getId());
                startActivity(intent);
            }
        });

        // If the trip does not offers Door-to-Door Service
        if (!firebaseTrip.getOffersD2DService()) {
            doorToDoorServiceInformationLayout.setVisibility(View.GONE);
        } else {

            String originAddress = firebaseTrip.getOrigin().getAddress();
            String destinationAddress = firebaseTrip.getDestination().getAddress();

            if (originAddress == null)
                originAddressOfDoorToDoorService.setVisibility(View.GONE);
            else
                originAddressOfDoorToDoorService.setText(originAddress);

            if (destinationAddress == null)
                destinationAddressOfDoorToDoorService.setVisibility(View.GONE);
            else
                destinationAddressOfDoorToDoorService.setText(destinationAddress);

            setView_OriginAndDestinationPreviewMap(firebaseTrip.getOrigin(), firebaseTrip.getDestination());
        }

        setView_BookingButton(firebaseTrip);
    }

    private void setView_OriginAndDestinationPreviewMap(final Location origin, final Location destination) {

        if (mOriginPreviewMap != null) {
            mOriginPreviewMap.clear();
        } else {
            Fragment originPreviewMapFragment = getSupportFragmentManager().findFragmentById(R.id.Fragment_TripDetailsActivity_OriginPreview);
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
                        mOriginPreviewMap = googleMap;
                        mOriginPreviewMap.getUiSettings().setZoomControlsEnabled(true);
                        mOriginPreviewMap.getUiSettings().setTiltGesturesEnabled(false);
                        mOriginPreviewMap.setPadding(5, 5, 5, 5); // left, top, right, bottom
                        mOriginPreviewMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                LatLng originMarkerCoordinates = new LatLng(origin.getLat(), origin.getLng());
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(originMarkerCoordinates)
                                        .zoom(16f) // 15f to set zoom to street level
                                        .build();

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                                mOriginPreviewMap.moveCamera(cameraUpdate);

                                mOriginPreviewMap.addMarker(new MarkerOptions()
                                        .position(originMarkerCoordinates)
                                        .title(getString(R.string.String_General_Origin)));
                            }
                        });
                    }
                });
            }
        }

        if (mDestinationPreviewMap != null) {
            mDestinationPreviewMap.clear();
        } else {
            Fragment originPreviewMapFragment = getSupportFragmentManager().findFragmentById(R.id.Fragment_TripDetailsActivity_DestinationPreview);
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
                        mDestinationPreviewMap = googleMap;
                        mDestinationPreviewMap.getUiSettings().setZoomControlsEnabled(true);
                        mDestinationPreviewMap.getUiSettings().setTiltGesturesEnabled(false);
                        mDestinationPreviewMap.setPadding(5, 5, 5, 5); // left, top, right, bottom
                        mDestinationPreviewMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                LatLng destinationMarkerCoordinates = new LatLng(destination.getLat(), destination.getLng());
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(destinationMarkerCoordinates)
                                        .zoom(16f) // 15f to set zoom to street level
                                        .build();

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                                mDestinationPreviewMap.moveCamera(cameraUpdate);

                                mDestinationPreviewMap.addMarker(new MarkerOptions()
                                        .position(destinationMarkerCoordinates)
                                        .title(getString(R.string.String_General_Destination)));
                            }
                        });
                    }
                });
            }
        }
    }

    private void setView_BookingButton(FirebaseTrip currentFirebaseTrip) {

        boolean alreadyPartOfTheTrip = false;
        boolean isTripFull = false;

        // If i'm already part of this trip, disable button
        // TODO FUTURE What if i want to book multiple times
        Map<String, FirebaseTripPassenger> tripPassengers = currentFirebaseTrip.getPassengers();

        if (tripPassengers != null)
            if (tripPassengers.containsKey(mCurrentUser.getId()))
                alreadyPartOfTheTrip = true;


        // Execute only if the current user is not part of the trip
        if (!alreadyPartOfTheTrip) {
            // If there is no empty seats, disable button
            if (currentFirebaseTrip.getNumberOfEmptySeats() == 0) {
                isTripFull = true;
            }
        }

        if (alreadyPartOfTheTrip) {
            mBookingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(TripDetailsActivity.this,
                            getString(R.string.String_ToastMessage_YouHaveAlreadyBookThisTrip),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else if (isTripFull) {
            mBookingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(TripDetailsActivity.this,
                            getString(R.string.String_ToastMessage_TripIsFull),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mBookingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TripDetailsActivity.this, BookingConfirmationActivity.class);
                    intent.putExtra("firebaseTrip", new Gson().toJson(mFirebaseTrip));
                    startActivityForResult(intent, RequestCodes.BOOK_TRIP);
                }
            });
        }
    }
}

package com.festeban26.ayni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.fares.utils.FareCalculator;
import com.festeban26.ayni.firebase.model.FirebaseD2DService;
import com.festeban26.ayni.firebase.model.FirebaseTripPassenger;
import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.google.maps.CalculateRouteAsyncTask;
import com.festeban26.ayni.google.maps.OnRouteResponseListener;
import com.festeban26.ayni.google.maps.model.Route;
import com.festeban26.ayni.utils.CurrencyHelper;
import com.festeban26.ayni.utils.RequestCodes;
import com.festeban26.ayni.utils.ResultCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.maps.model.DirectionsResult;

import org.joda.money.Money;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookingConfirmationActivity extends AppCompatActivity {


    private View mRequestDoorToDoorServiceLayout;
    private View mDoorToDoorServiceOriginAndDestinationLayout;
    private View mDoorToDoorServiceFaresLayout;

    private Switch mDoorToDoorServiceSwitch;

    private EditText mDoorToDoorOriginEditText;
    private EditText mDoorToDoorDestinationEditText;

    private TextView mSubtotalPriceTextView;
    private TextView mDoorToDoorServicePrice;
    private TextView mTotalPriceTextView;

    private Button mBookingButton;
    private Button mCancelButton;

    private FirebaseD2DService mD2DService;
    private LatLng mD2DOriginLatLng;
    private LatLng mD2DDestinationLatLng;

    private FirebaseTrip mFirebaseTrip;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == RequestCodes.GET_ORIGIN_COORDINATES || requestCode == RequestCodes.GET_DESTINATION_COORDINATES) {

            if (resultCode == ResultCodes.SUCCESS) {

                if (data != null && data.getExtras() != null) {
                    double latitude = data.getDoubleExtra("latitude", 0);
                    double longitude = data.getDoubleExtra("longitude", 0);

                    if (requestCode == RequestCodes.GET_ORIGIN_COORDINATES) {
                        mD2DOriginLatLng = new LatLng(latitude, longitude);

                        String originCity = mFirebaseTrip.getOrigin().getCity();
                        String displayText = getString(R.string.String_DroppedPinNear) + " " + originCity;
                        mDoorToDoorOriginEditText.setText(displayText);
                    } else {
                        mD2DDestinationLatLng = new LatLng(latitude, longitude);
                        String destinationCity = mFirebaseTrip.getDestination().getCity();
                        String displayText = getString(R.string.String_DroppedPinNear) + " " + destinationCity;
                        mDoorToDoorDestinationEditText.setText(displayText);
                    }

                    if (mD2DOriginLatLng != null && mD2DDestinationLatLng != null) {
                        createD2DService();
                    }
                }
            }
        }
    }

    private void createD2DService() {
        mDoorToDoorServicePrice.setText(getString(R.string.String_Calculating));

        mD2DService = new FirebaseD2DService();

        if (mFirebaseTrip.getOffersD2DService()) {
            LatLng originalTripOrigin = mFirebaseTrip.getOrigin().getLocation_asLatLng();
            LatLng originalTripDestination = mFirebaseTrip.getDestination().getLocation_asLatLng();

            List<LatLng> waypoints = new ArrayList<>();
            waypoints.add(mD2DOriginLatLng);
            waypoints.add(mD2DDestinationLatLng);
            String googleMapsApiKey = getString(R.string.google_maps_key);

            CalculateRouteAsyncTask asyncTask = new CalculateRouteAsyncTask(
                    originalTripOrigin,
                    originalTripDestination,
                    waypoints,
                    false,
                    googleMapsApiKey,
                    new OnRouteResponseListener() {
                        @Override
                        public void onRouteResults(Route result, DirectionsResult directionsResult) {

                            Money d2DFare = FareCalculator.getFare(
                                    mFirebaseTrip.getOriginalDistance(),
                                    result.getDistanceInMeters(),
                                    mFirebaseTrip.getOriginalDuration(),
                                    result.getDurationInSeconds());

                            mD2DService.setOrigin(result.getWaypoints().get(0));
                            mD2DService.setDestination(result.getWaypoints().get(1));
                            mD2DService.setFee(CurrencyHelper.getAsDouble(d2DFare));
                            mD2DService.setDistance(result.getDistanceInMeters());
                            mD2DService.setDuration(result.getDurationInSeconds());

                            mDoorToDoorServicePrice.setText(CurrencyHelper.getAsString(d2DFare));
                            Money total = d2DFare.plus(CurrencyHelper.getAsMoney(mFirebaseTrip.getPrice()));
                            mTotalPriceTextView.setText(CurrencyHelper.getAsString(total));

                            mDoorToDoorOriginEditText.setText(mD2DService.getOrigin().getAddress());
                            mDoorToDoorDestinationEditText.setText(mD2DService.getDestination().getAddress());
                        }
                    });
            asyncTask.execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        Toolbar toolbar = findViewById(R.id.Toolbar_BookingConfirmationActivity);
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

        TextView originCityTextView = findViewById(R.id.TextView_BookingConfirmationActivity_OriginCity);
        TextView destinationCityTextView = findViewById(R.id.TextView_BookingConfirmationActivity_DestinationCity);
        TextView dateTextView = findViewById(R.id.TextView_BookingConfirmationActivity_Date);
        TextView timeTextView = findViewById(R.id.TextView_BookingConfirmationActivity_Time);


        mDoorToDoorDestinationEditText = findViewById(R.id.EditText_BookingConfirmationActivity_DoorToDoorDestination);

        mSubtotalPriceTextView = findViewById(R.id.TextView_BookingConfirmationActivity_Subtotal);
        mDoorToDoorServicePrice = findViewById(R.id.TextView_BookingConfirmationActivity_DoorToDoorServicePrice);
        mTotalPriceTextView = findViewById(R.id.TextView_BookingConfirmationActivity_Total);

        String originCity = mFirebaseTrip.getOrigin().getCity();
        String destinationCity = mFirebaseTrip.getDestination().getCity();
        String date = mFirebaseTrip.getDate_asString();
        String time = mFirebaseTrip.getTime_asString();
        mTotalPriceTextView.setText(mFirebaseTrip.getPrice_asString());

        originCityTextView.setText(originCity);
        destinationCityTextView.setText(destinationCity);
        dateTextView.setText(date);
        timeTextView.setText(time);

        mRequestDoorToDoorServiceLayout = findViewById(R.id.Layout_BookingConfirmationActivity_RequestDoorToDoorService);
        mDoorToDoorServiceOriginAndDestinationLayout = findViewById(R.id.Layout_BookingConfirmationActivity_DoorToDoorServiceOriginAndDestination);
        mDoorToDoorServiceFaresLayout = findViewById(R.id.Layout_BookingConfirmationActivity_DoorToDoorServiceFares);
        mDoorToDoorServiceSwitch = findViewById(R.id.Switch_BookingConfirmationActivity_DoorToDoorService);

        setView_DoorToDoorServiceRelatedViews();

        mCancelButton = findViewById(R.id.Button_BookingConfirmationActivity_Cancel);
        mBookingButton = findViewById(R.id.Button_BookingConfirmationActivity_Book);

        setView_BookingButton();
        setView_CancelButton();
    }

    private void setView_DoorToDoorServiceRelatedViews() {

        mRequestDoorToDoorServiceLayout.setVisibility(View.GONE);
        mDoorToDoorServiceOriginAndDestinationLayout.setVisibility(View.GONE);
        mDoorToDoorServiceFaresLayout.setVisibility(View.GONE);

        if (mFirebaseTrip.getOffersD2DService()) {
            mRequestDoorToDoorServiceLayout.setVisibility(View.VISIBLE);
            mDoorToDoorServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        mDoorToDoorServiceOriginAndDestinationLayout.setVisibility(View.VISIBLE);
                        mDoorToDoorServiceFaresLayout.setVisibility(View.VISIBLE);

                        mSubtotalPriceTextView.setText(mFirebaseTrip.getPrice_asString());
                        if (mD2DService != null) {

                            Money d2DFee = CurrencyHelper.getAsMoney(mD2DService.getFee());
                            mDoorToDoorServicePrice.setText(CurrencyHelper.getAsString(d2DFee));
                            Money total = d2DFee.plus(CurrencyHelper.getAsMoney(mFirebaseTrip.getPrice()));
                            mTotalPriceTextView.setText(CurrencyHelper.getAsString(total));
                        } else {
                            mDoorToDoorServicePrice.setText("");
                            mTotalPriceTextView.setText("");
                        }


                    } else {
                        mDoorToDoorServiceOriginAndDestinationLayout.setVisibility(View.GONE);
                        mDoorToDoorServiceFaresLayout.setVisibility(View.GONE);

                        mSubtotalPriceTextView.setText("");
                        mTotalPriceTextView.setText(mFirebaseTrip.getPrice_asString());
                    }
                }
            });
        }

        mDoorToDoorServiceSwitch.setChecked(false);
        setView_DoorToDoorOriginEditText();
        setView_DoorToDoorDestinationEditText();

    }

    private void setView_DoorToDoorOriginEditText() {
        mDoorToDoorOriginEditText = findViewById(R.id.EditText_BookingConfirmationActivity_DoorToDoorOrigin);
        mDoorToDoorOriginEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoorToDoorOriginEditText.setError(null);
                Intent intent = new Intent(BookingConfirmationActivity.this, SelectMapLocationActivity.class);
                intent.putExtra("city", mFirebaseTrip.getOrigin().getCity());
                startActivityForResult(intent, RequestCodes.GET_ORIGIN_COORDINATES);
            }
        });
    }

    private void setView_DoorToDoorDestinationEditText() {
        mDoorToDoorDestinationEditText = findViewById(R.id.EditText_BookingConfirmationActivity_DoorToDoorDestination);
        mDoorToDoorDestinationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoorToDoorDestinationEditText.setError(null);
                Intent intent = new Intent(BookingConfirmationActivity.this, SelectMapLocationActivity.class);
                intent.putExtra("city", mFirebaseTrip.getDestination().getCity());
                startActivityForResult(intent, RequestCodes.GET_DESTINATION_COORDINATES);
            }
        });
    }

    private void setView_CancelButton() {
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setView_BookingButton() {
        mBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToBookTrip();
            }
        });
    }

    private void tryToBookTrip() {

        final FirebaseTrip firebaseTrip = mFirebaseTrip;
        String originCity = firebaseTrip.getOrigin().getCity();
        String destinationCity = firebaseTrip.getDestination().getCity();
        String id = firebaseTrip.getId();

        final DatabaseReference tripToBookDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Trips")
                .child(originCity + "-" + destinationCity)
                .child(firebaseTrip.getYear_asString())
                .child(firebaseTrip.getMonth_asString())
                .child(firebaseTrip.getDay_asString())
                .child(id);

        tripToBookDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    FirebaseTrip firebaseTrip = dataSnapshot.getValue(FirebaseTrip.class);

                    if (firebaseTrip != null) {
                        if (firebaseTrip.getNumberOfEmptySeats() > 0)
                            bookTrip(firebaseTrip, tripToBookDatabaseReference);
                        else {
                            goBackToTripDetailsActivityDueToNotEnoughSeats();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void goBackToTripDetailsActivityDueToNotEnoughSeats() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        final FirebaseTrip firebaseTrip = mFirebaseTrip;
        String originCity = firebaseTrip.getOrigin().getCity();
        String destinationCity = firebaseTrip.getDestination().getCity();
        String id = firebaseTrip.getId();

        databaseReference
                .child("Trips")
                .child(originCity + "-" + destinationCity)
                .child(firebaseTrip.getYear_asString())
                .child(firebaseTrip.getMonth_asString())
                .child(firebaseTrip.getDay_asString())
                .child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final FirebaseTrip updatedFirebaseTrip = dataSnapshot.getValue(FirebaseTrip.class);
                            Intent intent = new Intent();
                            intent.putExtra("updatedFirebaseTrip", new Gson().toJson(updatedFirebaseTrip));
                            setResult(ResultCodes.ANOTHER_USER_BOOKED_FIRST, intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void bookTrip(final FirebaseTrip tripToBook, DatabaseReference tripDatabaseReference) {

        FacebookUser me = AppAuth.getInstance().getCurrentFacebookUser(BookingConfirmationActivity.this);

        String myId = me.getId();
        String myFirstName = me.getFirstName();
        String myProfileImgUrl = me.getUserImageUrl();
        int friendsInCommonWithDriver = tripToBook.getDriver().getNumberOfFriendsInCommonWithMe(me);

        FirebaseTripPassenger meAsSimplePassenger = new FirebaseTripPassenger(myId, myFirstName, myProfileImgUrl, friendsInCommonWithDriver);

        Map<String, Object> updates = new HashMap<>();


        if (mDoorToDoorServiceSwitch.isChecked() && mD2DService != null) {
            meAsSimplePassenger.setD2DService(mD2DService);
        }

        tripToBook.addPassenger(meAsSimplePassenger);
        updates.put("passengers", tripToBook.getPassengers());

        // Update the main trip
        tripDatabaseReference.updateChildren(updates);

        FirebaseUser driver = tripToBook.getDriver();

        // First update the driver trip
        DatabaseReference driverTripDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("UserTrips")
                .child(driver.getId())
                .child(tripToBook.getId());

        driverTripDatabaseReference.updateChildren(updates);

        // Then add the trip to me
        DatabaseReference myTripDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("UserTrips")
                .child(myId)
                .child(tripToBook.getId());
        myTripDatabaseReference.setValue(tripToBook);

        // TODO: SKIP ME
        // Finally iterate and update each passenger trip and update it
        for (Map.Entry<String, FirebaseTripPassenger> entry : tripToBook.getPassengers().entrySet()) {

            String passengerId = entry.getValue().getId();

            DatabaseReference passengerDatabaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("UserTrips")
                    .child(passengerId)
                    .child(tripToBook.getId());

            passengerDatabaseReference.updateChildren(updates);
        }


        Toast.makeText(BookingConfirmationActivity.this,
                getString(R.string.String_ToastMessage_TripSuccesfullyBooked),
                Toast.LENGTH_LONG).show();

        setResult(ResultCodes.SUCCESS);
        finish();
    }
}

package com.festeban26.ayni.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.firebase.model.FirebaseTrip;
import com.festeban26.ayni.firebase.model.FirebaseUser;
import com.festeban26.ayni.firebase.model.Location;
import com.festeban26.ayni.google.maps.CalculateRouteAsyncTask;
import com.festeban26.ayni.google.maps.OnRouteResponseListener;
import com.festeban26.ayni.google.maps.WorkaroundMapFragment;
import com.festeban26.ayni.google.maps.model.Route;
import com.festeban26.ayni.utils.NetworkState;
import com.festeban26.ayni.utils.RequestCodes;
import com.festeban26.ayni.utils.ResultCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private ScrollView mScrollView;
    private Spinner mOriginCitySpinner;
    private Spinner mDestinationCitySpinner;
    private EditText mDateEditText;
    private EditText mTimeEditText;
    private RadioGroup mNumberOfSeatsRadioGroup;
    private EditText mPriceEditText;
    private Button mPostButton;

    private Switch mDoorToDoorServiceSwitch;
    private LinearLayout mDoorToDoorLayout;
    private EditText mDoorToDoorOriginEditText;
    private EditText mDoorToDoorDestinationEditText;

    final private Calendar mCalendar = Calendar.getInstance();
    private final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();

    private LatLng mDoorToDoorOriginLatLng;
    private LatLng mDoorToDoorDestinationLatLng;

    private GoogleMap mRoutePreviewMap;
    private LinearLayout mRoutePreviewLayout;

    private Route mDoorToDoorServiceRoute;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == RequestCodes.SIGN_IN) {
            if (resultCode == ResultCodes.SUCCESS)
                setViews();
            else
                finish();
        } else if (requestCode == RequestCodes.GET_ORIGIN_COORDINATES || requestCode == RequestCodes.GET_DESTINATION_COORDINATES) {
            if (resultCode == ResultCodes.SUCCESS) {

                if (data != null && data.getExtras() != null) {
                    double latitude = data.getDoubleExtra("latitude", 0);
                    double longitude = data.getDoubleExtra("longitude", 0);

                    if (requestCode == RequestCodes.GET_ORIGIN_COORDINATES) {
                        mDoorToDoorOriginLatLng = new LatLng(latitude, longitude);
                        String origin = mOriginCitySpinner.getSelectedItem().toString();
                        String displayText = getString(R.string.String_DroppedPinNear) + " " + origin;
                        mDoorToDoorOriginEditText.setText(displayText);
                    } else {
                        mDoorToDoorDestinationLatLng = new LatLng(latitude, longitude);
                        String destination = mDestinationCitySpinner.getSelectedItem().toString();
                        String displayText = getString(R.string.String_DroppedPinNear) + " " + destination;
                        mDoorToDoorDestinationEditText.setText(displayText);
                    }

                    boolean isDoorToDoorOriginSet = !TextUtils.isEmpty(mDoorToDoorOriginEditText.getText().toString());
                    boolean isDoorToDoorDestinationSet = !TextUtils.isEmpty(mDoorToDoorDestinationEditText.getText().toString());

                    if (isDoorToDoorOriginSet && isDoorToDoorDestinationSet)
                        showRoutePreview();
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.Toolbar_PostActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // If the user is not logged in
        if (!AppAuth.getInstance().isUserLoggedIn(this)) {
            Intent intent = new Intent(PostActivity.this, SignInActivity.class);
            intent.putExtra("SIGN_IN_DUE_TO_CONTENT_RESTRICTION", true);
            startActivityForResult(intent, RequestCodes.SIGN_IN);

        } else if (!NetworkState.isNetworkAvailable(this)) {
            Toast.makeText(getApplicationContext(),
                    getApplicationContext().getResources().getString(R.string.String_InternetConnectionNotAvailableMessage),
                    Toast.LENGTH_LONG)
                    .show();
            finish();
        } else {
            setViews();
        }

    }

    private void showRoutePreview() {
        mRoutePreviewLayout.setVisibility(View.VISIBLE);

        if (mRoutePreviewMap != null)
            mRoutePreviewMap.clear();

        CalculateRouteAsyncTask asyncTask = new CalculateRouteAsyncTask(
                mDoorToDoorOriginLatLng,
                mDoorToDoorDestinationLatLng,
                getString(R.string.google_maps_key),
                new OnRouteResponseListener() {
                    @Override
                    public void onRouteResults(Route result, DirectionsResult directionsResult) {
                        if (result != null && directionsResult != null) {

                            mDoorToDoorServiceRoute = result;

                            String startAddress = result.getOrigin().getAddress();
                            String endAddress = result.getDestination().getAddress();

                            mDoorToDoorOriginEditText.setText(startAddress);
                            mDoorToDoorDestinationEditText.setText(endAddress);

                            if (mRoutePreviewMap != null) {
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                builder.include(mDoorToDoorOriginLatLng);
                                builder.include(mDoorToDoorDestinationLatLng);
                                LatLngBounds bounds = builder.build();
                                int padding = 150; // offset from edges of the map in pixels
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                mRoutePreviewMap.moveCamera(cameraUpdate);

                                List<com.google.android.gms.maps.model.LatLng> decodedPath
                                        = PolyUtil.decode(directionsResult.routes[0].overviewPolyline.getEncodedPath());
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .addAll(decodedPath)
                                        .color(ContextCompat.getColor(PostActivity.this, R.color.Color_Post));

                                mRoutePreviewMap.addPolyline(polylineOptions);
                                String originMarkerTitle = getString(R.string.String_General_Origin);
                                String destinationMarkerTitle = getString(R.string.String_General_Destination);

                                mRoutePreviewMap.addMarker(new MarkerOptions().position(mDoorToDoorOriginLatLng).title(originMarkerTitle));
                                mRoutePreviewMap.addMarker(new MarkerOptions().position(mDoorToDoorDestinationLatLng).title(destinationMarkerTitle));
                            }
                        }
                    }
                });

        asyncTask.execute();
    }


    private void setViews() {

        mScrollView = findViewById(R.id.ScrollView_PostActivity);
        mOriginCitySpinner = findViewById(R.id.Spinner_PostActivity_OriginCity);
        mDestinationCitySpinner = findViewById(R.id.Spinner_PostActivity_DestinationCity);
        mDateEditText = findViewById(R.id.EditText_PostActivity_Date);
        mTimeEditText = findViewById(R.id.EditText_PostActivity_Time);
        mNumberOfSeatsRadioGroup = findViewById(R.id.RadioGroup_PostActivity_NumberOfSeats);
        mPriceEditText = findViewById(R.id.EditText_PostActivity_Price);
        mDoorToDoorServiceSwitch = findViewById(R.id.Switch_PostActivity_DoorToDoorService);
        mPostButton = findViewById(R.id.Button_PostActivity_Post);

        mDoorToDoorLayout = findViewById(R.id.LinearLayout_PostActivity_DoorToDoor);
        mDoorToDoorOriginEditText = findViewById(R.id.EditText_PostActivity_DoorToDoorOrigin);
        mDoorToDoorDestinationEditText = findViewById(R.id.EditText_PostActivity_DoorToDoorDestination);

        mRoutePreviewLayout = findViewById(R.id.LinearLayout_PostActivity_RoutePreview);

        mDoorToDoorLayout.setVisibility(View.GONE);
        mDoorToDoorServiceSwitch.setChecked(false);

        mRoutePreviewLayout.setVisibility(View.GONE);

        setView_OriginCityAndDestinationCitySpinners();
        setView_Date();
        setView_Time();
        setView_NumberOfSeats();
        setView_Price();
        setView_PostButton();
        setView_DoorToDoorServiceSwitch();

        mDoorToDoorOriginEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoorToDoorOriginEditText.setError(null);
                Intent intent = new Intent(PostActivity.this, SelectMapLocationActivity.class);
                intent.putExtra("city", mOriginCitySpinner.getSelectedItem().toString());
                startActivityForResult(intent, RequestCodes.GET_ORIGIN_COORDINATES);
            }
        });

        mDoorToDoorDestinationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoorToDoorDestinationEditText.setError(null);
                Intent intent = new Intent(PostActivity.this, SelectMapLocationActivity.class);
                intent.putExtra("city", mDestinationCitySpinner.getSelectedItem().toString());
                startActivityForResult(intent, RequestCodes.GET_DESTINATION_COORDINATES);
            }
        });

        if (mRoutePreviewMap == null) {
            final WorkaroundMapFragment mapFragment
                    = (WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.Fragment_PostActivity_RoutePreview);

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
                    }
                });
            }

        }
    }

    private void setView_OriginCityAndDestinationCitySpinners() {

        mDatabaseReference.child("AvailableCities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<String> availableCities = new ArrayList<>();
                if (dataSnapshot.exists())
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        availableCities.add(snapshot.getKey());

                String[] spinnersOptions = availableCities.toArray(new String[0]);

                ArrayAdapter<String> originCitiesAdapter = new ArrayAdapter<>(PostActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, spinnersOptions);

                ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(PostActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, spinnersOptions);

                mOriginCitySpinner.setAdapter(originCitiesAdapter);
                mDestinationCitySpinner.setAdapter(destinationAdapter);

                // By default select second option for the destination Spinner
                mDestinationCitySpinner.setSelection(1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setView_Date() {

        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                //Update date label;
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MMM/yyyy", Locale.getDefault());
                mDateEditText.setText(sdf.format(mCalendar.getTime()));
            }
        };

        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Remove error warning
                mDateEditText.setError(null);

                DatePickerDialog datePickerDialog = new DatePickerDialog(PostActivity.this, R.style.PostDialogTheme, onDateSetListener,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));

                // Set min date
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });
    }

    private void setView_Time() {

        final TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                mTimeEditText.setText(sdf.format(mCalendar.getTime()));
            }
        };

        mTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Remove error warning
                mTimeEditText.setError(null);

                new TimePickerDialog(PostActivity.this, R.style.PostDialogTheme, onTimeSetListener,
                        mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE), false).show();
            }
        });
    }

    private void setView_NumberOfSeats() {
        // Check first option by default
        RadioButton defaultRadioButton = findViewById(mNumberOfSeatsRadioGroup.getChildAt(0).getId());

        defaultRadioButton.setTextColor(ContextCompat.getColor(this, R.color.customWhite));
        defaultRadioButton.setBackground(ContextCompat.getDrawable(PostActivity.this,
                R.drawable.shape__post_activity__selected_number_of_seats_rectangle));

        mNumberOfSeatsRadioGroup.check(defaultRadioButton.getId());

        mNumberOfSeatsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // Reset all buttons background and text color
                for (int i = 0; i < group.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) group.getChildAt(i);

                    radioButton.setTextColor(ContextCompat.getColor(PostActivity.this, R.color.Gray));
                    radioButton.setBackground(ContextCompat.getDrawable(PostActivity.this,
                            R.drawable.shape__post_activity__unselected_number_of_seats_rectangle));

                }

                RadioButton radioButton = findViewById(checkedId);
                radioButton.setTextColor(ContextCompat.getColor(PostActivity.this, R.color.customWhite));
                radioButton.setBackground(ContextCompat.getDrawable(PostActivity.this,
                        R.drawable.shape__post_activity__selected_number_of_seats_rectangle));

            }
        });
    }

    private void setView_Price() {
        mPriceEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove error warning
                mPriceEditText.setError(null);
            }
        });
    }

    private void setView_PostButton() {
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean incompleteField = false;

                if (TextUtils.isEmpty(mDateEditText.getText().toString())) {
                    incompleteField = true;
                    mDateEditText.setError("Campo obligatorio");
                }

                if (TextUtils.isEmpty(mTimeEditText.getText().toString())) {
                    incompleteField = true;
                    mTimeEditText.setError("Campo obligatorio");
                }

                if (TextUtils.isEmpty(mPriceEditText.getText().toString())) {
                    incompleteField = true;
                    mPriceEditText.setError("Campo obligatorio");
                }

                if (mDoorToDoorServiceSwitch.isChecked()) {

                    if (TextUtils.isEmpty(mDoorToDoorOriginEditText.getText().toString())) {
                        incompleteField = true;
                        mDoorToDoorOriginEditText.setError("Campo obligatorio");
                    }

                    if (TextUtils.isEmpty(mDoorToDoorDestinationEditText.getText().toString())) {
                        incompleteField = true;
                        mDoorToDoorDestinationEditText.setError("Campo obligatorio");
                    }
                }

                if (incompleteField) {
                    Toast.makeText(PostActivity.this, "Campos incompletos", Toast.LENGTH_SHORT).show();
                } else {

                    String originCity = mOriginCitySpinner.getSelectedItem().toString();
                    String destinationCity = mDestinationCitySpinner.getSelectedItem().toString();
                    int year = mCalendar.get(Calendar.YEAR);
                    int month = mCalendar.get(Calendar.MONTH);
                    int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                    int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
                    int minute = mCalendar.get(Calendar.MINUTE);

                    RadioButton selectedRadioButton = mNumberOfSeatsRadioGroup.findViewById(mNumberOfSeatsRadioGroup.getCheckedRadioButtonId());
                    int numberOfSeats = mNumberOfSeatsRadioGroup.indexOfChild(selectedRadioButton) + 1;
                    int price = Integer.parseInt(mPriceEditText.getText().toString());

                    FirebaseTrip firebaseTrip = new FirebaseTrip();
                    firebaseTrip.setDate(year, month, day, hour, minute);

                    firebaseTrip.setNumberOfSeats(numberOfSeats);
                    firebaseTrip.setPrice(price);

                    FacebookUser currentFacebookUser = AppAuth.getInstance()
                            .getCurrentFacebookUser(PostActivity.this);

                    FirebaseUser me = currentFacebookUser.getAsFirebaseUser();

                    firebaseTrip.setDriver(me);

                    Location origin = new Location();
                    Location destination = new Location();

                    origin.setCity(originCity);
                    destination.setCity(destinationCity);

                    if (mDoorToDoorServiceSwitch.isChecked() && mDoorToDoorServiceRoute != null) {

                        firebaseTrip.setOffersD2DService(true);
                        final Route route = mDoorToDoorServiceRoute;
                        origin.setAddress(route.getOrigin().getAddress());
                        origin.setLat(route.getOrigin().getLat());
                        origin.setLng(route.getOrigin().getLng());

                        destination.setAddress(route.getDestination().getAddress());
                        destination.setLat(route.getDestination().getLat());
                        destination.setLng(route.getDestination().getLng());

                        firebaseTrip.setOriginalDistance(route.getDistanceInMeters());
                        firebaseTrip.setOriginalDuration(route.getDurationInSeconds());
                    }
                    firebaseTrip.setOrigin(origin);
                    firebaseTrip.setDestination(destination);


                    DatabaseReference tripsDatabaseReference = mDatabaseReference.child("Trips")
                            .child(originCity + "-" + destinationCity)
                            .child(firebaseTrip.getYear_asString())
                            .child(firebaseTrip.getMonth_asString())
                            .child(firebaseTrip.getDay_asString());

                    String pushId = tripsDatabaseReference.push().getKey();

                    String firebaseTripId = firebaseTrip.getYear_asString()
                            + firebaseTrip.getMonth_asString()
                            + firebaseTrip.getDay_asString()
                            + firebaseTrip.getHour_asString()
                            + firebaseTrip.getMinute_asString()
                            + pushId;

                    firebaseTrip.setId(firebaseTripId);

                    tripsDatabaseReference.child(firebaseTripId).setValue(firebaseTrip);

                    DatabaseReference usersTripsDatabaseReference = mDatabaseReference
                            .child("UserTrips")
                            .child(me.getId());

                    usersTripsDatabaseReference.child(firebaseTripId).setValue(firebaseTrip);

                    Toast.makeText(PostActivity.this, "Viaje publicado correctamente", Toast.LENGTH_SHORT).show();
                    setResult(ResultCodes.SUCCESS);
                    finish();
                }
            }
        });
    }

    private void setView_DoorToDoorServiceSwitch() {
        mDoorToDoorServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDoorToDoorLayout.setVisibility(View.VISIBLE);
                } else {
                    mDoorToDoorLayout.setVisibility(View.GONE);
                }
            }
        });
    }
}

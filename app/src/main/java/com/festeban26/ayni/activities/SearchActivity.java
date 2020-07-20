package com.festeban26.ayni.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.festeban26.ayni.R;
import com.festeban26.ayni.utils.NetworkState;
import com.festeban26.ayni.utils.RequestCodes;
import com.festeban26.ayni.utils.ResultCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    final private Calendar mCalendar = Calendar.getInstance();
    private final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();

    private Spinner mOriginCitySpinner;
    private Spinner mDestinationCitySpinner;
    private EditText mDateEditText;
    private Button mSearchButton;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.BOOK_TRIP) {
            if (resultCode == ResultCodes.SUCCESS) {
                setResult(ResultCodes.SUCCESS);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.Toolbar_SearchActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // If no internet connection is available, finish the activity
        if (!NetworkState.isNetworkAvailable(this)) {
            Toast.makeText(getApplicationContext(),
                    getApplicationContext().getResources().getString(R.string.String_InternetConnectionNotAvailableMessage),
                    Toast.LENGTH_LONG)
                    .show();
            finish();
        }

        // Set views
        mOriginCitySpinner = findViewById(R.id.Spinner_SearchActivity_OriginCity);
        mDestinationCitySpinner = findViewById(R.id.Spinner_SearchActivity_DestinationCity);
        mDateEditText = findViewById(R.id.EditText_SearchActivity_Date);
        mSearchButton = findViewById(R.id.Button_SearchActivity_Search);


        setOriginCityAndDestinationCityViewsFromOnlineDatabase();
        setDateView();
        setSearchButtonOnClickListener();
    }

    private void setDateView() {

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

                DatePickerDialog datePickerDialog = new DatePickerDialog(SearchActivity.this, R.style.SearchDialogTheme,
                        onDateSetListener,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));

                // Set min date
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });
    }

    private void setOriginCityAndDestinationCityViewsFromOnlineDatabase() {

        mDatabaseReference.child("AvailableCities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<String> availableCities = new ArrayList<>();
                if (dataSnapshot.exists())
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        availableCities.add(snapshot.getKey());

                String[] spinnersOptions = availableCities.toArray(new String[0]);

                ArrayAdapter<String> originAdapter = new ArrayAdapter<>(SearchActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, spinnersOptions);

                ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(SearchActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, spinnersOptions);

                mOriginCitySpinner.setAdapter(originAdapter);
                mDestinationCitySpinner.setAdapter(destinationAdapter);

                // By default select second option for the destination Spinner
                mDestinationCitySpinner.setSelection(1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setSearchButtonOnClickListener() {

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isSomeObligatoryFieldEmpty = false;

                if (mOriginCitySpinner.getSelectedItem() == null)
                    isSomeObligatoryFieldEmpty = true;

                if (mDestinationCitySpinner.getSelectedItem() == null)
                    isSomeObligatoryFieldEmpty = true;

                if (!isSomeObligatoryFieldEmpty) {

                    String originCity = mOriginCitySpinner.getSelectedItem().toString();
                    String destinationCity = mDestinationCitySpinner.getSelectedItem().toString();
                    // If no date is entered, calendar is, by default, set to current date
                    int year = mCalendar.get(Calendar.YEAR);
                    int month = mCalendar.get(Calendar.MONTH);
                    int day = mCalendar.get(Calendar.DAY_OF_MONTH);

                    Intent intent = new Intent(SearchActivity.this, SearchResultsActivity.class);
                    intent.putExtra("originCity", originCity);
                    intent.putExtra("destinationCity", destinationCity);
                    intent.putExtra("year", year);
                    intent.putExtra("month", month);
                    intent.putExtra("day", day);
                    startActivityForResult(intent, RequestCodes.BOOK_TRIP);
                }
            }
        });
    }

}

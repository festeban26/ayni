package com.festeban26.ayni.firebase.model;

import com.google.firebase.database.Exclude;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseTrip {

    private String mId;
    private Location mOrigin;
    private Location mDestination;
    private Calendar mCalendar;
    private int mPrice;
    private int mNumberOfSeats;
    private FirebaseUser mDriver;
    private Map<String, FirebaseTripPassenger> mPassengers;

    private boolean mOffersD2DService = false; // By default is false
    private Long mOriginalDistanceInMeters;
    private Long mOriginalDurationInSeconds;

    public FirebaseTrip() {
        mCalendar = Calendar.getInstance();
    }

    public String getId() {
        return this.mId;
    }

    public Location getOrigin() {
        return this.mOrigin;
    }

    public Location getDestination() {
        return this.mDestination;
    }

    public Map<String, Integer> getDate() {

        Map<String, Integer> date = new HashMap<>();
        date.put("year", this.mCalendar.get(Calendar.YEAR));
        date.put("month", this.mCalendar.get(Calendar.MONTH) + 1); // Plus 1 to set Jan to 1 instead of 0
        date.put("day", this.mCalendar.get(Calendar.DAY_OF_MONTH));
        date.put("hour", this.mCalendar.get(Calendar.HOUR_OF_DAY));
        date.put("minute", this.mCalendar.get(Calendar.MINUTE));
        return date;
    }

    public int getPrice() {
        return mPrice;
    }

    public int getNumberOfSeats() {
        return this.mNumberOfSeats;
    }

    public FirebaseUser getDriver() {
        return this.mDriver;
    }

    public Map<String, FirebaseTripPassenger> getPassengers() {
        return this.mPassengers;
    }

    public boolean getOffersD2DService(){
        return mOffersD2DService;
    }

    public Long getOriginalDistance(){
        return mOriginalDistanceInMeters;
    }

    public Long getOriginalDuration(){
        return mOriginalDurationInSeconds;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setOrigin(Location origin) {
        this.mOrigin = origin;
    }

    public void setDestination(Location destination) {
        this.mDestination = destination;
    }

    public void setDate(Map<String, Integer> date) {

        Integer year = date.get("year");
        Integer month = date.get("month");
        Integer day = date.get("day");
        Integer hour = date.get("hour");
        Integer minute = date.get("minute");

        if (year != null && month != null && day != null && hour != null && minute != null) {
            setDate(year, month - 1, day, hour, minute); // Minus one because on Calendar Jan = 0
        }
    }

    public void setPrice(int price) {
        this.mPrice = price;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.mNumberOfSeats = numberOfSeats;
    }

    public void setDriver(FirebaseUser driver) {
        this.mDriver = driver;
    }

    public void setPassengers(Map<String, FirebaseTripPassenger> passengers) {
        this.mPassengers = passengers;
    }

    public void setOffersD2DService(boolean offersD2DService){
        mOffersD2DService = offersD2DService;
    }

    public void setOriginalDistance(Long distanceInMeters){
        mOriginalDistanceInMeters = distanceInMeters;
    }

    public void setOriginalDuration(Long durationInSeconds){
        mOriginalDurationInSeconds = durationInSeconds;
    }

    @Exclude
    public Calendar getCalendar() {
        return this.mCalendar;
    }

    @Exclude
    public void addPassenger(FirebaseTripPassenger passenger) {

        // If its null initialize
        if(getPassengers() == null)
            setPassengers(new HashMap<String, FirebaseTripPassenger>());

        getPassengers().put(passenger.getId(), passenger);
    }

    @Exclude
    public void setDate(int year, int month, int day, int hour, int minute) {
        this.mCalendar.set(Calendar.YEAR, year);
        this.mCalendar.set(Calendar.MONTH, month);
        this.mCalendar.set(Calendar.DAY_OF_MONTH, day);
        this.mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        this.mCalendar.set(Calendar.MINUTE, minute);
    }

    @Exclude
    public int getNumberOfEmptySeats() {
        if(mPassengers == null){
            return mNumberOfSeats;
        }
        else{
            return mNumberOfSeats - mPassengers.size();
        }
    }


    @Exclude
    public String getDate_asString(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(this.mCalendar.getTime());
    }

    /**
     * Default pattern: EEEE, dd/MMMM/yyyy
     */
    @Exclude
    public String getDate_asString() {
        return getDate_asString("EEEE, dd/MMMM/yyyy");
    }

    @Exclude
    public String getTime_asString(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(this.mCalendar.getTime());
    }

    /**
     * Default pattern: h:mm a
     */
    @Exclude
    public String getTime_asString() {
        return getTime_asString("h:mm a");
    }

    @Exclude
    public int getYear() {
        return getCalendar().get(Calendar.YEAR);
    }

    @Exclude
    public String getYear_asString() {
        NumberFormat formatter = new DecimalFormat("00");
        return formatter.format(getYear());
    }

    @Exclude
    public int getMonth() {
        return getCalendar().get(Calendar.MONTH) + 1;
    }

    @Exclude
    public String getMonth_asString() {
        NumberFormat formatter = new DecimalFormat("00");
        return formatter.format(getMonth());
    }

    @Exclude
    public int getDay() {
        return this.mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    @Exclude
    public String getDay_asString() {
        NumberFormat formatter = new DecimalFormat("00");
        return formatter.format(getDay());
    }

    @Exclude
    public int getHour() {
        return this.mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    @Exclude
    public String getHour_asString() {
        NumberFormat formatter = new DecimalFormat("00");
        return formatter.format(getHour());
    }

    @Exclude
    public int getMinute() {
        return this.mCalendar.get(Calendar.MINUTE);
    }

    @Exclude
    public String getMinute_asString() {
        NumberFormat formatter = new DecimalFormat("00");
        return formatter.format(getMinute());
    }

    @Exclude
    public String getPrice_asString() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(getPrice());

    }
}

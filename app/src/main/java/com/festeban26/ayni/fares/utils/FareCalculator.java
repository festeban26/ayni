package com.festeban26.ayni.fares.utils;

import com.festeban26.ayni.fares.Fares;

import org.joda.money.Money;
import org.joda.money.format.MoneyFormatterBuilder;

import java.math.RoundingMode;
import java.util.Locale;


// Rounding modes
// https://docs.oracle.com/javase/6/docs/api/java/math/RoundingMode.html?is-external=true
public class FareCalculator {

    /**
     *
     * @param distance in meters
     * @param time in seconds
     * @return
     */
    public static Money getFare(long distance, long time){

        if(distance < 0)
            distance = 0;
        if(time < 0)
            time = 0;

        double distanceInKm = (double) distance / 1000;
        double timeInMinutes = (double) time / 60;

        Money distanceFare = Fares.PER_KM.multipliedBy(distanceInKm, RoundingMode.HALF_UP);
        Money timeFare = Fares.PER_MIN.multipliedBy(timeInMinutes, RoundingMode.HALF_UP);

        return distanceFare.plus(timeFare).plus(Fares.PICK_UP);
    }

    public static Money getFare(long originalDistance, long distanceWithDeviation, long originalTime, long timeWithDeviation){
        return getFare(distanceWithDeviation - originalDistance, timeWithDeviation - originalTime);
    }
}

package com.festeban26.ayni.utils;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.money.format.MoneyFormatterBuilder;

import java.math.BigDecimal;
import java.util.Locale;

public class CurrencyHelper {

    public static String getAsString(Money money){

        return new MoneyFormatterBuilder()
                .appendCurrencySymbolLocalized()
                .appendAmountLocalized()
                .toFormatter()
                .withLocale(new Locale("en", "US"))
                .print(money);
    }

    public static double getAsDouble(Money money){

        BigDecimal bigDecimal = money.getAmount();
        return bigDecimal.doubleValue();
    }

    public static Money getAsMoney(int value){
        return Money.of(CurrencyUnit.USD, value);
    }

    public static Money getAsMoney(double value){
        return Money.of(CurrencyUnit.USD, value);
    }

    public static String getAsString(double value){
        return getAsString(getAsMoney(value));
    }

    public static String getAsString(int value){
        return getAsString(getAsMoney(value));
    }
}

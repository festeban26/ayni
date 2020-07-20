package com.festeban26.ayni.fares;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 *  Tarifas (USD)
 *
 *                  Ordenanza   Uber    Cabify
 *  Minuto Espera       0.1     0.05    0.07
 *  Km                  0.4     0.25    0.29
 *  Carrera Mínima      1.45    1.5     1.81
 *  Arranque            0.5     0.6     0.63
 */
public class Fares {
    public static final Money PICK_UP = Money.of(CurrencyUnit.USD, 1.00); // 1$
    public static final Money PER_KM = Money.of(CurrencyUnit.USD, 0.20);
    public static final Money PER_MIN = Money.of(CurrencyUnit.USD, 0.05);
}

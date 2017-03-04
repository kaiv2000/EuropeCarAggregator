package com.kaiv.johnny.carfinder;

import java.util.Comparator;

public class carComparator implements Comparator<Car> {

    @Override
    public int compare(Car car1, Car car2) {
        return Integer.valueOf(car1.price) - Integer.valueOf(car2.price);
    }
}

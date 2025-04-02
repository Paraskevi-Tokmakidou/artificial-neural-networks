package com.mycompany.mlp.with.genetic.algorithms;

/**
 *
 * @author Paraskevi Tokmakidou
 */

import java.util.ArrayList;
import java.util.SplittableRandom;

public class RandomValues {

    private static final SplittableRandom SPLITTABLE_RANDOM = new SplittableRandom();

    public static ArrayList<Double> returnArrayOfDoubleValues(Double min, Double max, int count) {
        ArrayList<Double> array = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Double randomValue = min + (max - min) * SPLITTABLE_RANDOM.nextDouble(); // [min, max]
            array.add(randomValue);
        }

        return array;
    }

    public static int returnRandomInteger(int max) {
        return SPLITTABLE_RANDOM.nextInt(max + 1); // [0, max]
    }

    public static Double returnRandomDouble() {
        return SPLITTABLE_RANDOM.nextDouble(); // [0.0, 1.0]
    }
}
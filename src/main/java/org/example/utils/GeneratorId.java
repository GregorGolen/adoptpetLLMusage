package org.example.utils;

import java.util.Random;

public class GeneratorId {
    private static final Random random = new Random();

    public static long provideId() {
        long result;
        do {
            result = random.nextLong();
        } while (result <= 0);
        return result;
    }
}

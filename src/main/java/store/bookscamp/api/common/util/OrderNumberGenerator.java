package store.bookscamp.api.common.util;

import java.util.UUID;

public class OrderNumberGenerator {

    private OrderNumberGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
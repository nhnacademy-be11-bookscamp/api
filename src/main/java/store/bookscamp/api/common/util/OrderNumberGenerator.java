package store.bookscamp.api.common.util;

import java.util.UUID;

public class OrderNumberGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
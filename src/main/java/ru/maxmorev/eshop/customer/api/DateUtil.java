package ru.maxmorev.eshop.customer.api;

import java.time.Instant;

public class DateUtil {
    public static Long getCurrentTimestampInMilli() {
        return Instant.now().toEpochMilli();
    }
}

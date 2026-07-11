package com.fsocial.postservice.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String getNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formater = DateTimeFormatter.ofPattern("mm:HH dd-MM-yyyy");

        return now.format(formater);
    }
}
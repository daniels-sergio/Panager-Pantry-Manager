package com.example.smartpantrymanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Works out how urgent a pantry item's expiry date is, using the rule from the
 * assignment spec: expired = before today, expiring soon = within the next 3
 * days, normal = more than 3 days away (or no date set at all).
 *
 * Dates are stored as plain "yyyy-MM-dd" strings. This uses Calendar/SimpleDateFormat
 * (not java.time.LocalDate) so it runs on minSdk 24 without needing core library
 * desugaring.
 */
public final class ExpiryHelper {

    private ExpiryHelper() {
    }

    public static final String STORAGE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_FORMAT = "d MMMM yyyy";

    public enum Status {EXPIRED, EXPIRING_SOON, NORMAL, NO_DATE}

    public static Status getStatus(String isoDate) {
        Date date = parse(isoDate);
        if (date == null) {
            return Status.NO_DATE;
        }
        long daysUntilExpiry = daysBetween(startOfToday(), startOfDay(date));
        if (daysUntilExpiry < 0) {
            return Status.EXPIRED;
        }
        if (daysUntilExpiry <= 3) {
            return Status.EXPIRING_SOON;
        }
        return Status.NORMAL;
    }

    public static boolean isValidDate(String isoDate) {
        return parse(isoDate) != null;
    }

    public static String formatForDisplay(String isoDate) {
        Date date = parse(isoDate);
        if (date == null) {
            return "No expiry date";
        }
        SimpleDateFormat display = new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault());
        return "Expires: " + display.format(date);
    }

    private static Date parse(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat storage = new SimpleDateFormat(STORAGE_FORMAT, Locale.US);
        storage.setLenient(false);
        try {
            return storage.parse(isoDate.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private static Date startOfToday() {
        return startOfDay(new Date());
    }

    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static long daysBetween(Date from, Date to) {
        return TimeUnit.MILLISECONDS.toDays(to.getTime() - from.getTime());
    }
}

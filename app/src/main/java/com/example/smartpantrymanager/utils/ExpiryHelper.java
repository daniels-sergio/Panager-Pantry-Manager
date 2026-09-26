package com.example.smartpantrymanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Decides whether a pantry item is expired, expiring soon, or fine, and turns
 * stored dates into something readable.
 *
 * The rule:
 *  - Expired: the date is before today.
 *  - Expiring soon: today, or within the next 3 days.
 *  - Normal: more than 3 days away.
 *  - No date: the user didn't set one.
 *
 * Dates are saved as text like "2026-09-15". This file uses Java's older date
 * tools (Calendar and SimpleDateFormat) because the newer ones aren't available
 * on the oldest Android versions this app supports (Android 7).
 */
public final class ExpiryHelper {

    // Only holds helper methods, so nobody needs to create one.
    private ExpiryHelper() {
    }

    // How dates are saved ("2026-09-15") and how they're shown ("15 September 2026").
    public static final String STORAGE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_FORMAT = "d MMMM yyyy";

    // The four possible answers getStatus() can give.
    public enum Status {EXPIRED, EXPIRING_SOON, NORMAL, NO_DATE}

    /**
     * Works out which of the four statuses a saved date falls into.
     * The pantry list uses the answer to choose the item's colour.
     */
    public static Status getStatus(String isoDate) {
        Date date = parse(isoDate);
        if (date == null) {
            return Status.NO_DATE;
        }
        // Both dates are moved to midnight before comparing. Otherwise something
        // expiring today at 00:00 would already count as "expired" by lunchtime.
        long daysUntilExpiry = daysBetween(startOfToday(), startOfDay(date));
        if (daysUntilExpiry < 0) {
            return Status.EXPIRED;
        }
        if (daysUntilExpiry <= 3) {
            return Status.EXPIRING_SOON;
        }
        return Status.NORMAL;
    }

    // True if the text is a real date in year-month-day form. The add/edit screen
    // uses this to check what the user typed.
    public static boolean isValidDate(String isoDate) {
        return parse(isoDate) != null;
    }

    // Turns "2026-09-15" into "Expires: 15 September 2026" for the pantry list,
    // or "No expiry date" if there isn't one.
    public static String formatForDisplay(String isoDate) {
        Date date = parse(isoDate);
        if (date == null) {
            return "No expiry date";
        }
        // Locale.getDefault() writes the month name in the phone's language.
        SimpleDateFormat display = new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault());
        return "Expires: " + display.format(date);
    }

    /**
     * Turns saved text like "2026-09-15" into a Date that can be compared.
     * Returns null for empty or invalid text instead of crashing.
     */
    private static Date parse(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        // Locale.US keeps the reading of digits the same on every phone.
        SimpleDateFormat storage = new SimpleDateFormat(STORAGE_FORMAT, Locale.US);
        // Strict mode: reject impossible dates like "2026-02-31". Without this,
        // Java quietly rolls them over into March.
        storage.setLenient(false);
        try {
            return storage.parse(isoDate.trim());
        } catch (ParseException e) {
            return null; // the text wasn't a valid date
        }
    }

    private static Date startOfToday() {
        return startOfDay(new Date());
    }

    // Returns the same date with the time set to exactly midnight.
    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // Number of whole days from one date to another. Negative if "to" is earlier.
    // Dates are stored as milliseconds, so this subtracts them and converts to days.
    private static long daysBetween(Date from, Date to) {
        return TimeUnit.MILLISECONDS.toDays(to.getTime() - from.getTime());
    }
}

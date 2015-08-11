package net.mvla.mvhs;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class PrefUtils {

    public static final String PREF_WELCOME_DONE = "pref_welcome_done";
    public static final String PREF_OPEN_SCHEDULE_SETUP = "pref_open_schedule_setup";
    public static final String PREF_GUEST_MODE = "pref_guest_mode";

    public static final String PREF_SCHEDULE_PREFIX = "pref_schd_prd";
    public static final String PREF_SCHEDULE_ROOM = "_room";
    public static final String PREF_SCHEDULE_SBJCT = "_subjct";

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).apply();
    }

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public static boolean isGuest(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_GUEST_MODE, false);
    }

    public static void setGuestMode(final Context context, boolean student) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_GUEST_MODE, student).apply();
    }
}

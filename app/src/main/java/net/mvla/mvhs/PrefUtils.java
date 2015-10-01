package net.mvla.mvhs;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class PrefUtils {

    public static final String PREF_WELCOME_DONE = "pref_welcome_done";
    public static final String PREF_OPEN_SCHEDULE_SETUP = "pref_open_schedule_setup";
    public static final String PREF_CALENDAR_WELCOME = "pref_calendar_welcome";
    public static final String PREF_MODE = "pref_mode";


    public static final String PREF_SCHEDULE_PREFIX = "pref_schd_prd";
    public static final String PREF_SCHEDULE_ROOM = "_room";
    public static final String PREF_SCHEDULE_SBJCT = "_subjct";
    public static final String PREF_SCHEDULE_RALLY_B = "pref_schd_rally_b";

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).apply();
    }

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public static void markCalWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_CALENDAR_WELCOME, true).apply();
    }

    public static boolean isCalWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_CALENDAR_WELCOME, false);
    }

    public static int getMode(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sp.getString(PREF_MODE, "0"));
    }

    public static void setMode(final Context context, int mode) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_MODE, String.valueOf(mode)).apply();
    }
}

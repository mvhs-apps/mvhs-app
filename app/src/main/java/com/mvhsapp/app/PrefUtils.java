package com.mvhsapp.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class PrefUtils {

    public static final String PREF_WELCOME_DONE = "pref_welcome_done";
    public static final String PREF_OPEN_SCHEDULE_SETUP = "pref_open_schedule_setup";

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).apply();
    }

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }
}

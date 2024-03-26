package com;

import android.content.Context;
import android.content.SharedPreferences;

public class PinManager {
    private static final String PIN_PREFS_NAME = "PinPrefs";
    private static final String PIN_KEY = "PinKey";

    private static String pin = null;

    public static boolean isPinSet(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PIN_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.contains(PIN_KEY);
    }

    public static boolean checkPin(Context context, String enteredPin) {
        SharedPreferences prefs = context.getSharedPreferences(PIN_PREFS_NAME, Context.MODE_PRIVATE);
        String savedPin = prefs.getString(PIN_KEY, "");
        return savedPin.equals(enteredPin);
    }

    public static void setPin(Context context, String newPin) {
        SharedPreferences prefs = context.getSharedPreferences(PIN_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PIN_KEY, newPin);
        editor.apply();
    }
}

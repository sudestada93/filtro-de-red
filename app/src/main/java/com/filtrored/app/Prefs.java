package com.filtrored.app;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    private static final String KEY_ENABLED = "enabled";

    private Prefs() {
    }

    private static SharedPreferences get(Context context) {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    static boolean isEnabled(Context context) {
        return get(context).getBoolean(KEY_ENABLED, false);
    }

    static void setEnabled(Context context, boolean enabled) {
        get(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
}

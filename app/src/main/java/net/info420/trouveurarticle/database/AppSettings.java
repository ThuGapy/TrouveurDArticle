package net.info420.trouveurarticle.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings {
    private static final String PREF_REFRESH_TIME = "refresh_time";
    public static final int DefaultRefreshTime = 30;
    private static final String PREF_REFRESH_TIME_CELL_DATA = "refresh_time_cell_data";
    public static final int DefaultRefreshTimeCellData = 60;
    private static final String PREF_REFRESH_TIME_CELL_PLUGGING_IN = "refresh_time_cell_plugged_in";
    public static final int DefaultRefreshTimeCellPluggedIn = 15;
    private static final String PREF_DISABLE_REFRESH_CELL_DATA = "disable_refresh_cell_data";
    public static final boolean DefaultDisableRefreshCellData = true;
    private static final String PREF_DISABLE_REFRESH_BATTERY_LOW = "disable_refresh_battery_low";
    public static final boolean DefaultDisableRefreshBatteryLow = true;
    private static final String PREF_AUTOMATICALLY_REPLACE_PRODUCT_NAME = "automatically_replace_product_name";
    public static final boolean DefaultAutomaticallyReplaceProductName = true;
    public static final String PREF_OPTION_DENIED_AMOUNT = "time_dangerous_permission_denied";

    private SharedPreferences sharedPreferences;

    public AppSettings(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getRefreshTime() {
        return sharedPreferences.getInt(PREF_REFRESH_TIME, DefaultRefreshTime);
    }

    public void setRefreshTime(int time) {
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME, time).apply();
    }

    public int getRefreshTimeCellData() {
        return sharedPreferences.getInt(PREF_REFRESH_TIME_CELL_DATA, DefaultRefreshTimeCellData);
    }

    public void setRefreshTimeCellData(int time) {
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME_CELL_DATA, time).apply();
    }

    public int getRefreshTimeCellPluggedIn() {
        return sharedPreferences.getInt(PREF_REFRESH_TIME_CELL_PLUGGING_IN, DefaultRefreshTimeCellPluggedIn);
    }

    public void setRefreshTimeCellPluggedIn(int time) {
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME_CELL_PLUGGING_IN, time).apply();
    }

    public boolean getDisableRefreshCellData() {
        return sharedPreferences.getBoolean(PREF_DISABLE_REFRESH_CELL_DATA, DefaultDisableRefreshCellData);
    }

    public void setDisableRefreshCellData(boolean disabled) {
        sharedPreferences.edit().putBoolean(PREF_DISABLE_REFRESH_CELL_DATA, disabled).apply();
    }

    public boolean getDisableRefreshBatteryLow() {
        return sharedPreferences.getBoolean(PREF_DISABLE_REFRESH_BATTERY_LOW, DefaultDisableRefreshBatteryLow);
    }

    public void setDisableRefreshBatteryLow(boolean disabled) {
        sharedPreferences.edit().putBoolean(PREF_DISABLE_REFRESH_BATTERY_LOW, disabled).apply();
    }

    public boolean getAutomaticallyReplaceProductName() {
        return sharedPreferences.getBoolean(PREF_AUTOMATICALLY_REPLACE_PRODUCT_NAME, DefaultAutomaticallyReplaceProductName);
    }

    public void setAutomaticallyReplaceProductName(boolean enabled) {
        sharedPreferences.edit().putBoolean(PREF_AUTOMATICALLY_REPLACE_PRODUCT_NAME, enabled).apply();
    }

    public int getPermissionDeniedAmount() {
        return sharedPreferences.getInt(PREF_OPTION_DENIED_AMOUNT, 0);
    }

    public void addPermissionDeniedAmount() {
        sharedPreferences.edit().putInt(PREF_OPTION_DENIED_AMOUNT, sharedPreferences.getInt(PREF_OPTION_DENIED_AMOUNT, 0) + 1).apply();
    }

    public void ResetSettings() {
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME, DefaultRefreshTime).apply();
        sharedPreferences.edit().putInt(PREF_DISABLE_REFRESH_CELL_DATA, DefaultRefreshTimeCellData).apply();
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME_CELL_PLUGGING_IN, DefaultRefreshTimeCellPluggedIn).apply();
        sharedPreferences.edit().putBoolean(PREF_DISABLE_REFRESH_CELL_DATA, DefaultDisableRefreshCellData).apply();
        sharedPreferences.edit().putBoolean(PREF_DISABLE_REFRESH_BATTERY_LOW, DefaultDisableRefreshBatteryLow).apply();
        sharedPreferences.edit().putBoolean(PREF_AUTOMATICALLY_REPLACE_PRODUCT_NAME, DefaultAutomaticallyReplaceProductName);
    }
}

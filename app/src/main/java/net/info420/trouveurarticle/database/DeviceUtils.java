package net.info420.trouveurarticle.database;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

public class DeviceUtils {
    public static boolean IsDevicePluggedIn(Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);

        if(batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        }

        return false;
    }

    public static boolean IsDeviceUsingCellularData(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }

    public static boolean IsDeviceBatteryLow(Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPercentage = level * 100 / (float) scale;
            return batteryPercentage <= 20;
        }
        return false;
    }
}

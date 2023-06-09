package net.info420.trouveurarticle.database;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

// Classe d'utilitaire pour obtenir des informations sur l'appareil
public class DeviceUtils {
    // Fonction qui détermine si l'appareil est branché
    public static boolean IsDevicePluggedIn(Context context) {
        // Obtention du status de la batterie
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);

        if(batteryStatus != null) {
            // Si le status de la batterie est "en chargement" ou "plein", on considère que l'appareil est branché
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        }

        return false; // L'appareil n'est pas branché
    }

    // Fonction qui détermine si l'appareil utilise des données cellulaire
    public static boolean IsDeviceUsingCellularData(Context context) {
        // Obtention du manager de connectivité
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            // On vérifie que le status du réseau est "mobile", si l'appareil utilise des données cellulaires
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false; // L'appareil n'utilise pas de données cellulaires
    }

    // Fonction qui détermine si la batterie est faible
    public static boolean IsDeviceBatteryLow(Context context) {
        // Obtention du status de la batterie
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);

        if (batteryStatus != null) {
            // Si la batterie est en bas de 20%, la batterie est faible
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPercentage = level * 100 / (float) scale;
            return batteryPercentage <= 20;
        }
        return false; // La batterie n'est pas faible
    }
}

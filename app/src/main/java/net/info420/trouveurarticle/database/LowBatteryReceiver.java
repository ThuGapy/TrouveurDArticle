package net.info420.trouveurarticle.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.info420.trouveurarticle.Utils;

public class LowBatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            if(Utils.IsScrappingServiceRunning(context)) {
                Utils.SendNotification("Batterie faible!", "La batterie de votre appareil est faible, vous devriez brancher votre appareil pour obtenir les données les plus à jour possible.", context, new AppSettings(context));
            }
        }
    }
}

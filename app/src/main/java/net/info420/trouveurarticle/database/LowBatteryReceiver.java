package net.info420.trouveurarticle.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.info420.trouveurarticle.R;
import net.info420.trouveurarticle.Utils;

// Classe qui gère le broadcast receiver de batterie faible
public class LowBatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // On verifie qu'on vient de recevoir un message de batterie faible
        if(Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            if(Utils.IsScrappingServiceRunning(context)) { // On valide que le service de suivi est actif
                // On envoit une notification de batterie faible
                Utils.SendNotification(Utils.getResourceString(context, R.string.batterie_faible), Utils.getResourceString(context, R.string.la_batterie_de_votre_appareil_est_faible), context, new AppSettings(context));
            }
        }
    }
}

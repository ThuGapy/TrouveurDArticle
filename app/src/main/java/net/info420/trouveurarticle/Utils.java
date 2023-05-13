package net.info420.trouveurarticle;

import static net.info420.trouveurarticle.database.AppSettings.SETTINGS_PERMISSION;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.scrappers.ScrappingService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    // Fonction qui formate le prix (double en string)
    public static String FormatPrice(double price) {
        String priceTotal = String.valueOf((int)Math.floor(price));
        String priceDecimalText = String.valueOf(price);

        int dotIndex = priceDecimalText.indexOf(".");
        if (dotIndex == -1) {
            return "$" + price + ".00";
        } else {
            String decimal = priceDecimalText.substring(dotIndex + 1);
            if (decimal.length() == 0) {
                return "$" + priceTotal + ".00$";
            } else if (decimal.length() == 1) {
                return "$" + priceTotal + "." + decimal + "0";
            } else {
                return "$" + priceTotal + "." + decimal + "";
            }
        }
    }

    // Fonctui qui obtient un calendrier à une date spécifié à minuit
    public static Calendar GetStartOfDayCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    // Fonction qui arrête tous les services de suivi
    public static void StopAllRunningScrappingService(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName componentName = new ComponentName(context, ScrappingService.class);

        if (activityManager != null) {
            List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (service.service.equals(componentName)) {
                    Intent stopServiceIntent = new Intent(context, ScrappingService.class);
                    context.stopService(stopServiceIntent);

                    System.out.println("Service de suivi arrêté");
                }
            }
        }
    }

    // Fonction qui détermine si un service de suivi est en train de tourner
    public static boolean IsScrappingServiceRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null) {
            for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if(ScrappingService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }

        return false;
    }

    // Fonction qui ouvre le menu de paramètres si l'utilisateur a la permission
    public static void OpenSettings(Activity activity, Context context, AppSettings preferences, Intent intent) {
        if (ContextCompat.checkSelfPermission(context, "net.info420.trouveurarticle.permissions.OPTION_PERMISSION") != PackageManager.PERMISSION_GRANTED) {
            if (preferences.getPermissionDeniedAmount() > 0) {
                Utils.ShowPermissionReason(activity, context);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{"net.info420.trouveurarticle.permissions.OPTION_PERMISSION"}, SETTINGS_PERMISSION);
            }
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    // Fonction qui montre la raison du besoin de permission à l'utilisateur
    public static void ShowPermissionReason(Activity activity, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(Utils.getResourceString(context, R.string.cette_application_besoin_permission))
                .setPositiveButton(Utils.getResourceString(context, R.string.ouvrir_les_parametres), (dialog, which) -> OpenAppSettings(activity, context))
                .setNegativeButton(Utils.getResourceString(context, R.string.annuler), (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // Fonction qui ouvre les paramètres de l'applicatio au niveau d'android
    public static void OpenAppSettings(Activity activity, Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        ComponentActivity componentActivity = (ComponentActivity) activity;
        componentActivity.startActivityForResult(intent, SETTINGS_PERMISSION);
    }

    // Fonction qui valide si les notifications sont activés
    public static boolean AreNotificationsEnabled(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    // Foncton qui envoi une notification
    public static void SendNotification(String title, String content, Context context, AppSettings preferences) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crée le canal de notification si il n'existe pas déjà
        String channelId = "scrape_result_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = Utils.getResourceString(context, R.string.canal_trouveur_article);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Envoie la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(preferences.createNewNotification(), builder.build());
    }

    // Méthode pour obtenir une resource string (traduction) dans le fichier strings.xml
    public static String getResourceString(Context context, int stringID) {
        return context.getResources().getString(stringID);
    }
}

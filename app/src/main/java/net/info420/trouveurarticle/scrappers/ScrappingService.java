package net.info420.trouveurarticle.scrappers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import net.info420.trouveurarticle.R;
import net.info420.trouveurarticle.Utils;
import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.database.CursorWrapper;
import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.database.DeviceUtils;
import net.info420.trouveurarticle.database.LowBatteryReceiver;

// Classe qui gère le service d'avant plan de l'application
public class ScrappingService extends Service {
    // Déclaration des données membres
    private DatabaseHelper dbHelper;
    private AppSettings preferences;
    private Handler serviceHandler;
    private Runnable serviceRunnable;
    private boolean shouldFetchData = false;
    private int currentInterval;
    private LowBatteryReceiver receiver;

    // Initialisation des données membres et du service d'avant plan
    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(getApplicationContext());
        preferences = new AppSettings(getApplicationContext());
        serviceHandler = new Handler();

        CreateNotificationChannel(getApplicationContext());
        startForeground(1, CreateNotification(getApplicationContext()));

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
        receiver = new LowBatteryReceiver();
        registerReceiver(receiver, filter);
    }

    // Lorsque le service commence, on valide que celui-ci devrait être executer, sinon on commence le service "d'attente"
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GetShouldBeInterval();
        CancelCurrentRunnable();

        if(shouldFetchData) {
            ScheduleService();
        } else {
            SchedulePendingService();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // On supprime le receiver lorsque le service est supprimé
    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    private void ScheduleService() {
        // Création d'un runnable pour le service de suivi
        serviceRunnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Service de suivi en cours");
                int previousInterval = currentInterval;
                GetShouldBeInterval();

                CursorWrapper produitCursor = dbHelper.getAllItems();
                if(produitCursor != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(produitCursor.cursor.moveToNext()) {
                                // On obtient les informations du produit
                                String nomProduit = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("nomArticle"));
                                String amazonLink = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("amazon"));
                                String neweggLink = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("newegg"));
                                String canadaComputersLink = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("canadacomputers"));
                                String memoryExpressLink = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("memoryexpress"));

                                // On lance un nouveau fil (thread) pour amazon si le produit en a besoin
                                if(amazonLink != null && !amazonLink.equals("")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            AmazonScrapper amazonScrapper = new AmazonScrapper();;
                                            ScrapperResult amazonResult = amazonScrapper.Fetch(amazonLink);
                                            if(amazonResult != null) {
                                                dbHelper.createScrapeResult(amazonLink, amazonResult, nomProduit, StoreFront.Amazon, getApplicationContext(), preferences);
                                            }
                                        }
                                    }).start();
                                }

                                // On lance un nouveau fil (thread) pour newegg si le produit en a besoin
                                if(neweggLink != null && !neweggLink.equals("")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            NeweggScrapper neweggScrapper = new NeweggScrapper();;
                                            ScrapperResult neweggResult = neweggScrapper.Fetch(neweggLink);
                                            if(neweggResult != null) {
                                                dbHelper.createScrapeResult(neweggLink, neweggResult, nomProduit, StoreFront.Newegg, getApplicationContext(), preferences);
                                            }
                                        }
                                    }).start();
                                }

                                // On lance un nouveau fil (thread) pour canadacomputers si le produit en a besoin
                                if(canadaComputersLink != null && !canadaComputersLink.equals("")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            CanadaComputersScrapper canadaComputersScrapper = new CanadaComputersScrapper();;
                                            ScrapperResult canadaComputersResult = canadaComputersScrapper.Fetch(canadaComputersLink);
                                            if(canadaComputersResult != null) {
                                                dbHelper.createScrapeResult(canadaComputersLink, canadaComputersResult, nomProduit, StoreFront.CanadaComputers, getApplicationContext(), preferences);
                                            }
                                        }
                                    }).start();
                                }

                                // On lance un nouveau fil (thread) pour memoryexpress si le produit en a besoin
                                if(memoryExpressLink != null && !memoryExpressLink.equals("")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            MemoryExpressScrapper memoryExpressScrapper = new MemoryExpressScrapper();;
                                            ScrapperResult memoryExpressResult = memoryExpressScrapper.Fetch(memoryExpressLink);
                                            if(memoryExpressResult != null) {
                                                dbHelper.createScrapeResult(memoryExpressLink, memoryExpressResult, nomProduit, StoreFront.MemoryExpress, getApplicationContext(), preferences);
                                            }
                                        }
                                    }).start();
                                }

                                try {
                                    // On attend 2.5sec entre chaque article pour éviter de se faire détecter comme un robot
                                    Thread.sleep(2500);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }).start();
                }

                // Si on doit arreter de suivre les articles, on lance le service d'attente, sinon on continue
                if (!shouldFetchData) {
                    CancelCurrentRunnable();
                    SchedulePendingService();
                } else {
                    if (previousInterval != currentInterval) {
                        CancelCurrentRunnable();
                        ScheduleService();
                    } else {
                        serviceHandler.postDelayed(serviceRunnable, currentInterval * 1000);
                    }
                }
            }
        };

        // On lance le runnable
        serviceHandler.post(serviceRunnable);
    }

    // Méthode qui correspond au service d'attente, regarde à chaque 10 secondes si le service de suivi devrait être relancé
    private void SchedulePendingService() {
        serviceRunnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Service d'attente en cours");
                GetShouldBeInterval();

                if(shouldFetchData) {
                    CancelCurrentRunnable();
                    ScheduleService();
                }
                serviceHandler.postDelayed(this, 10000);
            }
        };
        serviceHandler.post(serviceRunnable);
    }

    private void CancelCurrentRunnable() {
        serviceHandler.removeCallbacks(serviceRunnable);
    }

    // Méthode qui calcul l'interval que le service devrait être executer
    private void GetShouldBeInterval() {
        int interval;
        boolean batteryLow = DeviceUtils.IsDeviceBatteryLow(getApplicationContext());
        boolean batteryCharging = DeviceUtils.IsDevicePluggedIn(getApplicationContext());
        boolean usingCellularData = DeviceUtils.IsDeviceUsingCellularData(getApplicationContext());

        if(!usingCellularData && !batteryLow) {
            if(batteryCharging) {
                interval = preferences.getRefreshTimeCellPluggedIn();
            } else {
                interval = preferences.getRefreshTime();
            }
        } else if(usingCellularData) {
            if(preferences.getDisableRefreshCellData()) {
                interval = 0;
            } else {
                interval = preferences.getRefreshTimeCellData();
            }
        } else if(batteryLow) {
            if(preferences.getDisableRefreshBatteryLow()) {
                interval = 0;
            } else {
                interval = preferences.getRefreshTime();
            }
        } else {
            interval = preferences.getRefreshTime();
        }

        currentInterval = interval;
        shouldFetchData = interval != 0;
    }

    // Méthode qui crée le canal de notification pour le service
    private void CreateNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Utils.getResourceString(context, R.string.service_de_suivi);
            String description = Utils.getResourceString(context, R.string.service_foreground_suivi);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("scrapping_foreground_channel", name, importance);
            channel.setDescription(description);
            channel.enableVibration(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Méthode qui crée la notification d'execution
    private Notification CreateNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "scrapping_foreground_channel")
                .setContentTitle(Utils.getResourceString(context, R.string.service_de_suivi))
                .setContentText(Utils.getResourceString(context, R.string.suivi_de_vos_produits))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder.build();
    }
}

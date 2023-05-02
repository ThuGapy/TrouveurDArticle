package net.info420.trouveurarticle.scrappers;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.database.CursorWrapper;
import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.database.DeviceUtils;

public class ScrappingService extends Service {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private AppSettings preferences;
    private Handler serviceHandler;
    private Runnable serviceRunnable;
    private boolean shouldFetchData = false;
    private int currentInterval;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(getApplicationContext());
        preferences = new AppSettings(getApplicationContext());
        serviceHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GetShouldBeInterval();
        CancelCurrentRunnable();

        if(shouldFetchData) {
            ScheduleService(currentInterval);
        } else {
            SchedulePendingService();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void ScheduleService(int interval) {
        serviceRunnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Running scrapping service");
                int previousInterval = currentInterval;
                GetShouldBeInterval();

                CursorWrapper produitCursor = dbHelper.getAllItems();
                if(produitCursor != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(produitCursor.cursor.moveToNext()) {
                                String nomProduit = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("nomArticle"));
                                String amazonLink = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("amazon"));
                                String neweggLink = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("newegg"));
                                String canadaComputersLink = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("canadacomputers"));
                                String memoryExpressLink = produitCursor.cursor.getString(produitCursor.cursor.getColumnIndexOrThrow("memoryexpress"));

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
                                    Thread.sleep(2500);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            produitCursor.Close();
                        }
                    }).start();
                }
                if (!shouldFetchData) {
                    CancelCurrentRunnable();
                    SchedulePendingService();
                } else {
                    if (previousInterval != currentInterval) {
                        CancelCurrentRunnable();
                        ScheduleService(currentInterval);
                    } else {
                        serviceHandler.postDelayed(serviceRunnable, currentInterval * 1000);
                    }
                }
            }
        };

        serviceHandler.post(serviceRunnable);
    }

    private void SchedulePendingService() {
        serviceRunnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Pending service running");
                GetShouldBeInterval();

                if(shouldFetchData) {
                    CancelCurrentRunnable();
                    ScheduleService(currentInterval);
                }
                serviceHandler.postDelayed(this, 10000);
            }
        };
        serviceHandler.post(serviceRunnable);
    }

    private void CancelCurrentRunnable() {
        serviceHandler.removeCallbacks(serviceRunnable);
    }

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
}

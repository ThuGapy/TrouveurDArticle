package net.info420.trouveurarticle.scrappers;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import net.info420.trouveurarticle.database.DatabaseHelper;

public class ScrappingService extends Service {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = dbHelper.getWritableDatabase();
        // Execution du service
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

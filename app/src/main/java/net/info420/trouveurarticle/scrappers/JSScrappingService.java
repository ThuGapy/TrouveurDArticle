package net.info420.trouveurarticle.scrappers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import net.info420.trouveurarticle.MainActivity;
import net.info420.trouveurarticle.R;

import org.jsoup.nodes.Document;

public class JSScrappingService extends Service {
    private static final int NOTIFICATION = 1;
    public static final String CHANNEL = "JSScrappingServiceChannel";

    private final IBinder binder = new ServiceBinder();

    public class ServiceBinder extends Binder {
        JSScrappingService getService() {
            return JSScrappingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Document GetDocument(String link) {
        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        return null;
    }

    private Notification createServiceNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setContentTitle("JavaScript Scrapping Service");
        builder.setContentText("Scrapping JavaScript en cours");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(createIntent());

        return builder.build();
    }

    private PendingIntent createIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}

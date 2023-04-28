package net.info420.trouveurarticle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.info420.trouveurarticle.scrappers.JSScrappingService;
import net.info420.trouveurarticle.scrappers.ScrapperResult;
import net.info420.trouveurarticle.scrappers.WalmartScrapper;

public class MainActivity extends AppCompatActivity {

    private static final int INTERNET_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createServiceNotificationChannel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("Trouveur d'article");
        toolbar.setSubtitle("Trouvez les articles en demande!");
        toolbar.setTitleTextColor(Color.WHITE);

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings(view);
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainView())
                .commit();

        BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                if(item.getItemId() == R.id.homeFragment) {
                    fragment = new MainView();
                    System.out.println("mainview fragment");
                } else if(item.getItemId() == R.id.addProductFragment) {
                    fragment = new AddProductView();
                    System.out.println("addproduct fragment");
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                System.out.println("changing fragment");
                return true;
            }
        });

        ValiderPermissions();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //AmazonScrapper scrapper = new AmazonScrapper();
                //ScrapperResult result = scrapper.Fetch("https://www.amazon.ca/Samsung-980-PRO-SSD-technologie/dp/B08RK2SR23");
                //ScrapperResult result = scrapper.Fetch("https://www.amazon.ca/Mario-Rabbids-Sparks-Hope-Bilingual/dp/B0977RZ19R/ref=sr_1_1_sspa?crid=E35R153WV3JQ&keywords=nintendo+switch+games&qid=1682081257&sprefix=nintendo+swit%2Caps%2C102&sr=8-1-spons&psc=1&spLa=ZW5jcnlwdGVkUXVhbGlmaWVyPUEzNk5BWUE3VDAxT0VZJmVuY3J5cHRlZElkPUEwMDA0ODY5MjI1RVMyRDJXRk1GSCZlbmNyeXB0ZWRBZElkPUEwODAyMTU1MU9PUkdGQzNIVVZBQyZ3aWRnZXROYW1lPXNwX2F0ZiZhY3Rpb249Y2xpY2tSZWRpcmVjdCZkb05vdExvZ0NsaWNrPXRydWU=");
                WalmartScrapper scrapper = new WalmartScrapper();
                ScrapperResult result = scrapper.Fetch("https://www.staples.ca/products/2956572-en-nintendo-switch-lite-hardware-grey");
                //ScrapperResult result = scrapper.Fetch("https://www.walmart.ca/en/ip/super-mario-3d-world-bowsers-fury-nintendo-switch/6000202191126", getApplicationContext());
                /* scrapper = new NeweggScrapper();
                //ScrapperResult result = scrapper.Fetch("https://www.newegg.ca/msi-geforce-rtx-4090-rtx-4090-gaming-x-trio-24g/p/N82E16814137761?Description=rtx%204090&cm_re=rtx_4090-_-14-137-761-_-Product");
                ScrapperResult result = scrapper.Fetch("https://www.newegg.ca/intel-core-i9-13900k-core-i9-13th-gen/p/N82E16819118412?Description=13900k&cm_re=13900k-_-19-118-412-_-Product");*/

                /*BestBuyScrapper scrapper = new BestBuyScrapper();
                ScrapperResult result = scrapper.Fetch("https://www.bestbuy.ca/en-ca/product/nintendo-switch-oled-model-console-white/15598575?icmp=Recos_3across_tp_sllng_prdcts&referrer=PLP_Reco", getApplicationContext());*/

                /*if(result != null)
                    System.out.println(result.GetStringifiedResult());*/
            }
        });//.start();
    }

    private void ValiderPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        }
    }

    private void createServiceNotificationChannel() {
        Notification.Builder builder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "Canal pour le service de scrapping JavaScript";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(JSScrappingService.CHANNEL, JSScrappingService.CHANNEL, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void Settings(View view) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
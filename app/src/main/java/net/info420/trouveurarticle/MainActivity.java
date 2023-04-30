package net.info420.trouveurarticle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.info420.trouveurarticle.scrappers.AmazonScrapper;
import net.info420.trouveurarticle.scrappers.CanadaComputersScrapper;
import net.info420.trouveurarticle.scrappers.MemoryExpressScrapper;
import net.info420.trouveurarticle.scrappers.NeweggScrapper;
import net.info420.trouveurarticle.scrappers.ScrapperResult;

public class MainActivity extends AppCompatActivity {

    private static final int INTERNET_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                .replace(R.id.fragment_container, new FollowedProductsView())
                .commit();

        BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                if(item.getItemId() == R.id.homeFragment) {
                    fragment = new FollowedProductsView();
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
                /*AmazonScrapper scrapper = new AmazonScrapper();
                String result = scrapper.FetchProductName("https://www.amazon.ca/Samsung-980-PRO-SSD-technologie/dp/B08RK2SR23");*/
                /*NeweggScrapper scrapper = new NeweggScrapper();
                String result = scrapper.FetchProductName("https://www.newegg.ca/msi-geforce-rtx-4090-rtx-4090-gaming-x-trio-24g/p/N82E16814137761?Description=rtx%204090&cm_re=rtx_4090-_-14-137-761-_-Product");*/
                /*CanadaComputersScrapper scrapper = new CanadaComputersScrapper();
                String result = scrapper.FetchProductName("https://www.canadacomputers.com/product_info.php?cPath=43_557_559&item_id=226645");*/
                MemoryExpressScrapper scrapper = new MemoryExpressScrapper();
                String result = scrapper.FetchProductName("https://www.memoryexpress.com/Products/MX00122931");
                System.out.println(result);
            }
        }).start();
    }

    private void ValiderPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        }
    }

    private void Settings(View view) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
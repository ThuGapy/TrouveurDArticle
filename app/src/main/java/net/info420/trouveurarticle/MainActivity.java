package net.info420.trouveurarticle;

import static net.info420.trouveurarticle.database.AppSettings.INTERNET_PERMISSION;
import static net.info420.trouveurarticle.database.AppSettings.SETTINGS_PERMISSION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.scrappers.ScrappingService;
import net.info420.trouveurarticle.views.OnProductInteractionListener;

public class MainActivity extends AppCompatActivity implements OnProductInteractionListener {
    private AppSettings preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = new AppSettings(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Trouveur d'articles");
        TextView toolbarSubtitle = toolbar.findViewById(R.id.toolbar_subtitle);
        toolbarSubtitle.setText("Trouvez les articles en demande!");

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                getMenuInflater().inflate(R.menu.options_menu, popupMenu.getMenu());

                MenuItem startScrappingItem = popupMenu.getMenu().findItem(R.id.action_start_scrapping);
                MenuItem stopScrappingItem = popupMenu.getMenu().findItem(R.id.action_stop_scrapping);

                if(Utils.IsScrappingServiceRunning(getApplicationContext())) {
                    startScrappingItem.setEnabled(false);
                } else {
                    stopScrappingItem.setEnabled(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemID = item.getItemId();
                        if(itemID == R.id.action_options) {
                            Settings(view);
                            return true;
                        } else if(itemID == R.id.action_start_scrapping) {
                            Intent startScrappingService = new Intent(getApplicationContext(), ScrappingService.class);
                            startService(startScrappingService);
                            return true;
                        } else if(itemID == R.id.action_stop_scrapping) {
                            Utils.StopAllRunningScrappingService(getApplicationContext());
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
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
                } else if(item.getItemId() == R.id.addProductFragment) {
                    fragment = new AddProductView();
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                System.out.println("changing fragment");
                return true;
            }
        });

        ValiderPermissions();
    }

    private void ValiderPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        }
    }

    private void Settings(View view) {
        Intent intent = new Intent(this, Settings.class);
        Utils.OpenSettings(getApplicationContext(), preferences, intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SETTINGS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Settings(null);
            } else {
                preferences.addPermissionDeniedAmount();
                Toast.makeText(this, "Impossible d'aller dans les options. Vous devez donner la permission à l'application", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void TriggerEdit(int editID) {
        Fragment fragment = new AddProductView(editID);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void EditDone() {
        Fragment fragment = new FollowedProductsView();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void OpenLink(String link) {
        if(link.equals("")) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }

    @Override
    public void SeeChart(int ID) {
        Intent intent = new Intent(this, ChartData.class);
        intent.putExtra("productID", ID);
        startActivity(intent);
    }
}
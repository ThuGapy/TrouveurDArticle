package net.info420.trouveurarticle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.scrappers.AmazonScrapper;
import net.info420.trouveurarticle.scrappers.CanadaComputersScrapper;
import net.info420.trouveurarticle.scrappers.MemoryExpressScrapper;
import net.info420.trouveurarticle.scrappers.NeweggScrapper;
import net.info420.trouveurarticle.scrappers.ScrapperResult;
import net.info420.trouveurarticle.scrappers.ScrappingService;
import net.info420.trouveurarticle.views.OnTriggerEditListener;

public class MainActivity extends AppCompatActivity implements OnTriggerEditListener {

    private static final int INTERNET_PERMISSION = 1;
    private static final int SETTINGS_PERMISSION = 2;
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

        if(!IsScrappingServiceRunning()) {
            Intent scrappingService = new Intent(this, ScrappingService.class);
            startService(scrappingService);
        }
    }

    private void ValiderPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        }
    }

    private void Settings(View view) {
        if (ContextCompat.checkSelfPermission(this, "net.info420.trouveurarticle.permissions.OPTION_PERMISSION") != PackageManager.PERMISSION_GRANTED) {
            System.out.println(preferences.getPermissionDeniedAmount());
            if (preferences.getPermissionDeniedAmount() > 0) {
                System.out.println("2");
                showPermissionReason();
            } else {
                System.out.println("3");
                ActivityCompat.requestPermissions(this, new String[]{"net.info420.trouveurarticle.permissions.OPTION_PERMISSION"}, SETTINGS_PERMISSION);
            }
        } else {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }
    }

    private void showPermissionReason() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Cette application a besoin d'une permission pour ouvrir la page d'options. Allez dans les paramètres de l'application pour l'activer.")
                .setPositiveButton("Ouvrir les paramètres", (dialog, which) -> openAppSettings())
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTINGS_PERMISSION);
    }

    private boolean IsScrappingServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null) {
            for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if(ScrappingService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }

        return false;
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
}
package net.info420.trouveurarticle;

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

// Classe qui gère le menu principal de l'application
public class MainActivity extends AppCompatActivity implements OnProductInteractionListener {
    private AppSettings preferences;

    // Lorsque le menu est créé
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = new AppSettings(getApplicationContext());

        // On ajoute la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        UpdateToolbarText();

        // Initialisation des écouteurs de click du menu d'options
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

        // On met le fragment des produits suivi par défaut
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FollowedProductsView())
                .commit();

        // Gestion du changement de fragment
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

                return true;
            }
        });
    }

    // Fonction qui met à jour la toolbar selon le language
    private void UpdateToolbarText() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(Utils.getResourceString(getApplicationContext(), R.string.app_name));
        TextView toolbarSubtitle = toolbar.findViewById(R.id.toolbar_subtitle);
        toolbarSubtitle.setText(Utils.getResourceString(getApplicationContext(), R.string.trouvez_les_articles_en_demande));
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateToolbarText();
    }

    // Ouvre les paramètres de l'application
    private void Settings(View view) {
        Intent intent = new Intent(this, Settings.class);
        Utils.OpenSettings(this, getApplicationContext(), preferences, intent);
    }

    // Validation des permissions du menu option
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SETTINGS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Settings(null);
            } else {
                preferences.addPermissionDeniedAmount();
                Toast.makeText(this, Utils.getResourceString(getApplicationContext(), R.string.impossible_aller_dans_les_options), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Lancement de la modification d'un produit
    @Override
    public void TriggerEdit(int editID) {
        Fragment fragment = new AddProductView(editID);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Finition de la modification d'un produit
    @Override
    public void EditDone() {
        Fragment fragment = new FollowedProductsView();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Ouverture d'un lien
    @Override
    public void OpenLink(String link) {
        if(link.equals("")) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }

    // Montrer les données d'un produit
    @Override
    public void SeeChart(int ID) {
        Intent intent = new Intent(this, ChartData.class);
        intent.putExtra("productID", ID);
        startActivity(intent);
    }
}
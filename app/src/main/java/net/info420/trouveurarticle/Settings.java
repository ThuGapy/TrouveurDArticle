package net.info420.trouveurarticle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.database.DatabaseHelper;

// Classe qui gère le menu Paramètres de l'application
public class Settings extends AppCompatActivity {
    private AppSettings settings;
    private boolean FromProduct;
    private int ID;

    // Lorsque le menu est créé
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new AppSettings(getApplicationContext());

        // On obtient la provence du lancement du monu
        Intent fromIntent = getIntent();
        FromProduct = fromIntent.getBooleanExtra("fromProduct", false);
        ID = fromIntent.getIntExtra("productID", 0);

        // On met la toolbar et on met à jour son texte avec la bonne traduction
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        UpdateToolbarText();

        // On initialise le bouton réinitialiser
        ImageButton resetSettingsButton = toolbar.findViewById(R.id.reset_settings_button);
        resetSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset(v);
            }
        });

        UpdateNotificationButtonVisibility();
        UpdateEntryData();
    }

    private void UpdateToolbarText() {
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(Utils.getResourceString(getApplicationContext(), R.string.options));
        TextView toolbarSubtitle = toolbar.findViewById(R.id.toolbar_subtitle);
        toolbarSubtitle.setText(Utils.getResourceString(getApplicationContext(), R.string.changer_les_parametres_de_lapplication));
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateNotificationButtonVisibility();
        UpdateToolbarText();
    }

    // On met à jour la visibilité du bouton de demande de permission de notification en fonction de si les permissions sont activés
    private void UpdateNotificationButtonVisibility() {
        if(Utils.AreNotificationsEnabled(this)) {
            Button notificationPermission = findViewById(R.id.give_notifications_permission);
            notificationPermission.setVisibility(View.GONE);
        }
    }

    // On ouvre le menu pour donner les permissions de notifications dans le menu android
    public void GiveNotificationsPermission(View view) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + getPackageName()));
        }
        startActivity(intent);
    }

    // Méthode qui supprime les données de moisonnage de données
    public void DeleteScrapeResults(View view) {
        // Message de confirmation de suppression
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Utils.getResourceString(getApplicationContext(), R.string.etes_vous_certain_de_vouloir_supprimer_le_produit));
        builder.setTitle(Utils.getResourceString(getApplicationContext(), R.string.confirmation));
        builder.setPositiveButton(Utils.getResourceString(getApplicationContext(), R.string.confirmer), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                dbHelper.deleteAllScrapeResults();
                Toast.makeText(getApplicationContext(), Utils.getResourceString(getApplicationContext(), R.string.donnees_des_pages_de_produits_supprimes), Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton(Utils.getResourceString(getApplicationContext(), R.string.annuler), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // On renvoie l'utilisateur d'où il arrive
    public void Retour(View view) {
        Intent intent;
        if(FromProduct) {
            intent = new Intent(getApplicationContext(), ChartData.class);
            intent.putExtra("productID", ID);
        } else {
            intent = new Intent(getApplicationContext(), MainActivity.class);
        }

        startActivity(intent);
    }

    // Méthode qui réinitialise les paramètres de l'application
    public void Reset(View view) {
        // Confirmation avant de réinitialiser les paramètres
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Utils.getResourceString(getApplicationContext(), R.string.etes_vous_certain_reinitialiser_parametres_application));
        builder.setTitle(Utils.getResourceString(getApplicationContext(), R.string.confirmation));
        builder.setPositiveButton(Utils.getResourceString(getApplicationContext(), R.string.confirmer), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settings.ResetSettings();
                UpdateEntryData();
            }
        });

        builder.setNegativeButton(Utils.getResourceString(getApplicationContext(), R.string.annuler), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Méthode qui sauvegarde les paramètres
    public void Sauvegarder(View view) {
        EditText refreshTimeEditText = findViewById(R.id.refresh_time);
        EditText refreshTimeCellDataEditText = findViewById(R.id.refresh_time_cell_data);
        EditText refreshTimeCellPluggedInEditText = findViewById(R.id.refresh_time_cell_plugged_in);
        CheckBox disableRefreshCellData = findViewById(R.id.disable_cell_data_refresh);
        CheckBox disableRefreshBatteryLow = findViewById(R.id.disable_refresh_battery_low);
        CheckBox enableAutomaticProductName = findViewById(R.id.automatically_replace_product_name);
        CheckBox enableAutomaticHomePageRefresh = findViewById(R.id.automatically_refresh_home_screen);

        settings.setRefreshTime(Integer.parseInt(String.valueOf(refreshTimeEditText.getText())));
        settings.setRefreshTimeCellData(Integer.parseInt(String.valueOf(refreshTimeCellDataEditText.getText())));
        settings.setRefreshTimeCellPluggedIn(Integer.parseInt(String.valueOf(refreshTimeCellPluggedInEditText.getText())));
        settings.setDisableRefreshCellData(disableRefreshCellData.isChecked());
        settings.setDisableRefreshBatteryLow(disableRefreshBatteryLow.isChecked());
        settings.setAutomaticallyReplaceProductName(enableAutomaticProductName.isChecked());
        settings.setAutomaticallyRefreshData(enableAutomaticHomePageRefresh.isChecked());

        Toast.makeText(this, Utils.getResourceString(getApplicationContext(), R.string.parametres_sauvegardes), Toast.LENGTH_LONG).show();
    }

    // Méthode qui met à jour les edittext avec les données actuels des paramètres
    public void UpdateEntryData() {
        EditText refreshTimeEditText = findViewById(R.id.refresh_time);
        EditText refreshTimeCellDataEditText = findViewById(R.id.refresh_time_cell_data);
        EditText refreshTimeCellPluggedInEditText = findViewById(R.id.refresh_time_cell_plugged_in);
        CheckBox disableRefreshCellData = findViewById(R.id.disable_cell_data_refresh);
        CheckBox disableRefreshBatteryLow = findViewById(R.id.disable_refresh_battery_low);
        CheckBox enableAutomaticProductName = findViewById(R.id.automatically_replace_product_name);
        CheckBox enableAutomaticHomePageRefresh = findViewById(R.id.automatically_refresh_home_screen);

        refreshTimeEditText.setText(String.valueOf(settings.getRefreshTime()));
        refreshTimeCellDataEditText.setText(String.valueOf(settings.getRefreshTimeCellData()));
        refreshTimeCellPluggedInEditText.setText(String.valueOf(settings.getRefreshTimeCellPluggedIn()));
        disableRefreshCellData.setChecked(settings.getDisableRefreshCellData());
        disableRefreshBatteryLow.setChecked(settings.getDisableRefreshBatteryLow());
        enableAutomaticProductName.setChecked(settings.getAutomaticallyReplaceProductName());
        enableAutomaticHomePageRefresh.setChecked(settings.getAutomaticallyRefreshData());
    }
}
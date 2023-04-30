package net.info420.trouveurarticle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.info420.trouveurarticle.database.AppSettings;

public class Settings extends AppCompatActivity {
    private AppSettings settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new AppSettings(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Options");
        TextView toolbarSubtitle = toolbar.findViewById(R.id.toolbar_subtitle);
        toolbarSubtitle.setText("Changez les paramètres de l'application!");

        ImageButton resetSettingsButton = toolbar.findViewById(R.id.reset_settings_button);
        resetSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset(v);
            }
        });

        UpdateEntryData();
    }

    public void Retour(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void Reset(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Êtes vous certain de vouloir réinitialiser les paramètres de l'application?");
        builder.setTitle("Confirmation");
        builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settings.ResetSettings();
                UpdateEntryData();
            }
        });

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void Sauvegarder(View view) {
        EditText refreshTimeEditText = findViewById(R.id.refresh_time);
        EditText refreshTimeCellDataEditText = findViewById(R.id.refresh_time_cell_data);
        EditText refreshTimeCellPluggedInEditText = findViewById(R.id.refresh_time_cell_plugged_in);
        CheckBox disableRefreshCellData = findViewById(R.id.disable_cell_data_refresh);
        CheckBox disableRefreshBatteryLow = findViewById(R.id.disable_refresh_battery_low);
        CheckBox enableAutomaticProductName = findViewById(R.id.automatically_replace_product_name);

        settings.setRefreshTime(Integer.parseInt(String.valueOf(refreshTimeEditText.getText())));
        settings.setRefreshTimeCellData(Integer.parseInt(String.valueOf(refreshTimeCellDataEditText.getText())));
        settings.setRefreshTimeCellPluggedIn(Integer.parseInt(String.valueOf(refreshTimeCellPluggedInEditText.getText())));
        settings.setDisableRefreshCellData(disableRefreshCellData.isChecked());
        settings.setDisableRefreshBatteryLow(disableRefreshBatteryLow.isChecked());
        settings.setAutomaticallyReplaceProductName(enableAutomaticProductName.isChecked());

        Toast.makeText(this, "Les paramètres sont sauvegardés", Toast.LENGTH_LONG).show();
    }

    public void UpdateEntryData() {
        EditText refreshTimeEditText = findViewById(R.id.refresh_time);
        EditText refreshTimeCellDataEditText = findViewById(R.id.refresh_time_cell_data);
        EditText refreshTimeCellPluggedInEditText = findViewById(R.id.refresh_time_cell_plugged_in);
        CheckBox disableRefreshCellData = findViewById(R.id.disable_cell_data_refresh);
        CheckBox disableRefreshBatteryLow = findViewById(R.id.disable_refresh_battery_low);
        CheckBox enableAutomaticProductName = findViewById(R.id.automatically_replace_product_name);

        refreshTimeEditText.setText(String.valueOf(settings.getRefreshTime()));
        refreshTimeCellDataEditText.setText(String.valueOf(settings.getRefreshTimeCellData()));
        refreshTimeCellPluggedInEditText.setText(String.valueOf(settings.getRefreshTimeCellPluggedIn()));
        disableRefreshCellData.setChecked(settings.getDisableRefreshCellData());
        disableRefreshBatteryLow.setChecked(settings.getDisableRefreshBatteryLow());
        enableAutomaticProductName.setChecked(settings.getAutomaticallyReplaceProductName());
    }
}
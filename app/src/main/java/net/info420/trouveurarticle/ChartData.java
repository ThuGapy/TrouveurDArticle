package net.info420.trouveurarticle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.database.FollowedItemAdapter;
import net.info420.trouveurarticle.database.LinkStatus;
import net.info420.trouveurarticle.database.PriceHistoryAdapter;
import net.info420.trouveurarticle.scrappers.ScrappingService;
import net.info420.trouveurarticle.views.OnProductInteractionListener;
import net.info420.trouveurarticle.views.OnRefreshRequestedListener;
import net.info420.trouveurarticle.views.graphs.Axis;
import net.info420.trouveurarticle.views.graphs.DayOfWeekFormatter;
import net.info420.trouveurarticle.views.graphs.DollarFormatter;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;

// Classe qui gère l'activité des données d'un produit
public class ChartData extends AppCompatActivity implements OnProductInteractionListener {

    private DatabaseHelper dbHelper;
    private PriceHistoryAdapter adapter;
    private int ID;
    private AppSettings preferences;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_data);

        // Obtention du produit à voir
        Intent intent = getIntent();
        ID = intent.getIntExtra("productID", 0);

        preferences = new AppSettings(this);
        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setVisibility(View.VISIBLE);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton settingsButton = findViewById(R.id.settings_button);

        // Initialisation du menu d'options
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(ChartData.this, view);
                getMenuInflater().inflate(R.menu.options_menu, popupMenu.getMenu());

                MenuItem startScrappingItem = popupMenu.getMenu().findItem(R.id.action_start_scrapping);
                MenuItem stopScrappingItem = popupMenu.getMenu().findItem(R.id.action_stop_scrapping);
                MenuItem refreshGraph = popupMenu.getMenu().findItem(R.id.action_refresh_graph);

                refreshGraph.setVisible(true);

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
                        } else if(itemID == R.id.action_refresh_graph) {
                            Refresh();
                            RefreshChart();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        LinearLayout textLayout = findViewById(R.id.text_layout);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) textLayout.getLayoutParams();
        layoutParams.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));

        // Initialisation de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        UpdateToolbarText();

        TextView productNameTitle = findViewById(R.id.product_name_title);

        dbHelper = new DatabaseHelper(getApplicationContext());
        RefreshChart();

        // Obtention des données si l'ID n'est pas 0
        if(ID != 0) {
            String productName = dbHelper.getProductName(ID);
            productNameTitle.setText(productName);

            Refresh();

            if(preferences.getAutomaticallyRefreshData()) {
                refreshHandler = new Handler();
                refreshRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Refresh();
                        refreshHandler.postDelayed(this, 10000);
                    }
                };

                refreshHandler.postDelayed(refreshRunnable, 10000);
            }
        }
    }

    // Méthode qui met à jour le texte de la toolbar avec la bonne traduction
    private void UpdateToolbarText() {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(Utils.getResourceString(getApplicationContext(), R.string.app_name));
        TextView toolbarSubtitle = findViewById(R.id.toolbar_subtitle);
        toolbarSubtitle.setText(Utils.getResourceString(getApplicationContext(), R.string.suivi_du_status_de_larticle));
    }

    // Supprimer le rafraichissement automatique si il est activé
    @Override
    public void onDestroy() {
        if (refreshHandler != null || refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }

        super.onDestroy();
    }

    // Met le texte de la toolbar à jour avec la bonne traduction
    @Override
    protected void onResume() {
        super.onResume();
        UpdateToolbarText();
    }

    // Rafraichi l'historique des boutiques
    public void Refresh() {
        try {
            List<LinkStatus> statusList = dbHelper.getStoreFrontStatus(ID);
            adapter = new PriceHistoryAdapter(getApplicationContext(), 0, statusList, (OnProductInteractionListener) this);
            ListView itemView = findViewById(R.id.historique);
            itemView.setAdapter(adapter);
        } catch (CursorIndexOutOfBoundsException ex) {}
    }

    // Rafraichi les données du graphique
    public void RefreshChart() {
        LineChart lineChart = findViewById(R.id.line_chart);
        lineChart.getDescription().setEnabled(false);

        ViewPortHandler viewPortHandler = lineChart.getViewPortHandler();
        viewPortHandler.setMinMaxScaleY(1f, 1f);
        viewPortHandler.setDragOffsetY(0f);

        Legend legend = lineChart.getLegend();
        legend.setTextSize(12f);

        // Obtention des données du graphique à partir de la BD
        LineData lineData = dbHelper.getLineData(ID);
        lineChart.setData(lineData);

        // Formattage des $ sur l'axe des Y
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawZeroLine(true);
        leftAxis.setZeroLineColor(Color.BLACK);
        leftAxis.setZeroLineWidth(1f);
        leftAxis.setValueFormatter(new DollarFormatter(Axis.Y));
        lineChart.getAxisRight().setEnabled(false);

        // Ajout du prix désiré
        double targetPrice = dbHelper.getTargetPrice(ID);
        LimitLine targetLine = new LimitLine((float)targetPrice, Utils.getResourceString(getApplicationContext(), R.string.prix_desire) + " (" + Utils.FormatPrice(targetPrice) + ")");
        targetLine.setLineColor(Color.BLACK);
        targetLine.setLineWidth(1f);
        targetLine.setTextColor(Color.BLACK);
        targetLine.setTextSize(12f);
        targetLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);

        leftAxis.addLimitLine(targetLine);

        // Formattage des jours sur l'axe des X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setXOffset(0f);
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new DayOfWeekFormatter());

        // On invalide le graphique pour le mettre à jour
        lineChart.invalidate();
    }

    // Ouverture des paramètres de l'application
    public void Settings(View view) {
        Intent intent = new Intent(getApplicationContext(), Settings.class);
        intent.putExtra("fromProduct", true);
        intent.putExtra("productID", ID);
        Utils.OpenSettings(this, getApplicationContext(), preferences, intent);
    }

    @Override
    public void TriggerEdit(int editID) {

    }

    @Override
    public void EditDone() {

    }

    // Ouverture du lien d'une boutique
    @Override
    public void OpenLink(String link) {
        if(link.equals("")) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }

    @Override
    public void SeeChart(int ID) {

    }
}
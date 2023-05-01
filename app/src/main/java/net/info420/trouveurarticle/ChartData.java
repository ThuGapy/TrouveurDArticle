package net.info420.trouveurarticle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.database.FollowedItemAdapter;
import net.info420.trouveurarticle.database.LinkStatus;
import net.info420.trouveurarticle.database.PriceHistoryAdapter;
import net.info420.trouveurarticle.views.OnProductInteractionListener;
import net.info420.trouveurarticle.views.OnRefreshRequestedListener;
import net.info420.trouveurarticle.views.graphs.DayOfWeekFormatter;
import net.info420.trouveurarticle.views.graphs.DollarFormatter;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;

public class ChartData extends AppCompatActivity implements OnProductInteractionListener {

    private DatabaseHelper dbHelper;
    private PriceHistoryAdapter adapter;
    private int ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_data);

        Intent intent = getIntent();
        ID = intent.getIntExtra("productID", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
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
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                intent.putExtra("fromProduct", true);
                intent.putExtra("productID", ID);
                startActivity(intent);
            }
        });

        LinearLayout textLayout = findViewById(R.id.text_layout);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) textLayout.getLayoutParams();
        layoutParams.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Trouveur d'articles");

        TextView toolbarSubtitle = findViewById(R.id.toolbar_subtitle);
        toolbarSubtitle.setText("Suivi du status de l'article!");

        setSupportActionBar(toolbar);

        TextView productNameTitle = findViewById(R.id.product_name_title);

        dbHelper = new DatabaseHelper(getApplicationContext());
        RefreshChart();

        if(ID != 0) {
            String productName = dbHelper.getProductName(ID);
            productNameTitle.setText(productName);

            Refresh();
        }
    }

    public void Refresh() {
        List<LinkStatus> statusList = dbHelper.getStoreFrontStatus(ID);
        adapter = new PriceHistoryAdapter(getApplicationContext(), 0, statusList, (OnProductInteractionListener) this);
        ListView itemView = findViewById(R.id.historique);
        itemView.setAdapter(adapter);
    }

    public void RefreshChart() {
        LineChart lineChart = findViewById(R.id.line_chart);
        lineChart.getDescription().setEnabled(false);

        ViewPortHandler viewPortHandler = lineChart.getViewPortHandler();
        viewPortHandler.setMinMaxScaleY(1f, 1f);
        viewPortHandler.setDragOffsetY(0f);

        Legend legend = lineChart.getLegend();
        legend.setTextSize(12f);

        LineData lineData = dbHelper.getLineData(ID);

        lineChart.setData(lineData);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawZeroLine(true);
        leftAxis.setZeroLineColor(Color.BLACK);
        leftAxis.setZeroLineWidth(1f);
        leftAxis.setValueFormatter(new DollarFormatter(DollarFormatter.Axis.Y));
        lineChart.getAxisRight().setEnabled(false);

        double targetPrice = dbHelper.getTargetPrice(ID);
        LimitLine targetLine = new LimitLine((float)targetPrice, "Prix désiré");
        targetLine.setLineColor(Color.BLACK);
        targetLine.setLineWidth(1f);
        targetLine.setTextColor(Color.BLACK);
        targetLine.setTextSize(12f);
        targetLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);

        leftAxis.addLimitLine(targetLine);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setXOffset(0f);
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new DayOfWeekFormatter());

        lineChart.invalidate();
    }

    @Override
    public void TriggerEdit(int editID) {

    }

    @Override
    public void EditDone() {

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

    }
}
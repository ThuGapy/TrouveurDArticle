package net.info420.trouveurarticle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.database.FollowedItemAdapter;
import net.info420.trouveurarticle.database.LinkStatus;
import net.info420.trouveurarticle.database.PriceHistoryAdapter;
import net.info420.trouveurarticle.views.OnProductInteractionListener;
import net.info420.trouveurarticle.views.OnRefreshRequestedListener;

import java.sql.Ref;
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
        layoutParams.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Trouveur d'articles");

        TextView toolbarSubtitle = findViewById(R.id.toolbar_subtitle);
        toolbarSubtitle.setText("Suivi du status de l'article!");

        setSupportActionBar(toolbar);

        if(ID != 0) {
            dbHelper = new DatabaseHelper(getApplicationContext());
            Refresh();
        }
    }

    public void Refresh() {
        List<LinkStatus> statusList = dbHelper.getStoreFrontStatus(ID);
        adapter = new PriceHistoryAdapter(getApplicationContext(), 0, statusList, (OnProductInteractionListener) this);
        ListView itemView = findViewById(R.id.historique);
        itemView.setAdapter(adapter);
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
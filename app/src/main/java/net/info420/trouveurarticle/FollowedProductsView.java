package net.info420.trouveurarticle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.database.CursorWrapper;
import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.database.FollowedItemAdapter;
import net.info420.trouveurarticle.views.OnRefreshRequestedListener;
import net.info420.trouveurarticle.views.OnProductInteractionListener;

// Classe qui gère le fragment des produits suivi
public class FollowedProductsView extends Fragment implements OnRefreshRequestedListener {
    // Déclaration des données membres
    private View fragmentView;
    private DatabaseHelper dbHelper;
    private CursorWrapper dataCursor;
    private FollowedItemAdapter itemAdapter;
    private AppSettings preferences;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    public FollowedProductsView() { }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Lorsque le fragment est créé
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_followed_products, container, false);

        preferences = new AppSettings(getContext());

        // Initialisation de la classe DatabaseHelper et rafraichissement de l'adapter
        dbHelper = new DatabaseHelper(getContext());
        Refresh();

        // Initialisation du bouton rafraichir
        ImageButton refreshButton = fragmentView.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Refresh();
            }
        });

        // On crée le rafraichissement automatique si il est activé dans les paramètres
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

        return fragmentView;
    }

    // Rafraichir l'adapter de la listview
    public void Refresh() {
        dataCursor = dbHelper.getAllItemsStockStatus();
        itemAdapter = new FollowedItemAdapter(getActivity(), dataCursor, (OnProductInteractionListener) getActivity(), (OnRefreshRequestedListener) this);
        ListView itemView = fragmentView.findViewById(R.id.item_listview);
        itemView.setAdapter(itemAdapter);
    }

    // Supression du rafraichissement automatique si il est activé
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }

        if (itemAdapter != null) {
            itemAdapter.changeCursor(null);
        }
    }

    // Rafraichir lorsque demandé
    @Override
    public void RequestRefresh() {
        Refresh();
    }
}
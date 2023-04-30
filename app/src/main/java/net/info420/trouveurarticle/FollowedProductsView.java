package net.info420.trouveurarticle;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.database.FollowedItemAdapter;
import net.info420.trouveurarticle.views.OnRefreshRequestedListener;
import net.info420.trouveurarticle.views.OnTriggerEditListener;

public class FollowedProductsView extends Fragment implements OnRefreshRequestedListener {
    private View fragmentView;
    private DatabaseHelper dbHelper;
    private Cursor dataCursor;
    private FollowedItemAdapter itemAdapter;
    public FollowedProductsView() { }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_followed_products, container, false);

        dbHelper = new DatabaseHelper(getContext());
        Refresh();

        ImageButton refreshButton = fragmentView.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Refresh();
            }
        });

        return fragmentView;
    }

    public void Refresh() {
        dataCursor = dbHelper.getAllItemsStockStatus();
        itemAdapter = new FollowedItemAdapter(getActivity(), (OnTriggerEditListener) getActivity(), (OnRefreshRequestedListener) this);
        itemAdapter.changeCursor(dataCursor);
        ListView itemView = fragmentView.findViewById(R.id.item_listview);
        itemView.setAdapter(itemAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (itemAdapter != null) {
            itemAdapter.changeCursor(null);
        }
    }

    @Override
    public void RequestRefresh() {
        Refresh();
    }
}
package net.info420.trouveurarticle.database;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import net.info420.trouveurarticle.R;

public class FollowedItemAdapter extends CursorAdapter {
    private Context applicationContext;
    private DatabaseHelper dbHelper;
    private ListView adapterListView;
    private enum PRICE_STATUS {
        GOOD,
        OVERPRICED,
        OOS,
        NO_DATA
    }

    public FollowedItemAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.following_product, parent, false);
        applicationContext = context;
        adapterListView = (ListView) parent;
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int idIndex = cursor.getColumnIndexOrThrow("_id");
        int nomArticleIndex = cursor.getColumnIndexOrThrow("nomArticle");
        int prixIndex = cursor.getColumnIndexOrThrow("prix");
        int amazonStockIndex = cursor.getColumnIndexOrThrow("amazon_stock");
        int amazonPriceIndex = cursor.getColumnIndexOrThrow("amazon_price");
        int neweggStockIndex = cursor.getColumnIndexOrThrow("newegg_stock");
        int neweggPriceIndex = cursor.getColumnIndexOrThrow("newegg_price");
        int canadaComputersStockIndex = cursor.getColumnIndexOrThrow("canadacomputers_stock");
        int canadaComputersPriceIndex = cursor.getColumnIndexOrThrow("canadacomputers_price");
        int memoryExpressStockIndex = cursor.getColumnIndexOrThrow("memoryexpress_stock");
        int memoryExpressPriceIndex = cursor.getColumnIndexOrThrow("memoryexpress_price");

        int elementID = cursor.getInt(idIndex);
        String elementName = cursor.getString(nomArticleIndex);

        View colorIndicatorView = view.findViewById(R.id.color_indicator);
        TextView itemName = view.findViewById(R.id.item_name);

        ImageButton editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageButton deleteButton = view.findViewById(R.id.delete_item_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Êtes vous certain de vouloir supprimer l'item: " + elementName + "?");
                builder.setTitle("Confirmation");
                builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper = new DatabaseHelper(context);
                        dbHelper.deleteItem(elementID);
                        dbHelper.close();

                        RefreshListView();
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
        });

        PRICE_STATUS status = PRICE_STATUS.NO_DATA;
        boolean inStock = false;
        double price = 0;

        if(cursor.getInt(amazonStockIndex) == 1) {
            inStock = true;
            price = cursor.getDouble(amazonPriceIndex);
        }
        if(cursor.getInt(neweggStockIndex) == 1) {
            inStock = true;
            if(cursor.getDouble(neweggPriceIndex) < price) {
                price = cursor.getDouble(amazonPriceIndex);
            }
        }
        if(cursor.getInt(canadaComputersStockIndex) == 1) {
            inStock = true;
            if(cursor.getDouble(canadaComputersPriceIndex) < price) {
                price = cursor.getDouble(canadaComputersPriceIndex);
            }
        }
        if(cursor.getInt(memoryExpressStockIndex) == 1) {
            inStock = true;
            if(cursor.getFloat(memoryExpressPriceIndex) < price) {
                price = cursor.getDouble(memoryExpressPriceIndex);
            }
        }

        float targetPrice = cursor.getFloat(prixIndex);
        if(inStock && price <= targetPrice) {
            status = PRICE_STATUS.GOOD;
        } else if(inStock && price > targetPrice) {
            status = PRICE_STATUS.OVERPRICED;
        } else if(!inStock && price == 0) {
            status = PRICE_STATUS.NO_DATA;
        } else {
            status = PRICE_STATUS.OOS;
        }

        itemName.setText(cursor.getString(nomArticleIndex));

        switch(status) {
            case GOOD:
                colorIndicatorView.setBackgroundColor(ContextCompat.getColor(context, R.color.instock_color));
                break;
            case OVERPRICED:
                colorIndicatorView.setBackgroundColor(ContextCompat.getColor(context, R.color.overpriced_color));
                break;
            case OOS:
                colorIndicatorView.setBackgroundColor(ContextCompat.getColor(context, R.color.oos_color));
                break;
            case NO_DATA:
                colorIndicatorView.setBackgroundColor(ContextCompat.getColor(context, R.color.displayerror_color));
                break;
        }

    }

    public void RefreshListView() {
        dbHelper = new DatabaseHelper(applicationContext);
        Cursor dataCursor = dbHelper.getAllItemsStockStatus();
        this.changeCursor(dataCursor);
        adapterListView.setAdapter(this);
        dbHelper.close();
    }
}

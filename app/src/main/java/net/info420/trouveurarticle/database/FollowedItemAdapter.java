package net.info420.trouveurarticle.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import net.info420.trouveurarticle.R;

public class FollowedItemAdapter extends CursorAdapter {
    private enum PRICE_STATUS {
        GOOD,
        OVERPRICED,
        OOS,
        ERROR
    }

    public FollowedItemAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.following_product, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int idIndex = cursor.getColumnIndexOrThrow("id");
        int nomArticleIndex = cursor.getColumnIndexOrThrow("nomArticle");
        int prixIndex = cursor.getColumnIndexOrThrow("prix");
        int amazonStockIndex = cursor.getColumnIndexOrThrow("amazon_stock");
        int amazonPriceIndex = cursor.getColumnIndexOrThrow("amazon_price");
        int bestbuyStockIndex = cursor.getColumnIndexOrThrow("bestbuy_stock");
        int bestbuyPriceIndex = cursor.getColumnIndexOrThrow("bestbuy_price");
        int staplesStockIndex = cursor.getColumnIndexOrThrow("staples_stock");
        int staplesPriceIndex = cursor.getColumnIndexOrThrow("staples_price");
        int neweggStockIndex = cursor.getColumnIndexOrThrow("newegg_stock");
        int neweggPriceIndex = cursor.getColumnIndexOrThrow("newegg_price");

        View colorIndicatorView = view.findViewById(R.id.color_indicator);
        TextView itemName = view.findViewById(R.id.item_name);

        PRICE_STATUS status = PRICE_STATUS.ERROR;
        boolean inStock = false;
        float price = 0f;

        if(cursor.getInt(amazonStockIndex) == 1) {
            inStock = true;
            price = cursor.getFloat(amazonPriceIndex);
        }
        if(cursor.getInt(bestbuyStockIndex) == 1) {
            inStock = true;
            if(cursor.getFloat(bestbuyPriceIndex) < price) {
                price = cursor.getFloat(amazonPriceIndex);
            }
        }
        if(cursor.getInt(staplesStockIndex) == 1) {
            inStock = true;
            if(cursor.getFloat(staplesPriceIndex) < price) {
                price = cursor.getFloat(staplesPriceIndex);
            }
        }
        if(cursor.getInt(neweggStockIndex) == 1) {
            inStock = true;
            if(cursor.getFloat(neweggPriceIndex) < price) {
                price = cursor.getFloat(neweggPriceIndex);
            }
        }

        float targetPrice = cursor.getFloat(prixIndex);
        if(inStock && price <= targetPrice) {
            status = PRICE_STATUS.GOOD;
        } else if(inStock && price > targetPrice) {
            status = PRICE_STATUS.OVERPRICED;
        } else if(!inStock) {
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
            case ERROR:
                colorIndicatorView.setBackgroundColor(ContextCompat.getColor(context, R.color.displayerror_color));
                break;
        }

    }
}

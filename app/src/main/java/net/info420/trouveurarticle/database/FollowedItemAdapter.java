package net.info420.trouveurarticle.database;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import net.info420.trouveurarticle.views.OnRefreshRequestedListener;
import net.info420.trouveurarticle.views.OnTriggerEditListener;

public class FollowedItemAdapter extends CursorAdapter {
    private Context applicationContext;
    private DatabaseHelper dbHelper;
    private ListView adapterListView;
    private OnTriggerEditListener activityEditListener;
    private OnRefreshRequestedListener fragmentRefreshListener;
    private enum PRICE_STATUS {
        GOOD,
        OVERPRICED,
        OOS,
        NO_DATA
    }

    public FollowedItemAdapter(Context context, OnTriggerEditListener editListener, OnRefreshRequestedListener refreshListener) {
        super(context, null, 0);
        activityEditListener = editListener;
        fragmentRefreshListener = refreshListener;
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
        int amazonLinkIndex = cursor.getColumnIndexOrThrow("amazon_link");
        int amazonStockIndex = cursor.getColumnIndexOrThrow("amazon_stock");
        int amazonPriceIndex = cursor.getColumnIndexOrThrow("amazon_price");
        int neweggLinkIndex = cursor.getColumnIndexOrThrow("newegg_link");
        int neweggStockIndex = cursor.getColumnIndexOrThrow("newegg_stock");
        int neweggPriceIndex = cursor.getColumnIndexOrThrow("newegg_price");
        int canadaComputersLinkIndex = cursor.getColumnIndexOrThrow("canadacomputers_link");
        int canadaComputersStockIndex = cursor.getColumnIndexOrThrow("canadacomputers_stock");
        int canadaComputersPriceIndex = cursor.getColumnIndexOrThrow("canadacomputers_price");
        int memoryExpressLinkIndex = cursor.getColumnIndexOrThrow("memoryexpress_link");
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
                activityEditListener.TriggerEdit(elementID);
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

                        fragmentRefreshListener.RequestRefresh();
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
        double price = -1;
        String lowestPriceLink = "";

        if(cursor.getInt(amazonStockIndex) == 1) {
            inStock = true;
            price = cursor.getDouble(amazonPriceIndex);
            lowestPriceLink = cursor.getString(amazonLinkIndex);
        }
        if(cursor.getInt(neweggStockIndex) == 1) {
            inStock = true;
            if(cursor.getDouble(neweggPriceIndex) < price || price == -1) {
                price = cursor.getDouble(neweggPriceIndex);
                lowestPriceLink = cursor.getString(neweggLinkIndex);
            }
        }
        if(cursor.getInt(canadaComputersStockIndex) == 1) {
            inStock = true;
            if(cursor.getDouble(canadaComputersPriceIndex) < price || price == -1) {
                price = cursor.getDouble(canadaComputersPriceIndex);
                lowestPriceLink = cursor.getString(canadaComputersLinkIndex);
            }
        }
        if(cursor.getInt(memoryExpressStockIndex) == 1) {
            inStock = true;
            if(cursor.getFloat(memoryExpressPriceIndex) < price || price == -1) {
                price = cursor.getDouble(memoryExpressPriceIndex);
                lowestPriceLink = cursor.getString(memoryExpressLinkIndex);
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

        TextView productPrice = view.findViewById(R.id.price_text);

        if(status == PRICE_STATUS.GOOD || status == PRICE_STATUS.OVERPRICED) {
            String seeLink = lowestPriceLink;
            itemName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityEditListener.OpenLink(seeLink);
                }
            });

            itemName.setTypeface(null, Typeface.BOLD);
            itemName.setPaintFlags(itemName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            itemName.setTextColor(context.getResources().getColor(R.color.dark_blue));

            productPrice.setVisibility(View.VISIBLE);
            String priceTotal = String.valueOf((int)Math.floor(price));
            String priceDecimalText = String.valueOf(price);

            int dotIndex = priceDecimalText.indexOf(".");
            if (dotIndex == -1) {
                productPrice.setText(String.valueOf(price) + ".00$");
            } else {
                String decimal = priceDecimalText.substring(dotIndex + 1);
                if (decimal.length() == 0) {
                    productPrice.setText(priceTotal + ".00$");
                } else if (decimal.length() == 1) {
                    productPrice.setText(priceTotal + "." + decimal + "0$");
                } else {
                    productPrice.setText(priceTotal + "." + decimal + "$");
                }
            }
        } else {
            productPrice.setVisibility(View.GONE);
        }
    }
}
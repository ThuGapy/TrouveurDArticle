package net.info420.trouveurarticle.database;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import net.info420.trouveurarticle.R;
import net.info420.trouveurarticle.Utils;
import net.info420.trouveurarticle.views.OnProductInteractionListener;

import java.util.List;

public class PriceHistoryAdapter extends ArrayAdapter<LinkStatus> {
    private OnProductInteractionListener productListener;

    public PriceHistoryAdapter(@NonNull Context context, int resource, @NonNull List<LinkStatus> statusList, OnProductInteractionListener listener) {
        super(context, resource, statusList);
        productListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View newView, @NonNull ViewGroup parent) {
        if(newView == null) {
            newView = LayoutInflater.from(getContext()).inflate(R.layout.availability_history_item, parent, false);
        }

        LinkStatus status = getItem(position);

        View colorIndicator = newView.findViewById(R.id.color_indicator);

        String nameSuffix = "";

        switch(status.getStatus()) {
            case GOOD:
                nameSuffix = " (" + Utils.FormatPrice(status.Prix) + ")";
                colorIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.instock_color));
                break;
            case OVERPRICED:
                nameSuffix = " (" + Utils.FormatPrice(status.Prix) + ")";
                colorIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.overpriced_color));
                break;
            case OOS:
                nameSuffix = " (" + "Rupture de stock" + ")";
                colorIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.oos_color));
                break;
            case NO_DATA:
                nameSuffix = " (" + "Aucune données" + ")";
                colorIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.displayerror_color));
                break;
        }

        TextView namePrice = newView.findViewById(R.id.item_name);

        switch(status.Boutique) {
            case Amazon:
                namePrice.setText("Amazon.ca" + nameSuffix);
                break;
            case Newegg:
                namePrice.setText("Newegg.ca" + nameSuffix);
                break;
            case CanadaComputers:
                namePrice.setText("CanadaComputers.com" + nameSuffix);
                break;
            case MemoryExpress:
                namePrice.setText("MemoryExpress.com" + nameSuffix);
                break;
        }

        if(status.getStatus() == PriceStatus.GOOD || status.getStatus() == PriceStatus.OVERPRICED) {
            namePrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    productListener.OpenLink(status.Link);
                }
            });

            namePrice.setTypeface(null, Typeface.BOLD);
            namePrice.setPaintFlags(namePrice.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            namePrice.setTextColor(getContext().getResources().getColor(R.color.dark_blue));
        }

        TextView elapsedTime = newView.findViewById(R.id.elapsed_time_text);
        if(status.getStatus() != PriceStatus.NO_DATA) {
            elapsedTime.setText(status.getElapsedTime());
        } else {
            elapsedTime.setVisibility(View.GONE);
        }

        return newView;
    }
}

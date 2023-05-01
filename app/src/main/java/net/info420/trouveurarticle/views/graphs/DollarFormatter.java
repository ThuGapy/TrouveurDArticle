package net.info420.trouveurarticle.views.graphs;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class DollarFormatter extends ValueFormatter
{
    @Override
    public String getFormattedValue(float value) {
        return String.format("$%.2f", value);
    }
}

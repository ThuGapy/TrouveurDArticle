package net.info420.trouveurarticle.views.graphs;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class DollarFormatter extends ValueFormatter
{
    public enum Axis {
        X,
        Y
    }
    private Axis axis;
    public DollarFormatter(Axis _axis) {
        axis = _axis;
    }
    @Override
    public String getFormattedValue(float value) {
        if(value == 0f && axis == Axis.X) {
            return "";
        }
        return String.format("$%.2f", value);
    }
}

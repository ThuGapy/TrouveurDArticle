package net.info420.trouveurarticle.views.graphs;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DayOfWeekFormatter extends ValueFormatter {
    private SimpleDateFormat dateFormat;

    public DayOfWeekFormatter() {
        dateFormat = new SimpleDateFormat("EE", Locale.getDefault());
    }

    @Override
    public String getFormattedValue(float value) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, (int) value - 6);
        Date date = calendar.getTime();
        return dateFormat.format(date);
    }
}

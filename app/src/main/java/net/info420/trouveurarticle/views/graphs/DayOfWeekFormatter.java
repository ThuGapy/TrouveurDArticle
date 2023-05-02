package net.info420.trouveurarticle.views.graphs;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Classe qui formatte un jour vers un abbréviation pour le graphique
 */
public class DayOfWeekFormatter extends ValueFormatter {
    /**
     * SimpleDateFormat, Le formatteur de date
     */
    private SimpleDateFormat dateFormat;

    /**
     * Constructeur qui initialise la classe
     */
    public DayOfWeekFormatter() {
        dateFormat = new SimpleDateFormat("EE", Locale.getDefault());
    }

    /**
     * Méthode qui obtient la valeur une fois formattée
     * @param value La valeur à être formatté
     * @return String, la valeur formattée
     */
    @Override
    public String getFormattedValue(float value) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, (int) value - 6);
        Date date = calendar.getTime();
        return dateFormat.format(date);
    }
}

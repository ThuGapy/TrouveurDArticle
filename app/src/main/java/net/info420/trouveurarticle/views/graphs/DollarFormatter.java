package net.info420.trouveurarticle.views.graphs;

import com.github.mikephil.charting.formatter.ValueFormatter;

// Classe qui formatte les données monétaires dans le graphique
public class DollarFormatter extends ValueFormatter
{
    private Axis axis;

    // Constructeur de la classe
    public DollarFormatter(Axis _axis) {
        axis = _axis;
    }

    // Méthode qui obtient la donnée formattée
    @Override
    public String getFormattedValue(float value) {
        // Si la valeur est "0" et qu'on est sur l'axe des X on retourne une String vide
        if(value == 0f && axis == Axis.X) {
            return "";
        }
        return String.format("$%.2f", value); // On formate pour obtenir un nombre avec deux décimales après la virgule
    }
}

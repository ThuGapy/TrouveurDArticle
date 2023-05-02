package net.info420.trouveurarticle.views.graphs;

import com.github.mikephil.charting.formatter.ValueFormatter;

/**
 * Classe qui formatte les données d'argent pour le graphique
 */
public class DollarFormatter extends ValueFormatter
{
    /**
     * Axis, Axe du graphique
     */
    private Axis axis;

    /**
     * Constructeur qui initialise les données membres de la classe
     * @param _axis
     */
    public DollarFormatter(Axis _axis) {
        axis = _axis;
    }

    /**
     * Méthode qui obtient la valeur une fois formattée
     * @param value Valeur à formatter
     * @return String, Valeur une fois formattée
     */
    @Override
    public String getFormattedValue(float value) {
        // Si la valeur est "0" et qu'on est sur l'axe des X on retourne une String vide
        if(value == 0f && axis == Axis.X) {
            return "";
        }
        return String.format("$%.2f", value); // On formate pour obtenir un nombre avec deux décimales après la virgule
    }
}

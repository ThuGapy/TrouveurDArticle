package net.info420.trouveurarticle.scrappers;

/**
 * Classe qui contient les informations du résultat du service de suivi
 */
public class ScrapperResult {
    /**
     * boolean, Si le produit est en stock
     */
    public boolean EnStock;
    /**
     * double, Le prix du produit
     */
    public double Prix;

    /**
     * Constructeur qui initialise les données de la classe
     * @param _stock Si le produit est en stock
     * @param _prix Le prix du produit
     */
    public ScrapperResult(boolean _stock, double _prix) {
        EnStock = _stock;
        Prix = _prix;
    }

    /**
     * Méthode qui donne une manière propre de montrer dans la console le résultat du service de suivi
     * @return String, Le résultat du service de suivi
     */
    public String GetStringifiedResult() {
        return "En stock: " + EnStock + " | Prix: " + Prix;
    }

    /**
     * Fonction qui compare deux ScrapperResult pour valider qu'ils sont pareils
     * @param result1 Premier ScrapperResult
     * @param result2 Deuxième ScrapperResult
     * @return Si les deux ScrapperResult sont pareil
     */
    public static boolean Same(ScrapperResult result1, ScrapperResult result2) {
        return (result1.EnStock == result2.EnStock) && (result1.Prix == result2.Prix);
    }
}

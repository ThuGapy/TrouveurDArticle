package net.info420.trouveurarticle.scrappers;

// Classe qui stocke les informations d'un produit
public class ScrapperResult {
    // Déclaration des données membres
    public boolean EnStock;
    public double Prix;

    // Constructeur de la classe
    public ScrapperResult(boolean _stock, double _prix) {
        EnStock = _stock;
        Prix = _prix;
    }

    // Méthode qui retourne une versione simplifié en texte du status de l'article
    public String GetStringifiedResult() {
        return "En stock: " + EnStock + " | Prix: " + Prix;
    }

    // Fonction qui compare deux résultats
    public static boolean Same(ScrapperResult result1, ScrapperResult result2) {
        return (result1.EnStock == result2.EnStock) && (result1.Prix == result2.Prix);
    }
}

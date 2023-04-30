package net.info420.trouveurarticle.scrappers;

public class ScrapperResult {
    public boolean EnStock;
    public double Prix;

    public ScrapperResult(boolean _stock, double _prix) {
        EnStock = _stock;
        Prix = _prix;
    }

    public String GetStringifiedResult() {
        return "En stock: " + EnStock + " | Prix: " + Prix;
    }

    public static boolean Same(ScrapperResult result1, ScrapperResult result2) {
        return (result1.EnStock == result2.EnStock) && (result1.Prix == result2.Prix);
    }
}

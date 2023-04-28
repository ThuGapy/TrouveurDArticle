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
}

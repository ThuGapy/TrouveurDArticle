package net.info420.trouveurarticle.scrappers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class BestBuyScrapper extends Scrapper{

    @Override
    public ScrapperResult Fetch(Document document) {
        boolean isInStock = false;
        double price = 0;

        // Obtention du status
        try {
            Element availabilityElement = document.selectFirst("div.product-inventory strong");
            String availabilityText = availabilityElement.text();

            isInStock = availabilityText.toLowerCase().contains("in stock");
        } catch (NullPointerException e) {
            System.out.println("Impossible d'obtenir le status de l'article");
            return null;
        }

        // Obtention du prix
        try {
            Element priceElement = document.selectFirst("li.price-current strong");
            String priceText = priceElement.text().replace(",", "");

            Element centsPrice = document.selectFirst("li.price-current sup");
            String centsText = centsPrice.text();

            price = Double.parseDouble(priceText + centsText);
        } catch(NullPointerException e) {
            System.out.println("Impossible d'obtenir le prix");
            return null;
        }

        return new ScrapperResult(isInStock, price);
    }
}

package net.info420.trouveurarticle.scrappers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

// Classe qui gère l'obtient de données sur canadacomputers
public class CanadaComputersScrapper extends Scrapper{
    public static final String StoreName = "canadacomputers.com";
    public static final String SearchLinkStart = "https://www.canadacomputers.com/search/results_details.php";

    // Fonction qui crée un lien de recherche canadacomputers
    public static String SearchLink(String toSearch) {
        return SearchLinkStart + "?language=fr&keywords=" + toSearch.replace(" ", "+");
    }

    // Fonction qui obtient les informations du produit sur canadacomputers
    @Override
    public ScrapperResult Fetch(Document document) {
        boolean inStock = false;
        double price = 0;

        Element priceElement = document.selectFirst("span.h2-big > strong");
        if(priceElement != null) {
            String priceText = priceElement.text();
            try {
                price = Double.parseDouble(priceText.replace("$", "").replace(",", ""));
            } catch(NumberFormatException ex) {
                try {
                    price = Double.parseDouble(priceText.replace("$", "").replace(" ", ""));
                } catch (NumberFormatException ex2) {}
            }
        } else {
            return null;
        }

        if(price == 0) {
            return null;
        }

        Element stockElement = document.selectFirst("#onlineinfo p");
        if(stockElement != null) {
            String stockString = stockElement.text();
            inStock = stockString.toLowerCase().contains("available to ship");
        } else {
            return null;
        }

        return new ScrapperResult(inStock, price);
    }


    // Fonction qui obtient le nom du produit
    @Override
    public String FetchProductName(Document document) {
        Element productNameElement = document.selectFirst("h1.h3.mb-0");
        try {
            if(productNameElement != null) {
                return productNameElement.text().trim();
            }
        } catch(NullPointerException ex) {}
        return null;
    }
}

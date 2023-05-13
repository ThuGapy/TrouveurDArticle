package net.info420.trouveurarticle.scrappers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

// Classe qui gère l'obtient de données sur memoryexpress
public class MemoryExpressScrapper extends Scrapper{
    public static final String StoreName = "memoryexpress.com";
    public static final String SearchLinkStart = "https://www.memoryexpress.com/Search/Products";
    // Fonction qui crée un lien de recherche memoryexpress
    public static String SearchLink(String toSearch) {
        return SearchLinkStart + "?Search=" + toSearch.replace(" ", "+");
    }

    // Fonction qui obtient les informations du produit sur memoryexpress
    @Override
    public ScrapperResult Fetch(Document document) {
        boolean inStock = false;
        double price = 0;

        Element priceElement = document.selectFirst(".GrandTotal");
        if(priceElement != null) {
            String priceText = priceElement.text();
            price = Double.parseDouble(priceText.toLowerCase().replace("only", "").replace("$", "").replace(",", ""));
        } else {
            return null;
        }

        Element stockElement = document.selectFirst("span.c-capr-inventory-store__availability");
        if(stockElement != null) {
            inStock = stockElement.hasClass("InventoryState_InStock");
        } else {
            return null;
        }

        return new ScrapperResult(inStock, price);
    }

    // Fonction qui obtient le nom du produit
    @Override
    public String FetchProductName(Document document) {
        Element productNameElement = document.selectFirst("header.c-capr-header > h1");
        try {
            if (productNameElement != null) {
                return productNameElement.text().trim();
            }
        } catch(NullPointerException ex) {}
        return null;
    }
}

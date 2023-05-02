package net.info420.trouveurarticle.scrappers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MemoryExpressScrapper extends Scrapper{
    public static final String StoreName = "memoryexpress.com";
    public static final String SearchLinkStart = "https://www.memoryexpress.com/Search/Products";
    public static String SearchLink(String toSearch) {
        return SearchLinkStart + "?Search=" + toSearch.replace(" ", "+");
    }
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

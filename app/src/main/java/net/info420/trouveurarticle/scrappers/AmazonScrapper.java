package net.info420.trouveurarticle.scrappers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class AmazonScrapper extends Scrapper{
    public static final String StoreName = "amazon.ca";
    public static final String SearchLinkStart = "https://www.amazon.ca/s?k=";
    public static String SearchLink(String toSearch) {
        return SearchLinkStart + toSearch.replace(" ", "+");
    }
    public static String LinkCleaner(String link) {
        if(link == null || link.equals("")) return null;
        int dpIndex = link.indexOf("dp/");
        int nextSlash = link.substring(dpIndex + 3).indexOf("/");
        return link.substring(0, dpIndex + nextSlash + 3) + "/";
    }

    @Override
    public ScrapperResult Fetch(Document document) {
        boolean isInStock = false;
        double price = 0;

        if(document == null)
            return null;

        // Obtention du status
        try {
            Element availabilityElement = document.selectFirst("div.a-section.a-spacing-none._p13n-desktop-sims-fbt_fbt-desktop_shipping-info-show-box__17yWM");
            Element stockElement = availabilityElement.selectFirst("span.a-color-attainable");
            String stockText = stockElement.text();
            isInStock = stockText.toLowerCase().contains("in stock");
        } catch (NullPointerException e) {
            System.out.println("Impossible d'obtenir le status de l'article");
            return null;
        }

        // Obtention du prix
        try {
            Element priceSpan = document.selectFirst("span.a-offscreen");
            String priceText = priceSpan.text();
            priceText = priceText.replace("$", "");
            price = Double.parseDouble(priceText);
        } catch(NullPointerException e) {
            System.out.println("Impossible d'obtenir le prix");
            return null;
        }

        return new ScrapperResult(isInStock, price);
    }

    @Override
    public String FetchProductName(Document document) {
        Element productNameElement = document.selectFirst("#productTitle");
        if(productNameElement != null) {
            return productNameElement.text().trim();
        }
        return null;
    }
}

package net.info420.trouveurarticle.scrappers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CanadaComputersScrapper extends Scrapper{
    public static final String StoreName = "canadacomputers.com";
    public static final String SearchLinkStart = "https://www.canadacomputers.com/search/results_details.php";
    public static String SearchLink(String toSearch) {
        return SearchLinkStart + "?language=fr&keywords=" + toSearch.replace(" ", "+");
    }
    @Override
    public ScrapperResult Fetch(Document document) {
        boolean inStock = false;
        double price = 0;

        Element priceElement = document.selectFirst("span.h2-big > strong");
        if(priceElement != null) {
            String priceText = priceElement.text();
            price = Double.parseDouble(priceText.replace("$", "").replace(",", ""));
        } else {
            return null;
        }

        Element stockElement = document.selectFirst("#onlineinfo p");
        if(stockElement != null) {
            String stockString = stockElement.text();
            inStock = stockString.toLowerCase().contains("available to ship");
        }

        return new ScrapperResult(inStock, price);
    }

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

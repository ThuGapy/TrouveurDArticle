package net.info420.trouveurarticle.scrappers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Scrapper {
    public static String LinkCleaner(String link) {
        return link;
    }
    protected Document GetDocument(String link) {
        try {
            // Crée la connection avec des headers similaires à une vrai connection
            Connection connection = Jsoup.connect(link);
            connection.header("User-Agent", getRandomUserAgent());
            connection.header("Accept-Encoding", "gzip, deflate");
            connection.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.header("DNT", "1");
            connection.header("Connection", "keep-alive");
            connection.header("Upgrade-Insecure-Requests", "1");
            connection.header("Accept-Language", "en-US,en;q=0.5");
            connection.header("Pragma", "no-cache");
            connection.header("Cache-Control", "no-cache");

            // Obtention du HTML
            Document document = connection.get();

            return document;
        } catch(IOException e) {
            System.out.println("Impossible d'obtenir le document");
            return null;
        }
    }

    public abstract ScrapperResult Fetch(Document doc);
    public abstract String FetchProductName(Document doc);

    public ScrapperResult Fetch(String link) {
        Document document = GetDocument(link);
        return document == null ? null : Fetch(document);
    };

    public String FetchProductName(String link) {
        Document document = GetDocument(link);
        return document == null ? "" : FetchProductName(document);
    }

    private static String getRandomUserAgent() {
        List<String> userAgents = Arrays.asList(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.2 Safari/605.1.15",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1"
        );

        Random random = new Random();
        int randomIndex = random.nextInt(userAgents.size());
        return userAgents.get(randomIndex);
    }
}

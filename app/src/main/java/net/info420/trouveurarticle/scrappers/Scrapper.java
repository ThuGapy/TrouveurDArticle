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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Scrapper {
    public static String LinkCleaner(String link) {
        return link;
    }
    protected Document GetDocument(String link) {
        try {
            // Crée la connection avec des headers similaires à une vrai connection
            Connection connection = Jsoup.connect(link);
            connection.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0");
            connection.header("Accept-Encoding", "gzip, deflate");
            connection.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.header("DNT", "1");
            connection.header("Connection", "close");
            connection.header("Upgrade-Insecure-Requests", "1");

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
}

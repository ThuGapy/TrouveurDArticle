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

public abstract class Scrapper {
    public String LinkCleaner(String link) {
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

    public ScrapperResult Fetch(String link) {
        Document document = GetDocument(link);
        System.out.println(document.html());

        return Fetch(document);
    };

    public ScrapperResult Fetch(String link, Context context) {
        Looper.prepare();
        Handler handler = new Handler();

        WebView webView = new WebView(context);
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36";
        webView.getSettings().setUserAgentString(userAgent);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        webView.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                String unescapedHtmlContent = StringEscapeUtils.unescapeJava(value);
                                unescapedHtmlContent = unescapedHtmlContent.substring(1, unescapedHtmlContent.length() - 1);
                                Document document = Jsoup.parse(unescapedHtmlContent);
                            }
                        });
                    }
                }, 5000);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        Map<String, String> headers = new HashMap<>();
        //headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        /*headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-CA,en;q=0.9");
        headers.put("Connection", "keep-alive");
        headers.put("DNT", "1");
        headers.put("Referer", "https://slanrevolution.net");
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "none");
        headers.put("Upgrade-Insecure-Requests", "1");*/

        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-CA,en;q=0.5");
        headers.put("Connection", "keep-alive");
        headers.put("Referer", "https://walmart.ca");
        //headers.put("Upgrade-Insecure-Requests", "1");

        webView.loadUrl(link, headers);

        Looper.loop();
        return null;
    }
}

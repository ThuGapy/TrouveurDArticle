package net.info420.trouveurarticle.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import net.info420.trouveurarticle.R;
import net.info420.trouveurarticle.Utils;
import net.info420.trouveurarticle.scrappers.AmazonScrapper;
import net.info420.trouveurarticle.scrappers.CanadaComputersScrapper;
import net.info420.trouveurarticle.scrappers.MemoryExpressScrapper;
import net.info420.trouveurarticle.scrappers.NeweggScrapper;
import net.info420.trouveurarticle.scrappers.Scrapper;
import net.info420.trouveurarticle.scrappers.ScrapperResult;
import net.info420.trouveurarticle.scrappers.StoreFront;
import net.info420.trouveurarticle.views.graphs.Axis;
import net.info420.trouveurarticle.views.graphs.DollarFormatter;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

// Classe qui gère le côté BD de l'application
public class DatabaseHelper extends SQLiteOpenHelper {
    // Initialisation des données membres
    private static final int DB_VERSION = 4;
    private static final String DB_NAME = "trouveur_article";
    private static final String ITEM_TABLE = "produits";
    private static final String LINK_TABLE = "scrape_results";
    private static final long WeekInMillis = 604800000;

    // Création de la table produits
    private static final String CreateItemTable = "CREATE TABLE " + ITEM_TABLE + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "nomArticle TEXT NOT NULL,"
            + "amazon TEXT,"
            + "newegg TEXT,"
            + "canadacomputers TEXT,"
            + "memoryexpress TEXT,"
            + "prix REAL NOT NULL"
            + ")";

    // Créationde la table des résultats de moisonnage de données
    private static final String CreateLinkTable = "CREATE TABLE " + LINK_TABLE + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "link TEXT NOT NULL,"
            + "instock INTEGER NOT NULL,"
            + "prix REAL NOT NULL,"
            + "temps TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))"
            + ")";

    // Constructeur de la classe
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Création des tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CreateItemTable);
        db.execSQL(CreateLinkTable);
    }

    // Mise à jour des tables
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ITEM_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LINK_TABLE);

        onCreate(db);
    }

    // Obtient tous les produits dans la BD
    public CursorWrapper getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ITEM_TABLE, null, null, null, null, null, "id DESC");
        return new CursorWrapper(cursor, db);
    }

    // Obtient le status de chaque produit dans la BD
    public CursorWrapper getAllItemsStockStatus() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.id as _id, p.nomArticle, p.prix, " +
                "       sr_amazon.link AS amazon_link, sr_amazon.instock AS amazon_stock, sr_amazon.prix AS amazon_price," +
                "       sr_newegg.link AS newegg_link, sr_newegg.instock AS newegg_stock, sr_newegg.prix AS newegg_price," +
                "       sr_canadacomputers.link AS canadacomputers_link, sr_canadacomputers.instock AS canadacomputers_stock, sr_canadacomputers.prix AS canadacomputers_price," +
                "       sr_memoryexpress.link AS memoryexpress_link, sr_memoryexpress.instock AS memoryexpress_stock, sr_memoryexpress.prix AS memoryexpress_price" +
                " FROM produits AS p" +
                " LEFT JOIN scrape_results AS sr_amazon ON p.amazon = sr_amazon.link AND sr_amazon.id = (SELECT MAX(id) FROM scrape_results WHERE link = p.amazon)" +
                " LEFT JOIN scrape_results AS sr_newegg ON p.newegg = sr_newegg.link AND sr_newegg.id = (SELECT MAX(id) FROM scrape_results WHERE link = p.newegg)" +
                " LEFT JOIN scrape_results AS sr_canadacomputers ON p.canadacomputers = sr_canadacomputers.link AND sr_canadacomputers.id = (SELECT MAX(id) FROM scrape_results WHERE link = p.canadacomputers)" +
                " LEFT JOIN scrape_results AS sr_memoryexpress ON p.memoryexpress = sr_memoryexpress.link AND sr_memoryexpress.id = (SELECT MAX(id) FROM scrape_results WHERE link = p.memoryexpress)";

        return new CursorWrapper(db.rawQuery(query, null), db);
    }

    // Obtient un produit par son ID
    public CursorWrapper getItem(int ID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id, nomArticle, amazon, newegg, canadacomputers, memoryexpress, prix FROM produits WHERE id = " + ID;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        return new CursorWrapper(cursor, db);
    }

    // Obtient le nom d'un produit par son ID
    public String getProductName(int ID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT nomArticle FROM produits WHERE id = " + ID;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        String productName = "";
        try {
            productName =  cursor.getString(cursor.getColumnIndexOrThrow("nomArticle"));
        } catch(IllegalArgumentException ex) {}

        cursor.close();
        return productName;
    }

    // Obtient les données les plus récent d'un lien
    public CursorWrapper getLatestLinkData(String link) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {"id", "link", "instock", "prix", "temps"};
        String where = "link = '" + link + "'";

        Cursor cursor = db.query("scrape_results", columns, where, null, null, null, "id DESC", "1");

        if(cursor != null) {
            cursor.moveToFirst();
        }

        return new CursorWrapper(cursor, db);
    }

    // Obtient le status selon la boutique à partir de l'ID d'un produit
    public List<LinkStatus> getStoreFrontStatus(int ID) {
        List<LinkStatus> statusList = new ArrayList<LinkStatus>();

        // Obtention de l'article et des liens des différentes boutiques
        CursorWrapper wrapper = getItem(ID);
        double prix = wrapper.cursor.getDouble(wrapper.cursor.getColumnIndexOrThrow("prix"));
        String amazonLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("amazon"));
        String neweggLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("newegg"));
        String canadaComputersLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("canadacomputers"));
        String memoryExpressLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("memoryexpress"));
        wrapper.Close();

        CursorWrapper linkData;
        double price;
        boolean stock;

        // Vérification amazon, si le produit utilise amazon
        if(amazonLink != null) {
            if (!amazonLink.equals("")) {
                linkData = getLatestLinkData(amazonLink);
                try {
                    price = linkData.cursor.getDouble(linkData.cursor.getColumnIndexOrThrow("prix"));
                    stock = linkData.cursor.getInt(linkData.cursor.getColumnIndexOrThrow("instock")) == 1;

                    LinkStatus amazonResult = new LinkStatus(stock, price, linkData.cursor.getString(linkData.cursor.getColumnIndexOrThrow("temps")), amazonLink, StoreFront.Amazon);
                    amazonResult.DetermineStatus(prix);
                    statusList.add(amazonResult);
                } catch (IllegalArgumentException | CursorIndexOutOfBoundsException ex) {
                    statusList.add(new LinkStatus(false, 0, "", "", StoreFront.Amazon));
                }

                linkData.Close();
            }
        }

        // Vérification newegg, si le produit utilise newegg
        if(neweggLink != null) {
            if (!neweggLink.equals("")) {
                linkData = getLatestLinkData(neweggLink);
                try {
                    price = linkData.cursor.getDouble(linkData.cursor.getColumnIndexOrThrow("prix"));
                    stock = linkData.cursor.getInt(linkData.cursor.getColumnIndexOrThrow("instock")) == 1;

                    LinkStatus neweggResult = new LinkStatus(stock, price, linkData.cursor.getString(linkData.cursor.getColumnIndexOrThrow("temps")), neweggLink, StoreFront.Newegg);
                    neweggResult.DetermineStatus(prix);
                    statusList.add(neweggResult);
                } catch (IllegalArgumentException | CursorIndexOutOfBoundsException ex) {
                    statusList.add(new LinkStatus(false, 0, "", "", StoreFront.Newegg));
                }

                linkData.Close();
            }
        }

        // Vérification canadacomputers, si le produit utilise canadacomputers
        if(canadaComputersLink != null) {
            if (!canadaComputersLink.equals("")) {
                linkData = getLatestLinkData(canadaComputersLink);
                try {
                    price = linkData.cursor.getDouble(linkData.cursor.getColumnIndexOrThrow("prix"));
                    stock = linkData.cursor.getInt(linkData.cursor.getColumnIndexOrThrow("instock")) == 1;

                    LinkStatus canadaComputersResult = new LinkStatus(stock, price, linkData.cursor.getString(linkData.cursor.getColumnIndexOrThrow("temps")), canadaComputersLink, StoreFront.CanadaComputers);
                    canadaComputersResult.DetermineStatus(prix);
                    statusList.add(canadaComputersResult);
                } catch (IllegalArgumentException | CursorIndexOutOfBoundsException ex) {
                    statusList.add(new LinkStatus(false, 0, "", "", StoreFront.CanadaComputers));
                }

                linkData.Close();
            }
        }

        // Vérification de memoryexpress, si le produit utilise memoryexpress
        if(memoryExpressLink != null) {
            if (!memoryExpressLink.equals("")) {
                linkData = getLatestLinkData(memoryExpressLink);
                try {
                    price = linkData.cursor.getDouble(linkData.cursor.getColumnIndexOrThrow("prix"));
                    stock = linkData.cursor.getInt(linkData.cursor.getColumnIndexOrThrow("instock")) == 1;

                    LinkStatus memoryExpressResult = new LinkStatus(stock, price, linkData.cursor.getString(linkData.cursor.getColumnIndexOrThrow("temps")), memoryExpressLink, StoreFront.MemoryExpress);
                    memoryExpressResult.DetermineStatus(prix);
                    statusList.add(memoryExpressResult);
                } catch (IllegalArgumentException | CursorIndexOutOfBoundsException ex) {
                    statusList.add(new LinkStatus(false, 0, "", "", StoreFront.MemoryExpress));
                }

                linkData.Close();
            }
        }

        // Ordre de la liste en fonction du prix et des disponibilités à l'aide de l'interface Comparable
        Collections.sort(statusList);
        return statusList;
    }

    // Fonction qui obtient le précédent résultat d'un lien pour la création d'un résultat de moisonnage de données
    public ScrapperResult getPreviousResult(String link) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT instock, prix FROM scrape_results WHERE link = '" + link + "' ORDER BY id DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        ScrapperResult result = null;

        if(cursor.moveToFirst()) {
            try {
                boolean inStock = cursor.getInt(cursor.getColumnIndexOrThrow("instock")) == 1;
                double prix = cursor.getDouble(cursor.getColumnIndexOrThrow("prix"));

                result = new ScrapperResult(inStock, prix);
            } catch(IllegalArgumentException ex) {}
        }

        cursor.close();
        db.close();
        return result;
    }

    // Obtient le prix désiré d'un article à partir d'un ID
    public double getTargetPrice(int ID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT prix FROM produits WHERE id = " + ID;
        Cursor cursor = db.rawQuery(query, null);

        double price = 0;

        if(cursor.moveToFirst()) {
            try {
                price = cursor.getDouble(cursor.getColumnIndexOrThrow("prix"));
            } catch (IllegalArgumentException ex) {}
        }

        cursor.close();
        db.close();
        return price;
    }

    // Obtient si un lien a des données de moisonnages enregistré dans la bd
    public boolean hasScrapeData(String link) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT COUNT(*) as count FROM scrape_results WHERE link = '" + link + "'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        boolean result;

        try {
            result = cursor.getInt(cursor.getColumnIndexOrThrow("count")) > 0;
        } catch(IllegalArgumentException ex) {
            result = false;
        }

        cursor.close();
        db.close();
        return result;
    }

    // Obtient les données de moisonnage à partir de minuit d'une journée
    public ScrapperResult getStartOfDayResult(String link, Date date) {
        Calendar calendar = Utils.GetStartOfDayCalendar(date);
        long startOfDayTimestamp = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long endOfDayTimestamp = calendar.getTimeInMillis();

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT id, instock, prix FROM scrape_results WHERE link = ? AND (strftime('%s', temps) >= strftime('%s', ?) AND strftime('%s', temps) < strftime('%s', ?)) ORDER BY id ASC LIMIT 1";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startOfDayString = sdf.format(new Date(startOfDayTimestamp));
        String endOfDayString = sdf.format(new Date(endOfDayTimestamp));

        Cursor cursor = db.rawQuery(query, new String[]{link, startOfDayString, endOfDayString});

        ScrapperResult result = null;
        if(cursor.moveToFirst()) {
            result = new ScrapperResult(cursor.getInt(cursor.getColumnIndexOrThrow("instock")) == 1, cursor.getDouble(cursor.getColumnIndexOrThrow("prix")));
        }

        cursor.close();
        db.close();
        return result;
    }

    // Obtient la ligne de graphique pour un lien
    public LineDataSet getLineDataSet(String link, String nom, int color) {
        List<Entry> entries = new ArrayList<>();
        Calendar currentCalendar = Utils.GetStartOfDayCalendar(new Date());
        currentCalendar.add(Calendar.DAY_OF_YEAR, -7);
        for (int i = 0; i < 7; i++) {
            currentCalendar.add(Calendar.DAY_OF_YEAR, 1);

            ScrapperResult startOfDayResult = getStartOfDayResult(link, currentCalendar.getTime());

            if(startOfDayResult == null) {
                if(i == 0) {
                    entries.add(new Entry(i, 0));
                } else {
                    entries.add(new Entry(i, entries.get(i - 1).getY()));
                }
            } else {
                entries.add(new Entry(i, (float)startOfDayResult.Prix));
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, nom);
        dataSet.setColor(color);
        dataSet.setValueFormatter(new DollarFormatter(Axis.X));
        dataSet.setValueTextSize(12f);
        dataSet.setLineWidth(3f);

        return dataSet;
    }

    // Obtient les données du graphique pour la page "ChartData"
    public LineData getLineData(int ID) {
        // Obtiention des liens du produit
        CursorWrapper wrapper = getItem(ID);
        String amazonLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("amazon"));
        String neweggLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("newegg"));
        String canadaComputersLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("canadacomputers"));
        String memoryExpressLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("memoryexpress"));
        wrapper.Close();

        List<ILineDataSet> dataSets = new ArrayList<>();

        // On crée la ligne amazon si le produit utilise amazon
        if(amazonLink != null) {
            if(!amazonLink.equals("")) {
                if(hasScrapeData(amazonLink)) {
                    dataSets.add(getLineDataSet(amazonLink, "Amazon", Color.BLUE));
                }
            }
        }

        // On crée la ligne newegg si le produit utilise newegg
        if(neweggLink != null) {
            if(!neweggLink.equals("")) {
                if(hasScrapeData(neweggLink)) {
                    dataSets.add(getLineDataSet(neweggLink, "Newegg", Color.RED));
                }
            }
        }

        // On crée la ligne canacomputers si le produit utilise canadacomputers
        if(canadaComputersLink != null) {
            if(!canadaComputersLink.equals("")) {
                if(hasScrapeData(canadaComputersLink)) {
                    dataSets.add(getLineDataSet(canadaComputersLink, "CanadaComputers", Color.YELLOW));
                }
            }
        }

        // On crée la ligne memoryexpress si le produit utilise memoryexpress
        if(memoryExpressLink != null) {
            if(!memoryExpressLink.equals("")) {
                if(hasScrapeData(memoryExpressLink)) {
                    dataSets.add(getLineDataSet(memoryExpressLink, "MemoryExpress", Color.MAGENTA));
                }
            }
        }

        LineData lineData = new LineData(dataSets);
        return lineData;
    }

    // Fonction qui ajoute un nouveau produit à la BD
    public void createNewItem(String productName, String amazon, String newegg, String canadacomputers, String memoryexpress, double prix) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("nomArticle", productName);
        values.put("amazon", AmazonScrapper.LinkCleaner(amazon));
        values.put("newegg", NeweggScrapper.LinkCleaner(newegg));
        values.put("canadacomputers", CanadaComputersScrapper.LinkCleaner(canadacomputers));
        values.put("memoryexpress", MemoryExpressScrapper.LinkCleaner(memoryexpress));
        values.put("prix", prix);

        db.insert("produits", null, values);
        db.close();
    }

    // Fonction qui ajoute un résultat de moisonnage dans la base de données
    public void createScrapeResult(String link, ScrapperResult result, String productName, StoreFront storeFront, Context context, AppSettings preferences) {
        // On détermine si le résultat précédent est pareil
        ScrapperResult previousResult = this.getPreviousResult(link);
        boolean shouldCreateScrapeResult = false;

        if(previousResult == null) {
            shouldCreateScrapeResult = true;
        } else {
            if(!ScrapperResult.Same(result, previousResult)) {
                shouldCreateScrapeResult = true;
            }
        }

        // On ajoute le résultat à la BD si il n'est pas pareil
        if(shouldCreateScrapeResult) {
            System.out.println("Adding new scrape result for " + link);
            System.out.println(result.GetStringifiedResult());
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("link", link);
            values.put("instock", result.EnStock ? "1" : "0");
            values.put("prix", result.Prix);

            db.insert("scrape_results", null, values);
            db.close();

            if(result.EnStock) {
                String title = "";
                String description = String.format(Utils.getResourceString(context, R.string.produit_est_disponible_au_prix_de), productName, Utils.FormatPrice(result.Prix));

                switch (storeFront) {
                    case Amazon:
                        title = Utils.getResourceString(context, R.string.suivi_de_produit_amazon);
                        break;
                    case Newegg:
                        title = Utils.getResourceString(context, R.string.suivi_de_produit_newegg);
                        break;
                    case CanadaComputers:
                        title = Utils.getResourceString(context, R.string.suivi_de_produit_canadacomputers);
                        break;
                    case MemoryExpress:
                        title = Utils.getResourceString(context, R.string.suivi_de_produit_memoryexpress);
                        break;
                }

                Utils.SendNotification(title, description, context, preferences);
            }
        } else {
            System.out.println("Le resultat precedent est similaire, on passe ce resultat");
        }
    }

    // Fonction qui met à jour un produit dans la BD
    public void updateItem(int ID, String productName, String amazon, String newegg, String canadacomputers, String memoryexpress, double prix) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE produits SET nomArticle = ?, amazon = ?, newegg = ?, canadacomputers = ?, memoryexpress = ?, prix = ? WHERE id = ?";
        String[] updateValues = {productName, AmazonScrapper.LinkCleaner(amazon), NeweggScrapper.LinkCleaner(newegg), CanadaComputersScrapper.LinkCleaner(canadacomputers), MemoryExpressScrapper.LinkCleaner(memoryexpress), String.valueOf(prix), String.valueOf(ID)};
        db.execSQL(query, updateValues);
        db.close();
    }

    // Fonction qui supprime les données de moisonnage relié à un produit
    public void deleteRelatedScrapeResults(int ID) {
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteDatabase db = getReadableDatabase();
        Cursor readCursor = db.rawQuery("SELECT amazon, newegg, canadacomputers, memoryexpress FROM produits WHERE id = " + ID, null);
        if(readCursor.moveToFirst()) {
            try {
                String amazonLink = readCursor.getString(readCursor.getColumnIndexOrThrow("amazon"));
                String neweggLink = readCursor.getString(readCursor.getColumnIndexOrThrow("newegg"));
                String canadaComputersLink = readCursor.getString(readCursor.getColumnIndexOrThrow("canadacomputers"));
                String memoryExpressLink = readCursor.getString(readCursor.getColumnIndexOrThrow("memoryexpress"));
                wdb.execSQL("DELETE FROM scrape_results WHERE link = '" + amazonLink + "' OR link = '" + neweggLink + "' OR link = '" + canadaComputersLink + "' OR link = '" + memoryExpressLink + "'");
            } catch (IllegalArgumentException ex) {}
        }

        readCursor.close();
        wdb.close();
        db.close();
    }

    // Fonction qui supprime un produit de la BD
    public void deleteItem(int ID) {
        deleteRelatedScrapeResults(ID);

        SQLiteDatabase db = getWritableDatabase();
        db.delete("produits", "id = ?", new String[] {String.valueOf(ID)});
        db.close();
    }

    // Fonction qui supprime toutes les données de la table "scrape_results" contenant les données de moisonnage
    public void deleteAllScrapeResults() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("scrape_results", null, null);
        db.close();
    }

    // Fonction qui supprime les données plus vieille qu'une semaine
    public void deleteOlderThanAWeek() {
        SQLiteDatabase db = getWritableDatabase();
        long date = System.currentTimeMillis() - WeekInMillis;
        db.delete("scrape_results", "temps < ?", new String[] {String.valueOf(date)});
        db.close();
    }
}

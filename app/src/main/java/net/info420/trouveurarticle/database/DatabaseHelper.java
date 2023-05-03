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

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 4;
    private static final String DB_NAME = "trouveur_article";
    private static final String ITEM_TABLE = "produits";
    private static final String LINK_TABLE = "scrape_results";
    private static final long WeekInMillis = 604800000;

    private static final String CreateItemTable = "CREATE TABLE " + ITEM_TABLE + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "nomArticle TEXT NOT NULL,"
            + "amazon TEXT,"
            + "newegg TEXT,"
            + "canadacomputers TEXT,"
            + "memoryexpress TEXT,"
            + "prix REAL NOT NULL"
            + ")";

    private static final String CreateLinkTable = "CREATE TABLE " + LINK_TABLE + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "link TEXT NOT NULL,"
            + "instock INTEGER NOT NULL,"
            + "prix REAL NOT NULL,"
            + "temps TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CreateItemTable);
        db.execSQL(CreateLinkTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ITEM_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LINK_TABLE);

        onCreate(db);
    }

    public CursorWrapper getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ITEM_TABLE, null, null, null, null, null, "id DESC");
        return new CursorWrapper(cursor, db);
    }

    public CursorWrapper getAllItemsStockStatus() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.id as _id, p.nomArticle, p.prix, " +
                "       (SELECT link FROM scrape_results WHERE link = p.amazon ORDER BY id DESC LIMIT 1) AS amazon_link," +
                "       (SELECT instock FROM scrape_results WHERE link = p.amazon ORDER BY id DESC LIMIT 1) AS amazon_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.amazon ORDER BY id DESC LIMIT 1) AS amazon_price," +
                "       (SELECT link FROM scrape_results WHERE link = p.newegg ORDER BY id DESC LIMIT 1) AS newegg_link," +
                "       (SELECT instock FROM scrape_results WHERE link = p.newegg ORDER BY id DESC LIMIT 1) AS newegg_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.newegg ORDER BY id DESC LIMIT 1) AS newegg_price," +
                "       (SELECT link FROM scrape_results WHERE link = p.canadacomputers ORDER BY id DESC LIMIT 1) AS canadacomputers_link," +
                "       (SELECT instock FROM scrape_results WHERE link = p.canadacomputers ORDER BY id DESC LIMIT 1) AS canadacomputers_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.canadacomputers ORDER BY id DESC LIMIT 1) AS canadacomputers_price," +
                "       (SELECT link FROM scrape_results WHERE link = p.memoryexpress ORDER BY id DESC LIMIT 1) AS memoryexpress_link," +
                "       (SELECT instock FROM scrape_results WHERE link = p.memoryexpress ORDER BY id DESC LIMIT 1) AS memoryexpress_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.memoryexpress ORDER BY id DESC LIMIT 1) AS memoryexpress_price" +
                " FROM produits AS p";

        return new CursorWrapper(db.rawQuery(query, null), db);
    }

    public CursorWrapper getItem(int ID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id, nomArticle, amazon, newegg, canadacomputers, memoryexpress, prix FROM produits WHERE id = " + ID;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        return new CursorWrapper(cursor, db);
    }

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

    public List<LinkStatus> getStoreFrontStatus(int ID) {
        List<LinkStatus> statusList = new ArrayList<LinkStatus>();

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

        Collections.sort(statusList);
        return statusList;
    }

    public ScrapperResult getPreviousResult(String link) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT instock, prix FROM scrape_results WHERE link = '" + link + "'";
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


    public LineData getLineData(int ID) {
        CursorWrapper wrapper = getItem(ID);
        String amazonLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("amazon"));
        String neweggLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("newegg"));
        String canadaComputersLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("canadacomputers"));
        String memoryExpressLink = wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("memoryexpress"));
        wrapper.Close();

        List<ILineDataSet> dataSets = new ArrayList<>();

        if(amazonLink != null) {
            if(!amazonLink.equals("")) {
                if(hasScrapeData(amazonLink)) {
                    dataSets.add(getLineDataSet(amazonLink, "Amazon", Color.BLUE));
                }
            }
        }
        if(neweggLink != null) {
            if(!neweggLink.equals("")) {
                if(hasScrapeData(neweggLink)) {
                    dataSets.add(getLineDataSet(neweggLink, "Newegg", Color.RED));
                }
            }
        }

        if(canadaComputersLink != null) {
            if(!canadaComputersLink.equals("")) {
                if(hasScrapeData(canadaComputersLink)) {
                    dataSets.add(getLineDataSet(canadaComputersLink, "CanadaComputers", Color.YELLOW));
                }
            }
        }

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

    public void createScrapeResult(String link, ScrapperResult result, String productName, StoreFront storeFront, Context context, AppSettings preferences) {
        ScrapperResult previousResult = this.getPreviousResult(link);
        boolean shouldCreateScrapeResult = false;

        if(previousResult == null) {
            shouldCreateScrapeResult = true;
        } else {
            if(!ScrapperResult.Same(result, previousResult)) {
                shouldCreateScrapeResult = true;
            }
        }

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

    public void updateItem(int ID, String productName, String amazon, String newegg, String canadacomputers, String memoryexpress, double prix) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE produits SET nomArticle = ?, amazon = ?, newegg = ?, canadacomputers = ?, memoryexpress = ?, prix = ? WHERE id = ?";
        String[] updateValues = {productName, AmazonScrapper.LinkCleaner(amazon), NeweggScrapper.LinkCleaner(newegg), CanadaComputersScrapper.LinkCleaner(canadacomputers), MemoryExpressScrapper.LinkCleaner(memoryexpress), String.valueOf(prix), String.valueOf(ID)};
        db.execSQL(query, updateValues);
        db.close();
    }

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

    public void deleteItem(int ID) {
        deleteRelatedScrapeResults(ID);

        SQLiteDatabase db = getWritableDatabase();
        db.delete("produits", "id = ?", new String[] {String.valueOf(ID)});
        db.close();
    }

    public void deleteAllScrapeResults() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("scrape_results", null, null);
        db.close();
    }

    public void deleteOlderThanAWeek() {
        SQLiteDatabase db = getWritableDatabase();
        long date = System.currentTimeMillis() - WeekInMillis;
        db.delete("scrape_results", "temps < ?", new String[] {String.valueOf(date)});
        db.close();
    }
}

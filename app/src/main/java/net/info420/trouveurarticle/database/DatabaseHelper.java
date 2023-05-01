package net.info420.trouveurarticle.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.info420.trouveurarticle.scrappers.AmazonScrapper;
import net.info420.trouveurarticle.scrappers.CanadaComputersScrapper;
import net.info420.trouveurarticle.scrappers.MemoryExpressScrapper;
import net.info420.trouveurarticle.scrappers.NeweggScrapper;
import net.info420.trouveurarticle.scrappers.ScrapperResult;
import net.info420.trouveurarticle.scrappers.StoreFront;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 3;
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
            + "temps TEXT DEFAULT CURRENT_TIMESTAMP"
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

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ITEM_TABLE, null, null, null, null, null, "id DESC");
        return cursor;
    }

    public Cursor getAllItemsStockStatus() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.id as _id, p.nomArticle, p.prix, " +
                "       (SELECT link FROM scrape_results WHERE link = p.amazon) AS amazon_link," +
                "       (SELECT instock FROM scrape_results WHERE link = p.amazon) AS amazon_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.amazon) AS amazon_price," +
                "       (SELECT link FROM scrape_results WHERE link = p.newegg) AS newegg_link," +
                "       (SELECT instock FROM scrape_results WHERE link = p.newegg) AS newegg_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.newegg) AS newegg_price," +
                "       (SELECT link FROM scrape_results WHERE link = p.canadacomputers) AS canadacomputers_link," +
                "       (SELECT instock FROM scrape_results WHERE link = p.canadacomputers) AS canadacomputers_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.canadacomputers) AS canadacomputers_price," +
                "       (SELECT link FROM scrape_results WHERE link = p.memoryexpress) AS memoryexpress_link," +
                "       (SELECT instock FROM scrape_results WHERE link = p.memoryexpress) AS memoryexpress_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.memoryexpress) AS memoryexpress_price" +
                " FROM produits AS p";

        return db.rawQuery(query, null);
    }

    public Cursor getItem(int ID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id, nomArticle, amazon, newegg, canadacomputers, memoryexpress, prix FROM produits WHERE id = " + ID;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        return cursor;
    }

    public Cursor getLatestLinkData(String link) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {"id", "link", "instock", "prix", "temps"};
        String where = "link = '" + link + "'";

        Cursor cursor = db.query("scrape_results", columns, where, null, null, null, "id DESC", "1");

        if(cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public List<LinkStatus> getStoreFrontStatus(int ID) {
        List<LinkStatus> statusList = new ArrayList<LinkStatus>();

        Cursor cursor = getItem(ID);
        double prix = cursor.getDouble(cursor.getColumnIndexOrThrow("prix"));
        String amazonLink = cursor.getString(cursor.getColumnIndexOrThrow("amazon"));
        String neweggLink = cursor.getString(cursor.getColumnIndexOrThrow("newegg"));
        String canadaComputersLink = cursor.getString(cursor.getColumnIndexOrThrow("canadacomputers"));
        String memoryExpressLink = cursor.getString(cursor.getColumnIndexOrThrow("memoryexpress"));

        Cursor linkData;
        double price;
        boolean stock;

        if(amazonLink != null) {
            if (!amazonLink.equals("")) {
                linkData = getLatestLinkData(amazonLink);
                try {
                    price = linkData.getDouble(linkData.getColumnIndexOrThrow("prix"));
                    stock = linkData.getInt(linkData.getColumnIndexOrThrow("instock")) == 1;

                    LinkStatus amazonResult = new LinkStatus(stock, price, linkData.getString(linkData.getColumnIndexOrThrow("temps")), amazonLink, StoreFront.Amazon);
                    amazonResult.DetermineStatus(prix);
                    statusList.add(amazonResult);
                } catch (IllegalArgumentException | CursorIndexOutOfBoundsException ex) {
                    statusList.add(new LinkStatus(false, 0, "", "", StoreFront.Amazon));
                }

                linkData.close();
            }
        }

        if(neweggLink != null) {
            if (!neweggLink.equals("")) {
                linkData = getLatestLinkData(neweggLink);
                try {
                    price = linkData.getDouble(linkData.getColumnIndexOrThrow("prix"));
                    stock = linkData.getInt(linkData.getColumnIndexOrThrow("instock")) == 1;

                    LinkStatus neweggResult = new LinkStatus(stock, price, linkData.getString(linkData.getColumnIndexOrThrow("temps")), neweggLink, StoreFront.Newegg);
                    neweggResult.DetermineStatus(prix);
                    statusList.add(neweggResult);
                } catch (IllegalArgumentException | CursorIndexOutOfBoundsException ex) {
                    statusList.add(new LinkStatus(false, 0, "", "", StoreFront.Newegg));
                }

                linkData.close();
            }
        }

        if(canadaComputersLink != null) {
            if (!canadaComputersLink.equals("")) {
                linkData = getLatestLinkData(canadaComputersLink);
                try {
                    price = linkData.getDouble(linkData.getColumnIndexOrThrow("prix"));
                    stock = linkData.getInt(linkData.getColumnIndexOrThrow("instock")) == 1;

                    LinkStatus canadaComputersResult = new LinkStatus(stock, price, linkData.getString(linkData.getColumnIndexOrThrow("temps")), canadaComputersLink, StoreFront.CanadaComputers);
                    canadaComputersResult.DetermineStatus(prix);
                    statusList.add(canadaComputersResult);
                } catch (IllegalArgumentException | CursorIndexOutOfBoundsException ex) {
                    statusList.add(new LinkStatus(false, 0, "", "", StoreFront.CanadaComputers));
                }

                linkData.close();
            }
        }

        if(memoryExpressLink != null) {
            if (!memoryExpressLink.equals("")) {
                linkData = getLatestLinkData(memoryExpressLink);
                try {
                    price = linkData.getDouble(linkData.getColumnIndexOrThrow("prix"));
                    stock = linkData.getInt(linkData.getColumnIndexOrThrow("instock")) == 1;

                    LinkStatus memoryExpressResult = new LinkStatus(stock, price, linkData.getString(linkData.getColumnIndexOrThrow("temps")), memoryExpressLink, StoreFront.MemoryExpress);
                    memoryExpressResult.DetermineStatus(prix);
                    statusList.add(memoryExpressResult);
                } catch (IllegalArgumentException | CursorIndexOutOfBoundsException ex) {
                    statusList.add(new LinkStatus(false, 0, "", "", StoreFront.MemoryExpress));
                }

                linkData.close();
            }
        }

        Collections.sort(statusList);
        return statusList;
    }

    public ScrapperResult getPreviousResult(String link) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT instock, prix FROM scrape_results WHERE link = '" + link + "'";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            try {
                boolean inStock = cursor.getInt(cursor.getColumnIndexOrThrow("instock")) == 1;
                double prix = cursor.getDouble(cursor.getColumnIndexOrThrow("prix"));

                return new ScrapperResult(inStock, prix);
            } catch(IllegalArgumentException ex) {
                return null;
            }
        } else {
            return null;
        }
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

    public void createScrapeResult(String link, ScrapperResult result) {
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
        } else {
            System.out.println("The previous result is the same, skipping this scrape result");
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
    }
}

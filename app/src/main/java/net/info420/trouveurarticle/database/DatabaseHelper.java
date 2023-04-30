package net.info420.trouveurarticle.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import net.info420.trouveurarticle.scrappers.AmazonScrapper;
import net.info420.trouveurarticle.scrappers.CanadaComputersScrapper;
import net.info420.trouveurarticle.scrappers.MemoryExpressScrapper;
import net.info420.trouveurarticle.scrappers.NeweggScrapper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
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
            + "prix DOUBLE NOT NULL"
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
                "       (SELECT instock FROM scrape_results WHERE link = p.amazon) AS amazon_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.amazon) AS amazon_price," +
                "       (SELECT instock FROM scrape_results WHERE link = p.newegg) AS newegg_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.newegg) AS newegg_price," +
                "       (SELECT instock FROM scrape_results WHERE link = p.canadacomputers) AS canadacomputers_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.canadacomputers) AS canadacomputers_price," +
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

    public void updateItem(int ID, String productName, String amazon, String newegg, String canadacomputers, String memoryexpress, double prix) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE produits SET nomArticle = ?, amazon = ?, newegg = ?, canadacomputers = ?, memoryexpress = ?, prix = ? WHERE id = ?";
        String[] updateValues = {productName, AmazonScrapper.LinkCleaner(amazon), NeweggScrapper.LinkCleaner(newegg), CanadaComputersScrapper.LinkCleaner(canadacomputers), MemoryExpressScrapper.LinkCleaner(memoryexpress), String.valueOf(prix), String.valueOf(ID)};
        db.execSQL(query, updateValues);
        db.close();
    }

    public void deleteItem(int ID) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("produits", "id = ?", new String[] {String.valueOf(ID)});
    }

    public void deleteOlderThanAWeek() {
        SQLiteDatabase db = getWritableDatabase();
        long date = System.currentTimeMillis() - WeekInMillis;
        db.delete("scrape_results", "temps < ?", new String[] {String.valueOf(date)});
    }
}

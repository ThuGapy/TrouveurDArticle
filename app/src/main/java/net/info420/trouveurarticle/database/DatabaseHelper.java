package net.info420.trouveurarticle.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "trouveur_article";
    private static final String ITEM_TABLE = "produits";
    private static final String LINK_TABLE = "scrape_results";

    private static final String CreateItemTable = "CREATE TABLE " + ITEM_TABLE + "("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "nomArticle TEXT NOT NULL,"
            + "amazon TEXT,"
            + "bestbuy TEXT,"
            + "staples TEXT,"
            + "newegg TEXT,"
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

        String query = "SELECT p.id, p.nomArticle, p.prix, " +
                "       (SELECT instock FROM scrape_results WHERE link = p.amazon) AS amazon_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.amazon) AS amazon_price," +
                "       (SELECT instock FROM scrape_results WHERE link = p.bestbuy) AS bestbuy_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.bestbuy) AS bestbuy_price," +
                "       (SELECT instock FROM scrape_results WHERE link = p.staples) AS staples_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.staples) AS staples_price," +
                "       (SELECT instock FROM scrape_results WHERE link = p.newegg) AS newegg_stock," +
                "       (SELECT prix FROM scrape_results WHERE link = p.newegg) AS newegg_price" +
                " FROM produits AS p";

        return db.rawQuery(query, null);
    }

    public void createNewItem(String productName, String amazon, String bestbuy, String staples, String newegg, float prix) {

    }
}

package net.info420.trouveurarticle.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CursorWrapper {
    public Cursor cursor;
    public SQLiteDatabase db;

    public CursorWrapper(Cursor _cursor, SQLiteDatabase _db) {
        cursor = _cursor;
        db = _db;
    }

    public void Close() {
        cursor.close();
        db.close();
    }
}

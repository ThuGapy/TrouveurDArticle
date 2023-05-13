package net.info420.trouveurarticle.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Classe qui gère une BD et un curseur
public class CursorWrapper {
    public Cursor cursor;
    public SQLiteDatabase db;

    // Constructeur de la classe
    public CursorWrapper(Cursor _cursor, SQLiteDatabase _db) {
        cursor = _cursor;
        db = _db;
    }

    // Ferme la BD et le curseur
    public void Close() {
        cursor.close();
        db.close();
    }
}

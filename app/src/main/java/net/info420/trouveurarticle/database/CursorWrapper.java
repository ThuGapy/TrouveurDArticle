package net.info420.trouveurarticle.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Classe qui gère une connexion à une base de données et un curseur
 */
public class CursorWrapper {
    /**
     * Cursor, Curseur qui représente le résultat d'une requête SQL
     */
    public Cursor cursor;
    /**
     * SQLiteDatabase, Connexion à la base de données
     */
    public SQLiteDatabase db;

    /**
     * Constructeur qui initialise la classe
     * @param _cursor Curseur qui représente le résultat d'une requête SQL
     * @param _db Connexion à la base de données
     */
    public CursorWrapper(Cursor _cursor, SQLiteDatabase _db) {
        cursor = _cursor;
        db = _db;
    }

    /**
     * Méthode qui ferme le curseur ainsi que la connexion à la base de données
     */
    public void Close() {
        cursor.close();
        db.close();
    }
}

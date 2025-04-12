package com.example.tpvolley.projetws;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "school1";
    private static final int DATABASE_VERSION = 3;

    private static final String CREATE_TABLE = "CREATE TABLE etudiant(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nom TEXT," +
            "prenom TEXT," +
            "date_naiss INTEGER," +
            "image_path TEXT)";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE etudiant ADD COLUMN date_naiss INTEGER");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE etudiant ADD COLUMN image_path TEXT");
        }
    }
}
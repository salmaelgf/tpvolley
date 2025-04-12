package com.example.tpvolley.projetws;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.tpvolley.projetws.Etudiant;
import com.example.tpvolley.projetws.MySQLiteHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EtudiantService {

    private static final String TABLE_NAME = "etudiant";
    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE_NAISS = "date_naiss";  // New column for date
    private static final String KEY_IMAGE_PATH = "image_path";
    private static String[] COLUMNS = {KEY_ID, KEY_NOM, KEY_PRENOM, KEY_DATE_NAISS, KEY_IMAGE_PATH};

    private MySQLiteHelper helper;

    public EtudiantService(Context context) {
        this.helper = new MySQLiteHelper(context);
    }

    public void create(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISS, e.getDateNaiss().getTime());
        values.put(KEY_IMAGE_PATH, e.getImagePath());  // Add image path

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void update(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISS, e.getDateNaiss().getTime());
        values.put(KEY_IMAGE_PATH, e.getImagePath());  // Update image path

        db.update(TABLE_NAME,
                values,
                "id = ?",
                new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public Etudiant findById(int id) {
        Etudiant e = null;
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME,
                COLUMNS,
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        if (c.moveToFirst()) {
            e = new Etudiant();
            e.setId(c.getInt(0));
            e.setNom(c.getString(1));
            e.setPrenom(c.getString(2));
            if (!c.isNull(3)) {
                e.setDateNaiss(new Date(c.getLong(3)));
            }
            e.setImagePath(c.getString(4));  // Get image path
        }
        c.close();
        db.close();
        return e;
    }

    public List<Etudiant> findAll() {
        List<Etudiant> eds = new ArrayList<>();
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, COLUMNS, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                Etudiant e = new Etudiant();
                e.setId(c.getInt(0));
                e.setNom(c.getString(1));
                e.setPrenom(c.getString(2));
                if (!c.isNull(3)) {
                    e.setDateNaiss(new Date(c.getLong(3)));
                }
                e.setImagePath(c.getString(4));  // Get image path
                eds.add(e);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return eds;
    }


    public void delete(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        db.delete(TABLE_NAME,
                "id = ?",
                new String[]{String.valueOf(e.getId())});
        Log.d("delete", "Deleted ID: " + e.getId());
        db.close();
    }


}
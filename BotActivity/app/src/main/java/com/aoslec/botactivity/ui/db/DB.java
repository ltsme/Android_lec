package com.aoslec.botactivity.ui.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {
    public static final int version = 1;

    public DB(Context context) { super(context, "contactdb", null, version); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableSql = "create table tb_contact (" + // table 이름 : tb_contact
                "_id integer primary key autoincrement," +
                "name not null," +
                "phone, " +
                "email)";
        db.execSQL(tableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion==version) {
            db.execSQL("drop table tb_contact");
            onCreate(db);
        }
    }
}

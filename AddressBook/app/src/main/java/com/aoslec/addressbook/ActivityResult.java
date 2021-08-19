package com.aoslec.addressbook;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

public class ActivityResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView nameView = findViewById(R.id.result_name);

        DB helper = new DB(ActivityResult.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select name, phone, email from tb_contact order by _id desc limit 1", null);
        while (cursor.moveToNext()){
            nameView.setText(cursor.getString(0));
        }
        db.close();
    }
}
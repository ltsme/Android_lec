package com.aoslec.memberinfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnInsert, btnUpdate, btnDelete, btnSelect;
    TextView tvResult;
    MemberInfo memberInfo;

    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memberInfo = new MemberInfo(MainActivity.this);

        btnInsert = findViewById(R.id.btn_insert);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
        btnSelect = findViewById(R.id.btn_select);

        btnInsert.setOnClickListener(onClickListener);
        btnUpdate.setOnClickListener(onClickListener);
        btnDelete.setOnClickListener(onClickListener);
        btnSelect.setOnClickListener(onClickListener);
    } // onCreate

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_insert:
                    try{
                        DB = memberInfo.getWritableDatabase();
                        String query = "Insert INTO member(username, userid, passwd) VALUES ('홍길동', 'hkdong', 1111)";
                        DB.execSQL(query);

                        memberInfo.close();
                        Toast.makeText(MainActivity.this, "Insert OK!", Toast.LENGTH_SHORT);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Insert Error!", Toast.LENGTH_SHORT);
                    }
                    break;

                case R.id.btn_update:
                    try{
                        DB = memberInfo.getWritableDatabase();
                        String query = "";
                        DB.execSQL(query);

                        memberInfo.close();
                        Toast.makeText(MainActivity.this, "Insert OK!", Toast.LENGTH_SHORT);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Insert Error!", Toast.LENGTH_SHORT);
                    }
                    break;

                case R.id.btn_select:
                    try{
                        DB = memberInfo.getWritableDatabase();
                        String query = "Select username, userid, passwd FROM member;";
                        Cursor cursor = DB.rawQuery(query, null);

                        StringBuffer stringBuffer = new StringBuffer();
                        while (cursor.moveToNext()){
                            String username = cursor.getString(0);
                            String userid = cursor.getString(1);
                            int passwd = cursor.getInt(2);

                            stringBuffer.append("username:" + username + ", userid :" + userid + ", passwd :" + passwd);
                        }
                        tvResult.setText(stringBuffer.toString());
                        cursor.close();
                        memberInfo.close();
                        Toast.makeText(MainActivity.this, "Insert OK!", Toast.LENGTH_SHORT);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Insert Error!", Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }; // onClickListener

} // MainActivity
package com.aoslec.addressbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText nameView;
    EditText phoneView;
    EditText emailView;
    EditText groupView;
    Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameView = findViewById(R.id.edit_name);
        phoneView = findViewById(R.id.edit_phone);
        emailView = findViewById(R.id.edit_email);
        groupView = findViewById(R.id.edit_group);

        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==addBtn){
                    String name = nameView.getText().toString();
                    String email = emailView.getText().toString();
                    String phone = phoneView.getText().toString();
                    String group = groupView.getText().toString();

                    if(name==null || name.equals("")){ // 비어있을 경우

                        Toast toast = Toast.makeText(MainActivity.this, "이름이 입력되지 않았습니다. ", Toast.LENGTH_SHORT);
                        toast.show();
                    }else {
                        DB helper = new DB(MainActivity.this);
                        SQLiteDatabase db = helper.getWritableDatabase();
                        db.execSQL("insert into tb_contact (name, phone, email) values (?,?,?)", new String[] {name, phone, email});
                        db.close();

                        Toast toast = Toast.makeText(MainActivity.this, name + "주소록에 입력 되었습니다.", Toast.LENGTH_SHORT);
                        toast.show();

                        Intent intent = new Intent(MainActivity.this, ActivityResult.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
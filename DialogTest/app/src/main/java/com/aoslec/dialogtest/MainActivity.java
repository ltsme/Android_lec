package com.aoslec.dialogtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.btn01);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder bld = new AlertDialog.Builder(MainActivity.this);
                bld.setTitle("알립니다.");
                bld.setMessage("대화상자를 열었습니다.");
                bld.setIcon(R.drawable.w1);
                bld.setPositiveButton("닫기", null);
                bld.setCancelable(false);
                bld.show();


            }
        });
    }
}
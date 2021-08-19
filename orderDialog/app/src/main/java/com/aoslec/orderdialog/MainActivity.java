package com.aoslec.orderdialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.call);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LinearLayout linearLayout = (LinearLayout) View.inflate(MainActivity.this, );
                        new AlertDialog.Builder((MainActivity.this)
                        .setTitle("주문 정보를 입력하세요")
                        .setIcon(R.mipmap.ic_launcher)
                        .setView(linearLayout)
                        .setpositiveButton("확인", new DialogInterface.OnClickListener(){
                            @override
                            public class onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setnega
                        .show();
                )
            }
        });
    }
}
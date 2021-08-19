package com.aoslec.selectdialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

//    int mselect = 0;
//
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.call);



//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle("음식을 선택하세요")
//                        .setIcon(R.mipmap.ic_launcher)
//                        .setItems(R.array.foods, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                String[] foods = getResources().getStringArray(R.array.foods);
//                                TextView textView = findViewById(R.id.textview);
//                                textView.setText("선택한 음식 : " + foods[which]);
//                            }
//                        })
//                        .setNegativeButton("취소", null)
//                        .show();
//            }
//        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("음식을 선택하세요")
                        .setIcon(R.mipmap.ic_launcher)
                        .setSingleChoiceItems(R.array.foods, mselect,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mselect = which;
                                    }
                                }

                        )
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] foods = getResources().getStringArray(R.array.foods);
                                TextView textView = findViewById(R.id.textview);
                                textView.setText("선택한 음식 : " + foods[which]);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });
    }
}
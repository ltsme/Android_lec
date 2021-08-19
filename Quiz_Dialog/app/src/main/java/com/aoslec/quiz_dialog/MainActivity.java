package com.aoslec.quiz_dialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int a,b;
    int result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn01);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("질문")
                        .setMessage("좌변을 선택하십시오")
                        .setCancelable(false)
                        .setPositiveButton("4", mClick)
                        .setNegativeButton("5", mClick)
                        .show();
            }
        });
    }

    DialogInterface.OnClickListener mClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE){
                a=4;
            }else {
                a=5;
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("질문2")
                    .setMessage("우변을 선택하십시오")
                    .setCancelable(false)
                    .setPositiveButton("8", mClick2)
                    .setNegativeButton("9", mClick2)
                    .show();
        }
    };

    DialogInterface.OnClickListener mClick2 = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                b = 8;
            } else {
                b = 9;
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("질문3")
                    .setMessage("어떤 연산을 할까요?")
                    .setCancelable(false)
                    .setPositiveButton("곱셈", mClick3)
                    .setNegativeButton("덧셈", mClick3)
                    .show();
        }
    };

        DialogInterface.OnClickListener mClick3 = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    result = a * b;
                } else {
                    result = a + b;
                }

                TextView textView = findViewById(R.id.tv_01);
                textView.setText("연산 결과는 : " + result);
                Toast.makeText(MainActivity.this, "연산을 완료했습니다.", Toast.LENGTH_SHORT);
                a = 0;
                b = 0;
            }
    };


}
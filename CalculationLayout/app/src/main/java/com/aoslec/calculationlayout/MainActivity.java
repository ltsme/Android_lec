package com.aoslec.calculationlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btn_C;
    TextView tv_Result;
    TextView viewPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_Result = findViewById(R.id.tv_result);
        btn_C = findViewById(R.id.btn_c);

        btn_C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_Result.setText("0");
            }
        });


    }
}
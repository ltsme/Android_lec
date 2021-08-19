package com.aoslec.tablecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText edittext1, edittext2;
    Button btnplus, btnminus, btnmul, btndiv;
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("계산기");
        edittext1 = findViewById(R.id.edittext1);
        edittext2 = findViewById(R.id.edittext2);

        btnplus = findViewById(R.id.btnplus);
        btnminus = findViewById(R.id.btnminus);
        btnmul = findViewById(R.id.btnmul);
        btndiv = findViewById(R.id.btndiv);

        btnplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result = String.valueOf(Integer.parseInt(edittext1.getText().toString()) + Integer.parseInt(edittext2.getText().toString()));
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });

        btnminus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result = String.valueOf(Integer.parseInt(edittext1.getText().toString()) - Integer.parseInt(edittext2.getText().toString()));
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });

        btnmul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result = String.valueOf(Integer.parseInt(edittext1.getText().toString()) * Integer.parseInt(edittext2.getText().toString()));
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });

        btndiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result = String.valueOf(Integer.parseInt(edittext1.getText().toString()) / Integer.parseInt(edittext2.getText().toString()));
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
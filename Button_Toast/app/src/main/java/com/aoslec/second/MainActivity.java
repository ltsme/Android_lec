package com.aoslec.second;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.Red);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "빨간 버튼을 누르다!" ,Toast.LENGTH_SHORT).show();
            }
        });
        button = findViewById(R.id.Green);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "녹색 버튼을 누르다!" ,Toast.LENGTH_SHORT).show();
            }
        });
        button = findViewById(R.id.Blue);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "파란 버튼을 누르다!" ,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
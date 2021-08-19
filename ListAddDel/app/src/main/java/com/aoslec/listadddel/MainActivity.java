package com.aoslec.listadddel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ConstraintLayout cl;
    TextView tv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv_01);
        tv.setOnClickListener(onClickListener);
        tv.setOnLongClickListener(onLongClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "OnClick()", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(MainActivity.this, "OnLongClick()", Toast.LENGTH_SHORT).show();
            return true;
        }
    };


}
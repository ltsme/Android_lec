package com.aoslec.frame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    Button button;
    ImageView imageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        imageview = findViewById(R.id.imagedog);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageview.getVisibility() == v.VISIBLE){
                    imageview.setVisibility(v.INVISIBLE);
                }else{
                    imageview.setVisibility(v.VISIBLE);
                }
            }
        });
    }
}
package com.example.nam.healthforyou.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.nam.healthforyou.R;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imageView = (ImageView)findViewById(R.id.cardia);
        imageView.setImageResource(R.drawable.cardiogram);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        },1000);
    }
}

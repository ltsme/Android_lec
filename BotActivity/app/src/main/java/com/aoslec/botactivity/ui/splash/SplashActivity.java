package com.aoslec.botactivity.ui.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.aoslec.botactivity.MainActivity;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(3000);

        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
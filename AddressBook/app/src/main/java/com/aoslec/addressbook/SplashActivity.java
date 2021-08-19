package com.aoslec.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.aoslec.addressbook.MainActivity;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(2000);
            // 다음에 넘어갈 액티비티 지정
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("state", "launch");
            startActivity(intent);
            finish();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
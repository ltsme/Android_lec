package com.aoslec.callother;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }



    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()){
                case R.id.web:
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
                    startActivity(intent);
                    break;
                case R.id.dial:
                    intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:010-1111-2222"));
                    startActivity(intent);
                    break;
                case R.id.list:
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.aoslec.listadddel","com.aoslec.listadddel.MainActivity"));
            }
        }
    }
}
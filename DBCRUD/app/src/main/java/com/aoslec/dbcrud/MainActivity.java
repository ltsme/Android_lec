package com.aoslec.dbcrud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        switch (v.){
            case R.id.btn_insert:
                break;

            case R.id.btn_update:
                break;
            case R.id.btn_delete:
                break;
            case R.id.btn_selectA:
                intent = new Intent(MainActivity.this, SelectAllActivity.class);
                intent.putExtra("macIP", tempIp);
                startActivity();
                break;
        }
    }


}
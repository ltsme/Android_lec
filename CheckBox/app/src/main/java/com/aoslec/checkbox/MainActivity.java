package com.aoslec.checkbox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    CheckBox cb1, cb2, cb3, cb4;
    String str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cb1 = findViewById(R.id.ch_01);
        cb2 = findViewById(R.id.ch_02);
        cb3 = findViewById(R.id.ch_03);
        cb4 = findViewById(R.id.ch_04);

        cb1.setOnCheckedChangeListener(checkChangeListener);
        cb2.setOnCheckedChangeListener(checkChangeListener);
        cb3.setOnCheckedChangeListener(checkChangeListener);
        cb4.setOnCheckedChangeListener(checkChangeListener);

    } // onCreate

    CompoundButton.OnCheckedChangeListener checkChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            String str = "";


            switch (buttonView.getId()){
                case R.id.ch_01:
                    if(isChecked == true){
                        str = "운동 ";
                    }else{
                        str = "";
                    }
                    break;
                case R.id.ch_02:
                    if(isChecked == true){
                        str = "요리 ";
                    }else{
                        str = "";
                    }
                    break;
                case R.id.ch_03:
                    if(isChecked == true){
                        str = "독서 ";
                    }else{
                        str = "";
                    }
                    break;
                case R.id.ch_04:
                    if(isChecked == true){
                        str = "여행 ";
                    }else{
                        str = "";
                    }
                    break;
            }
            Toast.makeText(MainActivity.this, str + "is checked", Toast.LENGTH_SHORT).show();
        }
    };
} // MainActivity
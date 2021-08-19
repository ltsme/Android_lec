package com.aoslec.quiz10;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    CheckBox checkBox;
    LinearLayout linearLayout;
    RadioGroup radioGroup;

    Button button;
    FrameLayout frameLayout;
    ImageView img1, img2, img3;

    String str = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkBox = findViewById(R.id.cb_01);
        linearLayout = findViewById(R.id.ll_01);
        radioGroup = findViewById(R.id.rg_01);

        button = findViewById(R.id.btn_01);
        frameLayout = findViewById(R.id.fl_01);
        img1 = findViewById(R.id.img_01);
        img2 = findViewById(R.id.img_02);
        img3 = findViewById(R.id.img_03);

        checkBox.setOnCheckedChangeListener(checkChageListener);
        radioGroup.setOnCheckedChangeListener(selectChageListener);
        button.setOnClickListener(clickListener);





    } // onCreate

    CompoundButton.OnCheckedChangeListener checkChageListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked == true){
                linearLayout.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.VISIBLE);
            }else {
                linearLayout.setVisibility(View.INVISIBLE);
                frameLayout.setVisibility(View.INVISIBLE);
            }
        }
    };

    RadioGroup.OnCheckedChangeListener selectChageListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.rb_01:
                    str = "puppy";
                    break;
                case R.id.rb_02:
                    str = "kitten";
                    break;
                case R.id.rb_03:
                    str = "rabbit";
                    break;
            }
        }
    };

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (str){
                case "puppy":
                    img1.setVisibility(View.VISIBLE);
                    img2.setVisibility(View.INVISIBLE);
                    img3.setVisibility(View.INVISIBLE);
                    break;
                case "kitten":
                    img1.setVisibility(View.INVISIBLE);
                    img2.setVisibility(View.VISIBLE);
                    img3.setVisibility(View.INVISIBLE);
                    break;
                case "rabbit":
                    img1.setVisibility(View.INVISIBLE);
                    img2.setVisibility(View.INVISIBLE);
                    img3.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };


} // MainActivity
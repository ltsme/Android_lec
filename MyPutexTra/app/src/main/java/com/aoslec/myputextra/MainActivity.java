package com.aoslec.myputextra;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final static int RValue = 0;
    TextView textView = null;
    Button button = null;
    Button button2 = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.tv_main);
        button = findViewById(R.id.gosecondbtn);
        button2 = findViewById(R.id.gothirdbtn);

        button.setOnClickListener(onClickListener);
        button2.setOnClickListener(onClickListener);


    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.gosecondbtn:
                    intent = new Intent(MainActivity.this, SecondActivity.class);
                    intent.putExtra("userid", "root");
                    intent.putExtra("passwd", 1111);

                    startActivity(intent);
                    break;

                case R.id.gothirdbtn:
                    intent = new Intent(MainActivity.this, ThirdActivity.class);
                    intent.putExtra("userid", "admin");
                    intent.putExtra("passwd", 2222);
                    startActivityForResult(intent, RValue);
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case MainActivity.RValue:
                textView.setText("Return Value : " + data.getStringExtra("result"));
                break;
            default:
                break;
        }
    }
}
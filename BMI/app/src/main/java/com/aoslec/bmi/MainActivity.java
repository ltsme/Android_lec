package com.aoslec.bmi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // 멤버 변수 선언
    RadioGroup radioGroup;
    RadioButton rbtnMan, rbtnWoman;
    EditText etextAge, etextHeight, etextWeight;
    TextView textResultnum, textResultstr;
    Button btnReset, btnResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위젯을 변수에 저장
        rbtnMan = findViewById(R.id.rbtnMan);
        rbtnWoman = findViewById(R.id.rbtnWoman);
        etextAge = findViewById(R.id.etextAge);
        etextHeight = findViewById(R.id.etextheight);
        etextWeight = findViewById(R.id.etextweight);
        textResultnum = findViewById(R.id.textresult);
        textResultstr = findViewById(R.id.textresultstr);
        btnReset = findViewById(R.id.btnreset);
        btnResult = findViewById(R.id.btnresult);
        radioGroup = findViewById(R.id.rg01);

        btnResult.setOnClickListener(onBtnResult);
        btnReset.setOnClickListener(onbtnreset);
    }

    // 결과 보여주는 메소드
    View.OnClickListener onBtnResult = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            double height = Integer.parseInt(etextHeight.getText().toString());
            double weight = Integer.parseInt(etextWeight.getText().toString());
            double result = weight / height / height * 10000;
            String strNum = Double.toString(result);
            strNum = String.format("%.2f", result);
            textResultnum.setText(strNum);

            if(result >=30){
                textResultstr.setText("고도비만");
            }else if(result >=25 && result <30 ){
                textResultstr.setText("비만");
            }else if(result >=23 && result <25 ){
                textResultstr.setText("과체중");
            }else if(result >=18.5 && result <23 ){
                textResultstr.setText("정상");
            }else{
                textResultstr.setText("저체중");
            }
        }
    };

    View.OnClickListener onbtnreset = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            etextAge.setText("");
            etextHeight.setText("");
            etextWeight.setText("");
            textResultnum.setText("");
            textResultstr.setText("");
            radioGroup.clearCheck();
        }
    };

}
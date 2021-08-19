package com.aoslec.datetime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Chronometer;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

public class MainActivity extends AppCompatActivity {
    Chronometer chronometer;
    Button btnStart, btnEnd;
    RadioButton rdoCal, rdoTime;
    CalendarView calendarView;
    TimePicker timePicker;
    TextView textView01, textView02,textView03,textView04,textView05;

    int selectYear, selectMonth, selectDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("시간 예약");

        //버튼
        btnStart = findViewById(R.id.btnstart);
        btnEnd = findViewById(R.id.btnend);

        //크로노미터
        chronometer = findViewById(R.id.chronometer01);

        //라디오 버튼
        rdoCal = findViewById(R.id.rdocal);
        rdoTime = findViewById(R.id.rdotime);

        //Framelayout 안의 2개의 위젯
        timePicker = findViewById(R.id.timepicker01);
        calendarView = findViewById(R.id.calenderview01);

        //TextView 안의 년,월,일,시,분
        textView01 = findViewById(R.id.textviewyear);
        textView02 = findViewById(R.id.textviewmonth);
        textView03 = findViewById(R.id.textviewday);
        textView04 = findViewById(R.id.textviewhour);
        textView05 = findViewById(R.id.textviewminute);

        //처음에 안보이게 설정
        timePicker.setVisibility(View.INVISIBLE);
        calendarView.setVisibility(View.INVISIBLE);

        //버튼 클릭 메소드
        rdoCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker.setVisibility(View.INVISIBLE);
                calendarView.setVisibility(View.VISIBLE);
            }
        });

        rdoTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker.setVisibility(View.VISIBLE);
                calendarView.setVisibility(View.INVISIBLE);
            }
        });

        //타이머 설정
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.currentThreadTimeMillis());
                chronometer.start();
                chronometer.setTextColor(Color.RED);
            }
        });

        //캘린더 선택
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectYear = year;
                selectMonth = month + 1;
                selectDay = dayOfMonth;
            }
        });

        // 예약 완료 버튼
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.stop();
                chronometer.setTextColor(Color.BLUE);

                textView01.setText(Integer.toString(selectYear));
                textView02.setText(Integer.toString(selectMonth));
                textView03.setText(Integer.toString(selectDay));

                textView04.setText(Integer.toString(timePicker.getCurrentHour()));
                textView05.setText(Integer.toString(timePicker.getCurrentMinute()));
            }
        });

    }// onCreate

}//MainActivity
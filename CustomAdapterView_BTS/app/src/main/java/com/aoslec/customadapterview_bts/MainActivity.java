package com.aoslec.customadapterview_bts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Bts> data = null;
    private BtsAdapter adapter = null;
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Data 만들기
        data = new ArrayList<Bts>();
        data.add(new Bts("1",R.drawable.rm1,"RM"));
        data.add(new Bts("2",R.drawable.jin2,"진"));
        data.add(new Bts("3",R.drawable.sugar3,"슈가"));
        data.add(new Bts("4",R.drawable.jhob4,"제이홉"));
        data.add(new Bts("5",R.drawable.jimin5,"지민"));
        data.add(new Bts("6",R.drawable.vi6,"뷔"));
        data.add(new Bts("7",R.drawable.jeongkook7,"정국"));


        // Adapter 연결
        adapter = new BtsAdapter(MainActivity.this, R.layout.custom_layout, data);

        // ListView
        listView = findViewById(R.id.lv_bts);
        listView.setAdapter(adapter);

    }
}
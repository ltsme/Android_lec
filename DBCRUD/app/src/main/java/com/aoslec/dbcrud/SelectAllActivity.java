package com.aoslec.dbcrud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.aoslec.dbcrud.Bean.Student;
import com.aoslec.dbcrud.NetworkTask.NetworkTask;
import com.aoslec.dbcrud.StudentAdapter;

import java.util.ArrayList;

public class SelectAllActivity extends AppCompatActivity {

    String urlAddr = null;
    ArrayList<Student> members;
    StudentAdapter adapter;
    ListView listView;
    String macIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_all);

        listView = findViewById(R.id.lv_student);

        Intent intent = getIntent();
        macIP = intent.getStringExtra("macIP");
        urlAddr = "http://" + macIP + ":8080/test/student_query_all.jsp";
        connectGetData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectGetData();
    }

    public void connectGetData(){
        try{
            NetworkTask networkTask = new NetworkTask(SelectAllActivity.this, urlAddr, "select");
            Object obj = networkTask.execute().get();
            members = (ArrayList<Student>) obj;

            adapter = new StudentAdapter(SelectAllActivity.this, R.layout.student_layout, );
            listView.setAdapter(adapter);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
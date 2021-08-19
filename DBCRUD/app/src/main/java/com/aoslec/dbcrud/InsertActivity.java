package com.aoslec.dbcrud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aoslec.dbcrud.NetworkTask.NetworkTask;

public class InsertActivity extends AppCompatActivity {

    String urlAddr = null;
    String scode, sname, sdept, sphone, macIP;

    EditText ecode, ename, edept, ephone;
    Button btnInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        Intent intent = getIntent();
        macIP = intent.getStringExtra("macIP");
        urlAddr = "http://" + macIP + ":8080/test/studentInsertReturn.jsp?";

        ecode = findViewById(R.id.insert_code);
        ename = findViewById(R.id.insert_name);
        edept = findViewById(R.id.insert_dept);
        ephone = findViewById(R.id.insert_phone);

        //입력 시 자릿수 제한
        ecode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        ename.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        edept.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        ephone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});

        btnInsert = findViewById(R.id.insert_btn);
        btnInsert.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            scode = ecode.getText().toString();
            sname = ename.getText().toString();
            sdept = edept.getText().toString();
            sphone = ephone.getText().toString();

            urlAddr = urlAddr + "code=" + scode + "&name=" + sname + "&dept=" + sdept + "&phone" + sphone;
            String result = connectInsertDate();
            if(result.equals("1")){
                Toast.makeText(InsertActivity.this, scode+"가 입력되었습니다.", Toast.LENGTH_SHORT);
            }else{
                Toast.makeText(InsertActivity.this, "입력이 실패 되었습니다.", Toast.LENGTH_SHORT);
            }
            finish();
        }
    };

    private String connectInsertDate(){
        String result = null;
        try{
            NetworkTask networkTask = new NetworkTask(InsertActivity.this, urlAddr, "insert");
            Object obj = networkTask.execute().get();
            result = (String) obj;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

}
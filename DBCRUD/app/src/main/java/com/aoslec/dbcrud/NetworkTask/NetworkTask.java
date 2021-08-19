package com.aoslec.dbcrud.NetworkTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.aoslec.dbcrud.Bean.Student;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NetworkTask extends AsyncTask<Integer, String, Object> {
    Context context = null;
    String mAddr = null;
    ProgressDialog progressDialog = null;
    ArrayList<Student> members;

    // Network Task를 검색, 입력, 수정, 삭제 구분 없이 하나로 사용하기 위해 생성자 변수를 추가
    String where = null;

    public NetworkTask(Context context, String mAddr, String where) {
        this.context = context;
        this.mAddr = mAddr;
        this.where = where;

        this.members = members;
        this.members = new ArrayList<Student>();
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Dialog");
        progressDialog.setMessage("sdfsdf");

    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        progressDialog.dismiss();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        progressDialog.dismiss();
    }

    @Override
    protected Object doInBackground(Integer... integers) {
        StringBuffer stringBuffer = new StringBuffer();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String result = null;

        try{
            URL url = new URL(mAddr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10000);
            if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                while(true){
                    String strline = bufferedReader.readLine();
                    if(strline == null) break;
                    stringBuffer.append(strline + "\n");
                }

                //있는 JSON을 불러오는게 아니라, 새로 만들어 낸다.
                if(where.equals("select")){
                    parserSelect(stringBuffer.toString());
                }else{
                    result = parserAction(stringBuffer.toString());
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(bufferedReader != null) bufferedReader.close(); // 뭔가 들어있으면 지운다.
                if(inputStreamReader != null) inputStreamReader.close();
                if(inputStream != null) inputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(where.equals("select")){
            return members;
        }else{
            return result;
        }
    }// doInBackground


    private String parserAction(String str){
        String returnValue = null;
        try{
            JSONObject jsonObject = new JSONObject(str);
            returnValue = jsonObject.getString("result");
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnValue;
    }

    private void parserSelect(String str){
        try {
            JSONObject jsonObject = new JSONObject(str);
            JSONArray jsonArray = new JSONArray(jsonObject.getString("students_info"));
            members.clear();

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                String code = jsonObject1.getString("code");
                String name = jsonObject1.getString("name");
                String dept = jsonObject1.getString("dept");
                String phone = jsonObject1.getString("phone");

                Student member = new Student(code, name, dept, phone);
                members.add(member);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}// NetworkTask

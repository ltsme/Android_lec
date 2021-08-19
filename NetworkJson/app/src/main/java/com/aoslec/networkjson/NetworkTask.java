package com.aoslec.networkjson;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

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
    ArrayList<JsonMember> members;

    public NetworkTask(Context context, String mAddr){
        this.context = context;
        this.mAddr = mAddr;
        this.members = new ArrayList<JsonMember>();
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Dialog");
        progressDialog.setMessage("down ...");
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        progressDialog.dismiss();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Object doInBackground(Integer... integers) {
        StringBuffer stringBuffer = new StringBuffer();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try{
            URL url = new URL(mAddr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10000);

            if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                while (true){
                    String strline = bufferedReader.readLine();
                    if(strline == null) break;
                    stringBuffer.append(strline +"\n");
                }
                parser(stringBuffer.toString());
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(bufferedReader != null) bufferedReader.close();
                if(inputStreamReader != null) inputStreamReader.close();
                if (inputStream != null) inputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    return  members;

    }

    private void parser(String str){
        try{
            JSONObject jsonObject = new JSONObject(str);
            JSONArray jsonArray = new JSONArray(jsonObject.getString("members_info"));
            members.clear();

            for (int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                String name = jsonObject1.getString("name");
                int age = jsonObject1.getInt("age");
                ArrayList<String> hobbies = new ArrayList<String>();
                JSONArray jsonArray1 = jsonObject1.getJSONArray("hobbies");
                for(int j=0; j<jsonArray1.length(); j++){
                    String hobby = jsonArray1.getString(j);
                    hobbies.add(hobby);
                }
                JSONObject jsonObject2 = jsonObject1.getJSONObject("info");
                int no = jsonObject2.getInt("no");
                String id = jsonObject2.getString("id");
                String pw = jsonObject2.getString("pw");

                JsonMember member = new JsonMember(name, age, hobbies, no, id, pw);
                members.add(member);
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }


} // End ....

package com.example.nam.healthforyou.component;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static com.example.nam.healthforyou.view.Login.msCookieManager;

/**
 * Created by NAM on 2017-07-29.
 */

public class Syncdbservice extends Service{
    DBhelper dbManager;
    int bpm;
    int res;
    String time;
    HttpURLConnection con;
    Context mContext;
    int id;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        id=startId;
        dbManager = new DBhelper(mContext, "healthforyou.db", null, 1);//DB생성

        List<JSONObject> notsynced_list = dbManager.getAllinfo();
        System.out.println(notsynced_list+"notsynced");
        if(notsynced_list.size()>0)
        {
            JSONArray jsonArray = new JSONArray();//연동이 되지 않은 데이터
            for(int i=0;i<notsynced_list.size();i++)
            {
                jsonArray.put(notsynced_list.get(i));///jsonArray에 list에서 JSON object를 꺼낸 후 넣어줌
            }
            syncdbtoserverTask syncdbtoserverTask = new syncdbtoserverTask();
            syncdbtoserverTask.execute(jsonArray.toString());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class syncdbtoserverTask extends AsyncTask<String,String,String>
    {
        DBhelper dbhelper = new DBhelper(mContext, "healthforyou.db", null, 1);
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s);
            if(s.equals("true"))
            {
                System.out.println("서비스 실행");
                dbhelper.update("UPDATE User_health SET is_synced = 1 WHERE is_synced = 0;");//데이터를 보낸 후 SQlite is_synced 수정
                dbhelper.close();
                stopSelf(id);//서비스 종료
            }else{

            }
        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/synclocaltoserver.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);
                //쿠키매니저에 저장되어있는 세션 쿠키를 사용하여 통신
                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                    con.setRequestProperty("Cookie", TextUtils.join(",",msCookieManager.getCookieStore().getCookies()));
                    System.out.println(msCookieManager.getCookieStore().getCookies()+"Request");
                }
                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

                writer.write(params[0]);

                writer.flush();
                writer.close();
                os.close();

                con.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine())!=null)
                {
                    if(sb.length()>0)
                    {
                        sb.append("\n");
                    }
                    sb.append(line);
                }

                //결과를 보여주는 부분 서버에서 true or false
                result = sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(con!=null)
                {
                    con.disconnect();
                }
            }

            return result;
        }
    }
}

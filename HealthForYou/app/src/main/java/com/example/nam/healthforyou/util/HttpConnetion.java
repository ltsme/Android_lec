package com.example.nam.healthforyou.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by NAM on 2017-07-06.
 */

public class HttpConnetion {

    public static HttpURLConnection connect(String urlAddress)
    {

        try {
            //URL을 지정해줌
            URL url = new URL(urlAddress);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            //HTTP 통신에 대한 속성을 정해줌
            con.setRequestMethod("POST");
            con.setConnectTimeout(2000);//2초
            con.setReadTimeout(2000);//2초
            con.setRequestProperty("ACCEPT-LANGUAGE","UTF-8");
            con.setDoInput(true);//request를 받겠다.
            con.setDoOutput(true);//response를 보낸다.

            return con;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}

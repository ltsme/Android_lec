package com.example.nam.healthforyou.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by NAM on 2017-07-28.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    String action;
    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            Intent startIntent = new Intent(context, Syncdbservice.class);
            context.startService(startIntent);


            Log.d("networkState","변경이 감지됨");
            Intent Service = new Intent(context, ClientSocketService.class);
            context.startService(Service);
         }

//        String status = NetworkUtil.getConnectivityStatusString(context);//인터넷의 변화를 띄워주는 부분
//        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
//        int con = NetworkUtil.getConnectivityStatus(context);///네트워크의 상태를 받아옴
//        System.out.println(con+"TYPE");
//        if (con == NetworkUtil.TYPE_WIFI || con== NetworkUtil.TYPE_MOBILE) {///인터넷에 연결된 상태이면 WIFI ? LTE ?
//            if( con == NetworkUtil.TYPE_WIFI)
//            {
//                Intent startIntent = new Intent(context, Syncdbservice.class);
//                context.startService(startIntent);
//
//            }else if(con == NetworkUtil.TYPE_MOBILE){
//                Intent startIntent = new Intent(context, Syncdbservice.class);
//                context.startService(startIntent);
//            }
//        }else{//네트워크가 연결이 안된 경우
//
//        }
    }


}

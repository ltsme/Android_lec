package com.example.nam.healthforyou.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.widget.Toast;

/**
 * Created by NAM on 2017-08-11.
 */

public class DatabaseChangedReceiver extends BroadcastReceiver {

    public static String ACTION_DATABASE_CHANGED = "com.example.nam.healthforyou.DATABASE_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
            context.sendBroadcast(new Intent("updateChat"));//Service에서 BroadCastReceiver를 호출하면 다시 알려줌 DB가 바뀌었다!!!!
            context.sendBroadcast(new Intent("updateChatroom"));//DB가 바뀌면 ChattingRoom도 바뀌므로 방송해줘야됨
            System.out.println("datbaseChanged call");
    }
}

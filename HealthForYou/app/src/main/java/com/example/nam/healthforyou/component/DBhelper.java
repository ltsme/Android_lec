package com.example.nam.healthforyou.component;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by NAM on 2017-07-22.
 */

public class DBhelper extends SQLiteOpenHelper {
    public Context mContext;
    public DBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE User_health(health_no INTEGER PRIMARY KEY AUTOINCREMENT,user_bpm INTEGER,user_res INTEGER,data_signdate TEXT,is_synced INTEGER,graph_image TEXT);");//건강데이터에 관한 로컬 DB table
        db.execSQL("CREATE TABLE User_friend(friend_no INTEGER PRIMARY KEY AUTOINCREMENT,user_friend TEXT,friendname TEXT,user_profile TEXT,user_update TEXT);");//친구목록에 대한 로컬 DB table
        db.execSQL("CREATE TABLE ChatMessage(message_no INTEGER PRIMARY KEY AUTOINCREMENT,room_type INTEGER,room_id TEXT,message_sender TEXT,senderName TEXT,message_content TEXT,message_date TEXT,is_looked INTEGER);");//채팅방에 따른 메세지 정보
        db.execSQL("CREATE TABLE GroupChat(room_no INTEGER PRIMARY KEY AUTOINCREMENT,room_id TEXT,room_member TEXT,room_name TEXT)");//그룹 채팅에 대한 방정보 생성 - 멤버만 표시
        Toast.makeText(mContext,"Table 생성완료", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
    }

    public void infoinsert(JSONObject healthInfo) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        try {
            values.put("user_bpm",healthInfo.getInt("bpm"));
            values.put("user_res",healthInfo.getInt("res"));
            values.put("data_signdate",healthInfo.getString("data_signdate"));
            values.put("is_synced",healthInfo.getInt("is_synced"));
            values.put("graph_image",healthInfo.getString("graph_image"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.insert("User_health",null,values);

    }

    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);

    }

    public void updateMessageState(String who)
    {
        SQLiteDatabase db = getWritableDatabase();
        String sql="UPDATE ChatMessage SET is_looked=1 WHERE is_looked=0 and room_id=?";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1,who);
        stmt.executeUpdateDelete();

    }

    public void delete(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);

    }

    public void updateProfile(String user_profile,String user_update,String userId)
    {
        SQLiteDatabase db = getWritableDatabase();
        String sql="UPDATE User_friend SET user_profile=?,user_update=? WHERE user_friend=?";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1,user_profile);
        stmt.bindString(2,user_update);
        stmt.bindString(3,userId);
        stmt.executeUpdateDelete();
        Log.d("DB","완료");
    }

    public String PrintData(String _query) {
        SQLiteDatabase db = getReadableDatabase();
        String str = "";

        Cursor cursor = db.rawQuery(_query, null);
        if(cursor.getCount()!=0)
        {
            while(cursor.moveToNext()) {
                str += cursor.getInt(0)
                        +":"
                        + cursor.getInt(1)
                        +":"
                        + cursor.getInt(2)
                        +":"
                        + cursor.getString(3)
                        +":"
                        + cursor.getInt(4)
                        + "\n";
            }
            cursor.close();
        }else{
          str="false";//저장된 자료가 없음
        }

        return str;
    }

    public int PrintCountData() {
        SQLiteDatabase db = getReadableDatabase();
        String str="";

        Cursor cursor = db.rawQuery("SELECT health_no FROM User_health;", null);

            while(cursor.moveToNext()) {
                str += cursor.getInt(0)
                        + "\n";
            }

        cursor.close();

        return cursor.getCount();
    }


    public List<JSONObject> PrintAvgData(String _query) {
        SQLiteDatabase db = getReadableDatabase();
        List<JSONObject> healthInfos = new ArrayList<>();

        Cursor cursor = db.rawQuery(_query, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject healthInfo =new JSONObject();
                try {
                    healthInfo.put("data_signdate",cursor.getString(0));
                    healthInfo.put("user_bpm",(cursor.getInt(1)));
                    healthInfo.put("user_res",(cursor.getInt(2)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                healthInfos.add(healthInfo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return healthInfos;
    }

    public JSONObject PrintMyAvgData(String _query) {/////나의 심박수 평균과 호흡 수 평균 데이터
        SQLiteDatabase db = getReadableDatabase();
        JSONObject healthInfo =new JSONObject();
        Cursor cursor = db.rawQuery(_query, null);
        if (cursor.moveToFirst()) {
            do {

                try {
                    healthInfo.put("user_bpm",(cursor.getInt(0)));
                    healthInfo.put("user_res",(cursor.getInt(1)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        return healthInfo;
    }

    public ArrayList<JSONObject> PrintMyAvgDataForChat(String _query) {/////나의 심박수 평균과 호흡 수 평균 데이터
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<JSONObject> healthInfoGridArray = new ArrayList<>();
        Cursor cursor = db.rawQuery(_query, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject healthInfo =new JSONObject();
                try {
                    healthInfo.put("user_bpm",(cursor.getInt(0)));
                    healthInfo.put("user_res",(cursor.getInt(1)));
                    healthInfo.put("data_signdate",(cursor.getString(2)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                healthInfoGridArray.add(healthInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return healthInfoGridArray;
    }

    public JSONObject PrintHealthData(String _query) {
        SQLiteDatabase db = getReadableDatabase();
        JSONObject healthInfo =new JSONObject();
        Cursor cursor = db.rawQuery(_query, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    healthInfo.put("user_bpm",(cursor.getInt(1)));
                    healthInfo.put("user_res",(cursor.getInt(2)));
                    healthInfo.put("data_signdate",cursor.getString(3));
                    healthInfo.put("is_synced",cursor.getInt(4));
                    healthInfo.put("graph_image",cursor.getString(5));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        return healthInfo;
    }

    public JSONObject PrintHealthChatdata(String _query) {///채팅용 DB 긁어오기
        SQLiteDatabase db = getReadableDatabase();
        JSONObject healthInfo =new JSONObject();
        Cursor cursor = db.rawQuery(_query, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    healthInfo.put("user_bpm",(cursor.getInt(1)));
                    healthInfo.put("user_res",(cursor.getInt(2)));
                    healthInfo.put("data_signdate",cursor.getString(3));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        return healthInfo;
    }

    public JSONObject PrintHealthChatdata_forgrid(String _query) {///채팅용 DB 긁어오기
        SQLiteDatabase db = getReadableDatabase();
        JSONObject healthInfo =new JSONObject();
        Cursor cursor = db.rawQuery(_query, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    healthInfo.put("user_bpm",(cursor.getInt(0)));
                    healthInfo.put("user_res",(cursor.getInt(1)));
                    healthInfo.put("data_signdate",(cursor.getString(2)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        return healthInfo;
    }

    public List<JSONObject> getAllinfo() {
        List<JSONObject> healthInfos = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM User_health WHERE is_synced=0 ORDER BY data_signdate desc";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONObject healthInfo = new JSONObject();
                try {
                    healthInfo.put("user_bpm",(cursor.getInt(1)));
                    healthInfo.put("user_res",(cursor.getInt(2)));
                    healthInfo.put("data_signdate",cursor.getString(3));
                    healthInfo.put("is_synced",cursor.getInt(4));
                    healthInfo.put("graph_image",cursor.getString(5));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Adding contact to list
                healthInfos.add(healthInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // 모든 healdata를 갖고옴
        return healthInfos;
    }

    public void friendinsert(JSONObject friendinfo) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        try {
            values.put("user_friend",friendinfo.getString("user_friend"));
            values.put("friendname",friendinfo.getString("user_name"));
            values.put("user_profile",friendinfo.getString("user_profile"));
            values.put("user_update",friendinfo.getString("user_update"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.insert("User_friend",null,values);

    }

    public List<JSONObject> getAllfriend()
    {
        List<JSONObject> friendlist = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM User_friend ORDER BY friendname ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONObject friend = new JSONObject();
                try {
                    friend.put("user_friend",(cursor.getString(1)));
                    friend.put("user_name",(cursor.getString(2)));
                    try{
                        friend.put("user_profile",(cursor.getString(3)));
                        friend.put("user_update",(cursor.getString(4)));
                    }catch(NullPointerException e)
                    {
                        friend.put("user_profile","0");
                        friend.put("user_update","0");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Adding contact to list
                friendlist.add(friend);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // 모든 healdata를 갖고옴
        return friendlist;
    }

    public JSONObject getnewfriend()
    {
        // Select All Query
        String selectQuery = "SELECT * FROM User_friend ORDER BY friend_no DESC limit 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        JSONObject newfriend = new JSONObject();
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    newfriend.put("user_friend",(cursor.getString(1)));
                    newfriend.put("user_name",(cursor.getString(2)));
                    newfriend.put("user_profile",(cursor.getString(3)));
                    newfriend.put("user_update",(cursor.getString(4)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        // 모든 healdata를 갖고옴
        return newfriend;
    }

    public JSONObject getFriend(String friendid)
    {
        // Select All Query
        String selectQuery = "SELECT * FROM User_friend WHERE user_friend= '" + friendid + "';";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        JSONObject friendinfo = new JSONObject();
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    friendinfo.put("user_friend",(cursor.getString(1)));
                    friendinfo.put("user_name",(cursor.getString(2)));
                    friendinfo.put("user_profile",(cursor.getString(3)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        // 모든 healdata를 갖고옴
        return friendinfo;
    }

    public int PrintCountfriend() {
        SQLiteDatabase db = getReadableDatabase();
        String str="";

        Cursor cursor = db.rawQuery("SELECT friend_no FROM User_friend;", null);

        while(cursor.moveToNext()) {
            str += cursor.getInt(0)
                    + "\n";
        }
        cursor.close();


        return cursor.getCount();
    }

    ////메세지를 등록 - 보내는 형식부터 JSON으로 할것
    public void messagejsoninsert(JSONObject jsonMessage) {
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("DB insert : "+jsonMessage.toString());
        /////전송된 데이터를 구분자를 통해 분리함

        //분리한 데이터를 SQlite에 저장 - 다른 사람이 보낸 메세지 타입

        ContentValues values = new ContentValues();
        /////메세지에 들어있는 정보를 분류해야함
        /////심박데이터가 있는 판단해야됨
        try {
            if(jsonMessage.getString("command").equals("/to")||jsonMessage.getString("command").equals("/tohealth"))///개인간의 대화를 나타냄
            {
                if(jsonMessage.optString("from").equals("me"))///내가 보낸거면
                {
                    values.put("room_id",jsonMessage.optString("who"));//이거는 내가 보낸것에 대한 id
                    values.put("is_looked",1);///보였는지 판단 보였으면 1,안보였으면 0
                }else{
                    values.put("room_id",jsonMessage.getString("from"));////개인간의 대화는 방의 id가 상대방으로 설정
                    values.put("is_looked",0);///안보인 것
                }
                values.put("message_sender",jsonMessage.getString("from"));//보낸 사람이 누구인지
                values.put("message_content",jsonMessage.getString("message"));///메세지의 내용
                values.put("message_date",jsonMessage.getString("date"));///메세지를 보낸 시간
                values.put("senderName",jsonMessage.getString("name"));//보낸 사람의 이름
                values.put("room_type",0);///개인간의 대화인지 그룹간의 대화인지 판단 0 - 개인, 1 - 그룹

            }else if(jsonMessage.getString("command").equals("/inform")||jsonMessage.getString("command").equals("/informhealth")){///그룹채팅을 의미 방번호가 오게됨

                values.put("room_id",jsonMessage.getString("room_no"));////그룹간의 대화는 방의 id가 방고유번호 - 서버 RoomManager가 부여
                values.put("message_sender",jsonMessage.getString("from"));////보낸 사람이 누구인지
                values.put("message_content",jsonMessage.getString("message"));
                values.put("message_date",jsonMessage.getString("date"));
                values.put("senderName",jsonMessage.getString("name"));//보낸 사람의 이름
                if(jsonMessage.optString("from").equals("me"))//구분하는 이유 내가 적은거는 나한테 바로 보임
                {
                    values.put("is_looked",1);///보였는지 판단 보였으면 1,안보였으면 0
                }else{
                    values.put("is_looked",0);///보였는지 판단 보였으면 1,안보였으면 0
                }
                values.put("room_type",1);///개인간의 대화인지 그룹간의 대화인지 판단 0 - 개인, 1 - 그룹
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.insert("ChatMessage",null,values);
    }

    public JSONObject updatemessage(String _query){
        SQLiteDatabase db = getReadableDatabase();

        JSONObject message =new JSONObject();
        Cursor cursor = db.rawQuery(_query, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    message.put("room_type",(cursor.getString(1)));
                    message.put("room_id",cursor.getString(2));
                    message.put("message_sender",cursor.getString(3));
                    message.put("senderName",(cursor.getString(4)));
                    message.put("message_content",(cursor.getString(5)));
                    message.put("message_date",cursor.getString(6));
                    message.put("is_looked",cursor.getInt(7));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return message;
    }

    public ArrayList<JSONObject> getAllmessage(String _query){

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<JSONObject> allmessage = new ArrayList<>();

        Cursor cursor = db.rawQuery(_query, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONObject message =new JSONObject();
                try {
                    message.put("room_type",(cursor.getString(1)));
                    message.put("room_id",(cursor.getString(2)));
                    message.put("message_sender",(cursor.getString(3)));
                    message.put("senderName",(cursor.getString(4)));
                    message.put("message_content",(cursor.getString(5)));
                    message.put("message_date",(cursor.getString(6)));
                    message.put("is_looked",(cursor.getString(7)));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                allmessage.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return allmessage;
    }

    public ArrayList<JSONObject> getPagingMessage(String who,String position,String itemcount)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM ChatMessage WHERE room_id=? ORDER BY message_no DESC LIMIT ?,?";//
        String[] args = new String[] { who,position,itemcount };

        ArrayList<JSONObject> pagingmessage = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, args);///쿼리를 통해 불러옴
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONObject message =new JSONObject();
                try {
                    message.put("room_type",(cursor.getString(1)));
                    message.put("room_id",(cursor.getString(2)));
                    message.put("message_sender",(cursor.getString(3)));
                    message.put("senderName",(cursor.getString(4)));
                    message.put("message_content",(cursor.getString(5)));
                    message.put("message_date",(cursor.getString(6)));
                    message.put("is_looked",(cursor.getString(7)));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pagingmessage.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pagingmessage;
    }

    public int getMessageCount(String who)//누구의 메세지를 조회 할것인지
    {
        SQLiteDatabase db = getReadableDatabase();


        String sql = "SELECT COUNT(message_no) FROM ChatMessage WHERE room_id=?";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1,who);
        long result = stmt.simpleQueryForLong();

        return (int)result;
    }

    public List<JSONObject> getChatroomList(String _query)//메세지를 기준으로 방을 나누고 DB에서 긁어오는 메소드
    {
        SQLiteDatabase db = getReadableDatabase();
        List<JSONObject> chatroomList = new ArrayList<>();

        Cursor cursor = db.rawQuery(_query, null);

        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                JSONObject chatList = new JSONObject();
                try {
                    chatList.put("room_type",(cursor.getInt(1)));
                    chatList.put("room_id",(cursor.getString(2)));
                    chatList.put("message_sender",(cursor.getString(3)));
                    chatList.put("senderName",(cursor.getString(4)));
                    chatList.put("message_content",(cursor.getString(5)));
                    chatList.put("message_date",(cursor.getString(6)));
                    chatList.put("is_looked",(cursor.getString(7)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Adding contact to list
                chatroomList.add(chatList);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // 모든 healdata를 갖고옴

        return chatroomList;
    }

    ////메세지를 등록 - 보내는 형식부터 JSON으로 할것
    public void makeRoominsert(JSONObject roomInfo) {
        SQLiteDatabase db = getWritableDatabase();
        String[] memberList=null;
        System.out.println("DB insert : "+roomInfo.toString());
        /////전송된 데이터를 구분자를 통해 분리함
        try {
            memberList = roomInfo.getString("who_receive").split(":");///친구들을 정보를 가지고 옴
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //분리한 데이터를 SQlite에 저장 - 다른 사람이 보낸 메세지 타입

        ContentValues values = new ContentValues();

        try {
            for(int i=0;i<memberList.length;i++)
            {
                values.put("room_id",roomInfo.getString("room_no"));
                values.put("room_member",memberList[i]);
                values.put("room_name",roomInfo.optString("room_name"));
                db.insert("GroupChat",null,values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getRoominfo(String room_no)
    {
        // Select All Query
        String selectQuery = "SELECT * FROM GroupChat GROUP by room_id HAVING room_id= '" + room_no + "';";
        String room_name=null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                room_name = cursor.getString(3);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return room_name;
    }

    public List<String> getYearofHealthdata()
    {
        String selectQuery = "SELECT strftime('%Y',data_signdate) as date from User_health GROUP BY strftime('%Y',data_signdate) ORDER BY date desc;";
        List<String> year=new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                year.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return year;
    }

    public List<String> getMonthofHealthdata(String year)
    {
        year=year+"-01-01";
        String selectQuery = "SELECT substr(strftime('%Y-%m-%d',data_signdate),6,2) as month,substr(strftime('%Y-%m-%d',data_signdate),1,4) as year from User_health WHERE year = strftime('%Y','" + year + "') GROUP BY strftime('%Y-%m',data_signdate) ORDER BY month ASC;";
        List<String> month=new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                month.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return month;
    }

}

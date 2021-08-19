package com.aoslec.memberinfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MemberInfo extends SQLiteOpenHelper {

    public MemberInfo(Context context){ // 애는 자기가 가진 화면이 없기때문에 처리된 결과를 여기서 선언한 컨텍스트로 가져와. 라는 뜻
        super(context, "MemberInfo.db", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {       // 이메소드는 처음 실행할때만 실행한다. 왜냐면 테이블 있으면 실행안되니까.
        String query = "CREATE TABLE member(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, userid TEXT, passwd INTEGER)";
        db.execSQL(query);// 쿼리만들었으니까 실행해라

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS member";
        db.execSQL(query);
        onCreate(db);
    }
}

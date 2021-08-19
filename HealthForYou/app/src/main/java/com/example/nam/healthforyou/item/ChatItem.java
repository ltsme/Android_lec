package com.example.nam.healthforyou.item;

/**
 * Created by NAM on 2017-08-08.
 */

public class ChatItem {

    private int type ;///내가 보낸 카톡과 아닌것을 구분하기 위함

    public String item_sender;
    public String item_senderId;
    public String item_content;//내용
    public String item_date;//보낸 날짜
    String item_senderName;

    public int user_bpm;//건강 데이터를 보내기 위한 부분
    public int user_res;
    public String data_signdate;//측정한 날짜

    /////리스트뷰 타입을 분류
    public void setType(int type) { this.type = type ; }
    public int getType() { return this.type ; }




}

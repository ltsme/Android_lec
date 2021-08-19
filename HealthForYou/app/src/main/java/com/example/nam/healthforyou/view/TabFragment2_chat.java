package com.example.nam.healthforyou.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.nam.healthforyou.item.Chatroomitem;
import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by NAM on 2017-08-04.
 */

public class TabFragment2_chat extends Fragment {
    RelativeLayout chat_list;
    ChatroomAdapter chatroomAdapter;
    ListView chatroomlv;
    DBhelper dBhelper;
    final static int UPDATE_CHATROOM = 1;
    final static int ADD_CHATROOM = 0;

    List<JSONObject> roomlist;
    List<Chatroomitem> lv_roomlist;
    //핸들러로 넘기기 위해 필요

    int roomlistno;
    int lv_roomlistno;
    boolean is_update;

    //브로드 캐스트 리시버 관련 flag
    boolean mIsReceiverRegistered;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        chat_list = (RelativeLayout)inflater.inflate(R.layout.tab_frag_chat,container,false); //친구목록을 갖고 있는 프레그먼트의 레이아웃

        dBhelper = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB 접근

        chatroomAdapter = new ChatroomAdapter();///리스트뷰 아답터 선언
        chatroomlv = (ListView)chat_list.findViewById(R.id.lv_chatlist);///채팅방 리스트뷰 선언
        chatroomlv.setAdapter(chatroomAdapter);

        chatroomlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ///개인방을 눌렀을 때와 그룹방을 눌렀을 때 채팅방에서 보내는 방법이 다름
                System.out.println(chatroomAdapter.getRoomitem(position).roomtype+" : roomtype");
                if(chatroomAdapter.getRoomitem(position).roomtype!=0)//roomtype이 1인거임 그룹채팅방
                {
                    Intent intent = new Intent(getActivity(),Chatroom.class);
                    intent.putExtra("from",2);///그룹간의 대화방을 나타내는 인텐트 + 이미 채팅방은 생성되어 있고 방한테 메세지를 바로 보낼 수 있음
                    intent.putExtra("room_id",chatroomAdapter.getRoomitem(position).room_id);///방번호를 채팅방에 알려줌
                    intent.putExtra("room_name",chatroomAdapter.getRoomitem(position).room_name);
                    startActivity(intent);
                }else{///개인 채팅방임
                    Intent intent = new Intent(getActivity(),Chatroom.class);
                    intent.putExtra("from",0);////- 개인 채팅방에서는 room_id를 기준으로 메세지를 보냄
                    intent.putExtra("who",chatroomAdapter.getRoomitem(position).room_id);///인텐트를 통해 내가 누구한테 보내는지 채팅 액티비티로 넘겨줌
                    intent.putExtra("room_name",chatroomAdapter.getRoomitem(position).room_name);
                    startActivity(intent);
                }
            }
        });

        return chat_list;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Thread thread = new Thread(){
                @Override
                public void run() {
                    roomlist = dBhelper.getChatroomList("SELECT * from ChatMessage WHERE is_looked=0 GROUP by room_id ORDER by message_date DESC limit 1;");///보지 않은 메세지가 있는 채팅방을 업데이트?!
                    System.out.println(roomlist);
                    lv_roomlist = chatroomAdapter.getRoom();///리스뷰에 있는 모든 방들을 갖고옴
                    for(int i=0;i<roomlist.size();i++)///업데이트 할 목록 -> 새로 메세지를 받거나 하면 roomlist가 업그레이드 됨
                    {
                        for(int j=0;j<lv_roomlist.size();j++)//listview에 등록된 방을 조사
                        {
                            try {
                                if(lv_roomlist.get(j).room_id.equals(roomlist.get(i).getString("room_id")))///기존에 방이름이 같은 것이 있다면
                                {
                                    lv_roomlistno=j;
                                    roomlistno=i;
                                    is_update = true;
                                    break;//같은 방을 찾으면 더이상 찾을 필요가 없음
                                }else{///방이름 중에 같은게 없으면 추가해줌
                                    lv_roomlistno=j;
                                    roomlistno=i;
                                    is_update = false;
                                    ///찾지 못하면 계속 찾아야됨
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(is_update)
                    {
                        handler.sendEmptyMessage(UPDATE_CHATROOM);
                    }else{
                        handler.sendEmptyMessage(ADD_CHATROOM);
                    }
                }
            };

            thread.start();
        }
    };
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what)
            {
                case ADD_CHATROOM:
                    System.out.println("ADDDDD");
                    try {///방이름 정해주는 부분 - 새로운 방이 생기면 바로 이름을 반영할 수 있도록 함
                        if(roomlist.size()==0)
                        {
                            return;
                        }
                        if(roomlist.get(roomlistno).optInt("room_type")==0)///////////방의 타입이 1:1이면 친구의 이름을 통해 채팅방 리스트에 넣어줌 - 처리는 Adapter에서
                        {
                            JSONObject friendinfo=dBhelper.getFriend(roomlist.get(roomlistno).optString("room_id"));//room_id는 개인과 개인일 때는 상대방의 아이디, 그룹채팅일때는 방번호임
                            if(friendinfo.length()!=0)//친구면 친구의 이름을
                            {
                                try {
                                    roomlist.get(roomlistno).put("room_name",friendinfo.optString("user_name"));
                                    roomlist.get(roomlistno).put("is_friend",true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else{//친구가 아니면 보낸 사람의 이름을
                                try {
                                    roomlist.get(roomlistno).put("room_name",roomlist.get(roomlistno).optString("senderName"));
                                    roomlist.get(roomlistno).put("is_friend",false);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }else{//그룹 채팅이라면
                            String room_name="그룹채팅 "+roomlist.get(roomlistno).optString("room_id");
                            roomlist.get(roomlistno).put("room_name",room_name);///배열을 String으로 바꿈
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }

                    if(roomlist.size()!=0)
                    {
                        chatroomAdapter.addRoom(0,roomlist.get(roomlistno));///같은 방이 없으면 새로 추가해줌 - 최근 값으로 추가해줌
                    }else{
                        chatroomAdapter.addRoom(roomlist.get(roomlistno));///같은 방이 없으면 새로 추가해줌 - 최근 값으로 추가해줌
                    }
                    chatroomAdapter.notifyDataSetChanged();
                    break;

                case UPDATE_CHATROOM:
                    System.out.println("UPDATE");
                    try {///방이름 정해주는 부분 - 같은 방에서 얘기를 나누면 이름을 그대로 바꿔주는 부분
                        if(roomlist.get(roomlistno).optInt("room_type")==0)///////////방의 타입이 1:1이면 친구의 이름을 통해 채팅방 리스트에 넣어줌 - 처리는 Adapter에서
                        {
                            JSONObject friendinfo=dBhelper.getFriend(roomlist.get(roomlistno).optString("room_id"));//room_id는 개인과 개인일 때는 상대방의 아이디, 그룹채팅일때는 방번호임
                            if(friendinfo.length()!=0)//친구면 친구의 이름을
                            {
                                try {
                                    roomlist.get(roomlistno).put("room_name",friendinfo.optString("user_name"));
                                    roomlist.get(roomlistno).put("is_friend",true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else{//친구가 아니면 보낸 사람의 이름을
                                try {
                                    roomlist.get(roomlistno).put("room_name",roomlist.get(roomlistno).optString("senderName"));
                                    roomlist.get(roomlistno).put("is_friend",false);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }else{//그룹 채팅이라면
                            String room_name="그룹채팅 "+roomlist.get(roomlistno).optString("room_id");
                            roomlist.get(roomlistno).put("room_name",room_name);///이름 설정
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    chatroomAdapter.updateRoom(lv_roomlistno,roomlist.get(roomlistno));//같은 id의 방이 있다면 UPDATE
                    chatroomAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    ////TODO registerReceiver 생명주기 고려
    @Override
    public void onResume() {
        super.onResume();
        if(!mIsReceiverRegistered){////onCreatView에 놓을 경우 화면 전환을 할 시 처음에는 되다가 안되는 경우가 생김
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter("updateChatroom"));///새로 메세지에 대한 채팅방 확인해보라는 말
            mIsReceiverRegistered = true;
        }
        Log.d("생명주기","onResumeTabfragment");
        roomlist = dBhelper.getChatroomList("SELECT * from ChatMessage GROUP by room_id ORDER by message_no DESC;");
        System.out.println(roomlist+"대화목록");

        for(int i=0;i<roomlist.size();i++)//모든 방에 대한 정보를 조사 하는 부분 + 모든 방의 이름을 정해주는 부분
        {
            try {
                if(roomlist.get(i).optInt("room_type")==0)///////////방의 타입이 1:1이면 친구의 이름을 통해 채팅방 리스트에 넣어줌 - 처리는 Adapter에서
                {
                    JSONObject friendinfo=dBhelper.getFriend(roomlist.get(i).optString("room_id"));//room_id는 개인과 개인일 때는 상대방의 아이디, 그룹채팅일때는 방번호임
                    if(friendinfo.length()!=0)//친구면 친구의 이름을
                    {
                        try {
                            roomlist.get(i).put("room_name",friendinfo.optString("user_name"));
                            roomlist.get(i).put("is_friend",true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{//친구가 아니면 보낸 사람의 이름을
                        try {
                            roomlist.get(i).put("room_name",roomlist.get(i).optString("senderName"));
                            roomlist.get(i).put("is_friend",false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    chatroomAdapter.addRoom(roomlist.get(i));
                }else{//그룹 채팅이라면
                    String room_name="그룹채팅 "+roomlist.get(i).optString("room_id");
                    System.out.println(room_name+"방이름");
                    roomlist.get(i).put("room_name",room_name);///배열을 String으로 바꿈roo
                    roomlist.get(i).put("is_friend",false);
                    chatroomAdapter.addRoom(roomlist.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        chatroomAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mIsReceiverRegistered){//flag를 통해 제어
            getActivity().unregisterReceiver(broadcastReceiver);///브로드캐스트 리시버 해제
            mIsReceiverRegistered = false;
        }
        Log.d("생명주기","onPauseTabfragment");
        chatroomAdapter.deleteRoomItem();
        roomlist.clear();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
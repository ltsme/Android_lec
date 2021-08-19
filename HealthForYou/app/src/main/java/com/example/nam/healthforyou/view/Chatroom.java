package com.example.nam.healthforyou.view;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.nam.healthforyou.component.ClientSocketService;
import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.component.NetworkChangeReceiver;
import com.example.nam.healthforyou.R;
import com.example.nam.healthforyou.item.ChatItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Chatroom extends AppCompatActivity implements AbsListView.OnScrollListener{
    //액티비티에서 선언.
    private ClientSocketService mService; //서비스 클래스
    private String who=null;//누구한테 보낼지
    ChatAdapter chatAdapter;
    ListView chatlist;
    final static int update_message=1;
    final static int sendRecentdata=2;
    final static int sendExactdata=3;
    final static int update_healthmessage=4;
    final static int all_messageUpdate=5;
    final static int initialSettingMessage=6;

    boolean dateposition=false;
    ChatItem receiveitem;
    DBhelper dBhelper;
    Context mContext;
    int room_id;
    boolean mServiceIsregistered;
    int sendtype;///채팅방의 종류
    String choose_date;///데이터를 선택할때 선택한 날짜!
    NetworkChangeReceiver networkChangeReceiver;

    ImageButton btn_sendhealthdata;//신체정보를 보내는 버튼

    //대화목록 페이징
    private final static int INSERT_COUNT = 20;//보여줄 아이템의 갯수
    private int itemCount=0;//아이템의 갯수
    private int pageCount=0;//페이지의 수를 계산해야됨

    private int currentPage=0;//현재 보여주고 있는 페이지
    private int startPage=0;//초기 페이지

    private int lastPage=pageCount-1;//보여줄수 있는 마지막 페이지
    private boolean is_loading=true;
    private boolean is_first=false;

    private int previousTotalItemCount=0;//이전에 불러온 데이터들
    private int visibleThreshold = 5;//이 이상 되면 불러오는 것
    private int callmessageCount=0;
    private int go_position=0;

    SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd",java.util.Locale.getDefault());
    SimpleDateFormat printFormat = new  SimpleDateFormat("yyyy년 M월 d일",java.util.Locale.getDefault());
    Date beforedate = null;
    Date afterdate = null;
    int timeitemCount=0;//시간에 따라 추가된 갯수
    /*
    * //채팅중인 것은  pastdata=false
    * //이전 데이터를 보는 부분은 true
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        registerReceiver(broadcastReceiver, new IntentFilter("updateChat"));///새로 온메세지를 확인해보라는 말

        //네트워크 변경감지
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);


        mContext = getApplicationContext();
        dBhelper = new DBhelper(mContext, "healthforyou.db", null, 1);///DB정의
        ///채팅 ListviewAdapter 정의
        chatAdapter = new ChatAdapter();
        ///채팅 내용에 대한 리스트뷰
        chatlist = (ListView)findViewById(R.id.chat);
        chatlist.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        chatlist.setAdapter(chatAdapter);
        chatlist.setOnScrollListener(this);

        /*DB에서
        * 1. 자료의 갯수를 통해
        * 2. 총 페이지의 갯수를 구하고
        * 3. 시작페이지와 마지막 페이지를 설정
        * */

        //EditText 정의및 포커스 시 키보드 업
        final EditText yourEditText= (EditText) findViewById(R.id.et_content);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);

        ///서비스에서 소켓을 연결하므로 여기서 서비스를 호출하면 mainThread에서 소켓을 호출하게됨
        startServcie_Thread socket_thread = new startServcie_Thread();
        socket_thread.start();
        timeitemCount=0;//시간에 따라 추가된 갯수
        ///전송버튼에 관한 로직
        final Button btn_send = (Button)findViewById(R.id.btn_health_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_message = (EditText)findViewById(R.id.et_content);
                final String message = et_message.getText().toString();

                //내가 보낸 메세지를 Listview에 추가
                ChatItem me_message = new ChatItem();
                ////시간을 나타내줌
                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // nowDate 변수에 값을 저장한다.
                final String formatDate = sdfNow.format(date);
                ///시간을 더해주기 전에 아이템에 넣어줌
                me_message.item_content=message;

                System.out.println(message+"메세지");
                System.out.println("sendtype: "+sendtype);
                System.out.println("who "+who);

                //////서비스를 통해 보내는 부분
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(sendtype==0)
                        {
                            mService.SendMessage(who,message,formatDate);///누구에게-메세지-시간
                            ///받을 때랑 저장 형식을 맞춰줌
                            JSONObject sendptopJSON = new JSONObject();
                            try {
                                sendptopJSON.put("command","/to");///서버에 보낼 명령어
                                sendptopJSON.put("from","me");///내가 보낸거임
                                sendptopJSON.put("who",who);
                                sendptopJSON.put("name","me");///나의 이름은 me
                                sendptopJSON.put("message",message);///어떤 내용인지
                                sendptopJSON.put("date",formatDate);///보낸 시간은
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dBhelper.messagejsoninsert(sendptopJSON);///JSON 형식으로 DB에 저장

                        }else if(sendtype==1)
                        {
                            mService.InfoMessage(Integer.parseInt(who),message,formatDate);/////who를 통해 보내므로 parseInt를 통해 값을 보내야됨
                            ///받을 때랑 저장 형식을 맞춰줌
                            JSONObject sendgroupJSON = new JSONObject();
                            try {
                                sendgroupJSON.put("command","/inform");///서버에 보낼 명령어
                                sendgroupJSON.put("room_no",who);
                                sendgroupJSON.put("from","me");///누가 받을 건지
                                sendgroupJSON.put("name","me");
                                sendgroupJSON.put("message",message);///어떤 내용인지
                                sendgroupJSON.put("date",formatDate);///보낸 시간은
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dBhelper.messagejsoninsert(sendgroupJSON);///JSON 형식으로 DB에 저장
                        }
                        scrollMyListViewToBottom();
                    }
                });

                thread.start();

                ///내가 보낸 메시지 타입
                me_message.setType(0);
                me_message.item_date=formatDate;
                itemCount=dBhelper.getMessageCount(who);
                System.out.println(itemCount+"아이템의 갯수");
                if(itemCount!=0)//아이템 갯수가 없다가 메세지를 보낸경우
                {
                    System.out.println("여기가 호출 아이템이 있다");
                    String rightbeforedate = chatAdapter.getItemtime(chatAdapter.getCount()-1).item_date;//지금 보내려고 하는 메세지의 날짜 이전
                    Date rightnowdate = null;
                    System.out.println(me_message.item_date+"과정1");
                    System.out.println(rightbeforedate+"과정2");
                    System.out.println(me_message.item_date.equals(rightbeforedate)+"결과");
                    //String인 날짜를 Date로 변환
                    Date previousdate=null;
                    Date nextdate=null;
                    try {
                        previousdate=dateFormat.parse(rightbeforedate);//전 메세지의 날짜
                        nextdate=dateFormat.parse(me_message.item_date);//지금 내가 보내는 메세지의 날짜
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    int dateResult=previousdate.compareTo(nextdate);
                    System.out.println(previousdate.toString()+"이전에 채팅방에 있는 날짜");
                    System.out.println(nextdate.toString()+"지금 받은 메세지");
                    System.out.println(dateResult+"비교비교날짜가 어떻게 되려나");
                    if (dateResult!=0)//지금 보내려고 하는 메세지의 날짜와 이전의 날짜가 같지않으면 날짜 변경선을 추가해줘야함
                    {
                        ChatItem chatItem = new ChatItem();
                        try {
                            rightnowdate = dateFormat.parse(me_message.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        chatItem.item_date = printFormat.format(rightnowdate);
                        chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                    }
                }
                else//아이템 갯수가 없다가 메세지를 보낸경우
                {
                    System.out.println("여기가 호출 아이템이 없다");
                    ChatItem chatItem = new ChatItem();
                    Date rightnowdate = null;
                    try {
                        rightnowdate=dateFormat.parse(me_message.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    chatItem.item_date = printFormat.format(rightnowdate);
                    chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                }

                chatAdapter.addItemME(me_message);
                chatAdapter.notifyDataSetChanged();

                et_message.setText("");
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0){
                    btn_send.setEnabled(true);
                    btn_send.setClickable(true);
                    btn_send.setFocusable(true);
                }
                else{
                    btn_send.setEnabled(false);
                    btn_send.setClickable(false);
                    btn_send.setFocusable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable edit) {
            }
        };
        yourEditText.addTextChangedListener(textWatcher);
        btn_sendhealthdata =(ImageButton)findViewById(R.id.ib_sendhealth);//건강데이터를 보내는 부분
        btn_sendhealthdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog();
                btn_sendhealthdata.setImageResource(R.drawable.send_health);//이미지를 변환시킴
            }
        });
    }

    private void scrollMyListViewToBottom() {//TODO scrollMyListViewToBottom
        chatlist.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                chatlist.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }

    //리스트뷰 페이징 부분
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {//TODO onScroll
        if (is_loading) {
            if (totalItemCount > previousTotalItemCount) {
                is_loading = false;
                previousTotalItemCount = totalItemCount;
                currentPage++;
            }
        }
        if (!is_loading && firstVisibleItem<=5){
            System.out.println("Scroll 호출");
            handler.sendEmptyMessage(all_messageUpdate);
            is_loading = true;
        }
    }

    private void ShowDialog()
    {
        LayoutInflater dialog = LayoutInflater.from(this);
        final View dialogLayout = dialog.inflate(R.layout.choicedata_dialog, null);
        final Dialog myDialog = new Dialog(this);

        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.setContentView(dialogLayout);
        myDialog.show();
        myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {//Dialog가 사라지면 이미지를 바꿔줌
            @Override
            public void onDismiss(DialogInterface dialog) {
                btn_sendhealthdata.setImageResource(R.drawable.not_sendhealth);
            }
        });
        LinearLayout lo_btn_recent = (LinearLayout)myDialog.findViewById(R.id.lo_recent_data);///최근데이터를 보내는 레이아웃 버튼 처럼 작동함
        lo_btn_recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(sendRecentdata);
                myDialog.dismiss();
            }
        });

        LinearLayout lo_btn_whole = (LinearLayout)myDialog.findViewById(R.id.lo_whole_data);///전체적인 데이터중 선택 할수 있는 버튼
        lo_btn_whole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Chatroom.this,Choosedata.class);
                startActivityForResult(intent,1);///특정한 날짜를 받아오라는 의미
                myDialog.dismiss();
            }
        });
    }

    @Override///결과값을 받아오는 부분
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode==1)//requestCode는 1 데이터를 받아와라
            {
                choose_date = data.getStringExtra("date");
                handler.sendEmptyMessage(sendExactdata);
            }
        }
    }

    //서비스 커넥션 선언.
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            ClientSocketService.ClientSocketServiceBinder binder = (ClientSocketService.ClientSocketServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback(mCallback); //콜백 등록
            ///서비스는 액티비티가 다뜨지 않으면 액티비티와 연결되지 않음
            ////대화 대상이 누구인지 인텐트를 통해 받는 부분
            mServiceIsregistered=true;
            Intent intent = getIntent();
            int from = intent.getIntExtra("from",-1);
            switch(from)
            {
                case 0:
                {
                    who = intent.getStringExtra("who");////누구한테 보낼지 정하는 부분
                    String roomname = intent.getStringExtra("room_name");
                    getSupportActionBar().setTitle(roomname);
                    System.out.println("who"+who);
                    sendtype = 0;
                    //1. 자료의 갯수를 구함
                    itemCount=dBhelper.getMessageCount(who);
                    System.out.println(itemCount+" 아이템의 갯수");
                    pageCount = (int)Math.ceil((double)itemCount/(double)INSERT_COUNT);//총 아이템의 갯수를 한페이지에 들어갈 목록의 갯수로 나누면 페이지의 갯수가 나옴 - 반올림 생각
                    System.out.println(pageCount+"페이지의 수");
                    currentPage =0;
                    lastPage = pageCount;

                    break;
                }

                case 1://초기 방 생성시 방을 요청해 달라고 하는 부분
                {
                    final String groupList = intent.getStringExtra("groupChat");
                    System.out.println("groupList"+groupList);
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            mService.RequestRoom(groupList);////방을 만들어달라 요청
                        }
                    };
                    thread.start();
                    sendtype = 1;
                    break;
                }

                case 2://채팅방은 이미 만들어져 있어서 보내기만 하면 되는 상황
                {
                    who = intent.getStringExtra("room_id");///room_id를 받아옴
                    String roomname = intent.getStringExtra("room_name");
                    System.out.println(roomname+"액션바 타이틀");
                    getSupportActionBar().setTitle(roomname);
                    sendtype=1;
                    System.out.println("방아이디 " + who);
                    //1. 자료의 갯수를 구함
                    itemCount=dBhelper.getMessageCount(who);
                    System.out.println(itemCount);
                    pageCount = (int)Math.ceil((double)itemCount/(double)INSERT_COUNT);//총 아이템의 갯수를 한페이지에 들어갈 목록의 갯수로 나누면 페이지의 갯수가 나옴 - 반올림 생각
                    currentPage =0;
                    lastPage = pageCount;
                    break;
                }
            }
            ///기존에 있던 채팅을 뿌려주는 부분
            handler.sendEmptyMessage(all_messageUpdate);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    //서비스에서 아래의 콜백 함수를 호출하며, 콜백 함수에서는 액티비티에서 처리할 내용 입력
    private ClientSocketService.ICallback mCallback = new ClientSocketService.ICallback() {
        @Override
        public void Knowroom(String room_no){
            if(who==null)////기존에 대화하고 있는지 판단해야됨
            {
                who = room_no;///////room_no를 who에다가 담음
                System.out.println(who+"넘어온 who");
            }
        }
    };

    //서비스 시작.
    public void startServiceMethod(){
        ////소켓에 연결되어 있는 서비스와 채팅창 액티비티를 연결
        Intent Service = new Intent(this, ClientSocketService.class);
        bindService(Service, mConnection, Context.BIND_AUTO_CREATE);
    }
    //


    //액티비티에서 서비스 함수 호출
    public class startServcie_Thread extends Thread
    {
        @Override
        public void run() {
            startServiceMethod();////상대방과 채팅을 해서 오면 서비스 호출
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(0,0);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mServiceIsregistered){/////
            unbindService(mConnection);//서비스와 액티비티의 통신을 끊음
            mServiceIsregistered=false;
            mService.unregisterCallback(mCallback);//Callback을 해제함
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case update_message:{/////받은 데이터를 리스트뷰에 띄움
                    if(chatAdapter.getCount()!=0)
                    {
                        String rightbeforedate=chatAdapter.getItemtime(chatAdapter.getCount()-1).item_date;//지금 받은 메세지의 날짜 이전
                        Date rightnowdate=null;
                        //String인 날짜를 Date로 변환
                        Date previousdate=null;
                        Date nextdate=null;
                        try {
                            previousdate=dateFormat.parse(rightbeforedate);//전 메세지의 날짜
                            nextdate=dateFormat.parse(receiveitem.item_date);//지금 내가 보내는 메세지의 날짜
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        int dateResult=previousdate.compareTo(nextdate);
                        System.out.println(previousdate.toString()+"이전에 채팅방에 있는 날짜");
                        System.out.println(nextdate.toString()+"지금 받은 메세지");
                        System.out.println(dateResult+"비교비교날짜가 어떻게 되려나");
                        if (dateResult!=0)//지금 보내려고 하는 메세지의 날짜와 이전의 날짜가 같지않으면 날짜 변경선을 추가해줘야함
                        {
                            ChatItem chatItem = new ChatItem();
                            try {
                                rightnowdate = dateFormat.parse(receiveitem.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            chatItem.item_date = printFormat.format(rightnowdate);
                            chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                        }
                    }else{
                        System.out.println("여기가 호출 아이템이 없다");
                        ChatItem chatItem = new ChatItem();
                        Date rightnowdate = null;
                        try {
                            rightnowdate=dateFormat.parse(receiveitem.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        chatItem.item_date = printFormat.format(rightnowdate);
                        chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                    }

                    chatAdapter.addItemYou(receiveitem);
                    chatAdapter.notifyDataSetChanged();

                    break;
                }

                case sendRecentdata://최근 건강 데이터를 보냄
                    {
                        scrollMyListViewToBottom();
                        final JSONObject recent_healthdata=dBhelper.PrintHealthChatdata("SELECT * FROM User_health ORDER BY data_signdate desc limit 1;");
                        System.out.println("recent"+recent_healthdata);
                        //내가 보낸 메세지를 Listview에 추가
                        ChatItem me_message = new ChatItem();
                        ////시간을 나타내줌
                        long now = System.currentTimeMillis();
                        // 현재시간을 date 변수에 저장한다.
                        Date date = new Date(now);
                        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        // nowDate 변수에 값을 저장한다.
                        final String formatDate = sdfNow.format(date);
                        ///시간을 더해주기 전에 아이템에 넣어줌
                        System.out.println(recent_healthdata+"최근 데이터Chat");
                        //////서비스를 통해 보내는 부분
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(sendtype==0)
                                {
                                    mService.SendHealthdata(who,recent_healthdata,formatDate);///누구에게-메세지-시간
                                    JSONObject sendptophealthJSON = new JSONObject();
                                    try {
                                        sendptophealthJSON.put("command","/tohealth");///서버에 보낼 명령어
                                        sendptophealthJSON.put("from","me");///내가 보낸거임
                                        sendptophealthJSON.put("who",who);
                                        sendptophealthJSON.put("senderName","me");///나의 이름은 me
                                        sendptophealthJSON.put("message",recent_healthdata);///어떤 내용인지
                                        sendptophealthJSON.put("date",formatDate);///보낸 시간은
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dBhelper.messagejsoninsert(sendptophealthJSON);///JSON 형식으로 DB에 저장
                                }else if(sendtype==1)
                                {
                                    mService.SendHealthdata(Integer.parseInt(who),recent_healthdata,formatDate);///누구에게-메세지-시간
                                    JSONObject sendgrouphealthJSON = new JSONObject();
                                    try {
                                        sendgrouphealthJSON.put("command","/informhealth");///서버에 보낼 명령어
                                        sendgrouphealthJSON.put("room_no",who);
                                        sendgrouphealthJSON.put("from","me");///내가 보낸거임
                                        sendgrouphealthJSON.put("name","me");
                                        sendgrouphealthJSON.put("message",recent_healthdata);///어떤 내용인지
                                        sendgrouphealthJSON.put("date",formatDate);///보낸 시간은
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dBhelper.messagejsoninsert(sendgrouphealthJSON);///JSON 형식으로 DB에 저장
                                }
                            }
                        });

                        thread.start();

                        ///내가 보낸 메시지 타입
                        me_message.setType(2);//건강 데이터 보내는 형식
                        me_message.item_date=formatDate;
                        me_message.user_bpm = recent_healthdata.optInt("user_bpm");
                        me_message.user_res = recent_healthdata.optInt("user_res");
                        me_message.data_signdate = recent_healthdata.optString("data_signdate");
                        String rightbeforedate=chatAdapter.getItemtime(chatAdapter.getCount()-1).item_date;//지금 보내려고 하는 메세지의 날짜 이전
                        Date rightnowdate=null;
                        //String인 날짜를 Date로 변환
                        Date previousdate=null;
                        Date nextdate=null;
                        try {
                            previousdate=dateFormat.parse(rightbeforedate);//전 메세지의 날짜
                            nextdate=dateFormat.parse(me_message.item_date);//지금 내가 보내는 메세지의 날짜
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        int dateResult=previousdate.compareTo(nextdate);

                        if (dateResult!=0)//지금 보내려고 하는 메세지의 날짜와 이전의 날짜가 같지않으면 날짜 변경선을 추가해줘야함
                        {
                            ChatItem chatItem = new ChatItem();
                            try {
                                rightnowdate = dateFormat.parse(me_message.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            chatItem.item_date = printFormat.format(rightnowdate);
                            chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                        }

                        if(itemCount==0)//아이템 갯수가 없다가 메세지를 보낸경우
                        {
                            ChatItem chatItem = new ChatItem();
                            try {
                                rightnowdate=dateFormat.parse(me_message.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            chatItem.item_date = printFormat.format(rightnowdate);
                            chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                        }
                        chatAdapter.addItemHealthME(me_message);
                        chatAdapter.notifyDataSetChanged();

                        break;
                    }

                case sendExactdata://TODO 저장하는 처리를 안해줬음
                    {
                        scrollMyListViewToBottom();
                        final JSONObject choose_healthdata=dBhelper.PrintHealthChatdata_forgrid("SELECT avg(user_bpm),avg(user_res),strftime('%Y-%m-%d',data_signdate) as date from User_health WHERE date= '" + choose_date + "' GROUP BY strftime('%Y-%m-%d',data_signdate);");
                        System.out.println("recent"+choose_healthdata);
                        //내가 보낸 메세지를 Listview에 추가
                        ChatItem me_message = new ChatItem();
                        ////시간을 나타내줌
                        long now = System.currentTimeMillis();
                        // 현재시간을 date 변수에 저장한다.
                        Date date = new Date(now);
                        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        // nowDate 변수에 값을 저장한다.
                        final String formatDate = sdfNow.format(date);
                        ///시간을 더해주기 전에 아이템에 넣어줌
                        System.out.println(choose_healthdata+"최근 데이터Chat");
                        //////서비스를 통해 보내는 부분
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(sendtype==0)
                                {
                                    mService.SendHealthdata(who,choose_healthdata,formatDate);///누구에게-메세지-시간
                                    JSONObject sendptophealthJSON = new JSONObject();
                                    try {
                                        sendptophealthJSON.put("command","/tohealth");///서버에 보낼 명령어
                                        sendptophealthJSON.put("from","me");///내가 보낸거임
                                        sendptophealthJSON.put("who",who);
                                        sendptophealthJSON.put("senderName","me");///나의 이름은 me
                                        sendptophealthJSON.put("message",choose_healthdata);///어떤 내용인지
                                        sendptophealthJSON.put("date",formatDate);///보낸 시간은
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dBhelper.messagejsoninsert(sendptophealthJSON);///JSON 형식으로 DB에 저장
                                }else if(sendtype==1)
                                {
                                    mService.SendHealthdata(Integer.parseInt(who),choose_healthdata,formatDate);///누구에게-메세지-시간
                                    JSONObject sendgrouphealthJSON = new JSONObject();
                                    try {
                                        sendgrouphealthJSON.put("command","/informhealth");///서버에 보낼 명령어
                                        sendgrouphealthJSON.put("room_no",who);
                                        sendgrouphealthJSON.put("from","me");///내가 보낸거임
                                        sendgrouphealthJSON.put("name","me");
                                        sendgrouphealthJSON.put("message",choose_healthdata);///어떤 내용인지
                                        sendgrouphealthJSON.put("date",formatDate);///보낸 시간은
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dBhelper.messagejsoninsert(sendgrouphealthJSON);///JSON 형식으로 DB에 저장
                                }
                            }
                        });

                        thread.start();

                        ///내가 보낸 메시지 타입
                        me_message.setType(2);//건강 데이터 보내는 형식
                        me_message.item_date=formatDate;
                        me_message.user_bpm = choose_healthdata.optInt("user_bpm");
                        me_message.user_res = choose_healthdata.optInt("user_res");
                        me_message.data_signdate = choose_healthdata.optString("data_signdate");

                        String rightbeforedate=chatAdapter.getItemtime(chatAdapter.getCount()-1).item_date;//지금 보내려고 하는 메세지의 날짜 이전
                        Date rightnowdate=null;
                        //String인 날짜를 Date로 변환
                        Date previousdate=null;
                        Date nextdate=null;
                        try {
                            previousdate=dateFormat.parse(rightbeforedate);//전 메세지의 날짜
                            nextdate=dateFormat.parse(me_message.item_date);//지금 내가 보내는 메세지의 날짜
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        int dateResult=previousdate.compareTo(nextdate);

                        if (dateResult!=0)//지금 보내려고 하는 메세지의 날짜와 이전의 날짜가 같지않으면 날짜 변경선을 추가해줘야함
                        {
                            ChatItem chatItem = new ChatItem();
                            try {
                                rightnowdate = dateFormat.parse(me_message.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            chatItem.item_date = printFormat.format(rightnowdate);
                            chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                        }

                        if(itemCount==0)//아이템 갯수가 없다가 메세지를 보낸경우
                        {
                            ChatItem chatItem = new ChatItem();
                            try {
                                rightnowdate=dateFormat.parse(me_message.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            chatItem.item_date = printFormat.format(rightnowdate);
                            chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                        }

                        chatAdapter.addItemHealthME(me_message);
                        chatAdapter.notifyDataSetChanged();
                        break;
                    }

                case update_healthmessage:{
                    chatAdapter.addItemHealthYou(receiveitem);//다른 사람이 메세지를 보내는 경우

                    String rightbeforedate=chatAdapter.getItemtime(chatAdapter.getCount()-1).item_date;//지금 받은 메세지의 날짜 이전
                    Date rightnowdate=null;
                    //String인 날짜를 Date로 변환
                    Date previousdate=null;
                    Date nextdate=null;
                    try {
                        previousdate=dateFormat.parse(rightbeforedate);//전 메세지의 날짜
                        nextdate=dateFormat.parse(receiveitem.item_date);//지금 내가 보내는 메세지의 날짜
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    int dateResult=previousdate.compareTo(nextdate);

                    if (dateResult!=0)//지금 보내려고 하는 메세지의 날짜와 이전의 날짜가 같지않으면 날짜 변경선을 추가해줘야함
                    {
                        ChatItem chatItem = new ChatItem();
                        try {
                            rightnowdate = dateFormat.parse(receiveitem.item_date);//지금 보내고 있는 메세지의 날짜에 해당되는 날짜 변경선을 추가시켜줘야함
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        chatItem.item_date = printFormat.format(rightnowdate);
                        chatAdapter.addItemTime(chatItem);//날짜 변경선을 추가해줌
                    }

                    chatAdapter.notifyDataSetChanged();
                    break;//이게 없어서 밑에부분이 실행됨
                }

                case all_messageUpdate:{
                    ///기존에 있던 채팅을 뿌려주는 부분//TODO paging
                    //ArrayList<JSONObject> messageList = dBhelper.getAllmessage("SELECT * from ChatMessage WHERE room_id= '" + who + "'"+"ORDER BY message_no DESC");
                    if(itemCount!=0)
                    {
                        ArrayList<JSONObject> messageList = dBhelper.getPagingMessage(who,String.valueOf((currentPage)*INSERT_COUNT),String.valueOf(INSERT_COUNT));

                        System.out.println(messageList.size()+"불리는 메세지의 갯수");
                        callmessageCount=messageList.size();

                        for(int i=0;i<messageList.size();i++)
                        {
                            JSONObject jsonObject=messageList.get(i);
                            JSONObject friendinfo=dBhelper.getFriend(jsonObject.optString("message_sender"));//room_id는 개인과 개인일 때는 상대방의 아이디, 그룹채팅일때는 방번호임
                            if(jsonObject.optString("message_sender").equals("me"))//나의 메세지
                            {
                                ChatItem chatitem = new ChatItem();
                                try {//JSON형식으로 파싱이 되면 message_content는 JSON 형식의 건강데이터임 - //건강데이터
                                    String message = jsonObject.optString("message_content");
                                    JSONObject healthdata = new JSONObject(message);
                                    System.out.println(healthdata);
                                    chatitem.user_bpm=healthdata.optInt("user_bpm");
                                    chatitem.user_res=healthdata.optInt("user_res");
                                    chatitem.data_signdate=healthdata.optString("data_signdate");
                                    chatitem.item_date = jsonObject.optString("message_date");
                                    chatitem.setType(2);

                                    chatitem.item_sender = jsonObject.optString("senderName");//친구의 아이디를 보여줌
                                    chatitem.item_senderId = jsonObject.optString("message_sender");//친구의 아이디를 보여줌

                                    chatAdapter.addItemHealthME(0,chatitem);

                                } catch (JSONException e) {//건강데이터가 아니면 그냥 메세지임 - //건강데이터가 아닌것은 그냥 메세지
                                    chatitem.item_content = jsonObject.optString("message_content");
                                    chatitem.item_date = jsonObject.optString("message_date");
                                    chatitem.setType(0);

                                    chatitem.item_sender = jsonObject.optString("senderName");//친구의 아이디를 보여줌
                                    chatitem.item_senderId = jsonObject.optString("message_sender");//친구의 아이디를 보여줌

                                    chatAdapter.addItemME(0,chatitem);
                                }

                            }else{//다른 사람이 보낸 메세지

                                ChatItem chatitem = new ChatItem();
                                try {//JSON형식으로 파싱이 되면 message_content는 JSON 형식의 건강데이터임 - //건강데이터
                                    String message = jsonObject.optString("message_content");
                                    JSONObject healthdata = new JSONObject(message);
                                    System.out.println(healthdata);
                                    chatitem.user_bpm=healthdata.optInt("user_bpm");
                                    chatitem.user_res=healthdata.optInt("user_res");
                                    chatitem.data_signdate=healthdata.optString("data_signdate");
                                    chatitem.item_date = jsonObject.optString("message_date");
                                    chatitem.setType(3);
                                    //senderId는 프로필 사진을 갖고 오기 위해 필요한 부분

                                    chatitem.item_sender = jsonObject.optString("senderName");//친구의 아이디를 보여줌
                                    chatitem.item_senderId = jsonObject.optString("message_sender");//친구의 아이디를 보여줌

                                    chatAdapter.addItemHealthYou(0,chatitem);

                                } catch (JSONException e) {//건강데이터가 아니면 그냥 메세지임 - //건강데이터가 아닌것은 그냥 메세지
                                    chatitem.item_content = jsonObject.optString("message_content");
                                    chatitem.item_date = jsonObject.optString("message_date");
                                    chatitem.setType(1);

                                    chatitem.item_sender = jsonObject.optString("senderName");//친구의 아이디를 보여줌
                                    chatitem.item_senderId = jsonObject.optString("message_sender");//친구의 아이디를 보여줌

                                    chatAdapter.addItemYou(0,chatitem);
                                }
                            }
                            //날짜 정해주는 부분 - //TODO 디버깅할것!!!!!!(날짜 추가되니까 안됨)


                            String before=messageList.get(i).optString("message_date");//현재 데이터에 대한 날짜를 갖고옴
                            try {
                                beforedate = dateFormat.parse(before);//Date로 파싱
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(i!=0)//0일때는 하면 안됨
                            {
                                String after=messageList.get(i-1).optString("message_date");
                                try {
                                    afterdate = dateFormat.parse(after);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                System.out.println(beforedate+"이전");
                                System.out.println(afterdate+"이후");

                                if (afterdate!=null) {///메세지가 2개 이상되서 비교 해야되는 경우
                                    int compare = beforedate.compareTo(afterdate);
                                    System.out.println(compare+"비교비교");
                                    if ( compare < 0) { //지금 뿌려지고 있는 리스트뷰 아이템이 이전것보다 날짜가 작으면
                                        ChatItem chatItem = new ChatItem();
                                        chatItem.item_date = printFormat.format(afterdate);
                                        chatAdapter.addItemTime(1, chatItem);
                                        timeitemCount++;
                                    }
                                }
                            }
                            if(beforedate !=null){//메세지가 하나밖에 없어서 비교할게 없는 경우
                                //가장위에 날짜변경선 해주는 부분
                                if(chatAdapter.getCount()==itemCount+timeitemCount)//모든 아이템의 갯수가 chatAdapter에 추가된 갯수와 같을때 + 날짜 추가된것도 고려
                                {
                                    ChatItem chatItem = new ChatItem();
                                    chatItem.item_date = printFormat.format(beforedate);
                                    chatAdapter.addItemTime(0,chatItem);
                                }
                            }
                        }

                        String updateStateQuery = "UPDATE ChatMessage SET is_looked=1 WHERE is_looked=0 and room_id= '" + who + "';";

                        dBhelper.update(updateStateQuery);
                        chatAdapter.notifyDataSetChanged();

                        Log.d("Chat",chatAdapter.getCount()+"리스트뷰에 추가된갯수");
                        Log.d("Chat",itemCount+"메세지 총갯수");

                        chatlist.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                        if(chatAdapter.getCount()>20){
                            chatlist.setSelection(callmessageCount+visibleThreshold+1);
                            System.out.println("20<x Call");
                        }else{
                            chatlist.setSelection(chatAdapter.getCount()-1);
                            System.out.println("20>=x Call");
                        }
                    }

                    break;
                }
            }


        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {////
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println(who+"BroadCastReceiver 처리");

            //메세지가 추가되면 - DB가 변경되면 페이지를 바꿔야됨
            itemCount=dBhelper.getMessageCount(who);
            System.out.println(itemCount+" 아이템의 갯수");
            pageCount = (int)Math.ceil((double)itemCount/(double)INSERT_COUNT);//총 아이템의 갯수를 한페이지에 들어갈 목록의 갯수로 나누면 페이지의 갯수가 나옴 - 반올림 생각
            chatlist.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            /////////////////////페이지 다시 계산해주는 부분
            String query = "SELECT * FROM ChatMessage WHERE is_looked=0 and room_id= '" + who + "'"+" ORDER BY message_no DESC LIMIT 1;";//////번호 순으로 처리
            JSONObject jsonObject=dBhelper.updatemessage(query);//room_id는 개인과 개인일 때는 상대방의 아이디, 그룹채팅일때는 방번호임
            System.out.println(jsonObject+"새로온 메세지를 불러옴");
            //분리한 데이터를 리스트뷰에 들어갈 아이템 객체로 변환 - 다른 사람이 보낸 메세지 타입
            if(jsonObject.length()!=0){//////JSONObject가 비었는지 판단 - 길이로 판단해야됨
                receiveitem = new ChatItem();

                try {//JSON형식으로 파싱이 되면 message_content는 JSON 형식의 건강데이터임
                    String message = jsonObject.optString("message_content");
                    JSONObject healthdata = new JSONObject(message);
                    System.out.println(healthdata);
                    receiveitem.user_bpm=healthdata.optInt("user_bpm");
                    receiveitem.user_res=healthdata.optInt("user_res");
                    receiveitem.data_signdate=healthdata.optString("data_signdate");
                    receiveitem.item_date = jsonObject.optString("message_date");
                    receiveitem.setType(3);

                    receiveitem.item_sender = jsonObject.optString("senderName");//친구의 이름을 넣어줌
                    receiveitem.item_senderId = jsonObject.optString("message_sender");//친구의 아이디를 보여줌

                    handler.sendEmptyMessage(update_healthmessage);

                } catch (JSONException e) {//건강데이터가 아니면 그냥 메세지임
                    receiveitem.item_content = jsonObject.optString("message_content");
                    receiveitem.item_date = jsonObject.optString("message_date");
                    receiveitem.setType(1);

                    receiveitem.item_sender = jsonObject.optString("senderName");//친구의 이름을 넣어줌
                    receiveitem.item_senderId = jsonObject.optString("message_sender");//친구의 아이디를 보여줌

                    handler.sendEmptyMessage(update_message);
                }
                dBhelper.updateMessageState(who);


            }else{
                System.out.println("다른 사람이 메세지를 보냄");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(networkChangeReceiver);
    }
}

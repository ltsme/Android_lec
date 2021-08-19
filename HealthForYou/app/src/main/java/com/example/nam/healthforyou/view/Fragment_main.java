package com.example.nam.healthforyou.view;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nam.healthforyou.component.ClientSocketService;
import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.util.NetworkUtil;
import com.example.nam.healthforyou.R;
import com.example.nam.healthforyou.util.RequestHttpConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by NAM on 2017-07-13.
 */

public class Fragment_main extends Fragment {

    String strurl = "http://kakapo12.vps.phps.kr/mainactivity.php";
    HttpURLConnection con;

    //보여줄 데이터
    String id;
    int bpm;
    int res;
    String graph_image;
    String time;
    JSONObject health_data;

    //데이터를 뿌려주는 TextView
    TextView heart_rate;
    TextView RIIV;
    TextView date;
    TextView graphmessage;
    ImageView graph;
    //Fragment 이동시 저장시켜주는 부분
    final static int update_main=1;
    final static int no_data=2;
    DBhelper dbManager;

    Handler main_handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what)
            {
                case update_main:
                {
                    System.out.println("update_main");
                    //텍스트뷰에 뿌려줌
                    heart_rate.setText(bpm+" BPM");
                    RIIV.setText(res+" 회/분");
                    date.setText(time);
                    graphmessage.setText("맥박 그래프");
                    //graph.setImageResource(R.drawable.image);

                    if(graph_image!=null)
                    {
                        byte[] a = Base64.decode(graph_image,Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(a,0,a.length);////비트맵으로 변환
                        graph.setImageBitmap(bitmap);
                    }else{
                        //main_handler.sendEmptyMessage(no_data);
                        System.out.println("여기로 빠지나");
                    }

                    break;
                }

                case no_data:
                {
                    System.out.println("no_data");
                    heart_rate.setText("--");
                    RIIV.setText("--");
                    graph.setImageResource(R.drawable.sad);
                    graphmessage.setText("측정한 데이터가 없습니다.");

                    break;
                }
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout main = (RelativeLayout) inflater.inflate(R.layout.frag_main,container,false);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("메인");//Action Bar이름 지정
        heart_rate =(TextView)main.findViewById(R.id.tv_heartrate);
        RIIV = (TextView)main.findViewById(R.id.tv_riiv);
        date = (TextView)main.findViewById(R.id.tv_date);
        graph = (ImageView)main.findViewById(R.id.graphImage);
        graphmessage = (TextView)main.findViewById(R.id.graphmessage);
        ////DB를 불러옴
        dbManager = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB생성
        String init=dbManager.PrintData("SELECT * FROM User_health;");//유저의 건강정보 모두 받아오기
        int datacount = dbManager.PrintCountData();
        System.out.println(datacount);

        int type= NetworkUtil.getConnectivityStatus(getActivity().getApplicationContext());
        if(type==NetworkUtil.TYPE_MOBILE||type==NetworkUtil.TYPE_WIFI)
        {
            //채팅창을 누를 시에 Socket을 연다
            ServicesocketThread servicesocketThread = new ServicesocketThread();
            servicesocketThread.start();
        }

        //생각해야 될 부분


        /*
        1. SQLite와 서버 DB연동
        2. 연동 타이밍을 생각해야됨
          - 결과를 기록할 때 로컬에도 기록할 것인지
          - 아니면 메인부분에서만 기록할 것인지
        */

        //System.out.println(init+"init");//유저의 건강정보 모두 출력

        if(datacount!=0)///////갯수로 체크 SQLite에 데이터 가 있으면
        {
                System.out.println("SQlite");
               //자료가 있으면 최근 데이터(limit 사용)를 SQlite에서 받아옴
                JSONObject local_healthdata=dbManager.PrintHealthData("SELECT * FROM User_health ORDER BY data_signdate desc limit 1;");
                System.out.println("local"+local_healthdata);
                try {
                    bpm=local_healthdata.getInt("user_bpm");
                    res=local_healthdata.getInt("user_res");
                    time=local_healthdata.getString("data_signdate");
                    graph_image=local_healthdata.getString("graph_image");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //텍스트뷰에 뿌려줌
                main_handler.sendEmptyMessage(update_main);
        }else{////Sqlite에 데이터가 없으면 서버 DB조회
            try {
                URL url = new URL(strurl);
                con = (HttpURLConnection)url.openConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }

            NetworkTask networkTask = new NetworkTask(strurl, null);///서버에서 데이터를 받아오는 부분
            networkTask.execute();

        }



        // AsyncTask를 통해 HttpURLConnection 수행.
        return main;
    }

    //UI-Thread를 통해서 Socket을 열면 netWorkThreadException 발생 - Thread를 통해 쓰레드 시작
    public class ServicesocketThread extends Thread{
        @Override
        public void run() {
            startServiceMethod();
        }
    }

    //서비스 시작. - 소켓 연결
    public void startServiceMethod(){
        Intent Service = new Intent(getActivity(), ClientSocketService.class);
        getActivity().startService(Service);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpConnection requestHttpURLConnection = new RequestHttpConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            //System.out.println("response"+s);
            int network_status=NetworkUtil.getConnectivityStatus(getActivity().getApplicationContext());
            ///인터넷 연결이 안되어있음에 대한 예외처리
            if(network_status==NetworkUtil.TYPE_MOBILE||network_status==NetworkUtil.TYPE_WIFI)
            {
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    if(jsonArray.length()!=0)///JSON array갯수로 데이터가 있는지 판단 데이터가 있으면
                    {
                        System.out.println("jsonArray"+jsonArray);
                        for(int i=0;i<jsonArray.length();i++)
                        {
                            health_data=new JSONObject(jsonArray.getString(i));
                            health_data.put("is_synced",1);
                            //DB에 자료를 넣어줌 - LocalDB(SQlite)
                            dbManager.infoinsert(health_data);
                            //System.out.println(health_data+"local에 넣는 자료");
                        }

                        //데이터를 SQlite에서 갖고와서 뿌려줌 - 인터넷이 연결 안됐을 때 도 생각?
                        JSONObject local_healthdata=dbManager.PrintHealthData("SELECT * FROM User_health ORDER BY data_signdate desc limit 1;");
                        System.out.println(local_healthdata+"local_healthdata");
                        try {
                            bpm=local_healthdata.getInt("user_bpm");
                            res=local_healthdata.getInt("user_res");
                            time=local_healthdata.getString("data_signdate");
                            graph_image=local_healthdata.getString("graph_image");/////이미지에 대한 bytearray를 String으로 불러옴
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("update_main");
                        //텍스트를 핸들러를 통해 띄어줌
                        main_handler.sendEmptyMessage(update_main);
                    }else{////데이터가 없으면
                        System.out.println("no_data");
                        main_handler.sendEmptyMessage(no_data);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(getActivity(),"인터넷이 연결되지 않았습니다",Toast.LENGTH_SHORT).show();
            }
        }
    }
}

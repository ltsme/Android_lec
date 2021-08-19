package com.example.nam.healthforyou.view;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.util.Base64;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.util.InternalImageManger;
import com.example.nam.healthforyou.R;
import com.mommoo.permission.MommooPermission;
import com.mommoo.permission.listener.OnPermissionDenied;
import com.mommoo.permission.repository.DenyInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Context mContext;
    private static final String TAG ="MainActivity";

    //버튼에 대한 선언
    Button btn_main;
    Button btn_chat;
    Button btn_meas;
    Button btn_result;

    //Back button
    private BackPressCloseHandler backPressCloseHandler;

    HttpURLConnection con;

    UiTask uiTask;

    DBhelper dBhelper;

    ////날짜에 따른 요일 알려주는 메쏘드
    static public int getDateDay(String date, String dateType) throws Exception {

        String day = "" ;

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType, Locale.getDefault()) ;
        Date nDate = dateFormat.parse(date) ;

        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);

        int dayNum = cal.get(Calendar.DAY_OF_WEEK) ;

        return dayNum ;
    }

    static public int getDateMonth(String date, String dateType) throws Exception {

        String tempdate = date+"01";//////DB에서 긁어온 데이터가 월별로 GROUP BY 되어 있으므로
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType,Locale.getDefault()) ;
        Date nDate = dateFormat.parse(tempdate) ;

        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);

        int dayMonth = cal.get(Calendar.MONTH) ;

        return dayMonth+1;////MONTH는 JAVA에서 0~11이므로 1씩더해줘야됨 -> 1~12
    }

    @Override////메뉴 세팅
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id)
        {
            case R.id.action_settings:
            {
                Intent intent = new Intent(this,Setting.class);
                startActivity(intent);
                finish();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    //메인 페이지를 불러옴
    setContentView(R.layout.activity_main);
    mContext = getApplicationContext();
    dBhelper = new DBhelper(getApplicationContext(),"healthforyou.db", null, 1);
    //AsyncTask를 통해 미리 불러옴
    uiTask = new UiTask();
    uiTask.execute();

    //뒤로가는 부분
    backPressCloseHandler = new BackPressCloseHandler(this);
    // 위젯에 대한 참조.
    btn_main=(Button)findViewById(R.id.btn_frag1_main);
    btn_chat=(Button)findViewById(R.id.btn_frag2_chat);
    btn_meas=(Button)findViewById(R.id.btn_frag3_meas);
    btn_result=(Button)findViewById(R.id.btn_frag4_result);

    btn_main.setOnClickListener(this);
    btn_chat.setOnClickListener(this);
    btn_meas.setOnClickListener(this);
    btn_result.setOnClickListener(this);


//  findViewById(R.id.btn_frag1_main).setOnClickListener(this);
//  findViewById(R.id.btn_frag2_chat).setOnClickListener(this);
//  findViewById(R.id.btn_frag3_meas).setOnClickListener(this);
//  findViewById(R.id.btn_frag4_result).setOnClickListener(this);

    //카메라를 사용하기 위한 권한얻기
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
    {
        new MommooPermission.Builder(this)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                .setOnPermissionDenied(new OnPermissionDenied() {
                    @Override
                    public void onDenied(List<DenyInfo> deniedPermissionList) {
                        for (DenyInfo denyInfo : deniedPermissionList){
                            System.out.println("isDenied : " + denyInfo.getPermission() +" , "+
                                    "userNeverSeeChecked : " + denyInfo.isUserNeverAskAgainChecked());
                        }
                    }
                })
                .setPreNoticeDialogData("알려드립니다","Healthforyou를 사용하기 위해서는 권한을 수락해주세요")
                .setOfferGrantPermissionData("앱 설정으로 이동하여","신체정보를 측정하기 위하여 카메라를 사용하기 위한 권한이 필요합니다")
                .build()
                .checkPermissions();
    }

        // URL 설정.
        String strurl = "http://kakapo12.vps.phps.kr/mainactivity.php";

        try {
            URL url = new URL(strurl);
            con = (HttpURLConnection)url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //기존의 친구들 정보를 가지고 와야함
        List<JSONObject> friendList = dBhelper.getAllfriend();
        System.out.println(friendList+"보낼친구");
        //친구의 새로운 정보를 받아오는 부분
        syncfriendTask syncfriendTask = new syncfriendTask();
        syncfriendTask.execute(friendList.toString());

        Intent intent =getIntent();
        int FCMrequest = intent.getIntExtra("FCM",1001);
        if(FCMrequest==0)
        {
            String who=intent.getStringExtra("WHO");
            String type=intent.getStringExtra("TYPE");

            Fragment_chat fragment_chat = new Fragment_chat();//프래그먼트와 액티비티가 통신
            Bundle bundle = new Bundle();//번들에
            bundle.putString("WHO", who);//누구한테 온건지
            bundle.putString("TYPE",type);//방의 종류 0-개인채팅 , 1-그룹채팅

            fragment_chat.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, fragment_chat)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_frag1_main:
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_main())
                    .commit();
                btn_main.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.home_focus, 0, 0);
                btn_chat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.rotate, 0, 0);
                btn_meas.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.electrocardiogram, 0, 0);
                btn_result.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.list, 0, 0);
                break;

            case R.id.btn_frag2_chat:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frag_container_, new Fragment_chat())
                        .commit();
                btn_main.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.home, 0, 0);
                btn_chat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.rotate_focus, 0, 0);
                btn_meas.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.electrocardiogram, 0, 0);
                btn_result.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.list, 0, 0);
                break;

            case R.id.btn_frag3_meas:
                Fragment_meas fragment_meas = new Fragment_meas();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frag_container_, fragment_meas)
                        .commit();
                btn_main.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.home, 0, 0);
                btn_chat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.rotate, 0, 0);
                btn_meas.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.electrocardiogram_focus, 0, 0);
                btn_result.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.list, 0, 0);
                break;

            case R.id.btn_frag4_result:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frag_container_, new Fragment_result())
                        .commit();
                btn_main.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.home, 0, 0);
                btn_chat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.rotate, 0, 0);
                btn_meas.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.electrocardiogram, 0, 0);
                btn_result.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.list_focus, 0, 0);
                break;
        }
    }

    public class UiTask extends  AsyncTask<Void,Void,Void>{
        @Override
        protected void onPostExecute(Void aVoid) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_result())
                    .commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_chat())
                    .commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_meas())
                    .commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_main())
                    .commit();
            getSupportActionBar().setTitle("메인");
            btn_main.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.home_focus, 0, 0);
            btn_chat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.rotate, 0, 0);
            btn_meas.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.electrocardiogram, 0, 0);
            btn_result.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.list, 0, 0);
        }

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }
    }

    public class syncfriendTask extends AsyncTask<String,String,String>
    {
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s+"결과");
            System.out.println("서비스 실행");
            if(s.equals("false"))///친구의 정보중 바뀐게 없음
            {

            }else{
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject profile_data=new JSONObject(jsonArray.getString(i));
                        String Id=profile_data.optString("user_friend");
                        byte[] a = Base64.decode(profile_data.optString("user_profile"),Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(a,0,a.length);////비트맵으로 변환
                        boolean is_update=new InternalImageManger(mContext).setFileName(Id+"_Image").setDirectoryName("PFImage").deleteFile();
                        System.out.println("is_update : "+is_update);
                        if(is_update)
                        {
                            new InternalImageManger(mContext).setFileName(Id+"_Image").setDirectoryName("PFImage").save(bitmap);//저장
                            //파일의 이름, update 된 날짜, 사용자 정보
                            dBhelper.updateProfile(Id+"_Image",profile_data.optString("user_update"),profile_data.optString("user_friend"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("업데이트 완료");
            dBhelper.close();

        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/syncfriendinfo.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);

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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Toast toast;

        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                toast.cancel();
                Intent t = new Intent(activity, MainActivity.class);
                activity.startActivity(t);
                activity.moveTaskToBack(true);
                activity.finish();

                System.exit(0);
            }
        }

        public void showGuide() {
            toast = Toast.makeText(activity, "한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static Bitmap getCircularBitmap(@NonNull Bitmap bitmap)
    {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    static public Bitmap resizeBitmap(Bitmap original,int height,int width) {
        Bitmap result = Bitmap.createScaledBitmap(original, width, height, false);
        if (result != original) {
            original.recycle();
        }
        return result;
    }
}



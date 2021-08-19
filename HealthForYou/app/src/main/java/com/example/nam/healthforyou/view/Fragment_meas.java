package com.example.nam.healthforyou.view;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jtransforms.fft.DoubleFFT_1D;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;

import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.CameraActivity;
import com.example.nam.healthforyou.util.NetworkUtil;
import com.example.nam.healthforyou.R;
import com.example.nam.healthforyou.util.javaViewCameraControl;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;


import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import filter.Butterworth;
import util.thirdparty.weka_plugin.FastICA;
import util.thirdparty.weka_plugin.LogCosh;


import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

/**
 * Created by NAM on 2017-07-13.
 */
///////////////Fragment 로 옮김 완료-2017.07.21

public class Fragment_meas extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2, OnChartValueSelectedListener{

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }
    /*
    * OPENCV를 컴퓨터에 지우면 컴파일이 안됨
    * - 현재 2017.08.08 기준 2017.08.06일에 한 디버깅자료가 지워짐 다시 해야됨
    * -
    *
    * */
    Context mContext;
    //Sqlite DB
    DBhelper dbManager;
    JSONObject healthinfo;
    final static private int size=1024;

    private LinearLayout meas;

    private static final String TAG = "opencv";
    private javaViewCameraControl mOpenCvCameraView;
    ////카메라 영상 받아오는 부분
    private Mat matInput;
    private Mat matResult;
    int sum;//intensity의 합 - green
    int redsum;//intensity의 합 2 -red
    private int bluesum;//intensity의 합 3 -blue
    int moving;

    ////이전이미지와 현재이미지 비교
    private Mat previous=null;
    private Mat current=null;
    int move_point;

    ////버튼 바꿔주는 부분 색깔이 변해야함
    Button mainBtn_meas;
    Button mainBtn_result;

    ////그래프
    private LineChart mChart;//차트
    private Thread thread;//값 넣어주는 부분

    private int valleyX=0;
    private int W_size=20;
    private ArrayList<Integer> peakPoint;
    private ArrayList<Integer> tempforPeak;//피크를 구하기위한 ArrayList
    private ArrayList<Integer> localMinX;//피크를 구하기위한 ArrayList

    int peak=0;
    int X=0;
    int max=0;

    private ArrayList<Integer> heart_data; ///피크값을 구하기 위해 그래프 말고 Array에도 데이터를 넣어줌

    ArrayList<Double> Raw_data;//카메라에서 측정된값그대로 갖고 있음
    //ExponentialMovingAverage filter;
    private ArrayList<Integer> heart_maf; ///피크값을 구하기 위해 그래프 말고 Array에도 데이터를 넣어줌
    double alpha;
    Double oldValue;
    double peakInterval;
    private ArrayList<Integer> arraybpm;//순간순간마다의 bpm을 저장해서 평균을 낼 역할
    int bpm;
    int avebpm;
    final static int sampling_rate=30;///카메라 fps 기준 : 30Hz
    final static int minute=60;///1분은 60초


    ////네이티브 메소드
    public native int redDetection(long matAddrInput, long matAddrResult);
    public native int greenDetection(long matAddrInput, long matAddrResult);
    public native int blueDetection(long matAddrInput, long matAddrResult);
    public native int moveDetection(long previous,long current);

    ////제어부분
    //*핸들러 메세지 정의
    static final int detectGo=0;
    static final int is_moving=1;
    static final int no_moving=2;
    static final int setprogress=3;///핸들러에 프로그레스바를 갱신하라는 메시지
    static final int detectDone=4;///검사를 완료 하였다는 메시지
    static final int update_heartrate=5;
    static final int detectStop=6;///손가락인지 판단하였을 때 아닐 경우 Stop 시키는 메세지
    static final int final_heartrate=7;
    boolean detectStart;
    boolean is_Stop;//////손가락이 아니여서 종료된건지 판단
    //**프로그레스 바 값 부분
    ProgressBar detectComplete;

    //***동작제어 버튼 정의
    Button btn_start;
    LinearLayout btn_detectdone;
    Button btn_restart;
    Button btn_result;

    //****메세지 텍스트뷰 부분
    TextView heart_rate;// 심박수 나타내주는 부분
    TextView follow_message;// 측정 관련 메세지를 보여주는 부분
    TextView tv_measriiv;// 호흡수 나타내주는 부분
    boolean is_ready=false;//준비중임을 나타냄 - 핸들러와 관련

    //*****Thread
    setTextthread setTextthread; ///텍스트 바꿔주는 쓰레드 클래스
    setHeartratethread setHeartratethread; ///심장박동수 바꿔주는 쓰레드 클래스
    setProgressthread setProgressthread;//프로그레스바를 갱신해주는 쓰레드

    ////필터 부분(1~3Hz)->BPM(60 ~ 180)
    double filtered_Raw;
    private final int N = 32;
    private int n = 0;
    private double[] x = new double[N];
    private final double[] h =
            {
                    -0.00777032810402619650,
                    -0.00145348285755657340,
                    0.00437702232954987210,
                    0.00506935759526942410,
                    -0.00304154764950843480,
                    -0.02080923158617735800,
                    -0.04518057774120583200,
                    -0.06940630529736967200,
                    -0.08472268157704579400,
                    -0.08312513257882411800,
                    -0.06036952920357524500,
                    -0.01811431842405999300,
                    0.03571383114026974900,
                    0.08869415672122030200,
                    0.12741697127285534000,
                    0.14160746393137291000,
                    0.12741697127285534000,
                    0.08869415672122030200,
                    0.03571383114026974900,
                    -0.01811431842405999300,
                    -0.06036952920357524500,
                    -0.08312513257882411800,
                    -0.08472268157704579400,
                    -0.06940630529736967200,
                    -0.04518057774120583200,
                    -0.02080923158617735800,
                    -0.00304154764950843480,
                    0.00506935759526942410,
                    0.00437702232954987210,
                    -0.00145348285755657340,
                    -0.00777032810402619650,
                    -0.01075837690475049300
            };

    public double filter(double x_in)
    {
        double y = 0.0;

        //Store the current input, overwriting the oldest input
        x[n] = x_in;

        // Multiply the filter coefficients by the previous inputs and sum
        for (int i=0; i<N; i++)
        {
            y+=h[i]*x[((N-i)+n)%N];
        }

        // Increment the input buffer index to the next location
        n = (n+1)%N;

        return y;
    }

    //호흡
    //호흡 구하기 위한 ICA(Independent Component Analysis)구현
    ArrayList<Double> red_IndeComp_array;
    ArrayList<Double> green_IndeComp_array;
    ArrayList<Double> blue_IndeComp_array;
    //FFT 클래스 객체 선언
    ArrayList<Double> meaning_data;//의미 있는 값
    double[] fft_heart_rate;
    DoubleFFT_1D fft;//FFT 객체 선언

    double re;
    double im;

    double[] magnitude= new double[256];
    double max_magnitude = Double.MIN_VALUE;
    int max_index = -1;
    int fft_heart;
    int final_heart;

    ArrayList<Double> finger_date;//손가락인지 판단하기 위한 값
    double[] fft_finger_rate;//손가락인지 판단하기 위한 배열
    DoubleFFT_1D finger_fft;
    double re_fi;
    double im_im;
    double[] finger_magnitude = new double[256];
    double finger_max_magnitude = Double.MIN_VALUE;
    int finger_index = -1;

    //호흡수를 판단하는 FFT
    double[][] output;

    //FFT
    boolean phase1=false;//FFT 128 point
    boolean phase2=false;//FFT 256 point
    boolean phase3=false;//FFT 512 point

    //100Hz라고 초기화 - 존재할 수 없는 값임-60*100=6000 심장이 6000번 뛴다?!
    double phase1_freq=100;
    double phase2_freq=100;
    double phase3_freq=100;

    ImageView test;
    //측정 완료 시간 기록
    //날짜 지정해주는 부분
    long now;
    Date date;
    SimpleDateFormat sdf;
    String getTime;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    //통신으로 보낼 데이터 부분
    int averes=0; //평균 호흡수

    //통신부분
    HttpURLConnection con;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        meas = (LinearLayout) inflater.inflate(R.layout.frag_meas,container,false);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("측정하기");//Action Bar이름 지정
        mOpenCvCameraView =(javaViewCameraControl)meas.findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setAlpha(0);//프리뷰
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mOpenCvCameraView.setMaxFrameSize(200, 200);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        mContext = getActivity().getApplicationContext();
        //SQlite DB 접근
        dbManager = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB생성
        mainBtn_meas= (Button)getActivity().findViewById(R.id.btn_frag3_meas);
        mainBtn_result=(Button)getActivity().findViewById(R.id.btn_frag4_result);

        //얼굴을 통해 맥박수를 측정하는 액티비티로 이동
        Button btngotofacePPG = (Button)meas.findViewById(R.id.btnGotofaceppg);
        btngotofacePPG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivity(intent);
            }
        });

        //////////////////////측정순서 제어
        //1) 측정 시작 초기 단계
        btn_start = (Button)meas.findViewById(R.id.start_measure);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(detectGo);
                heart_rate.setText("--");
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.turnFlashOn();
                detectComplete.setProgress(0);//프로그레스바도 초기화
                detectStart=true;/////시작하는 부분
                X=0;//X값도 초기화 시켜줘야됨
                //심박수 초기화
                avebpm=0;
                fft_heart=0;
                final_heart=0;
                is_Stop=false;//정지하는 거 초기화
                ///쓰레드 부분
                setTextthread = new setTextthread();
                setTextthread.start();////text를 테스트 하는 부분
                setHeartratethread = new setHeartratethread();
                setHeartratethread.start();///BPM을 정해주는 부분
                setProgressthread = new setProgressthread();
                setProgressthread.start();

                feedMultiple();//그래프에 데이터를 넣는 부분
            }
        });

        //2) 측정 재시작
        btn_restart = (Button)meas.findViewById(R.id.btn_redetect);
        btn_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //기존에 있던 데이터 clear
                heart_rate.setText("--");
                detectComplete.setProgress(0);//프로그레스바도 초기화
                X=0;//X값도 초기화 시켜줘야됨
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.turnFlashOn();
                handler.sendEmptyMessage(detectGo);
                //심박수 초기화
                avebpm=0;
                fft_heart=0;
                final_heart=0;
                detectStart=true;/////시작하는 부분
                is_Stop=false;/////정지하는 거 초기화
                ///쓰레드 부분
                setTextthread = new setTextthread();
                setTextthread.start();////text를 정해주는 부분
                setHeartratethread = new setHeartratethread();
                setHeartratethread.start();///BPM을 정해주는 부분
                setProgressthread = new setProgressthread();
                setProgressthread.start();

                feedMultiple();//그래프에 데이터를 넣는 부분
            }
        });

        //3) 측정 기록
        btn_result = (Button)meas.findViewById(R.id.btn_result);
        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int conn= NetworkUtil.getConnectivityStatus(getActivity().getApplicationContext());

                if (conn == NetworkUtil.TYPE_MOBILE || conn== NetworkUtil.TYPE_WIFI)//인터넷이 연결되었을때
                {
                    healthinfo = new JSONObject();//JSON Object를 생성해서
                    ///////차트의 이미지를 ByteArray로 변환

                    Bitmap bmp = mChart.getChartBitmap();

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    String base64String = Base64.encodeToString(byteArray,Base64.DEFAULT);

                    ///////byteArray로 보내는 이유는 Bitmap은 이미 메모리에 올라가 있으므로 Array로 바꿀시 접근이 더 용이
                    //////file로 보낼 시 file로 생성 후 보내야 되는 시간 필요

                    //bytearray로 이미지 그리기 테스트

                    infoSaveTask infosave = new infoSaveTask();///네트워크 부분 AsyncTask 로 기록 후 측정내역으로 넘어감
                    try {
                        healthinfo.put("bpm", final_heart);
                        healthinfo.put("res", averes);
                        healthinfo.put("is_synced",1);//서버와 Sync 된 데이터
                        healthinfo.put("graph_image",base64String);////이미지 byteArray를 Base64encoding 후 JSON에 저장
                        //날짜도 저장
                        healthinfo.put("data_signdate",getTime);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(healthinfo.toString());
                    infosave.execute(healthinfo.toString()); //신체정보를 Json 형식 -> 서버
                    dbManager.infoinsert(healthinfo); //신체정보를 Json 형식 -> SQlite
                    ////JSON에 저장되어 있던 bytearray를 String으로 뽑아 다시 bytearray로 변화해주고 이미지로 뿌려지는지 테스트

                } else if(conn == NetworkUtil.TYPE_NOT_CONNECTED){//인터넷이 연결 안되었을 때 SQLite
                    healthinfo = new JSONObject();
                    try {
                        healthinfo.put("bpm", final_heart);
                        healthinfo.put("res", averes);

                        //날짜도 저장

                        healthinfo.put("data_signdate",getTime);
                        healthinfo.put("is_synced",0);//서버와 Sync 되지 않은 데이터

                        ///////차트의 이미지를 ByteArray로 변환
                        Bitmap bmp = mChart.getChartBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        String base64String = Base64.encodeToString(byteArray,Base64.DEFAULT);////이미지 byteArray를 Base64encoding 후 JSON에 저장

                        ///////byteArray로 보내는 이유는 Bitmap은 이미 메모리에 올라가 있으므로 Array로 바꿀시 접근이 더 용이
                        //////file로 보낼 시 file로 생성 후 보내야 되는 시간 필요

                        healthinfo.put("graph_image",base64String);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(healthinfo.toString());
                    //DB에 결과를 기록하고
                    dbManager.infoinsert(healthinfo);//신체정보를 Json 형식 -> SQlite
                    Toast.makeText(getActivity(), "결과를 기록하였습니다.", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frag_container_, new Fragment_result())
                            .commit();
                    //버튼 색변화
                    mainBtn_meas.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.electrocardiogram, 0, 0);
                    mainBtn_result.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.list_focus, 0, 0);
                }

            }
        });

        //////****측정 완료후 제어
        btn_detectdone = (LinearLayout)meas.findViewById(R.id.btn_detectdone); ////버튼을 담고 있는 레이아웃
        btn_detectdone.setVisibility(View.GONE);

        //*프로그레스 바
        detectComplete = (ProgressBar)meas.findViewById(R.id.progressDetecting);

        //**측정이 시작되면 보여지는 텍스트뷰
        heart_rate = (TextView)meas.findViewById(R.id.heart_rate); // 심장박동수를 보여주는 텍스트 뷰
        follow_message = (TextView)meas.findViewById(R.id.message); // 동작하는 부분을 보여주는 텍스트 뷰 ex) 측정중입니다. 측정시 움직이지 마세요

        tv_measriiv = (TextView)meas.findViewById(R.id.tv_measriiv);


        //그래프 setting
        mChart = (LineChart)meas.findViewById(R.id.heartGraph);

        //LineChart chart = new LineChart(mContext);
        mChart.setOnChartValueSelectedListener(this);
        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        //data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setInverted(true);
        mChart.setAutoScaleMinMaxEnabled(true);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        //사용될 어레이리스트 생성
        Raw_data = new ArrayList<>();

        //심작박동 측정에 사용될 데이터
        heart_data = new ArrayList<>();
        tempforPeak = new ArrayList<>();
        peakPoint = new ArrayList<>();
        localMinX = new ArrayList<>();
        meaning_data = new ArrayList<>();
        ////ExponentialMovingAverage 알파값을 통해 MAF의 특성이 달라짐
        alpha = 0.05;
        heart_maf = new ArrayList<>();
        arraybpm = new ArrayList<>();
        //손가락 판단을 위한 ArrayList
        finger_date = new ArrayList<>();
        //호흡 filter
        //arrayListforrriiv = new ArrayList<>();

        red_IndeComp_array = new ArrayList<>();
        green_IndeComp_array = new ArrayList<>();
        blue_IndeComp_array = new ArrayList<>();
        return meas;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,getActivity(), mLoaderCallback);
        } else {
            Log.d(TAG, "onResume :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("생명주기","Pause");
        mOpenCvCameraView.disableView();//카메라를 꺼줌
        //쓰레드 제어부분-종료
        detectStart=false;
        if(setTextthread!=null)//null check
        {
            if(!setTextthread.isInterrupted())//isInterrupted check
            {
                setTextthread.interrupt();//텍스를 바꿔주는 쓰레드
            }
        }
        if(thread!=null)//null check
        {
            if(!thread.isInterrupted())//isInterrupted check
            {
                thread.interrupt();///데이터를 처리하는 쓰레드
            }
        }
        if(setHeartratethread!=null)//null check
        {
            if(!setHeartratethread.isInterrupted())//isInterrupted check
            {
                setHeartratethread.interrupt();//심박수를 갱신하는 쓰레드
            }
        }

        if(setProgressthread!=null)//null check
        {
            if(!setProgressthread.isInterrupted())//isInterrupted check
            {
                setProgressthread.interrupt();//심박수를 갱신하는 쓰레드
            }
        }

        btn_start.setVisibility(View.VISIBLE);
        follow_message.setText("측정 시작 버튼을 눌러주세요");//
        heart_rate.setText("--");
        ///그동안 사용했던 데이터 초기화
        heart_data.clear(); // 심장에 대한 데이터 clear
        heart_maf.clear(); // 심장에 valley를 구하기 위한 데이터 clear
        peakPoint.clear(); // peakPoint 초기화
        localMinX.clear(); // 윈도우를 정하기 위한 데이터 clear
        arraybpm.clear(); // 박동에 대한 데이터 clear
        W_size=20;//윈도우 사이즈 초기화
        X=0;
        meaning_data.clear();//FFT를 수행하기 위한 데이터 clear
        max_index=-1;//max_index초기화
        max_magnitude=Double.MIN_VALUE;//max_value초기화

        //FFT 측정 초기화
        phase1=false;
        phase2=false;
        phase3=false;
    }

    @Override
    public void onStop() {
        super.onStop();
//        Log.d("생명주기","Stop");
//        mOpenCvCameraView.disableView();//카메라를 꺼줌
//        //쓰레드 제어부분-종료
//        detectStart=false;
//        if(setTextthread!=null)//null check
//        {
//            if(!setTextthread.isInterrupted())//isInterrupted check
//            {
//                setTextthread.interrupt();//텍스를 바꿔주는 쓰레드
//            }
//        }
//        if(thread!=null)//null check
//        {
//            if(!thread.isInterrupted())//isInterrupted check
//            {
//                thread.interrupt();///데이터를 처리하는 쓰레드
//            }
//        }
//        if(setHeartratethread!=null)//null check
//        {
//            if(!setHeartratethread.isInterrupted())//isInterrupted check
//            {
//                setHeartratethread.interrupt();//심박수를 갱신하는 쓰레드
//            }
//        }
//
//        if(setProgressthread!=null)//null check
//        {
//            if(!setProgressthread.isInterrupted())//isInterrupted check
//            {
//                setProgressthread.interrupt();//심박수를 갱신하는 쓰레드
//            }
//        }
//
//        btn_start.setVisibility(View.VISIBLE);
//        follow_message.setText("측정 시작 버튼을 눌러주세요");//
//        heart_rate.setText("--");
//        ///그동안 사용했던 데이터 초기화
//        heart_data.clear(); // 심장에 대한 데이터 clear
//        heart_maf.clear(); // 심장에 valley를 구하기 위한 데이터 clear
//        peakPoint.clear(); // peakPoint 초기화
//        localMinX.clear(); // 윈도우를 정하기 위한 데이터 clear
//        arraybpm.clear(); // 박동에 대한 데이터 clear
//        W_size=20;//윈도우 사이즈 초기화
//        X=0;
//        meaning_data.clear();//FFT를 수행하기 위한 데이터 clear
//        max_index=-1;//max_index초기화
//        max_magnitude=Double.MIN_VALUE;//max_value초기화
//
//        //FFT 측정 초기화
//        phase1=false;
//        phase2=false;
//        phase3=false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("생명주기","Destroy");
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {////30FPS 샘플링 rate = 30Hz
        mOpenCvCameraView.setFrameRate(30,30);
        matInput = inputFrame.rgba();
        /////빨간색을 받아오는 부분
        if ( matResult != null )matResult.release();
        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        sum=greenDetection(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());//이부분에서 Frame에 대한 메모리를 해제 하면 redDetection이 수행되지 않음
        redsum=redDetection(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());//여기부분에서 input메모리를 해제 시켜줘야됨
        bluesum=blueDetection(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());
        /////움직임을 감지하는 부분
        previous = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        current = new Mat(matInput.rows(), matInput.cols(), matInput.type());

        /////현재영상과 이전 영상을 비교하는 부분
        if(previous!=null)//이전 프레임이 비어있으면
        {
            current=matInput;///인풋을 현재 프레임에 넣어주고
        }else{
            current=matInput;///인풋을 현재 프레임에 넣어주고
            previous = current;//현재프레임을 이전프레임으로 넣어주고
        }
        move_point=moveDetection(previous.getNativeObjAddr(),current.getNativeObjAddr());//현재프레임과 이전프레임을 비교

        previous = current;/////비교후에 현재프레임을 이전 프레임에 넣어줌
        ////움직이지 않을때 - 손가락을 갖다댔을 때 값이 3 나옴(실험 결과)
        ////손가락을 갖다댔을 때와 평상시인데 움직임이 없는 경우를 구분해야됨
        //return matInput;
        return inputFrame.rgba();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "심장박동 데이터");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        //set.setHighLightColor(Color.rgb(244, 117, 117));
        //set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        return set;
    }

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(),(float)Math.abs(filtered_Raw/100)), 0);///그래프에 데이터를 넣는 부분
            //data.addEntry(new Entry(set.getEntryCount(),(float) average(Math.abs(filtered_Raw/100))), 0);///MOVING AVERAGE 거친 데이터 그래프
            //data.addEntry(new Entry(set.getEntryCount(),(float)Math.abs(filtered_forriiv/100)), 0);///호흡신호 측정하기 위한 데이터를 넣는 부분
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(150);

            //mChart.setVisibleYRangeMinimum(1, YAxis.AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    //Exponential Moving Average - alpha =0.03
    public double average(double value) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
        return newValue;

    }

    private void feedMultiple() {//33ms second마다 sleep

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                if(moving==2)//움직이지 않을때 - 33ms 단위로 넣어줌 30Hz sampling rate
                {
                    filtered_Raw=filter(sum);//1~3Hz BandPass Filter
                    //System.out.println((int)Math.abs(filtered_Raw/1000));//**************심장박동 출력보기**************
                    heart_data.add((int)Math.abs(filtered_Raw/100)); ///peak 값을 찾기 위해 ArrayList에 넣어줌
                    heart_maf.add((int)average(Math.abs(filtered_Raw/100)));
                    Raw_data.add((double)sum/100);

                    //호흡을 구하기 위한 ArrayList - 실시간 처리안함
                    red_IndeComp_array.add((double)redsum/100);
                    green_IndeComp_array.add((double)sum/100);
                    blue_IndeComp_array.add((double)bluesum/100);

                    handler.sendEmptyMessage(setprogress);//진행상황 반영

                    ////초기에 데이터가 많이 튄다. 그러므로 50전까지는 데이터를 버린다.
                    ////2초정도
                    ////교정을 어떻게 할까? 고민해볼것
                    ///윈도우 크기 교정하는 부분
                    if(heart_data.size()<=128)
                    {
                        handler.sendEmptyMessage(detectGo);//시작 핸들러

                        ////윈도우 크기를 정해주는 부분
                        if(heart_maf.size()>2)////
                        {
                            if(heart_maf.get(X-2)>=heart_maf.get(X-1)&&heart_maf.get(X-1)<=heart_maf.get(X))////실시간으로 valley 값을 구해줌
                            {
                                valleyX = X-1;
                                localMinX.add(valleyX);
                            }

                            if(localMinX.size()>2)
                            {
                                W_size=localMinX.get(localMinX.size()-1)-localMinX.get(localMinX.size()-2);//가장 최근의 윈도우의값을 구해줌
                            }

                            if(W_size<10||W_size>60)
                            {
                                W_size = 20;
                            }
                            //System.out.println("윈도우 크기: "+W_size);
                        }
                    }

                    if(heart_data.size()>128)////W_size를 구할 시간을 줘야됨
                    {
                        detectStart=true;/////시작하는 부분
                        is_ready=true;///준비중 메세지를 그만띄움
                        //데이터의 크기가 윈도우 크기 만큼 찼을 때까지 0일 수는 없으니
                        //W_size로 나머지를 구해주면 배수일 때 옮겨주게 됨
                        //데이터는 0~W_size,W_size~2*W_size 만큼 갖고오게됨
                        //X는 들어오는 데이터임
                        //진짜로 측정시작하는 데이터

                        if(heart_data.size()%W_size==0)/////지금 데이터의 갯수가 이전 윈도우가 정해졌을때보다 윈도우만큼 켜져있다면
                        //W_size가 바뀌면서 갑자기 나눠질수도 있음-2017.07.20 예를들면 윈도우 사이즈가 50이다가 51로 바뀌었을때 heart_data.size가 51이면 데이터가 충분하지 않은데 바로 나눠짐
                        {
                            for(int i=heart_data.size()-W_size;i<heart_data.size()-1;i++)
                            {
                                tempforPeak.add(heart_data.get(i));/////i부터
                            }

                            max=Collections.max(tempforPeak);
                            peak=tempforPeak.indexOf(max);///피크를 구하기 위해 필요함
                            System.out.println("피크를 구하기 위한 array:"+tempforPeak);
                            //System.out.println("최대값 : "+(peak+heart_data.size()-W_size));
                            //System.out.println("여기부터 : "+(heart_data.size()-W_size)+"저기까지 : "+(heart_data.size()-1));
                            //System.out.println("heart_data:"+heart_data);
                            //System.out.println(heart_data);
                            peakPoint.add(peak+heart_data.size()-W_size);//////array안에서 최고값이므로 평소 데이터에서 X값을 구해야됨
                            //System.out.println("피크들의 X값:"+peakPoint);////피크값들의 X값
                        }
                        tempforPeak.clear();////피크를 위해 저장했던 ArrayList 초기화

                        ////윈도우 크기를 정해주는 부분
                        if(heart_maf.size()>2)////
                        {
                            if(heart_maf.get(X-2)>=heart_maf.get(X-1)&&heart_maf.get(X-1)<=heart_maf.get(X))////실시간으로 valley 값을 구해줌
                            {
                                valleyX = X-1;
                                localMinX.add(valleyX);
                            }

                            if(localMinX.size()>2)
                            {
                                W_size=localMinX.get(localMinX.size()-1)-localMinX.get(localMinX.size()-2);//가장 최근의 윈도우의값을 구해줌
                            }

                            if(W_size<10||W_size>60)
                            {
                                W_size = 20;
                            }
                        }

                        if(peakPoint.size()>2)/////피크의 간격을 구하는 부분
                        {
                            peakInterval=0;//피크 간격 초기화
                            for(int i=1;i<peakPoint.size();i++)
                            {
                                peakInterval+=peakPoint.get(i)-peakPoint.get(i-1);
                            }
                            peakInterval=peakInterval/(peakPoint.size()-1);
                            //그때그때마다 구해주는 방향
                            bpm = (int)((1/peakInterval)*sampling_rate*minute);//bpm을 구하는 식은 (1/PPI)*60초*샘플링 레이트 = BPM;
                            int sumbpm=0;
                            arraybpm.add(bpm);
                            for(int i=0;i<arraybpm.size();i++) //평균 심박수에 대한 데이터 구하기
                            {
                                sumbpm+=arraybpm.get(i);
                            }
                            avebpm = sumbpm/arraybpm.size();
                            //System.out.println((float)peakInterval);
                        }

                        //손가락인지 판단하는 알고리즘
                        //peak 끝점부터 시작점의 길이가 128 이상 이면서 FFT를 진행하지 않았을 때
                        ///*************************128포인트 FFT***********************////
                        if(peakPoint.size()>=3)//피크가 3개이상 있을때
                        {
                            if(peakPoint.get(peakPoint.size()-1)-peakPoint.get(0)>=128&&!phase1)/////끝에서 처음까지의 데이터가 128개이상
                            {
                                finger_fft = new DoubleFFT_1D(128);
                                fft_finger_rate = new double[256];
                                for(int i=peakPoint.get(0);i<peakPoint.get(peakPoint.size()-1);i++)
                                {
                                    finger_date.add((double)heart_data.get(i));/////fft 판단을 위한 데이터 복사
                                }

                                //DC를 제거해주기 위해 평균을 구함
                                int fingersum=0;
                                for(int i=0;i<finger_date.size();i++)
                                {
                                    fingersum+=finger_date.get(i);
                                }

                                for(int i=0;i<finger_date.size();i++)//신호에서 평균값제거
                                {
                                    finger_date.set(i,finger_date.get(i)-fingersum/finger_date.size());
                                }
                                //fft를 수행하기 위한 배열에 옮겨담음
                                for(int i=0;i<128;i++)//
                                {
                                    fft_finger_rate[2*i]=finger_date.get(i);
                                    fft_finger_rate[2*i+1]=0;
                                }
                                //System.out.println("[피크]"+peakPoint);
                                //System.out.println(finger_date);
                                finger_fft.realForward(fft_finger_rate);

                                for(int i=0;i<128/2-1;i++)//magnitude 계산
                                {
                                    re_fi=fft_finger_rate[2*i];
                                    im_im=fft_finger_rate[2*i+1];
                                    finger_magnitude[i]=sqrt(re_fi*re_fi+im_im*im_im);
                                }

                                //System.out.println("fingerMagnitude"+Arrays.toString(finger_magnitude));
                                //크기를 비교하여 가장 큰값을 찾아냄
                                for(int i=0;i<128/2-1;i++)
                                {
                                    if((int)finger_magnitude[i]>(int)finger_max_magnitude)//부동소수점 연산때문에 Wrapper 클래스의 compare method를 사용해야함
                                    {
                                        finger_max_magnitude = finger_magnitude[i];
                                        finger_index = i;
                                    }
                                }
                                //Sampling_rate=30
                                phase1_freq = (double)finger_index*sampling_rate/64;//Magnitude가 가장 큰 index를 통해 주파수를 구해줌
                                System.out.println("손가락주파수:"+phase1_freq+"phase1");

                                //초기화 시켜줌
                                finger_date.clear();
                                finger_index=-1;
                                finger_max_magnitude=Double.MIN_VALUE;
                                phase1=true;//더이상 검사를 수행하지 않도록

                                if(phase1_freq<0 || phase1_freq>=3)
                                {
                                    handler.sendEmptyMessage(detectStop);
                                }
                            }

                            ///**********************256포인트 FFT*************************////
                            if(peakPoint.get(peakPoint.size()-1)-peakPoint.get(0)>=256&&!phase2)/////끝에서 처음까지의 데이터가 256개이상
                            {
                                finger_fft = new DoubleFFT_1D(256);
                                fft_finger_rate = new double[512];
                                for(int i=peakPoint.get(0);i<peakPoint.get(peakPoint.size()-1);i++)
                                {
                                    finger_date.add((double)heart_data.get(i));/////fft 판단을 위한 데이터 복사
                                }

                                //DC를 제거해주기 위해 평균을 구함
                                int fingersum=0;
                                for(int i=0;i<finger_date.size();i++)
                                {
                                    fingersum+=finger_date.get(i);
                                }

                                for(int i=0;i<finger_date.size();i++)//신호에서 평균값제거
                                {
                                    finger_date.set(i,finger_date.get(i)-fingersum/finger_date.size());
                                }
                                //fft를 수행하기 위한 배열에 옮겨담음
                                for(int i=0;i<256;i++)//
                                {
                                    fft_finger_rate[2*i]=finger_date.get(i);
                                    fft_finger_rate[2*i+1]=0;
                                }
                                //System.out.println("[피크]"+peakPoint);
                                //System.out.println(finger_date);
                                finger_fft.realForward(fft_finger_rate);

                                for(int i=0;i<256/2-1;i++)//magnitude 계산
                                {
                                    re_fi=fft_finger_rate[2*i];
                                    im_im=fft_finger_rate[2*i+1];
                                    finger_magnitude[i]=sqrt(re_fi*re_fi+im_im*im_im);
                                }

                                //System.out.println("fingerMagnitude"+Arrays.toString(finger_magnitude));
                                //크기를 비교하여 가장 큰값을 찾아냄
                                for(int i=0;i<256/2-1;i++)
                                {
                                    if((int)finger_magnitude[i]>(int)finger_max_magnitude)//부동소수점 연산때문에 Wrapper 클래스의 compare method를 사용해야함
                                    {
                                        finger_max_magnitude = finger_magnitude[i];
                                        finger_index = i;
                                    }
                                }
                                //Sampling_rate=30
                                phase2_freq = (double)finger_index*sampling_rate/128;//Magnitude가 가장 큰 index를 통해 주파수를 구해줌
                                System.out.println("손가락주파수:"+phase2_freq+"phase2");

                                //초기화 시켜줌
                                finger_date.clear();
                                finger_index=-1;
                                finger_max_magnitude=Double.MIN_VALUE;
                                phase2=true;//더이상 검사를 하지 않도록함

                                if(phase2_freq<0 || phase2_freq>=2.16)
                                {
                                    handler.sendEmptyMessage(detectStop);
                                }
                            }

                            ///***********************512포인트 FFT*************************///
                            if(peakPoint.get(peakPoint.size()-1)-peakPoint.get(0)>=512&&!phase3)/////끝에서 처음까지의 데이터가 512개이상 - 끝나고 수행하는 point와 같음
                            {
                                finger_fft = new DoubleFFT_1D(512);
                                fft_finger_rate = new double[1024];
                                for(int i=peakPoint.get(0);i<peakPoint.get(peakPoint.size()-1);i++)
                                {
                                    finger_date.add((double)heart_data.get(i));/////fft 판단을 위한 데이터 복사
                                }

                                //DC를 제거해주기 위해 평균을 구함
                                int fingersum=0;
                                for(int i=0;i<finger_date.size();i++)
                                {
                                    fingersum+=finger_date.get(i);
                                }

                                for(int i=0;i<finger_date.size();i++)//신호에서 평균값제거
                                {
                                    finger_date.set(i,finger_date.get(i)-fingersum/finger_date.size());
                                }
                                //fft를 수행하기 위한 배열에 옮겨담음
                                for(int i=0;i<512;i++)//
                                {
                                    fft_finger_rate[2*i]=finger_date.get(i);
                                    fft_finger_rate[2*i+1]=0;
                                }
                                //System.out.println("[피크]"+peakPoint);
                                //System.out.println(finger_date);
                                finger_fft.realForward(fft_finger_rate);

                                for(int i=0;i<512/2-1;i++)//magnitude 계산
                                {
                                    re_fi=fft_finger_rate[2*i];
                                    im_im=fft_finger_rate[2*i+1];
                                    finger_magnitude[i]=sqrt(re_fi*re_fi+im_im*im_im);
                                }

                                //System.out.println("fingerMagnitude"+Arrays.toString(finger_magnitude));
                                //크기를 비교하여 가장 큰값을 찾아냄
                                for(int i=0;i<512/2-1;i++)
                                {
                                    if((int)finger_magnitude[i]>(int)finger_max_magnitude)//부동소수점 연산때문에 Wrapper 클래스의 compare method를 사용해야함
                                    {
                                        finger_max_magnitude = finger_magnitude[i];
                                        finger_index = i;
                                    }
                                }
                                //Sampling_rate=30
                                phase3_freq = (double)finger_index*sampling_rate/256;//Magnitude가 가장 큰 index를 통해 주파수를 구해줌
                                System.out.println("손가락주파수:"+phase3_freq+"phase3");

                                //초기화 시켜줌
                                finger_date.clear();
                                finger_index=-1;
                                finger_max_magnitude=Double.MIN_VALUE;
                                //System.out.println(finger_date.size()+"끝났을때");
                                phase3=true;//더이상 검사를 하지 않도록 함

                                if(phase3_freq<0 || phase3_freq>=2.16)
                                {
                                    handler.sendEmptyMessage(detectStop);
                                }
                            }

                        }

                    }

                    X++;
                    addEntry();
                }
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {//try-catch를 통해 쓰레드 인터럽트
                        while(heart_data.size()<size)///데이터가 1000개가 쌓일때까지 - 기준 생각해보기
                        {
                        // Don't generate garbage runnables inside the loop.
                            getActivity().runOnUiThread(runnable);
                            Thread.sleep(33);/////데이터 넣는 속도-카메라 프레임과 동기화
                        }   //30FPS이므로 30Hz 1초에 30개의 데이터-이 값들을 다 넣어주는게 아니라 33ms 단위로 넣어줌-그래프에
                        handler.sendEmptyMessage(detectDone);
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        handler.sendEmptyMessage(final_heartrate);///마지막 심장박동수를 띄어줌
                        e.printStackTrace();
                    }

            }
        });
        thread.start();
    }

    public class setTextthread extends Thread
    {
        @Override
        public void run() {
            try {
                while(true)
                {
                    if(detectStart)
                    {
                        if(move_point<1000)//움직이지 않을때
                        {
                            handler.sendEmptyMessage(no_moving);
                        }else{////움직일때
                            handler.sendEmptyMessage(is_moving);//그래프에 데이터를 넣지 않음
                        }
                        sleep(1000);///1초에 한번씩 체크
                    }
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(final_heartrate);///마지막 심장박동수를 띄어줌
            }
        }
    }

    public class setHeartratethread extends Thread
    {
        @Override
        public void run() {
            try {
                while(true)
                {
                    if(detectStart)
                    {
                        handler.sendEmptyMessage(update_heartrate);//심장박동수를 갱신해주는 메시지
                        sleep(500);///0.5초에 한번씩 체크
                    }
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(final_heartrate);///마지막 심장박동수를 띄어줌

            }
        }
    }

    public class setProgressthread extends Thread//프로그레스바를 갱신해주는 쓰레드
    {
        @Override
        public void run() {
            try {
                while(true)
                {
                    if(detectStart)
                    {
                        handler.sendEmptyMessage(setprogress);//프로그레스바 갱신
                        sleep(100);///0.1초에 한번씩 체크
                    }
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(final_heartrate);///마지막 심장박동수를 띄어줌
            }
        }
    }

    private class infoSaveTask extends AsyncTask<String,String,String>
    {
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("true"))
            {
               Toast.makeText(getActivity(), "결과를 기록하였습니다.", Toast.LENGTH_SHORT).show();
               getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frag_container_, new Fragment_result())
                            .commit();
               //버튼 색변화
               mainBtn_meas.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.electrocardiogram, 0, 0);
               mainBtn_result.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.list_focus, 0, 0);
            }else{
                Toast.makeText(getActivity(), "결과를 기록하는데 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/healthinfosave.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);
                //쿠키매니저에 저장되어있는 세션 쿠키를 사용하여 통신
                if (Login.msCookieManager.getCookieStore().getCookies().size() > 0) {
                    // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                    con.setRequestProperty("Cookie", TextUtils.join(",", Login.msCookieManager.getCookieStore().getCookies()));
                    System.out.println(Login.msCookieManager.getCookieStore().getCookies()+"Request");
                }
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
                System.out.println(result);

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

    ////////움직임 알림/측정중/측정완료 알려줌
     Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case detectGo:{
                    btn_start.setVisibility(View.GONE);///시작 버튼을 누르면 사라짐
                    btn_detectdone.setVisibility(View.GONE);
                    heart_rate.setVisibility(View.VISIBLE);
                    follow_message.setVisibility(View.VISIBLE);
                    //FFT 측정 초기화
                    phase1=false;
                    phase2=false;
                    phase3=false;
                    phase1_freq=100;
                    phase2_freq=100;
                    phase3_freq=100;
                    break;
                }

                case is_moving: {
                        follow_message.setText("측정중에는 움직이지 말아주세요");
                        moving = is_moving;
                    break;
                }

                case no_moving: {

                    moving = no_moving;
                    if(detectComplete.getProgress()<=10)
                    {
                        follow_message.setText("측정을 준비중입니다");
                    }
                    else if(detectComplete.getProgress()<=60)
                    {
                        follow_message.setText("측정중입니다");
                        tv_measriiv.setText("측정중입니다");
                    }
                    if(detectComplete.getProgress()>60)//50%가 넘어가면
                    {
                        follow_message.setText("거의 측정이 완료되었습니다");
                    }
                    break;
                }

                case setprogress: {
                    detectComplete.setMax(100);
                    detectComplete.setProgress((heart_data.size() /10));
                    detectComplete.invalidate();
                    break;
                }

                case detectDone:
                {
                    btn_detectdone.setVisibility(View.VISIBLE);//측정 완료 시에 보여주는 레이아웃
                    btn_detectdone.bringToFront(); ////FrameLayout에서 가장 앞쪽에 위치하도록 해줌
                    follow_message.setText("측정 완료");//측정 지시 메시지 안보이게
                    //평균데이터를 빼주지 않으면 DC가 포함되어 있는 것이므로

                    fft = new DoubleFFT_1D(512);//meaning_data 만큼
                    fft_heart_rate = new double[1024];//실수부 허수부 고려

                    //peak 값 이후로 평가해야 쓰레기 데이터가 포함되지 않는다.
                    //peak가 처음 발견된 직후부터 마지막 peak 까지의 데이터만 가지고 평가

                    //처음 peak부터 마지막 peak까지 옮겨담음
                    for(int i=peakPoint.get(0);i<peakPoint.get(peakPoint.size()-1);i++)//FFT를 수행하기 위하여 배열에 복사
                    {
                        meaning_data.add((double)heart_data.get(i));////
                    }

                    //DC를 제거 해주기 위하여 평균을 구해 줌
                    int data_sum=0;
                    for(int i=0;i<meaning_data.size();i++)
                    {
                        data_sum+=meaning_data.get(i);
                    }

                    int average=data_sum/meaning_data.size();

                    //평균을 제외한 값으로 치환해줌
                    for(int i=0;i<meaning_data.size();i++)
                    {
                        meaning_data.set(i,meaning_data.get(i)-average);
                    }
                    //System.out.println(meaning_data);
                    for(int i=0;i<512;i++)//FFT를 수행하기 위하여 배열에 복사
                    {
                        fft_heart_rate[2*i]=meaning_data.get(i);////
                        fft_heart_rate[2*i+1]=0;
                    }
                    //--*FFT 변환
                    //System.out.println("testValue"+Arrays.toString(fft_heart_rate));
                    fft.realForward(fft_heart_rate);
                    //System.out.println("fft 복소수"+Arrays.toString(fft_heart_rate));

                    //--**변환된 값으로부터 Magnitude 계산
                    for(int i=0;i<512/2-1;i++)
                    {
                        re = fft_heart_rate[2*i];
                        im = fft_heart_rate[2*i+1];
                        magnitude[i] = sqrt(re*re+im*im);
                    }
                    //System.out.println("Magnitude"+Arrays.toString(magnitude));
                    //크기를 비교하여 가장 큰값을 찾아냄
                    for(int i=0;i<512/2-1;i++)
                    {
                        if((int)magnitude[i]>(int)max_magnitude)//부동소수점 연산때문에 Wrapper 클래스의 compare method를 사용해야함
                        {
                            max_magnitude = magnitude[i];
                            max_index = i;
                        }
                    }
                    //Sampling_rate=30
                    double freq = (double)max_index*sampling_rate/256;//Magnitude가 가장 큰 index를 통해 주파수를 구해줌
                    System.out.println("주파수:"+freq);
                    fft_heart = (int)(freq*60);
                    System.out.println("fft_heart"+fft_heart);
                    if(fft_heart>=60)
                    {
                        final_heart=(fft_heart+avebpm)/2;///Peak detection을 통해 구한 값과 FFT를 통해 구한값의 평균을 통해 최종 심박수 구함
                    }else{
                        final_heart=avebpm;///fft를 통해 나온 값이 0이게 되면 Peak detection을 통해 구한 값을 최종값으로 선정
                    }
                    System.out.println("final_heart "+final_heart);

                    ///그동안 사용했던 데이터 초기화
                    heart_data.clear(); // 심장에 대한 데이터 clear
                    heart_maf.clear(); // 심장에 valley를 구하기 위한 데이터 clear
                    peakPoint.clear(); // peakPoint 초기화
                    localMinX.clear(); // 윈도우를 정하기 위한 데이터 clear
                    arraybpm.clear(); // 박동에 대한 데이터 clear
                    meaning_data.clear();//FFT를 수행하기 위한 데이터 clear
                    //arrayListforrriiv.clear();//호흡에 사용된 Array초기화
                    Raw_data.clear();//raw data clear
                    W_size=20;//윈도우 사이즈 초기화

                    max_index=-1;//max_index초기화
                    max_magnitude=Double.MIN_VALUE;//max_value초기화

                    mOpenCvCameraView.turnFlashOff();//플래시를 꺼줌
                    mOpenCvCameraView.disableView();//카메라를 꺼줌

                    //FFT 측정 초기화
                    phase1=false;
                    phase2=false;
                    phase3=false;

                    //호흡 처리
                    List<Double> testing_red = red_IndeComp_array.subList(73,1023);
                    List<Double> testing_green = green_IndeComp_array.subList(73,1023);
                    List<Double> testing_blue = blue_IndeComp_array.subList(73,1023);

                    //StackOverflow 경고
                    //필요한 이유 : 측정시간이 짧기 때문에 적당한 크기로 반복적으로 늘려줄 필요가 있음
                    for(int i=0;i<2;i++)
                    {
                        testing_red.addAll(testing_red);
                        testing_green.addAll(testing_green);
                        testing_blue.addAll(testing_blue);
                    }

                    double[] red_Array=new double[testing_red.size()];
                    double[] green_Array=new double[testing_green.size()];
                    double[] blue_Array=new double[testing_blue.size()];

                    for(int count=0;count<testing_red.size();count++)//testing_red사이즈 만큼 계속 넣어줌 4회
                    {
                        //ArrayList에 있는 원소를 옮겨담음
                        red_Array[count]=testing_red.get(count);//RED
                        green_Array[count]=testing_green.get(count);//GREEN
                        blue_Array[count]=testing_blue.get(count);
                    }

                    final double[][] icadata = new double[][]{
                            red_Array
                            ,green_Array
                            ,blue_Array
                    };

                    System.out.println("2차원 배열(행)"+icadata.length);
                    System.out.println("2차원 배열(열)"+icadata[0].length);
                    /*TODO ICA의 가정이 틀렸을 수도 있다는 의문(2017.09.16)
                    *  ICA의 명제는 A1 A2의 혼합된 신호가 있을 때
                    *  독립된 신호 a1 a2를 구할 수 있다.
                    *  A1과 A2가 혼합된 신호가 아니거나 상관없는 신호일 경우에
                    *  a1과 a2를 구할 수 없다.*/
                    //TODO 논문을 찾아보면 RGB를 3개의 성분을 사용하여 ICA를 구현한다.(2017.09.16)
                    /* 이를 적용하여 값을 찾아냄
                    *  값의 정확성은 테스트를 해봐야됨
                    * */
                    //output = FastIca.fastICA(icadata,10,0.01,3);

                    try {
                        FastICA fastICA = new FastICA(new LogCosh(),1E-4, 1000, true);
                        fastICA.fit(icadata,3);
                        System.out.println("ICA 수행완료");
                        output=fastICA.getEM();
                        Butterworth butterworth_low = new Butterworth();
                        butterworth_low.lowPass(2,30,1);

                        if(output!=null){
                            for(int i=0;i<icadata[0].length;i++)//filter를 통해 처리해줌
                            {
                                output[0][i]=butterworth_low.filter(output[0][i]);
                                output[1][i]=butterworth_low.filter(output[1][i]);
                                output[2][i]=butterworth_low.filter(output[2][i]);
                            }

                            System.out.println("필터링 완료");
                            double[] resValue=new double[3];
                            try{
                                for(int j=0;j<3;j++)
                                {
                                    System.out.println(findFFTmax(output[j])+"여기");
                                    resValue[j]=findFFTmax(output[j]);//호흡과 관련된 최대값에 대한 값을 배열에 넣어줌
                                }

                                int[] calculateRes = new int[3];

                                System.out.println(resValue[0]*60.0);
                                System.out.println(resValue[1]*60.0);
                                System.out.println(resValue[2]*60.0);

                                calculateRes[0]= (int)(resValue[0]*60.0);
                                calculateRes[1]= (int)(resValue[1]*60.0);
                                calculateRes[2]= (int)(resValue[2]*60.0);

                                System.out.println("1 : "+calculateRes[0]+" 2 : "+calculateRes[1]);

                                ///호흡수 필터를 씌운 값이기 때문에 더 작은 것을 호흡속도로 생각
                                if(calculateRes[0]<=calculateRes[1])//1,2를 비교
                                {
                                    averes=calculateRes[0];//작은값을 넣어줌
                                    if(calculateRes[0]<=calculateRes[2])//1,3을 비교
                                    {
                                        averes=calculateRes[0];//작은 값
                                    }else{
                                        averes=calculateRes[3];//3이 가장 작게 됨
                                    }
                                }else{
                                    averes=calculateRes[1];
                                    if(calculateRes[1]<=calculateRes[2])//1,3을 비교
                                    {
                                        averes=calculateRes[1];//작은 값
                                    }else{
                                        averes=calculateRes[2];//3이 가장 작게 됨
                                    }
                                }

                                System.out.println(averes+"호흡값");
                                if(averes>60)//만약 정해진 값이 쓰레기값 420이렇게 나오면 0으로 만들어줌
                                {
                                    averes=0;
                                }

                                if(averes==0)//호흡측정에 실패하면
                                {
                                    Toast.makeText(mContext,"   호흡측정 실패\n다시 측정해주세요",Toast.LENGTH_SHORT).show();
                                }

                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(mContext,"호흡 알고리즘 수행실패\n다시 측정해주세요",Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    testing_red.clear();
                    testing_green.clear();
                    //쓰레드 제어부분
                    if(setTextthread!=null)//null check
                    {
                        if(!setTextthread.isInterrupted())//isInterrupted check
                        {
                            setTextthread.interrupt();//텍스를 바꿔주는 쓰레드
                        }
                    }
                    if(thread!=null)//null check
                    {
                        if(!thread.isInterrupted())//isInterrupted check
                        {
                            thread.interrupt();///데이터를 처리하는 쓰레드
                        }
                    }

                    if(setProgressthread!=null)//null check
                    {
                        if(!setProgressthread.isInterrupted())//isInterrupted check
                        {
                            setProgressthread.interrupt();///데이터를 처리하는 쓰레드
                        }
                    }
                    ///**심박수를 갱신하는 쓰레드를 가장 늦게 종료 - 이유 마지막 보정 심박수값을 출력시켜줘야됨
                    if(setHeartratethread!=null)//null check
                    {
                        if(!setHeartratethread.isInterrupted())//isInterrupted check
                        {
                            setHeartratethread.interrupt();//심박수를 갱신하는 쓰레드
                        }
                    }



                    break;
                }

                case update_heartrate:
                {
                    if(avebpm!=0)///Peak detection으로 찾은 심박수가 0이 아닐때
                    {
                        heart_rate.setText(avebpm+" BPM");
                    }
                    else{
                        heart_rate.setText("--");
                    }
                    break;
                }

                case detectStop:
                {
                    //쓰레드 제어부분
                    is_Stop=true;
                    if(setTextthread!=null)//null check
                    {
                        if(!setTextthread.isInterrupted())//isInterrupted check
                        {
                            setTextthread.interrupt();//텍스트를 바꿔주는 쓰레드
                        }
                    }
                    if(thread!=null)//null check
                    {
                        if(!thread.isInterrupted())//isInterrupted check
                        {
                            thread.interrupt();///데이터를 처리하는 쓰레드
                        }
                    }
                    if(setHeartratethread!=null)//null check
                    {
                        if(!setHeartratethread.isInterrupted())//isInterrupted check
                        {
                            setHeartratethread.interrupt();//심박수를 갱신하는 쓰레드
                        }
                    }
                    ////--Stop이 불릴 경우 쓰레드부터 종류시켜줌 - 쓰레드가 돌면서 값을 계속 추가해서 OutofIndex 발생
                    btn_start.setVisibility(View.VISIBLE);
                    follow_message.setText("손가락을 정확한 위치에 놓고 다시 측정해주세요");//
                    heart_rate.setText("--X");
                    ///그동안 사용했던 데이터 초기화
                    heart_data.clear(); // 심장에 대한 데이터 clear
                    heart_maf.clear(); // 심장에 valley를 구하기 위한 데이터 clear
                    peakPoint.clear(); // peakPoint 초기화
                    localMinX.clear(); // 윈도우를 정하기 위한 데이터 clear
                    arraybpm.clear(); // 박동에 대한 데이터 clear
                    W_size=20;//윈도우 사이즈 초기화

                    meaning_data.clear();//FFT를 수행하기 위한 데이터 clear
                    max_index=-1;//max_index초기화
                    max_magnitude=Double.MIN_VALUE;//max_value초기화

                    //FFT 측정 초기화
                    phase1=false;
                    phase2=false;
                    phase3=false;

                    mOpenCvCameraView.turnFlashOff();//플래시를 꺼줌
                    mOpenCvCameraView.disableView();//카메라를 꺼줌

                    break;
                }

                case final_heartrate:
                {
                    if(!is_Stop)
                    {
                        detectStart=false;
                        heart_rate.setText(final_heart+" BPM");
                        tv_measriiv.setText(averes+" 회/분");
                        follow_message.setText("측정 완료");//측정 지시 메시지 안보이게
                        //날짜 지정해주는 부분
                        now = System.currentTimeMillis();
                        date = new Date(now);
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        getTime = sdf.format(date);

                    }else{
                        detectStart=false;
                        btn_start.setVisibility(View.VISIBLE);
                        follow_message.setText("손가락을 정확한 위치에 놓고 \n 다시 측정해주세요");//
                        heart_rate.setText("--X");
                        tv_measriiv.setText("--X");
                    }

                }
            }
        }
    };

    public double findFFTmax(double[] value)//scale을 정하는게 나으려나??
    {
        int nPoint=4096;
        DoubleFFT_1D fft_1D=new DoubleFFT_1D(nPoint);
        double[] resdata = new double[nPoint];//fft를 위해 사용하는 자료

        double real_part;
        double image_part;
        double[] fft_magnitude = new double[nPoint/2];
        double fft_maxmag=Double.MIN_VALUE;
        int max_fftindex=-1;
        int data_sum=0;
        int data_average;

        ArrayList<Double> fft_mag=new ArrayList<>();
//        for (double aValue : value) {
//            data_sum += aValue;
//        }
//
//        data_average=data_sum/value.length;//값들의 평균을 구해서 빼줌
//        System.out.println(data_average+"데이터 평균");
//        for(int i=0;i<value.length;i++)//FFT를 수행하기 위하여 배열에 복사
//        {
//            value[i]= value[i]-data_average;////평균값을 빼줌
//        }

        for(int i=0;i<nPoint/2;i++)//FFT를 수행하기 위하여 배열에 복사
        {
            resdata[2*i]= value[i];////
            resdata[2*i+1]=0;
        }
        //--*FFT 변환

        fft.realForward(resdata);

        //--**변환된 값으로부터 Magnitude 계산
        for(int i=0;i<nPoint/2-1;i++)
        {
            real_part = resdata[2*i];
            image_part = resdata[2*i+1];
            fft_magnitude[i] = sqrt(real_part*real_part+image_part*image_part);
        }
        System.out.println("Magnitude"+ Arrays.toString(magnitude));
        //크기를 비교하여 가장 큰값을 찾아냄
        for(int i=5;i<65;i++)//fft magnitude에서 값을 결정해줌
        {
            fft_mag.add(fft_magnitude[i]);
        }

        fft_maxmag=Collections.max(fft_mag);
        max_fftindex = fft_mag.indexOf(fft_maxmag);
        System.out.println(max_fftindex+"maxindex");
        ///필터적인 개념으로 7을 빼주었는데 이는 index를 나타내고 있는 것이므로 다시더해서 복구해야함
        return (double)(max_fftindex+5)*sampling_rate/(nPoint/2);
    }
}

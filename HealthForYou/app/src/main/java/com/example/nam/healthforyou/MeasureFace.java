package com.example.nam.healthforyou;


import android.content.Context;
import android.graphics.Color;
import android.media.FaceDetector;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


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
import com.tzutalin.dlib.FaceDet;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


import java.util.ArrayList;
import java.util.Collections;

public class MeasureFace extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, OnChartValueSelectedListener {

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }
    static {
        if(!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    Context mContext;
    FaceDet faceDet;
    private static final String TAG = "opencv";
    //private CameraBridgeViewBase mOpenCvCameraView;
    private javaViewCameraControl mOpenCvCameraView;
    ////카메라 영상 받아오는 부분
    private Mat matInput;
    private Mat matResult;
    int sum;//intensity의 합
    int moving;
    float filtered_beat;//필터이후에 값
    ////이전이미지와 현재이미지 비교
    private Mat previous=null;
    private Mat current=null;

    ////핸들러 메세지 정의
    static final int is_moving=1;
    static final int no_moving=2;
    TextView message;

    ////그래프
    private LineChart mChart;//차트
    private Thread thread;//값 넣어주는 부분

    private int peakX=0;//피크의 x축값
    private int valleyX=0;

    private int W_size=20;
    private ArrayList<Integer> peakPoint;
    private ArrayList<Integer> tempforPeak;//피크를 구하기위한 ArrayList
    private ArrayList<Integer> tempforValley;//밸리를 구하기위한 ArrayList

    private ArrayList<Integer> localMinX;//피크를 구하기위한 ArrayList

    int localmin;
    int peak=0;
    int X=0;

    int max=0;

    int count=0;//데이터를 세는데 필요

    private YAxis leftAxis;
    private ArrayList<Integer> heart_data; ///피크값을 구하기 위해 그래프 말고 Array에도 데이터를 넣어줌

    //ExponentialMovingAverage maf;
    private ArrayList<Integer> heart_maf; ///피크값을 구하기 위해 그래프 말고 Array에도 데이터를 넣어줌
    double alpha;
    Double oldValue;
    double peakInterval;
    ////네이티브 메소드
    //public native int redDetection(long matAddrInput, long matAddrResult);
    //public native int moveDetection(long previous,long current);

    ////필터 부분
    double filtered_Raw;
    private final int N = 32;
    private int n = 0;
    private double[] x = new double[N];
    private final double[] h =
    {
            -0.00825998710050537990,
            -0.00094549491400912290,
            0.00162817839503944160,
            -0.01104320682553394000,
            -0.03522777356983981100,
            -0.05215865024345721400,
            -0.04496990747950474500,
            -0.01987572938995437300,
            -0.00796052088164146510,
            -0.03759964387008083600,
            -0.09790882454086007000,
            -0.13180532798007569000,
            -0.07790881312870971700,
            0.06745778393854905100,
            0.22802929229187494000,
            0.29801216506635481000,
            0.22802929229187494000,
            0.06745778393854905100,
            -0.07790881312870971700,
            -0.13180532798007569000,
            -0.09790882454086007000,
            -0.03759964387008083600,
            -0.00796052088164146510,
            -0.01987572938995437300,
            -0.04496990747950474500,
            -0.05215865024345721400,
            -0.03522777356983981100,
            -0.01104320682553394000,
            0.00162817839503944160,
            -0.00094549491400912290,
            -0.00825998710050537990,
            -0.00865680610025201280
    };

    public double filter(double x_in)
    {
        double y = 0.0;

        //Store the current input, overwriting the oldest input
        x[n] = x_in;

        // Multiply the filter coefficients by the previous inputs and sum
        for (int i=0; i<N; i++)
        {
            y += h[i] * x[((N - i) + n) % N];
        }

        // Increment the input buffer index to the next location
        n = (n + 1) % N;

        return y;
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_measure);
        //////////////OPENCV
        mOpenCvCameraView =(javaViewCameraControl)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setAlpha(0);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mOpenCvCameraView.setMaxFrameSize(200, 200);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        faceDet = new FaceDet();

        //////////////////////측정순서 제어
        Button btn_start = (Button)findViewById(R.id.start_measure);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.turnFlashOn();
                feedMultiple();
            }
        });
        ///측정의 정확성을 위해서 사용자에게 메세지를 알려줄 핸들러
        message = (TextView)findViewById(R.id.message);

        //CHART setting
        mChart = (LineChart)findViewById(R.id.heartGraph);

        //LineChart chart = new LineChart(mContext);
        mChart.setOnChartValueSelectedListener(this);
        // enable description text
        mChart.getDescription().setEnabled(true);

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
        data.setValueTextColor(Color.WHITE);

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

        leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        mChart.setAutoScaleMinMaxEnabled(true);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        heart_data = new ArrayList<>();

        tempforPeak = new ArrayList<>();
        peakPoint = new ArrayList<>();

        tempforValley = new ArrayList<>();
        localMinX = new ArrayList<>();
        ////ExponentialMovingAverage 알파값을 생성자에 넣어줘야됨
        alpha = 0.03;
        heart_maf = new ArrayList<>();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mOpenCvCameraView.setFrameRate(30,30);
        matInput = inputFrame.rgba();
        /////빨간색을 받아오는 부분
        if ( matResult != null ) matResult.release();
        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        //sum=redDetection(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

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
        //int move_point=moveDetection(previous.getNativeObjAddr(),current.getNativeObjAddr());//현재프레임과 이전프레임을 비교
        previous = current;/////비교후에 현재프레임을 이전 프레임에 넣어줌
        ////움직이지 않을때 - 손가락을 갖다댔을 때 값이 3 나옴(실험 결과)
        ////손가락을 갖다댔을 때와 평상시인데 움직임이 없는 경우를 구분해야됨
//        if(move_point<100)
//        {
//            handler.sendEmptyMessage(no_moving);
//        }else{////움직일때
//            handler.sendEmptyMessage(is_moving);//그래프에 데이터를 넣지 않음
//        }
        return matInput;
    }
    ////////움직임 알림/측정중/측정완료 알려줌
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case is_moving:
                {
                    message.setText("측정중에는 움직이지 말아주세요");
                    moving=is_moving;
                    break;
                }

                case no_moving:
                {
                    message.setText("측정중입니다");
                    moving=no_moving;
                    break;
                }
            }
        }
    };

    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "심장박동 데이터");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
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

            //data.addEntry(new Entry(set.getEntryCount(),(float)Math.abs(filtered_Raw/100)), 0);///그래프에 데이터를 넣는 부분
            data.addEntry(new Entry(set.getEntryCount(),(float)average(Math.abs(filtered_Raw/100))), 0);

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(100);
            //mChart.setVisibleYRangeMinimum(1, YAxis.AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    public double average(double value) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
        return newValue;
    }

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                if(moving==2)//움직이지 않을때
                {
                    filtered_Raw=filter(sum);//1~5Hz BandPass Filter
                    //System.out.println((int)Math.abs(filtered_Raw/1000));//**************심장박동 출력보기**************

                    heart_data.add((int)Math.abs(filtered_Raw/100)); ///peak 값을 찾기 위해 ArrayList에 넣어줌
                    heart_maf.add((int)average(Math.abs(filtered_Raw/100)));
                    //System.out.println((int)average(Math.abs(filtered_Raw/100)));
                    ////초기에 데이터가 많이 튄다. 그러므로 50전까지는 데이터를 버린다.
                    ////2초정도
                    ////교정을 어떻게 할까? 고민해볼것
                    if(heart_data.size()>50)
                    {
                        //데이터의 크기가 윈도우 크기 만큼 찼을 때까지 0일 수는 없으니
                        //W_size로 나머지를 구해주면 배수일 때 옮겨주게 됨
                        //데이터는 0~W_size,W_size~2*W_size 만큼 갖고오게됨
                        //X는 들어오는 데이터임

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

                            System.out.println("피크들의 X값:"+peakPoint);////피크값들의 X값
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
                            //tempforValley.clear();
                            if(localMinX.size()>2)
                            {
                                W_size=localMinX.get(localMinX.size()-1)-localMinX.get(localMinX.size()-2);
                                count=heart_data.size();//////윈도우 사이즈가 정해졌을때 데이터의 갯수
                            }

                            if(W_size<10||W_size>60)
                            {
                                W_size = 20;
                            }

                            System.out.println("윈도우 크기: "+W_size);
                        }

                        if(peakPoint.size()>2)/////피크의 간격을 구하는 부분
                        {
                            peakInterval=0;//피크 간격 초기화
                            for(int i=1;i<peakPoint.size();i++)
                            {
                                peakInterval+=peakPoint.get(i)-peakPoint.get(i-1);
                            }
                            peakInterval=peakInterval/(peakPoint.size()-1);
                            System.out.println((float)peakInterval);
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
                for (int i = 0; i < 15000; i++) {/////데이터를 15000개만 넣어줌

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(33);/////데이터 넣는 속도-카메라 프레임과 동기화
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
}

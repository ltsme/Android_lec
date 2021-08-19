/*
 * Copyright 2016-present Tzutalin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nam.healthforyou;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.Message;
import android.os.Trace;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import junit.framework.Assert;

import org.jtransforms.fft.DoubleFFT_1D;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import filter.Butterworth;
import filter.DirectFormAbstract;
import filter.Filter;
import filter.MovingAverage;
import util.thirdparty.weka_plugin.FastICA;
import util.thirdparty.weka_plugin.LogCosh;

import static android.view.View.GONE;
import static java.lang.Math.sqrt;

/**
 * Class that takes in preview frames and converts the image to Bitmaps to process with dlib lib.
 */
public class OnGetImageListener implements OnImageAvailableListener, OnChartValueSelectedListener {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private int mFramenum;

    private long detectStartTime;
    private long detectDoneTime;
    private int sampling_rate=10;

    public native int redDetection(long matAddrInput, long matAddrResult);
    public native int greenDetection(long matAddrInput, long matAddrResult);
    public native int hueDetection(long matAddrInput, long matAddrResult);


    private static final boolean SAVE_PREVIEW_BITMAP = false;

    private static final int INPUT_SIZE = 160;//크기를 정해줌 - 해상도가 떨어지면 얼굴 검출이 안됨
    private static final String TAG = "OnGetImageListener";

    private int mScreenRotation = 90;
    private List<VisionDetRet> results;//얼굴찾는 부분에 대한 결과값
    private int mPreviewWdith = 0;
    private int mPreviewHeight = 0;
    private byte[][] mYUVBytes;
    private int[] mRGBBytes = null;
    private Bitmap mRGBframeBitmap = null;
    private Bitmap mCroppedBitmap = null;

    private boolean mIsComputing = false;
    private Handler mInferenceHandler;

    private Context mContext;
    private FaceDet mFaceDet;

    //측정 관련 레이아웃
    private TextView tv_faceheartrate;//심박수를 나타내는 텍스트 뷰
    private Button btn_control;//측정을 제어하는 버튼
    private Button btn_resultsave;//결과를 저장하라는 버튼
    private ProgressBar pb_detect;//진행되는 정도를 나타내주는 프로그레스바
    private TextView tv_follow;//측정을 도와주는 안내메세지
    private Activity activity;//뷰들이 위치한 액티비티 - 뷰를 접근할 수 있는건 메인쓰레드
    private LineChart chart;//그래프를 그리는 차트
    /////측정관련 변수
    private boolean is_start=false;//시작을 나타냄
    int face_heartrate;//최종적으로 나올 심박수

    final static int initView=0;
    private final static int facePPGstart=1;
    private final static int facePPGdone=2;
    private final static int cannotFindFace=3;
    private final static int canFindFace=4;
    private final static int facePPGdetecting=5;

    private int avebp;

    private int progresspercentage=0;

    private int redsumleft;
    private int greensumleft;
    private int huesumleft;

    private int redsumright;
    private int greensumright;
    private int huesumright;

    int mframeNum;

    double[][] face_signal_leftrect=new double[3][512];//최종적으로 나오는 신호 - 왼쪽 뺨 네모 RGB
    double[][] face_signal_rightrect=new double[3][512];//최종적으로 나오는 신호 - 오른쪽 뺨 네모 RGB
    double[][] face_signal_leftcheek;//최종적으로 나오는 신호 - 왼쪽 뺨
    double[][] face_signal_rightcheek;//최종적으로 나오는 신호 - 오른쪽 뺨

    private List<Double> MaxmagnitudeList = new ArrayList<>();
    private List<Double> lastSignal = new ArrayList<>();//최종적으로 심박수값이라 결정된 신호
    //Moving Average filter를 위한 변수


    //얼굴의 위치를 나타냄
    private Point leftface;
    private Point leftnose;

    private Point rightface;
    private Point rightnose;

    //왼쪽뺨에 대한 위치
    private Point leftcheek;

    //오른쪽 뺨에 대한 위치
    private Point rightcheek;

    private Rect leftrect;

    private Rect rightrect;


    private FloatingCameraWindow mWindow;
    private Paint mFaceLandmardkPaint;
    private Paint mFaceLandmarkRect;
    private Paint mFaceRoiMark;
    private Paint mCheekRoimark;
    private Bitmap mResizedBitmap = null;
    private Bitmap mInversedBipmap = null;
    private Bitmap roiBitmap= null;

    public void initialize(
            final Context context,
            final AssetManager assetManager,
            final TrasparentTitleView scoreView,
            final Handler handler,
            final TextView tv_faceheartrate,//심박수 텍스트뷰
            final Button btn_control,//버튼 제어
            final Button btn_resultsave,//버튼 결과저장
            final ProgressBar pb_detect,//프로그레스바
            final TextView tv_follow,
            final Activity activity,
            final LineChart chart) {//안내메세지 텍스트뷰

        this.mContext = context;
        //this.mTransparentTitleView = scoreView;
        this.mInferenceHandler = handler;

        /////측정관련/////
        this.tv_faceheartrate=tv_faceheartrate;
        this.btn_control=btn_control;
        this.pb_detect=pb_detect;
        this.btn_resultsave=btn_resultsave;
        this.tv_follow=tv_follow;
        this.activity=activity;
        this.chart=chart;

        chart.setOnChartValueSelectedListener(this);
        btn_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_start=true;
                facehandler.sendEmptyMessage(facePPGstart);
            }
        });
        facehandler.sendEmptyMessage(initView);//차트를 초기화
        mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
        mWindow = new FloatingCameraWindow(mContext);

        mFaceLandmardkPaint = new Paint();
        mFaceLandmardkPaint.setColor(Color.GREEN);
        mFaceLandmardkPaint.setStrokeWidth(2);
        mFaceLandmardkPaint.setStyle(Paint.Style.STROKE);

        mFaceLandmarkRect = new Paint();
        mFaceLandmarkRect.setColor(Color.BLUE);
        mFaceLandmarkRect.setStrokeWidth(2);
        mFaceLandmarkRect.setStyle(Paint.Style.STROKE);

        mFaceRoiMark = new Paint();
        mFaceRoiMark.setColor(Color.RED);
        mFaceRoiMark.setStrokeWidth(2);
        mFaceRoiMark.setStyle(Paint.Style.STROKE);

        mCheekRoimark = new Paint();
        mCheekRoimark.setAntiAlias(true);
        mCheekRoimark.setColor(Color.CYAN);
        mCheekRoimark.setStrokeWidth(1);
        mCheekRoimark.setStyle(Paint.Style.STROKE);
    }

    public void deInitialize() {
        synchronized (OnGetImageListener.this) {
            if (mFaceDet != null) {
                mFaceDet.release();
            }

            if (mWindow != null) {
                mWindow.release();
            }
        }
    }

    private void drawResizedBitmap(final Bitmap src, final Bitmap dst) {

        Display getOrient = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        Point point = new Point();
        getOrient.getSize(point);
        int screen_width = point.x;
        int screen_height = point.y;
        Log.d(TAG, String.format("screen size (%d,%d)", screen_width, screen_height));
        if (screen_width < screen_height) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
            mScreenRotation = -90;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
            mScreenRotation = 0;
        }

        Assert.assertEquals(dst.getWidth(), dst.getHeight());
        final float minDim = Math.min(src.getWidth(), src.getHeight());

        final Matrix matrix = new Matrix();

        // We only want the center square out of the original rectangle.
        final float translateX = -Math.max(0, (src.getWidth() - minDim) / 2);
        final float translateY = -Math.max(0, (src.getHeight() - minDim) / 2);
        matrix.preTranslate(translateX, translateY);

        final float scaleFactor = dst.getHeight() / minDim;
        matrix.postScale(scaleFactor, scaleFactor);

        // Rotate around the center if necessary.
        if (mScreenRotation != 0) {
            matrix.postTranslate(-dst.getWidth() / 2.0f, -dst.getHeight() / 2.0f);
            matrix.postRotate(mScreenRotation);
            matrix.postTranslate(dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
        }

        final Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, matrix, null);
    }

    public Bitmap imageSideInversion(Bitmap src){
        Matrix sideInversion = new Matrix();
        sideInversion.setScale(-1, 1);
        Bitmap inversedImage = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), sideInversion, false);
        return inversedImage;
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            // No mutex needed as this method is not reentrant.
            if (mIsComputing) {
                image.close();
                return;
            }
            mIsComputing = true;

            Trace.beginSection("imageAvailable");

            final Plane[] planes = image.getPlanes();

            // Initialize the storage bitmaps once when the resolution is known.
            if (mPreviewWdith != image.getWidth() || mPreviewHeight != image.getHeight()) {
                mPreviewWdith = image.getWidth();
                mPreviewHeight = image.getHeight();
                Log.d(TAG, String.format("Initializing at size %dx%d", mPreviewWdith, mPreviewHeight));
                mRGBBytes = new int[mPreviewWdith * mPreviewHeight];
                mRGBframeBitmap = Bitmap.createBitmap(mPreviewWdith, mPreviewHeight, Config.ARGB_8888);
                mCroppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

                mYUVBytes = new byte[planes.length][];
                for (int i = 0; i < planes.length; ++i) {
                    mYUVBytes[i] = new byte[planes[i].getBuffer().capacity()];
                }
            }

            for (int i = 0; i < planes.length; ++i) {
                planes[i].getBuffer().get(mYUVBytes[i]);
            }

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            ImageUtils.convertYUV420ToARGB8888(
                    mYUVBytes[0],
                    mYUVBytes[1],
                    mYUVBytes[2],
                    mRGBBytes,
                    mPreviewWdith,
                    mPreviewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    false);

            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            Log.e(TAG, "Exception!", e);
            Trace.endSection();
            return;
        }

        mRGBframeBitmap.setPixels(mRGBBytes, 0, mPreviewWdith, 0, 0, mPreviewWdith, mPreviewHeight);
        drawResizedBitmap(mRGBframeBitmap, mCroppedBitmap);
        mInversedBipmap = imageSideInversion(mCroppedBitmap);

//        Mat InversedMat = new Mat(mInversedBipmap.getHeight(),mInversedBipmap.getWidth(),CvType.CV_8UC4);
//        Mat InversedMatResult = new Mat(mInversedBipmap.getHeight(),mInversedBipmap.getWidth(),CvType.CV_8UC3);
//        Utils.bitmapToMat(mInversedBipmap,InversedMat);
//        Imgproc.cvtColor(InversedMat,InversedMatResult,Imgproc.COLOR_RGB2BGR);
//
//        mResizedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);
//        Utils.matToBitmap(InversedMatResult,mResizedBitmap);

        mInferenceHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!new File(Constants.getFaceShapeModelPath()).exists()) {
                            //mTransparentTitleView.setText("Copying landmark model to " + Constants.getFaceShapeModelPath());
                            FileUtils.copyFileFromRawToOthers(mContext, R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath());
                        }

                        long startTime = System.currentTimeMillis();
                        synchronized (OnGetImageListener.this) {
                            results = mFaceDet.detect(mInversedBipmap);
                        }
                        long endTime = System.currentTimeMillis();
                        //mTransparentTitleView.setText("Time cost: " + String.valueOf((endTime - startTime) / 1000f) + " sec");
                        //System.out.println((endTime - startTime) / 1000f);
                        // Draw on bitmap

                        if (results.size()!=0) {
                            for (final VisionDetRet ret : results) {
                                float resizeRatio = 1.0f;
                                RectF bounds = new RectF();
                                int rectSizeindex = 3;
                                bounds.left = ((ret.getLeft() + rectSizeindex) * resizeRatio);
                                bounds.top = ((ret.getTop() - rectSizeindex) * resizeRatio);
                                bounds.right = ((ret.getRight() - rectSizeindex) * resizeRatio);
                                bounds.bottom = ((ret.getBottom() - rectSizeindex) * resizeRatio);
                                Canvas canvas = new Canvas(mInversedBipmap);
                                //canvas.drawRoundRect(bounds,10,10,mFaceLandmarkRect);

                                // Draw landmark
                                ArrayList<Point> landmarks = ret.getFaceLandmarks();
                                for (Point point : landmarks) {
                                    int pointX = (int) (point.x * resizeRatio);
                                    int pointY = (int) (point.y * resizeRatio);
                                    canvas.drawCircle(pointX, pointY, 0.5f, mFaceLandmardkPaint);
                                }

                                leftface = landmarks.get(2);
                                leftnose = landmarks.get(31);

                                rightface = landmarks.get(14);
                                rightnose = landmarks.get(35);

                                //왼쪽뺨에 대한 위치
                                leftcheek = new Point();
                                leftcheek.x = (leftface.x + leftnose.x) / 2;
                                leftcheek.y = (leftface.y + leftnose.y) / 2;

                                //오른쪽 뺨에 대한 위치
                                rightcheek = new Point();
                                rightcheek.x = (rightface.x + rightnose.x) / 2;
                                rightcheek.y = (rightface.y + rightnose.y) / 2;

                                //왼쪽 뺨에 대해서 사각형을 그려줌
                                leftrect = new Rect();
                                leftrect.left = leftcheek.x - 5;
                                leftrect.right = leftcheek.x + 5;
                                leftrect.top = leftcheek.y + 5;
                                leftrect.bottom = leftcheek.y - 5;

                                canvas.drawRect(leftrect, mCheekRoimark);

                                //오른쪽 뺨에 대해서 사각형을 그려줌
                                rightrect = new Rect();
                                rightrect.left = rightcheek.x - 5;
                                rightrect.right = rightcheek.x + 5;
                                rightrect.top = rightcheek.y + 5;
                                rightrect.bottom = rightcheek.y - 5;

                                canvas.drawRect(rightrect, mCheekRoimark);

                                //양쪽 볼에 뺨에 대한 처리
                                if(is_start)
                                {
                                    if(mframeNum==0)
                                    {
                                        detectStartTime=System.currentTimeMillis();
                                    }

                                    Bitmap leftBM=ImageUtils.cropBitmapuseCanvasRectCheek(mInversedBipmap,leftrect);
                                    Bitmap rightBM=ImageUtils.cropBitmapuseCanvasRectCheek(mInversedBipmap,rightrect);
                                    Mat leftmat = new Mat(leftBM.getHeight(),leftBM.getWidth(),CvType.CV_8UC3);
                                    Mat rightmat = new Mat(rightBM.getHeight(),rightBM.getWidth(),CvType.CV_8UC3);
                                    Utils.bitmapToMat(leftBM,leftmat);
                                    Utils.bitmapToMat(rightBM,rightmat);
                                    redsumleft=redDetection(leftmat.getNativeObjAddr(),leftmat.getNativeObjAddr())+redDetection(rightmat.getNativeObjAddr(),rightmat.getNativeObjAddr());
                                    greensumleft = greenDetection(leftmat.getNativeObjAddr(),leftmat.getNativeObjAddr())+greenDetection(rightmat.getNativeObjAddr(),rightmat.getNativeObjAddr());
                                    huesumleft = hueDetection(leftmat.getNativeObjAddr(),leftmat.getNativeObjAddr())+hueDetection(rightmat.getNativeObjAddr(),rightmat.getNativeObjAddr());

                                    if(mframeNum>511)//512 의 데이터를 받음
                                    {
//                                        detectDoneTime=System.currentTimeMillis();
//                                        System.out.println(detectDoneTime-detectStartTime+"측정시간ms");
                                        if(is_start)//처음 한번만 끝마치는 것을 호출하기 위한 조건문
                                        {
                                            facehandler.sendEmptyMessage(facePPGdone);//측정을 끝마침
                                        }

                                    }else{
                                        //RGB 순
                                        face_signal_leftrect[0][mframeNum]=redsumleft;
                                        face_signal_leftrect[1][mframeNum]=greensumleft;
                                        face_signal_leftrect[2][mframeNum]=huesumleft;

                                    }
                                    mframeNum++;
                                    Log.d("facePPG",redsumleft+"");
                                    Log.d("facePPG",greensumleft+"");
                                    Log.d("facePPG",huesumleft+"");
                                    System.out.println(mframeNum+"길이");
                                    facehandler.sendEmptyMessage(facePPGdetecting);//측정중임을 나타내는 Frame 갱신
                                }

                                //TODO 왼쪽 뺨
                                Canvas roicanvas = new Canvas(mInversedBipmap);

                                Path leftarea = new Path();
                                //1번으로 옮겨감
                                leftarea.moveTo(landmarks.get(1).x, landmarks.get(1).y);
                                //2~6 왼쪽얼굴외곽선
                                leftarea.lineTo(landmarks.get(2).x, landmarks.get(2).y);
                                leftarea.lineTo(landmarks.get(3).x, landmarks.get(3).y);
                                leftarea.lineTo(landmarks.get(4).x, landmarks.get(4).y);
                                leftarea.lineTo(landmarks.get(5).x, landmarks.get(5).y);
                                leftarea.lineTo(landmarks.get(6).x, landmarks.get(6).y);

                                //입꼬리부터 왼쪽 콧망울을 따라감
                                leftarea.lineTo(landmarks.get(48).x, landmarks.get(48).y);
                                leftarea.lineTo(landmarks.get(31).x, landmarks.get(31).y);

                                //눈선을 따라감
                                leftarea.lineTo(landmarks.get(39).x, landmarks.get(39).y);
                                leftarea.lineTo(landmarks.get(40).x, landmarks.get(40).y);
                                leftarea.lineTo(landmarks.get(41).x, landmarks.get(41).y);
                                leftarea.lineTo(landmarks.get(36).x, landmarks.get(36).y);

                                //끝마침
                                leftarea.close();
                                //ROI 설정
                                roicanvas.drawPath(leftarea, mFaceRoiMark);

                                //TODO 오른쪽 뺨
                                Path rightarea = new Path();

                                rightarea.moveTo(landmarks.get(15).x, landmarks.get(15).y);

                                //1번으로 옮겨감

                                //14~10 오른쪽얼굴외곽선

                                rightarea.lineTo(landmarks.get(14).x, landmarks.get(14).y);
                                rightarea.lineTo(landmarks.get(13).x, landmarks.get(13).y);
                                rightarea.lineTo(landmarks.get(12).x, landmarks.get(12).y);
                                rightarea.lineTo(landmarks.get(11).x, landmarks.get(11).y);
                                rightarea.lineTo(landmarks.get(10).x, landmarks.get(10).y);
                                //입꼬리부터 오른쪽 콧망울을 따라감
                                rightarea.lineTo(landmarks.get(54).x, landmarks.get(54).y);
                                rightarea.lineTo(landmarks.get(35).x, landmarks.get(35).y);

                                //눈선을 따라감
                                rightarea.lineTo(landmarks.get(42).x, landmarks.get(42).y);
                                rightarea.lineTo(landmarks.get(47).x, landmarks.get(47).y);
                                rightarea.lineTo(landmarks.get(46).x, landmarks.get(46).y);
                                rightarea.lineTo(landmarks.get(45).x, landmarks.get(45).y);

                                rightarea.close();
                                roicanvas.drawPath(rightarea, mFaceRoiMark);
                            }

                            if(!is_start)//시작하지 않았을때
                            {
                                facehandler.sendEmptyMessage(canFindFace);
                            }

                        }else{
                            //System.out.println("얼굴을 찾지못함");
                            facehandler.sendEmptyMessage(cannotFindFace);
                        }
                        mWindow.setRGBBitmap(mInversedBipmap);
                        mIsComputing = false;
                    }
                });

        Trace.endSection();
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

    private Handler facehandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what)
            {
                case initView:
                {
                    //LineChart chart = new LineChart(mContext);

                    // enable description text
                    chart.getDescription().setEnabled(false);

                    // enable touch gestures
                    chart.setTouchEnabled(true);
                    // enable scaling and dragging
                    chart.setDragEnabled(true);
                    chart.setScaleEnabled(false);
                    chart.setDrawGridBackground(false);

                    // if disabled, scaling can be done on x- and y-axis separately
                    chart.setPinchZoom(false);

                    // set an alternative background color
                    chart.setBackgroundColor(Color.WHITE);

                    LineData data = new LineData();
                    //data.setValueTextColor(Color.WHITE);

                    // add empty data
                    chart.setData(data);

                    // get the legend (only possible after setting data)
                    Legend l = chart.getLegend();

                    // modify the legend ...
                    l.setForm(Legend.LegendForm.LINE);
                    l.setTextColor(Color.WHITE);

                    XAxis xl = chart.getXAxis();
                    xl.setTextColor(Color.WHITE);
                    xl.setDrawGridLines(false);
                    xl.setAvoidFirstLastClipping(false);
                    xl.setEnabled(false);
                    xl.setDrawLabels(false);

                    YAxis leftAxis = chart.getAxisLeft();
                    leftAxis.setTextColor(Color.WHITE);
                    leftAxis.setInverted(true);
                    chart.setAutoScaleMinMaxEnabled(true);
                    leftAxis.setDrawGridLines(false);
                    leftAxis.setDrawLabels(false);
                    YAxis rightAxis = chart.getAxisRight();
                    rightAxis.setEnabled(false);

                    break;
                }


                case facePPGstart:
                {
                    //버튼제어 부분
                    btn_control.setVisibility(GONE);
                    tv_follow.setText("움직이지 마세요");
                    tv_faceheartrate.setText("--");
                    break;
                }

                case facePPGdetecting://진행중임
                {
                    progresspercentage = mframeNum*100/512;
                    pb_detect.setProgress(progresspercentage);
                    if(progresspercentage>60)
                    {
                        tv_follow.setText("측정이 거의 완료되었습니다");
                    }else if(progresspercentage>30){
                        tv_follow.setText("움직일 경우 측정의 오차가 생길 수 있습니다");
                    }

                    break;
                }

                case facePPGdone:{
                    mframeNum=0;//프레임 넘버 초기화
                    btn_control.setVisibility(View.VISIBLE);
                    tv_follow.setText("측정이 완료되었습니다");
                    is_start=false;//측정종료

                    final double[][] icadata = new double[][]{
                            face_signal_leftrect[0]
                            ,face_signal_leftrect[1]
                            ,face_signal_leftrect[2]
                    };


                    try {
                        FastICA fastICA = new FastICA(new LogCosh(),1E-4, 1000, true);
                        fastICA.fit(icadata,3);

                        System.out.println("ICA 수행완료");

                        double[][] output=fastICA.getEM();

                        if(output!=null){
                            Filter filter = new Filter();
                            //a 와 b 는 filter 계수값
                            BigDecimal[] a = new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(-2.94186766992504), BigDecimal.valueOf(4.70231728846433), BigDecimal.valueOf(-4.63410965621041), BigDecimal.valueOf(3.07744779365828), BigDecimal.valueOf(-1.24661968102405), BigDecimal.valueOf(0.278059917634546)};
                            BigDecimal[] b = new BigDecimal[]{BigDecimal.valueOf(0.0180989330075146), BigDecimal.valueOf(0), BigDecimal.valueOf(-0.0542967990225439), BigDecimal.valueOf(0), BigDecimal.valueOf(0.0542967990225439), BigDecimal.valueOf(0), BigDecimal.valueOf(-0.0180989330075146)};
                            MovingAverage maf = new MovingAverage(10);
                            for(int i=0;i<output.length;i++)//3개의 분리된 신호에 대해서
                            {
                                BigDecimal[] bigDecimals = new BigDecimal[output[0].length];//입력값
                                BigDecimal[] outDecimals = new BigDecimal[output[0].length];//아웃풋
                                for(int j=0;j<bigDecimals.length;j++)
                                {
                                    bigDecimals[j]= BigDecimal.valueOf(output[i][j]);
                                    System.out.println(bigDecimals[j]);
                                }

                                outDecimals=filter.filter(b,a,bigDecimals);//필터를 통해 지나와서

                                for(int k=0;k<bigDecimals.length;k++)
                                {
                                    output[i][k]=outDecimals[k].doubleValue();
                                    //MAF 수행
                                    maf.newNum(output[i][k]);
                                    output[i][k]=maf.getAvg();
                                }
                            }

                            System.out.println(Arrays.toString(output[0]));
                            System.out.println(Arrays.toString(output[1]));
                            System.out.println(Arrays.toString(output[2]));

                            System.out.println("필터링 완료");

                            double[] bpValue=new double[3];

                            try{
                                for(int j=0;j<3;j++)
                                {
                                    bpValue[j]=findFFTmax(output[j]);//호흡과 관련된 최대값에 대한 값을 배열에 넣어줌
                                }

                                int[] calculateBP = new int[3];

                                calculateBP[0]= (int)(bpValue[0]*60.0);
                                calculateBP[1]= (int)(bpValue[1]*60.0);
                                calculateBP[2]= (int)(bpValue[2]*60.0);
                                //012 순으로 값이 나옴
                                //Magnitude를 비교해야함

                                System.out.println("1 : "+calculateBP[0]+" 2 : "+calculateBP[1]+" 3: "+calculateBP[2]);

                                double Powerofsignal= Collections.max(MaxmagnitudeList);
                                avebp=calculateBP[MaxmagnitudeList.indexOf(Powerofsignal)];//가장 큰 파워를 갖고 있는 신호의 인덱스를 통해 심박수를 구함

                                //그래프에 최종신호에 대한 그래프를 그려주기 위하여 수행하는 작업
                                for(int t=0;t<output[MaxmagnitudeList.indexOf(Powerofsignal)].length;t++)
                                {
                                    lastSignal.add(output[MaxmagnitudeList.indexOf(Powerofsignal)][t]);//최종 신호 출력값을 ArrayList에 넣음
                                }

                                //그래프에 값을 넣어줌

                                for(int k=0;k<lastSignal.size();k++)
                                {
                                    addEntry(lastSignal.get(k));
                                }
                                //그래프에 값을 뿌려줌

                                System.out.println(avebp+"심박값");

                                if(avebp>150)//만약 정해진 값이 쓰레기값 420이렇게 나오면 0으로 만들어줌
                                {
                                    avebp=0;
                                }

                                if(avebp==0)//호흡측정에 실패하면
                                {
                                    Toast.makeText(mContext,"   심박측정 실패\n다시 측정해주세요",Toast.LENGTH_SHORT).show();
                                }else{
                                    tv_faceheartrate.setText(avebp+" BPM");
                                }

                            }catch(Exception e){
                                e.printStackTrace();
                                Toast.makeText(mContext,"심박 알고리즘 수행실패\n다시 측정해주세요",Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mContext,"심박 알고리즘 수행실패\n다시 측정해주세요",Toast.LENGTH_SHORT).show();
                    }

                    break;
                }

                case cannotFindFace:{
                    tv_follow.setText("얼굴을 찾을 수 없습니다");
                    break;
                }

                case canFindFace:{
                    tv_follow.setText("측정을 시작할 수 있습니다");
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

    public double findFFTmax(double[] value)//scale을 정하는게 나으려나??
    {
        int nPoint=482;
        DoubleFFT_1D fft=new DoubleFFT_1D(nPoint);
        double[] bpdata = new double[nPoint];//fft를 위해 사용하는 자료
        double[] testSignal = new double[482];
        double real_part;
        double image_part;
        double[] fft_magnitude = new double[nPoint/2];
        double fft_maxmag;
        int max_fftindex;
        //들어온 value에서 값을 잘라내야함
        List<Double> cutSignal = new ArrayList<>();
        for(int i=0;i<value.length;i++)
        {
            if(i>30)//값을 잘라내는 중
            {
                cutSignal.add(value[i]);
            }else{
                System.out.println("신호 필터링중");
            }
        }
        //잘라낸 신호를 다시 배열에 넣어줌
        for(int count=0;count<cutSignal.size();count++)
        {
            testSignal[count]=cutSignal.get(count);
        }

        ArrayList<Double> fft_mag=new ArrayList<>();

        for(int i=0;i<nPoint/2;i++)//FFT를 수행하기 위하여 배열에 복사
        {
            bpdata[2*i]= testSignal[i];////
            bpdata[2*i+1]=0;
        }
        //--*FFT 변환

        fft.realForward(bpdata);

        //--**변환된 값으로부터 Magnitude 계산
        for(int i=0;i<nPoint/2-1;i++)
        {
            real_part = bpdata[2*i];
            image_part = bpdata[2*i+1];
            fft_magnitude[i] = sqrt(real_part*real_part+image_part*image_part);
        }

        System.out.println("Magnitude"+ Arrays.toString(fft_magnitude));
        //크기를 비교하여 가장 큰값을 찾아냄
        for(int i=0;i<51;i++)//fft magnitude에서 값을 결정해줌 - //~120BPM
        {
            fft_mag.add(fft_magnitude[i]);
        }

        fft_maxmag=Collections.max(fft_mag);
        MaxmagnitudeList.add(fft_maxmag);/////가장 큰 Magnitude의 값을 리스트에 넣어줌
        max_fftindex = fft_mag.indexOf(fft_maxmag);
        fft_mag.clear();
        System.out.println((double)(max_fftindex)*sampling_rate/(256)+"maxindexFrequency");
        return (double)(max_fftindex)*(double)sampling_rate/(256.0);
    }

    private void addEntry(double value) {

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(),(float)(value)), 0);///그래프에 데이터를 넣는 부분
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(50);
            //mChart.setVisibleYRangeMinimum(1, YAxis.AxisDependency.LEFT);
            // move to the first entry
            chart.moveViewToX(0);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }


}

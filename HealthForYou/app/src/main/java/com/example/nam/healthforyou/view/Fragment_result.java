package com.example.nam.healthforyou.view;


import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.util.Myvalueformatter;
import com.example.nam.healthforyou.util.NetworkUtil;
import com.example.nam.healthforyou.R;
import com.example.nam.healthforyou.util.RequestHttpConnection;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by NAM on 2017-07-13.
 */

public class Fragment_result extends Fragment {
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    RelativeLayout mView;
    ViewPager mViewPager;
    ViewPagerAdapter myAdapter;
    DBhelper dBhelper;
    BarChart mybarChart;
    ArrayList<BarEntry> barEntries;
    BarDataSet dataset;
    float avgbpmPeople;
    BarData data;
    HttpURLConnection con;
    final static int update_graph=0;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (RelativeLayout)inflater.inflate(R.layout.frag_result,container,false);
        mViewPager = (ViewPager)mView.findViewById(R.id.health_viewpager);
        dotsLayout = (LinearLayout)mView.findViewById(R.id.layoutDots);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("????????????");//Action Bar?????? ??????
        myAdapter = new ViewPagerAdapter(getChildFragmentManager());
        dBhelper = new DBhelper(getActivity().getApplicationContext(),"healthforyou.db", null, 1);
        String strurl = "http://kakapo12.vps.phps.kr/averagehealdata.php";
        //?????? ?????? ???????????? ????????? ????????? ???
        JSONObject avedata = dBhelper.PrintMyAvgData("SELECT avg(user_bpm),avg(user_res),data_signdate from User_health;");///??????????????? ???????????? ?????? ?????? ?????? chat?????? ????????????
        System.out.println(avedata);

        mybarChart = (BarChart)mView.findViewById(R.id.mybarchart);
        mybarChart.getDescription().setEnabled(false);/////?????? ?????????
        mybarChart.setTouchEnabled(false);
        mybarChart.setPinchZoom(false);
        YAxis leftAxis = mybarChart.getAxisLeft();

        //X??? label??? ??????

        // the labels that should be drawn on the XAxis
        final String[] quarters = new String[] { "???", "??????"};

        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return quarters[(int) value];
            }

        };

        XAxis xAxis = mybarChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mybarChart.getAxisRight();
        rightAxis.setEnabled(false);
        //Y??????
        barEntries = new ArrayList<>();

        try {
            barEntries.add(new BarEntry(0f,avedata.getInt("user_bpm")));///?????? ???????????? ????????????

        } catch (JSONException e) {
            e.printStackTrace();
        }

        dataset = new BarDataSet(barEntries,"?????????");//Y????????? ??????
        dataset.setValueTextSize(15);
        data = new BarData(dataset);
        data.setBarWidth(0.3f);
        ////????????? ????????? ?????? ????????????
        int network_status= NetworkUtil.getConnectivityStatus(getActivity().getApplicationContext());
        if(network_status == NetworkUtil.TYPE_MOBILE|| network_status == NetworkUtil.TYPE_WIFI) {
            NetworkTask networkTask = new NetworkTask(strurl, null);
            networkTask.execute();
        }else{
            Toast.makeText(getActivity().getApplicationContext(),"????????? ??????????????? ??????????????????",Toast.LENGTH_SHORT).show();
            barEntries.add(new BarEntry(1f,0f));//
            dataset = new BarDataSet(barEntries,"?????????");//Y????????? ??????
            dataset.setValueTextSize(15);
            data = new BarData(dataset);
            data.setBarWidth(0.3f);
            mybarChart.setData(data);
            data.setValueFormatter(new Myvalueformatter());
            data.notifyDataChanged();
            dataset.notifyDataSetChanged();
            mybarChart.notifyDataSetChanged();
            mybarChart.invalidate();
        }
        mybarChart.setData(data);

        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.frag_today,
                R.layout.frag_week,
                R.layout.frag_month,
                };

        // adding bottom dots
        addBottomDots(0);

        mViewPager.setAdapter(myAdapter);

        try {
            URL url = new URL(strurl);
            con = (HttpURLConnection)url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dotsLayout.bringToFront();
        return mView;
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(getActivity());
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // ?????? ????????? ????????? ??????.
            RequestHttpConnection requestHttpURLConnection = new RequestHttpConnection();
            result = requestHttpURLConnection.request(url, values); // ?????? URL??? ?????? ???????????? ????????????.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ///????????? ????????? ?????? ????????????
            if(s!=null && !s.equals(""))//????????? ??????????????? ?????? ???????????? ????????? ??????????????? null??? ??????
            {
                if(!s.equals("????????? ??????"))
                {
                    System.out.println((int)Float.parseFloat(s));
                    barEntries.add(new BarEntry(1f,(int)Float.parseFloat(s)));////???????????? ????????? ?????????
                    dataset = new BarDataSet(barEntries,"?????????");//Y????????? ??????
                    dataset.setValueTextSize(15);
                    data = new BarData(dataset);
                    data.setBarWidth(0.3f);
                    mybarChart.setData(data);
                    data.setValueFormatter(new Myvalueformatter());
                    data.notifyDataChanged();
                    dataset.notifyDataSetChanged();
                    mybarChart.notifyDataSetChanged();
                    mybarChart.invalidate();
                    //handler.sendEmptyMessage(update_graph);

                }
            }
        }
    }
}

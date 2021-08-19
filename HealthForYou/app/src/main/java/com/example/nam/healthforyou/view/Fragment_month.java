package com.example.nam.healthforyou.view;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.util.Myvalueformatter;
import com.example.nam.healthforyou.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by NAM on 2017-07-29.
 */

public class Fragment_month extends Fragment {
    BarChart barChart;
    DBhelper dBhelper;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_month, container, false);

        dBhelper = new DBhelper(getActivity().getApplicationContext(),"healthforyou.db", null, 1);
        //////////쿼리문 중요//////////////
        List<JSONObject> mydata = dBhelper.PrintAvgData("SELECT strftime('%Y%m',data_signdate) as date, avg(user_bpm),avg(user_res) from User_health WHERE date > strftime('%Y%m','now','localtime','-12 month') GROUP BY strftime('%Y%m',data_signdate) ORDER BY date desc limit 12;");

        System.out.println(mydata+"MONTH");

        int month;//월은 1부터 시작임
        String monthwhat="";

        barChart = (BarChart)view.findViewById(R.id.monthgraph);
        barChart.getDescription().setEnabled(false);/////라벨 없애줌
        barChart.setTouchEnabled(false);
        barChart.setPinchZoom(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setSpaceTop(2f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        //Y축값
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        //X축 label값 결정
        //X축값
        final String[] labels = new String[12];//

        Calendar c = Calendar.getInstance();////현재 날짜를 받아옴
        month = c.get(Calendar.MONTH)+1;/////1월의 경우 0을 받아오므로 +1

        //이번달을 가장 마지막에 두고 전달을 계산
        for(int i=labels.length-1;i>=0;i--)
        {
            switch(month)
            {
                case 1:
                    monthwhat = "1월";
                    break ;

                case 2:
                    monthwhat = "2월";
                    break ;

                case 3:
                    monthwhat = "3월";
                    break ;

                case 4:
                    monthwhat = "4월";
                    break ;

                case 5:
                    monthwhat = "5월";
                    break ;

                case 6:
                    monthwhat = "6월";
                    break ;

                case 7:
                    monthwhat = "7월";
                    break ;

                case 8:
                    monthwhat = "8월";
                    break;

                case 9:
                    monthwhat = "9월";
                    break;

                case 10:
                    monthwhat = "10월";
                    break;

                case 11:
                    monthwhat = "11월";
                    break;

                case 12:
                    monthwhat = "12월";
                    break;
            }
            labels[i]=monthwhat;
            month--;
            if(month==0) month=labels.length;/////다시 세줌
        }

        for(int i=0;i<labels.length;i++)
        {
            barEntries.add(new BarEntry(i,0));
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(12);

        String dataMonth = null;

        //Y축값
        //월에 맞는 값을 찾는 반복문
        if(mydata.size()!=0)//12달의 데이터가 있으면
        {
            for(int i=0;i<mydata.size();i++)
            {
                try {
                    month= MainActivity.getDateMonth(mydata.get(i).getString("data_signdate"),"yyyyMMdd");///데이터의 월을 받아와야됨
                    System.out.println(month+"DB긁어오는");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {//데이터의 월을 조사
                    switch(month)
                    {
                        case 1:
                            dataMonth = "1월";
                            break ;

                        case 2:
                            dataMonth = "2월";
                            break ;

                        case 3:
                            dataMonth = "3월";
                            break ;

                        case 4:
                            dataMonth = "4월";
                            break ;

                        case 5:
                            dataMonth = "5월";
                            break ;

                        case 6:
                            dataMonth = "6월";
                            break ;

                        case 7:
                            dataMonth = "7월";
                            break ;

                        case 8:
                            dataMonth = "8월";
                            break;

                        case 9:
                            dataMonth = "9월";
                            break;

                        case 10:
                            dataMonth = "10월";
                            break;

                        case 11:
                            dataMonth = "11월";
                            break;

                        case 12:
                            dataMonth = "12월";
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //x축에서 요일 비교 비교 - 지속적인 테스트 요망
                for(int j=0;j<labels.length;j++)
                {
                    if(labels[j].equals(dataMonth))/////지금 꺼낸 JSON object의 date를 X축과 비교하여 같다면
                    {
                        barEntries.set(j,new BarEntry(j,mydata.get(i).optInt("user_bpm")));//그 X축에 값을 넣어줌
                    }
                }
            }
        }///이미 기존에 0으로 채워놨기 때문에 처리할 필요가 없음

        BarDataSet dataset = new BarDataSet(barEntries,"심박수");//Y축값을 입력
        dataset.setValueTextSize(15);

        BarData data = new BarData(dataset);
        data.setBarWidth(0.3f);
        data.setValueFormatter(new Myvalueformatter());
        barChart.setData(data);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

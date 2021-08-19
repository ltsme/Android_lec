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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by NAM on 2017-07-29.
 */

public class Fragment_week extends Fragment {
    BarChart barChart;
    DBhelper dBhelper;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_week, container, false);
        /////현재시간을 받아옴
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyyMMdd", Locale.getDefault() );
        Date currentTime = new Date ();
        String mTime = mSimpleDateFormat.format (currentTime);
        ///현재 시간을 기준으로 일주일 전 데이터를 DB에서 평균을 내 구해옴
        dBhelper = new DBhelper(getActivity().getApplicationContext(),"healthforyou.db", null, 1);
        //////////쿼리문 중요//////////////
        List<JSONObject> mydata = dBhelper.PrintAvgData("SELECT strftime('%Y%m%d',data_signdate) as date, avg(user_bpm),avg(user_res) from User_health WHERE date >= strftime('%Y%m%d','now','localtime','-7 day') GROUP BY strftime('%Y%m%d',data_signdate) ORDER BY date desc limit 7;");

        System.out.println(mydata+"Week");

        barChart = (BarChart)view.findViewById(R.id.barchart);
        barChart.getDescription().setEnabled(false);/////라벨 없애줌
        barChart.setTouchEnabled(false);
        barChart.setPinchZoom(false);
        YAxis leftAxis = barChart.getAxisLeft();

        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        String day = null;
        int date = 0;//아무 요일이 아님
        try {
            date = MainActivity.getDateDay(mTime,"yyyyMMdd");//요일의 숫자를 받아옴
        } catch (Exception e){///JSON exception 포함
            e.printStackTrace();
        }

        //Y축값
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        //X축값
        final String[] labels = new String[7];//
        //오늘의 요일은 가장 마지막에 두고 앞에 요일을 계산
        for(int i=labels.length-1;i>=0;i--)
        {
                switch(date)
                {
                    case 1:
                        day = "일";
                        break ;
                    case 2:
                        day = "월";
                        break ;
                    case 3:
                        day = "화";
                        break ;
                    case 4:
                        day = "수";
                        break ;
                    case 5:
                        day = "목";
                        break ;
                    case 6:
                        day = "금";
                        break ;
                    case 7:
                        day = "토";
                        break ;
                }
            labels[i]=day;
            date--;
            if(date==0) date=labels.length;/////다시 세줌
        }

        //X축 label값 결정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        for(int i=0;i<labels.length;i++)
        {
            barEntries.add(new BarEntry(i,0));
        }

        String dataDay = null;
        int dataDate;
        //Y축값
        //요일에 맞는 값을 찾는 반복문
        if(mydata.size()!=0)//일주일간의 데이터가 있으면
        {
            for(int i=0;i<mydata.size();i++)
            {
                try {//데이터의 요일을 조사
                    dataDate= MainActivity.getDateDay(mydata.get(i).getString("data_signdate"),"yyyyMMdd");
                    switch(dataDate)
                    {
                        case 1:
                            dataDay = "일";
                            break ;
                        case 2:
                            dataDay = "월";
                            break ;
                        case 3:
                            dataDay = "화";
                            break ;
                        case 4:
                            dataDay = "수";
                            break ;
                        case 5:
                            dataDay = "목";
                            break ;
                        case 6:
                            dataDay = "금";
                            break ;
                        case 7:
                            dataDay = "토";
                            break ;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //x축에서 요일 비교 비교 - 지속적인 테스트 요망
                for(int j=0;j<labels.length;j++)
                {
                    if(labels[j].equals(dataDay))/////지금 꺼낸 JSON object의 date를 X축과 비교하여 같다면
                    {
                        barEntries.set(j,new BarEntry(j,mydata.get(i).optInt("user_bpm")));//그 X축에 값을 넣어줌
                    }
                }
            }
        }///데이터가 없으면 이미 추가 시켜놨기 때문에 할필요가 없음

        System.out.println(barEntries);
        BarDataSet dataset = new BarDataSet(barEntries,"심박수");//Y축값을 입력
        dataset.setValueTextSize(15);
        BarData data = new BarData(dataset);
        data.setValueFormatter(new Myvalueformatter());
        data.setBarWidth(0.5f);
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

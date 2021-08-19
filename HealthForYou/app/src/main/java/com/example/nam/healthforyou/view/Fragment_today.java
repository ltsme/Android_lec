package com.example.nam.healthforyou.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by NAM on 2017-07-29.
 */

public class Fragment_today extends Fragment {
    DBhelper dBhelper;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_today, container, false);
        TextView tv_avebpm = (TextView)view.findViewById(R.id.today_avebpm);
        TextView tv_averes = (TextView)view.findViewById(R.id.today_averes);
        TextView tv_nodatamessage = (TextView)view.findViewById(R.id.nodatamessage);
        /////현재시간을 받아옴
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyyMMdd", Locale.getDefault() );
        Date currentTime = new Date ();
        String mTime = mSimpleDateFormat.format(currentTime);
        System.out.println(mTime);
        dBhelper = new DBhelper(getActivity().getApplicationContext(),"healthforyou.db", null, 1);

        //limit절을 통해 평균을 구해놓은 쿼리를 최신순으로 정렬 후 한개만 limit를 통해 갖고 옴
        List<JSONObject> todaydata = dBhelper.PrintAvgData("SELECT strftime('%Y%m%d',data_signdate) as date,avg(user_bpm),avg(user_res) from User_health GROUP BY strftime('%Y%m%d',data_signdate) ORDER BY date desc limit 1;");

        System.out.println(todaydata+"today");

        try {
            if(todaydata.size()!=0)///최근순으로 데이터를 뽑았을 때 데이터가 있으면
            {
                if(todaydata.get(0).getString("data_signdate").equals(mTime))//현재시간과 최근 데이터의 날짜와 오늘날짜가 같다면 오늘 측정한 데이터임
                {
                    int average_today_bpm;
                    int average_today_res;
                    try {
                        average_today_bpm=todaydata.get(0).getInt("user_bpm");
                        average_today_res=todaydata.get(0).getInt("user_res");
                        tv_avebpm.setText(average_today_bpm+" BPM");
                        tv_averes.setText(average_today_res+" 회/분");
                        tv_nodatamessage.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{////오늘 날짜인 데이터가 없으면
                    tv_avebpm.setText("--");
                    tv_averes.setText("--");
                    tv_nodatamessage.setVisibility(View.VISIBLE);
                }
            }else{//데이터가 없으면
                tv_avebpm.setText("--");
                tv_averes.setText("--");
                tv_nodatamessage.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

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

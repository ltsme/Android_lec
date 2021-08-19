package com.example.nam.healthforyou.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nam.healthforyou.R;
import com.example.nam.healthforyou.item.HealthChooseItem;

import java.util.ArrayList;

/**
 * Created by NAM on 2017-08-21.
 */

public class healthChooseChatAdapter extends BaseAdapter {
    ArrayList<HealthChooseItem> healthItemArrayList = new ArrayList<>();
    LayoutInflater inflater;
    @Override
    public int getCount() {
        return healthItemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Context mContext = parent.getContext();

        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.choiceofdata, parent, false);
        }

        HealthChooseItem healthChooseItem = healthItemArrayList.get(position);

        TextView health_userbpm = (TextView)convertView.findViewById(R.id.tvgv_chatHeart1);
        TextView health_userres = (TextView)convertView.findViewById(R.id.tvgv_chatRes1);
        TextView health_datasigndate = (TextView)convertView.findViewById(R.id.tvgv_signdate);
        health_userbpm.setText(healthChooseItem.gv_userbpm+"bpm");
        health_userres.setText(healthChooseItem.gv_userres+"/min");
        health_datasigndate.setText("측정날짜 : "+ healthChooseItem.gv_signdate);

        return convertView;
    }

    public void addItem(HealthChooseItem item)
    {
        healthItemArrayList.add(item);//gridView에 아이템을 추가
    }

    public HealthChooseItem getGridItem(int position)
    {
        return healthItemArrayList.get(position);
    }

    public void deleteItem(){
        healthItemArrayList.clear();
    }
}

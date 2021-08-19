package com.aoslec.customadapterview_bts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class BtsAdapter extends BaseAdapter {

    private Context mContext = null;
    private int layout = 0;
    private ArrayList<Bts> data = null;
    private LayoutInflater inflater = null;

    public BtsAdapter(Context mContext, int layout, ArrayList<Bts> data) {
        this.mContext = mContext;
        this.layout = layout;
        this.data = data;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position).getNumber();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(this.layout, parent, false);
        }
        TextView tv_number = convertView.findViewById(R.id.tv_number);
        ImageView iv_icon = convertView.findViewById(R.id.iv_bts);
        TextView tv_name = convertView.findViewById(R.id.tv_name);
        tv_number.setText(data.get(position).getNumber());
        iv_icon.setImageResource(data.get(position).getIcon());
        tv_name.setText(data.get(position).getName());

        if(position % 2 == 1){
            convertView.setBackgroundColor(0x5000ff00);
        }else {
            convertView.setBackgroundColor(0x2000ff00);
        }

        return convertView;
    }
}

package com.aoslec.addressbook;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MemberAdapter extends BaseAdapter {
    private Context mContext = null;
    private int layout = 0;
    private ArrayList<Member> data = null;
    private LayoutInflater inflater = null;

    public MemberAdapter(Context mContext, int layout, ArrayList<Member> data) {
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
        return data.get(position).getName();
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
        TextView name = convertView.findViewById(R.id.result_name);
        name.setText(data.get(position).getName());

        if(position %2 ==1){
            convertView.setBackgroundColor(Color.GRAY);
        }else {

        }

        return convertView;
    }
}

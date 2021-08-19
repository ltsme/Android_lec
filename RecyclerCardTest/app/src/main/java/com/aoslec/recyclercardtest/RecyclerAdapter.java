package com.aoslec.recyclercardtest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{
    private String[] titles = {"Monday 월", "Monday 화", "Monday 수", "Monday 목", "Monday 금", "Monday 토", "Monday 일"};
    private String[] details= {"월요일 기상정보", "화요일 기상정보", "수요일 기상정보", "목월요일 기상정보", "금요일 기상정보", "토요일 기상정보", "일요일 기상정보"};
    private int[] images = {R.drawable.w2, R.drawable.w3, R.drawable.w4, R.drawable.w5, R.drawable.w6, R.drawable.w7};

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView itemImage;
        public TextView itemTitle;
        public TextView itemDetail;

        public ViewHolder(View itemView){
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemDetail = itemView.findViewById(R.id.item_detail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Snackbar.make(v, details[position], Snackbar.LENGTH_LONG).setAction("",null).show();
                }
            });
        }
    }

    //ViewHolder 만들기
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    //ViewHolder 쓰기
    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
        holder.itemTitle.setText(titles[position]);
        holder.itemDetail.setText(details[position]);
        holder.itemImage.setImageResource(images[position]);
    }

    //ViewHolder 카운트
    @Override
    public int getItemCount() {
        return titles.length;
    }
}

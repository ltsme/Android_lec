package com.example.nam.healthforyou.view;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nam.healthforyou.util.InternalImageManger;
import com.example.nam.healthforyou.R;
import com.example.nam.healthforyou.item.Chatroomitem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by NAM on 2017-08-14.
 */

public class ChatroomAdapter extends BaseAdapter {
    LayoutInflater inflater;
    List<Chatroomitem> chatroomitemList = new ArrayList<>();
    @Override
    public int getCount() {
        return chatroomitemList.size();
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
        Context context = parent.getContext();

        if(convertView==null)
        {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chatroomlist, parent, false);//viewholder 추천
        }

        Chatroomitem chatroomitem = chatroomitemList.get(position);
        //String completePath = context.getFilesDir().getParent()+"/"+"app_PFImage"+"/"+chatroomitem.room_id+"_Image";
        //System.out.println(completePath+"저장소");
        //"/data/user/0/com.example.nam.healthforyou/app_PFImage/"
        //File file = new File(completePath);
        //Uri imageUri = Uri.fromFile(file);



        /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
        ImageView iv_chatroomprofile = (ImageView) convertView.findViewById(R.id.iv_chatroomprofile) ;
        TextView tv_chatroomid = (TextView) convertView.findViewById(R.id.tv_chatroomid);
        TextView tv_recentdate = (TextView)convertView.findViewById(R.id.tv_recentdate);
        TextView tv_recentmessage = (TextView)convertView.findViewById(R.id.tv_recentchat);

        if(chatroomitem.roomtype==0)//1:1 채팅
        {
            if(!chatroomitem.is_friend)//방이름이 안정해진 경우 - 친구가 아닌경우
           {
                tv_chatroomid.setText(chatroomitem.room_name);
                ////방 프로필 설정해주는 부분
                Glide.with(context)
                        .load(R.drawable.no_profile)
                        .asBitmap()
                        .override(64,64)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .error(R.drawable.no_profile)
                        .transform(new CropCircleTransformation(context))
                        .into(iv_chatroomprofile);

            }else{//방이름이 정해진 경우 - 친구인 경우 - 친구의 프로필 사진을 띄어줌
                String fileName=chatroomitem.room_id+"_Image";
                File file = new InternalImageManger(context).setFileName(fileName).setDirectoryName("PFImage").loadFile();
                //"/data/user/0/com.example.nam.healthforyou/app_PFImage/"
                Uri imageUri = Uri.fromFile(file);
                tv_chatroomid.setText(chatroomitem.room_name);
                Glide.with(context)
                        .load(imageUri)
                        .asBitmap()
                        .override(80,80)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .error(R.drawable.no_profile)
                        .transform(new CropCircleTransformation(context))
                        .into(iv_chatroomprofile);
            }
            try {//건강정보 체크
                JSONObject is_healthinfo = new JSONObject(chatroomitem.recentmessage);
                tv_recentmessage.setText("건강 정보");
            } catch (JSONException e) {
                tv_recentmessage.setText(chatroomitem.recentmessage);
            }

        }else if(chatroomitem.roomtype==1)//그룹채팅
        {
            tv_chatroomid.setText(chatroomitem.room_name);
            Glide.with(context)
                    .load(R.drawable.teamchat)
                    .asBitmap()
                    .override(64,64)
                    .centerCrop()
                    .error(R.drawable.no_profile)
                    .transform(new CropCircleTransformation(context))
                    .into(iv_chatroomprofile);
        }

        tv_recentdate.setText(chatroomitem.recentdate);
        try {
            JSONObject is_healthinfo = new JSONObject(chatroomitem.recentmessage);
            tv_recentmessage.setText("건강 정보");
        } catch (JSONException e) {
            tv_recentmessage.setText(chatroomitem.recentmessage);
        }


        return convertView;
    }

    public void addRoom(JSONObject chatroom){
        Chatroomitem chatroomitem = new Chatroomitem();
        try {
            chatroomitem.room_id=chatroom.getString("room_id");
            chatroomitem.room_name=chatroom.optString("room_name");
            chatroomitem.recentdate = chatroom.getString("message_date");
            chatroomitem.recentmessage = chatroom.getString("message_content");
            chatroomitem.roomtype = chatroom.getInt("room_type");
            chatroomitem.is_friend = chatroom.getBoolean("is_friend");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatroomitemList.add(chatroomitem);
    }

    ////Overloading - addroom
    public void addRoom(int index,JSONObject chatroom){///첫번째에 업데이트 하고 싶은경우
        Chatroomitem chatroomitem = new Chatroomitem();
        try {
            chatroomitem.room_id=chatroom.getString("room_id");
            chatroomitem.room_name=chatroom.optString("room_name");
            chatroomitem.recentdate = chatroom.getString("message_date");
            chatroomitem.recentmessage = chatroom.getString("message_content");
            chatroomitem.roomtype = chatroom.getInt("room_type");
            chatroomitem.is_friend = chatroom.getBoolean("is_friend");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatroomitemList.add(index,chatroomitem);
    }

    public void updateRoom(int index,JSONObject chatroom)//이미기존에 있는 방의 내용을 Update
    {
        Chatroomitem chatroomitem = new Chatroomitem();
        try {
            chatroomitem.room_id=chatroom.getString("room_id");
            chatroomitem.room_name=chatroom.optString("room_name");
            chatroomitem.recentdate = chatroom.getString("message_date");
            chatroomitem.recentmessage = chatroom.getString("message_content");
            chatroomitem.roomtype = chatroom.getInt("room_type");
            chatroomitem.is_friend = chatroom.getBoolean("is_friend");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatroomitemList.remove(index);///인덱스에 따른 값을 바꾸고
        chatroomitemList.add(0,chatroomitem);
    }

    public List<Chatroomitem> getRoom()//채팅방을 모두 갖고옴
    {
        return chatroomitemList;
    }

    public Chatroomitem getRoomitem(int position)
    {
        return chatroomitemList.get(position);
    }

    public void deleteRoomItem(){
        chatroomitemList.clear();
    }
}

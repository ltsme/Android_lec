package com.example.nam.healthforyou.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.example.nam.healthforyou.item.ChatItem;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by NAM on 2017-08-08.
 */

public class ChatAdapter extends BaseAdapter {
    private List<ChatItem> chatItemList =new ArrayList<>();

    //내가 쓴 카톡과 아닌것을 구분하기 위한 type
    private static final int ITEM_VIEW_TYPE_ME = 0 ;
    private static final int ITEM_VIEW_TYPE_YOU = 1 ;

    private static final int ITEM_VIEW_TYPE_HEALTHME = 2;
    private static final int ITEM_VIEW_TYPE_HEALTHYOU= 3;

    private static final int ITEM_VIEW_TIME =4;
    LayoutInflater inflater;
    Bitmap bitmap=null;
    private String timestamp=null;
    private SimpleDateFormat chatFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",java.util.Locale.getDefault());
    private SimpleDateFormat forChatprint = new SimpleDateFormat("a K:mm",java.util.Locale.getDefault());
    @Override
    public int getCount() {
        return chatItemList.size();
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
        View row = convertView;
        Context context = parent.getContext();
        if (row == null) {
            // inflator를 생성하여, chatting_message.xml을 읽어서 View객체로 생성한다.
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        // Array List에 들어 있는 채팅 문자열을 읽어
        ChatItem msg = chatItemList.get(position);

//        String completePath = context.getFilesDir().getParent()+"/"+"app_PFImage"+"/"+msg.item_senderId+"_Image";
//        System.out.println(completePath+"저장소");
        //"/data/user/0/com.example.nam.healthforyou/app_PFImage/"

        switch(msg.getType())
        {
            case ITEM_VIEW_TYPE_ME:
            {
                row = inflater.inflate(R.layout.chatlayout2_me, parent, false);

                // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
                TextView msgText = (TextView)row.findViewById(R.id.tv_content);
                TextView msgDate = (TextView)row.findViewById(R.id.tv_sendtime);
                /////리스트에 정보를 출력

                try {
                    Date date = chatFormat.parse(msg.item_date);
                    timestamp = forChatprint.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                msgText.setText(msg.item_content);
                //msgDate.setText(msg.item_date);
                msgDate.setText(timestamp);
                msgText.setTextColor(Color.parseColor("#000000"));
                break;
            }

            case ITEM_VIEW_TYPE_YOU:
            {
                row = inflater.inflate(R.layout.chatlayout1_you, parent, false);
                // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
                TextView msgId = (TextView)row.findViewById(R.id.tv_sender);
                TextView msgText = (TextView)row.findViewById(R.id.tv_content);
                TextView msgDate = (TextView)row.findViewById(R.id.tv_sendtime);
                ImageView profile = (ImageView)row.findViewById(R.id.iv_chatimage);
                /////리스트에 정보를 출력
                msgText.setText(msg.item_content);
                msgId.setText(msg.item_sender);
                try {
                    Date date = chatFormat.parse(msg.item_date);
                    timestamp = forChatprint.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //msgDate.setText(msg.item_date);
                msgDate.setText(timestamp);
                msgText.setText(msg.item_content);
                //msgDate.setText(msg.item_date);
                msgText.setTextColor(Color.parseColor("#000000"));
                if(msg.item_senderId!=null)
                {
                    String fileName=msg.item_senderId+"_Image";
                    File file = new InternalImageManger(context).setFileName(fileName).setDirectoryName("PFImage").loadFile();
                    //"/data/user/0/com.example.nam.healthforyou/app_PFImage/"
                    Uri imageUri = Uri.fromFile(file);
                    Glide.with(context)
                            .load(imageUri)
                            .asBitmap()
                            .override(50,50)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .centerCrop()
                            .error(R.drawable.no_profile)
                            .transform(new CropCircleTransformation(context))
                            .into(profile);
                }else{
                    Glide.with(context)
                            .load(R.drawable.no_profile)
                            .asBitmap()
                            .override(50,50)
                            .into(profile);
                }

                break;
            }

            case ITEM_VIEW_TYPE_HEALTHME:
            {
                row = inflater.inflate(R.layout.chatlayout3_mehealth, parent, false);
                // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
                TextView msgbpm = (TextView)row.findViewById(R.id.tv_chatHeart1);
                TextView msgres = (TextView)row.findViewById(R.id.tv_chatRes1);
                TextView msgDate = (TextView)row.findViewById(R.id.tv_sendtime);
                TextView chatDatasigndate = (TextView)row.findViewById(R.id.tv_chatdatasigndate);
                /////리스트에 정보를 출력
                msgbpm.setText(msg.user_bpm+"bpm");
                msgres.setText(msg.user_res+"/min");
                //msgDate.setText(msg.item_date);
                try {
                    Date date = chatFormat.parse(msg.item_date);
                    timestamp = forChatprint.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //msgDate.setText(msg.item_date);
                msgDate.setText(timestamp);
                chatDatasigndate.setText("측정날짜 :"+msg.data_signdate);
                break;
            }

            case ITEM_VIEW_TYPE_HEALTHYOU:
            {
                row = inflater.inflate(R.layout.chatlayout4_youhealth, parent, false);
                // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
                TextView msgId = (TextView)row.findViewById(R.id.tv_sender);
                TextView msgbpm = (TextView)row.findViewById(R.id.tv_chatHeart1);
                TextView msgres = (TextView)row.findViewById(R.id.tv_chatRes1);
                TextView msgDate = (TextView)row.findViewById(R.id.tv_sendtime);
                TextView chatDatasigndate = (TextView)row.findViewById(R.id.tv_chatdatasigndate);
                ImageView profile = (ImageView)row.findViewById(R.id.iv_healthchatimage);
                /////리스트에 정보를 출력
                msgId.setText(msg.item_sender);
                msgbpm.setText(msg.user_bpm+"bpm");
                msgres.setText(msg.user_res+"/min");
                //msgDate.setText(msg.item_date);
                try {
                    Date date = chatFormat.parse(msg.item_date);
                    timestamp = forChatprint.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //msgDate.setText(msg.item_date);
                msgDate.setText(timestamp);
                chatDatasigndate.setText("측정날짜 :"+msg.data_signdate);

                if(msg.item_senderId!=null)
                {
                    String fileName=msg.item_senderId+"_Image";
                    File file = new InternalImageManger(context).setFileName(fileName).setDirectoryName("PFImage").loadFile();
                    //"/data/user/0/com.example.nam.healthforyou/app_PFImage/"
                    Uri imageUri = Uri.fromFile(file);
                    Glide.with(context)
                            .load(imageUri)
                            .asBitmap()
                            .override(50,50)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .error(R.drawable.no_profile)
                            .transform(new CropCircleTransformation(context))
                            .into(profile);
                }else{
                    Glide.with(context)
                            .load(R.drawable.no_profile)
                            .asBitmap()
                            .override(50,50)
                            .into(profile);
                }

                break;
            }

            case ITEM_VIEW_TIME:{
                row = inflater.inflate(R.layout.chatlayout5, parent, false);
                TextView dateline = (TextView)row.findViewById(R.id.chattime);
                dateline.setText(msg.item_date);
            }
        }
        return row;
    }

    public void addItemYou(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_YOU);
        chatItemList.add(item);
    }

    public void addItemYou(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_YOU);
        chatItemList.add(0,item);
    }

    public void addItemME(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_ME);
        chatItemList.add(item);
    }

    public void addItemME(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_ME);
        chatItemList.add(0,item);
    }

    public void addItemHealthME(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_HEALTHME);
        chatItemList.add(item);
    }

    public void addItemHealthME(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_HEALTHME);
        chatItemList.add(0,item);
    }

    public void addItemHealthYou(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_HEALTHYOU);
        chatItemList.add(item);
    }

    public void addItemHealthYou(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_HEALTHYOU);
        chatItemList.add(0,item);
    }

    public boolean addItemTime(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TIME);
        chatItemList.add(index,item);
        return true;
    }

    public boolean addItemTime(ChatItem item)
    {
        item.setType(ITEM_VIEW_TIME);
        chatItemList.add(item);
        return true;
    }

    public ChatItem getItemtime(int position)
    {
        return chatItemList.get(position);
    }

    public void deleteItemtime(int position)
    {
        chatItemList.remove(position);
    }
}

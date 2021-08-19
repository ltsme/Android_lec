package com.example.nam.healthforyou.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.example.nam.healthforyou.R;
import com.example.nam.healthforyou.item.ProfileItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by NAM on 2017-08-04.
 */

public class ListViewAdapter extends BaseAdapter {

    public class ViewHolder{
        public int number;
        ImageView iv_img;
        TextView tv_name;
        CheckBox checkBox;
    }

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<ProfileItem> profileItems = new ArrayList<>();
    private static final int FRIENDLIST_TYPE=0;
    private static final int GROUPCHATLIST_TYPE=1;
    Bitmap bitmap=null;
    Context mContext;
    String completePath;
    @Override
    public int getCount() {
        return profileItems.size();
    }

    @Override
    public Object getItem(int position) {
        return profileItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        View v = convertView;
        ViewHolder viewHolder = null;
        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        ProfileItem profileItem = profileItems.get(position);
//        bitmap = new InternalImageManger(context).//내부저장공간에서 불러옴
//                setFileName(profileItem.profileName).///파일 이름
//                setDirectoryName("PFImage").
//                load();
        String fileName = profileItem.profileName;
        completePath = context.getFilesDir().getParent()+"/"+"app_PFImage"+"/"+profileItem.profileName;
        //String completePath ="/data/user/0/com.example.nam.healthforyou/app_PFImage"+"/"+profileItem.profileName;
        //System.out.println(completePath+"저장소");
        //File file = new InternalImageManger(context).setFileName(fileName).setDirectoryName("PFImage").loadFile();
        //"/data/user/0/com.example.nam.healthforyou/app_PFImage/"

        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch(profileItem.getType())
            {
                case FRIENDLIST_TYPE:
                {
                    v = inflater.inflate(R.layout.listitem, parent, false);
                    viewHolder = new ViewHolder();
                /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
                    viewHolder.iv_img = (ImageView)v.findViewById(R.id.iv_profile);
                    viewHolder.tv_name = (TextView)v.findViewById(R.id.tv_name);
                    v.setTag(viewHolder);
                    break;
                }

                case GROUPCHATLIST_TYPE:
                {
                    v = inflater.inflate(R.layout.listitem2, parent, false);
                /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
                    viewHolder = new ViewHolder();
                    viewHolder.iv_img = (ImageView) v.findViewById(R.id.iv_profilegroup);
                    viewHolder.tv_name = (TextView) v.findViewById(R.id.tv_namegroup);
                    viewHolder.checkBox = (CheckBox) v.findViewById(R.id.cb_addgroup);
                    viewHolder.checkBox.setFocusable(false);
                    viewHolder.checkBox.setClickable(false);
                    v.setTag(viewHolder);
                    break;
                }
            }
        }else{
            viewHolder = (ViewHolder)v.getTag();
        }

        switch(profileItem.getType())
        {
            case FRIENDLIST_TYPE:
            {
                Glide.with(context)
                        .load(imageUri)
                        .asBitmap()
                        .override(100,100)
                        .centerCrop()
                        .signature(new StringSignature(profileItem.profileLastupdate))
                        .transform(new RoundedCornersTransformation(context,10,10))
                        .error(R.drawable.no_profile)
                        .into(viewHolder.iv_img);

                viewHolder.tv_name.setText(profileItem.name);
                break;
            }

            case GROUPCHATLIST_TYPE:
            {
                Glide.with(context)
                        .load(imageUri)
                        .asBitmap()
                        .override(100,100)
                        .centerCrop()
                        .signature(new StringSignature(profileItem.profileLastupdate))
                        .transform(new RoundedCornersTransformation(context,10,10))
                        .error(R.drawable.no_profile)
                        .into(viewHolder.iv_img);

                viewHolder.tv_name.setText(profileItem.name);
                viewHolder.checkBox.setChecked(((ListView)parent).isItemChecked(position));////체크 박스 기억
                if(viewHolder.checkBox.isChecked())
                {
                    profileItem.checked=true;
                }else{
                    profileItem.checked=false;
                }
                break;
            }
        }


        return v;
    }

    public void addItemFriend(JSONObject friendprofile)
    {
        ProfileItem profileItem = new ProfileItem();
        try {
            profileItem.name=friendprofile.getString("user_name");//이름을 담고
            profileItem.email=friendprofile.getString("user_friend");//이메일을 담고
            profileItem.profileName=friendprofile.getString("user_profile");//프로필 이미지를 담는다
            profileItem.profileLastupdate=friendprofile.getString("user_update");
            profileItem.setType(FRIENDLIST_TYPE);
            ////프로필 사진을 담아야됨
        } catch (JSONException e) {
            e.printStackTrace();
        }
        profileItems.add(profileItem);//////유저의 프로필 추가
    }

    public void addItemNewFriend(JSONObject friendprofile)
    {
        ProfileItem profileItem = new ProfileItem();
        try {
            profileItem.name=friendprofile.getString("user_name");//이름을 담고
            profileItem.email=friendprofile.getString("user_friend");//이메일을 담고
            profileItem.profileName=friendprofile.getString("user_profile");//프로필 이미지를 담는다
            profileItem.profileLastupdate=friendprofile.getString("user_update");
            profileItem.setType(FRIENDLIST_TYPE);
            ////프로필 사진을 담아야됨
        } catch (JSONException e) {
            e.printStackTrace();
        }
        profileItems.add(0,profileItem);//////유저의 프로필 추가
    }

    public void addGroupFriend(ProfileItem profileItem)
    {
        profileItems.add(profileItem);//////유저의 프로필 추가
    }

    public ProfileItem getprofile(int position)
    {
        return profileItems.get(position);
    }

    public void Deleteall()
    {
        profileItems.clear();
    }
}
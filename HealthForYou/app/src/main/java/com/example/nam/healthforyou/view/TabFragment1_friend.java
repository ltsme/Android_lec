package com.example.nam.healthforyou.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.util.InternalImageManger;
import com.example.nam.healthforyou.item.ProfileItem;
import com.example.nam.healthforyou.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by NAM on 2017-08-04.
 */

public class TabFragment1_friend extends Fragment {

    final static int UPDATE_FRIEND=0;
    RelativeLayout tabfrag_friend;///
    List<JSONObject> friendlist;
    DBhelper dBhelper;
    ListViewAdapter listViewAdapter;
    ListView profileList;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    View layout;

    final static int ACT_ADDFRIEND =0;
    private FloatingActionMenu fam;
    private FloatingActionButton fab_team_chat, fab_add_user;

    private String loginemailid;
    Context mContext;
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tabfrag_friend = (RelativeLayout)inflater.inflate(R.layout.tab_frag_friend,container,false); //??????????????? ?????? ?????? ?????????????????? ????????????
        dBhelper = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB ??????
        mContext = getActivity().getApplicationContext();
        SharedPreferences useremail = getActivity().getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
        loginemailid=useremail.getString("useremail","false");
        listViewAdapter = new ListViewAdapter();//////????????? ??????

        int count=dBhelper.PrintCountfriend();
        listViewAdapter = new ListViewAdapter();//////????????? ??????
        if(count!=0)
        {
            friendlist=dBhelper.getAllfriend();
            if(friendlist!=null)
            {
                System.out.println(friendlist+"????????????");
            }

            for(int i=0;i<friendlist.size();i++)//NULLPointer Exception ??????
            {
                //?????? ???????????? ?????? - ?????? ????????? ???????????? ???????????????
                if(!loginemailid.equals(friendlist.get(i).optString("user_friend")))//???????????? ????????? ???????????? ?????? ?????? - SQlite??? ????????? ???????????? ??????
                {
                    System.out.println(friendlist.get(i));
                    listViewAdapter.addItemFriend(friendlist.get(i));///????????? ???????????? ??????
                }
            }
            listViewAdapter.notifyDataSetChanged();
        }

        profileList = (ListView)tabfrag_friend.findViewById(R.id.lv_friendlist);//????????????
        profileList.setAdapter(listViewAdapter);///??????????????? ????????? ??????
        profileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        fab_team_chat = (FloatingActionButton)tabfrag_friend.findViewById(R.id.fab2);
        fab_add_user = (FloatingActionButton)tabfrag_friend.findViewById(R.id.fab3);
        fam = (FloatingActionMenu)tabfrag_friend.findViewById(R.id.fab_menu);
        fam.setMenuButtonColorNormal(R.color.floating_normal);
        fam.setMenuButtonColorPressed(R.color.floating_pressed);
        //handling each floating action button clicked
        fab_team_chat.setOnClickListener(onButtonClick());
        fab_add_user.setOnClickListener(onButtonClick());

        fam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fam.isOpened()) {
                    fam.close(true);
                }
            }
        });

        fam.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //????????? ??????????????? ??????
                layout = inflater.inflate(R.layout.custom_profiledialog,null);

                final ProfileItem clickProfile= listViewAdapter.getprofile(position);
                Bitmap bitmap = new InternalImageManger(mContext).//???????????????????????? ?????????
                        setFileName(clickProfile.profileName).///?????? ??????
                        setDirectoryName("PFImage").
                        load();
                ImageView iv_profile = (ImageView)layout.findViewById(R.id.iv_dialogprofile);
                if(bitmap!=null)//????????? ????????????
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    Glide.with(mContext)
                            .load(stream.toByteArray())
                            .asBitmap()
                            .override(256,256)
                            .transform(new RoundedCornersTransformation(mContext,10,10))
                            .error(R.drawable.no_profile)
                            .into(iv_profile);
                }else{//????????? ????????????
                    Glide.with(mContext)
                            .load(R.drawable.no_profile)
                            .asBitmap()
                            .override(128,128)
                            .centerCrop()
                            .error(R.drawable.no_profile)
                            .into(iv_profile);
                }

                iv_profile.bringToFront();
                iv_profile.invalidate();
                TextView tv_name = (TextView)layout.findViewById(R.id.tv_dialogname);
                tv_name.setText(clickProfile.name);

                TextView tv_email = (TextView)layout.findViewById(R.id.tv_dialogemail);
                tv_email.setText(clickProfile.email);

                //???????????? ??????????????? ??????
                Button btn_chattofriend = (Button)layout.findViewById(R.id.btn_chat);
                btn_chattofriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        Intent intent = new Intent(getActivity(),Chatroom.class);
                        intent.putExtra("from",0);////???????????? ????????? ???????????? ????????? - ?????????????????? ?????? ??? ??? ??????
                        intent.putExtra("who",clickProfile.email);///???????????? ?????? ?????? ???????????? ???????????? ?????? ??????????????? ?????????
                        intent.putExtra("room_name",clickProfile.name);//???????????? ?????? ??????????????? ?????????
                        startActivity(intent);
                    }
                });

                builder = new AlertDialog.Builder(getActivity());
                builder.setView(layout);
                alertDialog = builder.create();
                alertDialog.show();
            }
        });

        return tabfrag_friend;
    }

    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == fab_add_user) {//////////?????? ??????
                    Intent intent =new Intent(getActivity(),Addfriend.class);
                    startActivityForResult(intent,ACT_ADDFRIEND);////????????? ??????
                    getActivity().overridePendingTransition(R.anim.slide_up,R.anim.no_change);

                } else if (view == fab_team_chat) {
                    Intent intent =new Intent(getActivity(),AddGroupChat.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_up,R.anim.no_change);
                }
                fam.close(true);
            }
        };
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1)//RESULT_CODE OK
        {
            if(requestCode==ACT_ADDFRIEND)////????????? ????????? ????????? ????????? ??????
            {
                System.out.println(dBhelper.getnewfriend());
                listViewAdapter.addItemNewFriend(dBhelper.getnewfriend());///????????? ????????? ????????? ??????????????? ??????
                listViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("????????????","onStart");
    }

    @Override
    public void onResume() {
        Log.d("????????????","onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("????????????","onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("????????????","onStop");
        super.onStop();
    }
}

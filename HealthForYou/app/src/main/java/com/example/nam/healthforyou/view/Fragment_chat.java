package com.example.nam.healthforyou.view;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.nam.healthforyou.component.ClientSocketService;
import com.example.nam.healthforyou.R;

/**
 * Created by NAM on 2017-07-13.
 */

public class Fragment_chat extends Fragment{

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayout chat;
    private int[] tabIcons = {
            R.drawable.friend,
            R.drawable.chat
    };
    String who;//FCM을 보낸 사람을 나타냄
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        chat = (LinearLayout)inflater.inflate(R.layout.frag_chat,container,false);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("헬스톡");//Action Bar이름 지정
//        //채팅창을 누를 시에 Socket을 연다
//        ServicesocketThread servicesocketThread = new ServicesocketThread();
//        servicesocketThread.start();

        tabLayout = (TabLayout)chat.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("친구목록").setIcon(R.drawable.friend));
        tabLayout.addTab(tabLayout.newTab().setText("채팅목록").setIcon(R.drawable.chat));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager)chat.findViewById(R.id.pager);

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //
        Bundle extra = getArguments();
        if(extra!=null)
        {
            String who = extra.getString("WHO");
            String type = extra.getString("TYPE");
            Intent intent = new Intent(getActivity(),Chatroom.class);
            //그룹간의 대화를 나타내는 건지 개인간의 대화를 나타내는 건지
            if(type.equals("0"))            {
                intent.putExtra("from",0);//개인 채팅 의미
                intent.putExtra("who",who);

            }else if(type.equals("1"))
            {
                intent.putExtra("from",2);//그룹채팅 의미
                intent.putExtra("room_id",who);
            }
            startActivity(intent);
            //ViewPager에서
            viewPager.setCurrentItem(1);//채팅 페이지
        }

        return chat;
    }

    //UI-Thread를 통해서 Socket을 열면 netWorkThreadException 발생 - Thread를 통해 쓰레드 시작
    public class ServicesocketThread extends Thread{
        @Override
        public void run() {
            startServiceMethod();
        }
    }

    //서비스 시작. - 소켓 연결
    public void startServiceMethod(){
        Intent Service = new Intent(getActivity(), ClientSocketService.class);
        getActivity().startService(Service);
    }
}

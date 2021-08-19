package com.example.nam.healthforyou.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.nam.healthforyou.view.TabFragment1_friend;
import com.example.nam.healthforyou.view.TabFragment2_chat;

/**
 * Created by NAM on 2017-08-04.
 */

public class TabPagerAdapter extends FragmentStatePagerAdapter {
    private int tabCount;

    public TabPagerAdapter(FragmentManager fm,int tabCount) {
        super(fm);
        this.tabCount=tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TabFragment1_friend tabFragment1 = new TabFragment1_friend();
                return tabFragment1;
            case 1:
                TabFragment2_chat tabFragment2 = new TabFragment2_chat();
                return tabFragment2;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}

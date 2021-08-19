package com.example.nam.healthforyou.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.nam.healthforyou.view.Fragment_month;
import com.example.nam.healthforyou.view.Fragment_today;
import com.example.nam.healthforyou.view.Fragment_week;

/**
 * Created by NAM on 2017-07-29.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private int NUM_ITEMS = 3;
    private String[] titles= new String[]{"First Fragment", "Second Fragment","Third Fragment"};

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return  NUM_ITEMS ;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Fragment_today();
            case 1:
                return new Fragment_week();
            case 2:
                return new Fragment_month();
            default:
                return null;
        }
    }
}

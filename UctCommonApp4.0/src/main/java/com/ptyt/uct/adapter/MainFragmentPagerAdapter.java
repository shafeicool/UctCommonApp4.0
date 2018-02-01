package com.ptyt.uct.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * @Description:
 * @Date: 2017/5/9
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    //fragments列表
    private List<Fragment> fragments;
    //tab名
    private String[] pagerTitles;

    public MainFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments, String[] pagerTitles) {
        super(fm);
        this.fragments = fragments;
        this.pagerTitles = pagerTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return pagerTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pagerTitles[position];
    }
}

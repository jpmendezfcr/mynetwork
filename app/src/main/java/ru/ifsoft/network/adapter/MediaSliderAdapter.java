package ru.ifsoft.network.adapter;

import android.util.Log;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import java.util.ArrayList;
import java.util.List;

public class MediaSliderAdapter extends FragmentStateAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();

    private static int PAGE_REFRESH_STATE = PagerAdapter.POSITION_UNCHANGED;
    ViewPager2 mMenuPager;

    public MediaSliderAdapter(FragmentManager fa, ViewPager2 mMenuPager, Lifecycle lifecycle) {

        //super(fa);
        super(fa, lifecycle);
        this.mMenuPager = mMenuPager;
    }

    public void addFragment(Fragment fragment) {

        mFragmentList.add(fragment);
    }

    public void removeFragment(int position) {

        mFragmentList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public Fragment createFragment(int position) {

        Log.e("ItemFragment", "createFragment");

        //return new ItemFragment();

        return mFragmentList.get(position);
    }

    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public int getItemCount() {

        return mFragmentList.size();
    }
}
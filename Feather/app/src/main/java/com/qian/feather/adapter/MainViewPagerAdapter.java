package com.qian.feather.adapter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.*;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.*;

public class MainViewPagerAdapter extends FragmentStateAdapter {
    private Context context;
    private List<Fragment> fragmentList;
    public MainViewPagerAdapter(FragmentActivity fragmentActivity,Context context,List<Fragment> fragmentList) {
        super(fragmentActivity);
        this.context = context;
        this.fragmentList = fragmentList;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                context.sendBroadcast(new Intent("HomeFragment"));
                return fragmentList.get(0);
            case 1:
                context.sendBroadcast(new Intent("ContactsFragment"));
                return fragmentList.get(1);
            case 2:
                context.sendBroadcast(new Intent("ExploreFragment"));
                return fragmentList.get(2);
            case 3:
                context.sendBroadcast(new Intent("SelfFragment"));
                return fragmentList.get(3);
        }
        return null;
    }
    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}

package com.kvajpoj.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kvajpoj.fragments.FragmentCounters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andrej on 10.9.2015.
 */


public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public static final int TAB_FAVORITES = 0;
    public static final int TAB_LIST = 1;
    public static final int TAB_MAP = 2;

    private static final int TAB_COUNT = 2;
    List<Object> mPages = Collections.EMPTY_LIST;
    private FragmentManager mFragmentManager;
    private Context mContext;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
        mContext = context;

        mPages = new ArrayList<>();
        mPages.add(FragmentCounters.newInstance("Favorites", ""));
        mPages.add(FragmentCounters.newInstance("List", ""));


    }

    public Fragment getItem(int num) {
        Fragment fragment = null;

        switch (num) {
            case TAB_FAVORITES:
                return (Fragment) mPages.get(0);
            case TAB_LIST:
                return (Fragment) mPages.get(1);

            //case TAB_MAP:
            //    fragment = FragmentUpcoming.newInstance("", "");
            //    break;
        }
        return fragment;

    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        if (position == TAB_FAVORITES) return "Priljubljeni";
        if (position == TAB_LIST) return "Seznam";
        //return getResources().getStringArray(R.array.tabs)[position];
        return "BLa";

    }

}

package net.oschina.app.v2.activity;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import net.oschina.app.v2.base.BaseViewPagerAdapter;

/**
 * Created by Sim on 2015/3/5.
 */
public interface IPagerFragment {

    public BaseViewPagerAdapter getPagerAdapter();

    public ViewPager getViewPager();

    public Fragment getCurrentFragment();
}

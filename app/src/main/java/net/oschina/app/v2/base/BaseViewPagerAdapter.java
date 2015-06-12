package net.oschina.app.v2.base;

import android.support.v4.app.FragmentManager;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;

/**
 * Created by Sim on 2015/3/7.
 */
public abstract class BaseViewPagerAdapter extends CacheFragmentStatePagerAdapter {

    public BaseViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }
}

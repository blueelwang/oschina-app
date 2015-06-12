package net.oschina.app.v2.activity.tweet.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.IMainTab;
import net.oschina.app.v2.activity.IPagerFragment;
import net.oschina.app.v2.activity.tweet.adapter.TweetTabPagerAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.BaseViewPagerAdapter;
import net.oschina.app.v2.ui.tab.SlidingTabLayout;

public class TweetViewPagerFragment extends BaseFragment implements
        IPagerFragment {

	//private PagerSlidingTabStrip mTabStrip;
	private ViewPager mViewPager;
	private TweetTabPagerAdapter mTabAdapter;
    private SlidingTabLayout mSlidingTabLayout;

    @Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_viewpager2, container,
				false);
		//mTabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        Activity parentActivity = getActivity();
        if (parentActivity instanceof IMainTab) {
            mSlidingTabLayout = ((IMainTab) parentActivity).getSlidingTabLayout();
        } else {
            throw new RuntimeException(TweetViewPagerFragment.class.getSimpleName() + "'s parent activity must be a IMainTab");
        }
        //(SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.v2_tab_indicator2, R.id.tv_name);
		mViewPager = (ViewPager) view.findViewById(R.id.main_tab_pager);

		if (mTabAdapter == null) {
			mTabAdapter = new TweetTabPagerAdapter(getChildFragmentManager());
		}
		mViewPager.setOffscreenPageLimit(mTabAdapter.getCount());
		mViewPager.setAdapter(mTabAdapter);
		//mViewPager.setOnPageChangeListener(this);
		//mTabStrip.setViewPager(mViewPager);

        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
		return view;
	}

    @Override
    public BaseViewPagerAdapter getPagerAdapter() {
        return mTabAdapter;
    }

    @Override
    public ViewPager getViewPager() {
        return mViewPager;
    }

    @Override
    public Fragment getCurrentFragment() {
        return mTabAdapter.getItemAt(mViewPager.getCurrentItem());
    }
}
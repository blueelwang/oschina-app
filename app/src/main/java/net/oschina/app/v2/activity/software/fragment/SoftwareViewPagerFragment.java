package net.oschina.app.v2.activity.software.fragment;

import net.oschina.app.v2.activity.common.SimpleBackActivity;
import net.oschina.app.v2.activity.software.adapter.SoftwareTabPagerAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.BaseTabFragment;
import net.oschina.app.v2.ui.tab.SlidingTabLayout;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonlin.osc.happy.R;

public class SoftwareViewPagerFragment extends BaseFragment implements
		OnPageChangeListener {

	//private PagerSlidingTabStrip mTabStrip;
	private ViewPager mViewPager;
	private SoftwareTabPagerAdapter mTabAdapter;
	private int mCurrentPos;
    private SlidingTabLayout mSlidingTabLayout;

    @Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_viewpager, container,
				false);
		//mTabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.v2_tab_indicator2, R.id.tv_name);
		mViewPager = (ViewPager) view.findViewById(R.id.main_tab_pager);

		if (mTabAdapter == null) {
			mTabAdapter = new SoftwareTabPagerAdapter(getChildFragmentManager(),
					getActivity(), mViewPager);
		}
		mViewPager.setOffscreenPageLimit(mTabAdapter.getCacheCount());
		mViewPager.setAdapter(mTabAdapter);
		mViewPager.setOnPageChangeListener(this);
		//mTabStrip.setViewPager(mViewPager);
        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        ((SimpleBackActivity)getActivity()).hideActionBarShadow();
		return view;
	}
	
	@Override
	public boolean onBackPressed() {
		BaseTabFragment fragment = mTabAdapter.getItem(mCurrentPos);
		if(fragment instanceof SoftwareCataglogFragment) {
			return fragment.onBackPressed();
		}
		return super.onBackPressed();
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		//mTabStrip.onPageScrollStateChanged(arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		//mTabStrip.onPageScrolled(arg0, arg1, arg2);
		mTabAdapter.onPageScrolled(arg0);
	}

	@Override
	public void onPageSelected(int arg0) {
		mCurrentPos = arg0;
		//mTabStrip.onPageSelected(arg0);
		mTabAdapter.onPageSelected(arg0);
	}
}
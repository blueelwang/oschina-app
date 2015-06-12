package net.oschina.app.v2.activity.search.fragment;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.search.adapter.SearchTabPagerAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.ui.tab.SlidingTabLayout;
import net.oschina.app.v2.utils.SimpleTextWatcher;
import net.oschina.app.v2.utils.TDevice;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.tonlin.osc.happy.R;

public class SearchViewPagerFragment extends BaseFragment implements
		OnPageChangeListener, OnClickListener {

	//private PagerSlidingTabStrip mTabStrip;
	private ViewPager mViewPager;
	private SearchTabPagerAdapter mTabAdapter;

    private View mRlContent;

    private SlidingTabLayout mSlidingTabLayout;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		getActivity().getWindow().setSoftInputMode(mode);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_search, container,
				false);
		//mTabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.v2_tab_indicator2, R.id.tv_name);
		mViewPager = (ViewPager) view.findViewById(R.id.main_tab_pager);

		if (mTabAdapter == null) {
			mTabAdapter = new SearchTabPagerAdapter(getChildFragmentManager(),
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

        mRlContent = view.findViewById(R.id.rl_content);
		return view;
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
		//mTabStrip.onPageSelected(arg0);
		mTabAdapter.onPageSelected(arg0);
	}

	public void handleSearch(String content) {
		if (!TextUtils.isEmpty(content)) {
			mTabAdapter.search(content);
			mRlContent.setVisibility(View.VISIBLE);
		}
	}
}
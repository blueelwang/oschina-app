package net.oschina.app.v2.activity.news.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.news.NewsTab;
import net.oschina.app.v2.activity.news.fragment.NewsFragment;
import net.oschina.app.v2.base.BaseTabFragment;
import net.oschina.app.v2.base.BaseViewPagerAdapter;
import net.oschina.app.v2.utils.TLog;

public final class NewsTabPagerAdapter extends BaseViewPagerAdapter {

    public NewsTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

//    public NewsTabPagerAdapter(FragmentManager mgr, Context context,
//			ViewPager viewpager) {
//		super(mgr, NewsTab.values().length, context.getApplicationContext(),
//				viewpager);
//		NewsTab[] values = NewsTab.values();
//		for (int i = 0; i < values.length; i++) {
//			Fragment fragment = null;
//			List<Fragment> list = mgr.getFragments();
//			if (list != null) {
//				Iterator<Fragment> iterator = list.iterator();
//				while (iterator.hasNext()) {
//					fragment = iterator.next();
//					if (fragment.getClass() == values[i].getClz()) {
//						break;
//					}
//				}
//			}
//			BaseTabFragment tabFragment = (BaseTabFragment) fragment;
//			if (tabFragment == null)
//				try {
//					tabFragment = (BaseTabFragment) values[i].getClz()
//							.newInstance();
//				} catch (InstantiationException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				}
//			tabFragment.a(this);
//			if (!tabFragment.isAdded()) {
//				Bundle args = new Bundle();
//				args.putInt(NewsFragment.BUNDLE_KEY_CATALOG,
//						values[i].getCatalog());
//
//                if (0 < mScrollY) {
//                    TLog.log("MainActivity", "setParams:scrollY" + mScrollY);
//                    args.putInt(BaseTabFragment.ARG_INITIAL_POSITION, 1);
//                    tabFragment.setArguments(args);
//                }
//
//				tabFragment.setArguments(args);
//			}
//			fragments[values[i].getIdx()] = tabFragment;
//		}
//	}
//
//	public final int getCacheCount() {
//		return 2;
//	}

    public final int getCount() {
        return NewsTab.values().length;
    }

    @Override
    public final CharSequence getPageTitle(int i) {
        NewsTab tab = NewsTab.getTabByIdx(i);
        int idx = 0;
        CharSequence title = "";
        if (tab != null)
            idx = tab.getTitle();
        if (idx != 0)
            title = AppContext.string(idx);
        return title;
    }

    @Override
    protected Fragment createItem(int position) {
        final int pattern = position % 3;
        NewsTab[] values = NewsTab.values();
        Fragment f = null;
        try {
            f = (Fragment) values[pattern].getClz().newInstance();

            Bundle args = new Bundle();
            args.putInt(NewsFragment.BUNDLE_KEY_CATALOG, values[pattern].getCatalog());

            f.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return f;
    }
}
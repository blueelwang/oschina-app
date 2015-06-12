package net.oschina.app.v2.activity.tweet.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.active.ActiveTab;
import net.oschina.app.v2.activity.active.fragment.ActiveFragment;
import net.oschina.app.v2.activity.tweet.TweetTab;
import net.oschina.app.v2.activity.tweet.fragment.TweetFragment;
import net.oschina.app.v2.base.BaseViewPagerAdapter;

public final class TweetTabPagerAdapter extends BaseViewPagerAdapter {
    public TweetTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

//	public TweetTabPagerAdapter(FragmentManager mgr, Context context,
//			ViewPager viewpager) {
//		super(mgr, TweetTab.values().length, context.getApplicationContext(),
//				viewpager);
//		TweetTab[] values = TweetTab.values();
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
//			//tabFragment.a(this);
//			if (!tabFragment.isAdded()) {
//				Bundle args = new Bundle();
//				args.putInt(TweetFragment.BUNDLE_KEY_CATALOG,
//						values[i].getCatalog());
//				tabFragment.setArguments(args);
//			}
//			fragments[values[i].getIdx()] = tabFragment;
//		}
//	}
//
//	public final int getCacheCount() {
//		return 1;
//	}

	public final int getCount() {
		return TweetTab.values().length;
	}

	public final CharSequence getPageTitle(int i) {
		TweetTab tab = TweetTab.getTabByIdx(i);
		int resId = 0;
		CharSequence title = "";
		if (tab != null)
			resId = tab.getTitle();
		if (resId != 0)
			title = AppContext.string(resId);
		return title;
	}

    @Override
    protected Fragment createItem(int position) {
        final int pattern = position % 3;
        TweetTab[] values = TweetTab.values();
        Fragment f = null;
        try {
            f = (Fragment) values[pattern].getClz().newInstance();

            Bundle args = new Bundle();
            args.putInt(TweetFragment.BUNDLE_KEY_CATALOG, values[pattern].getCatalog());

            f.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return f;
    }
}
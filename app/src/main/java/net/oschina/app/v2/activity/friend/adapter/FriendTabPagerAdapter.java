package net.oschina.app.v2.activity.friend.adapter;

import java.util.Iterator;
import java.util.List;

import net.oschina.app.v2.activity.friend.FriendTab;
import net.oschina.app.v2.activity.friend.fragment.FriendFragment;
import net.oschina.app.v2.base.BaseTabFragment;
import net.oschina.app.v2.ui.tab.SlidingTabPagerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

public final class FriendTabPagerAdapter extends SlidingTabPagerAdapter {

	public FriendTabPagerAdapter(FragmentManager mgr, Context context,
			ViewPager viewpager) {
		super(mgr, FriendTab.values().length, context.getApplicationContext(),
				viewpager);
		FriendTab[] values = FriendTab.values();
		for (int i = 0; i < values.length; i++) {
			Fragment fragment = null;
			List<Fragment> list = mgr.getFragments();
			if (list != null) {
				Iterator<Fragment> iterator = list.iterator();
				while (iterator.hasNext()) {
					fragment = iterator.next();
					if (fragment.getClass() == values[i].getClz()) {
						break;
					}
				}
			}
			BaseTabFragment tabFragment = (BaseTabFragment) fragment;
			if (tabFragment == null)
				try {
					tabFragment = (BaseTabFragment) values[i].getClz()
							.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			//tabFragment.a(this);

			Bundle args = new Bundle();
			args.putInt(FriendFragment.BUNDLE_KEY_CATALOG,
					values[i].getCatalog());
			tabFragment.setArguments(args);

			fragments[values[i].getIdx()] = tabFragment;
		}
	}

	public final int getCacheCount() {
		return 1;
	}

	public final int getCount() {
		return FriendTab.values().length;
	}

	public final CharSequence getPageTitle(int i) {
		FriendTab tab = FriendTab.getTabByIdx(i);
		int resId = 0;
		CharSequence title = "";
		if (tab != null)
			resId = tab.getTitle();
		if (resId != 0)
			title = context.getText(resId);
		return title;
	}
}
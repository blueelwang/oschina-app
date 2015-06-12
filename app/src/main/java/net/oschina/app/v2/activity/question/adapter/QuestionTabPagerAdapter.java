package net.oschina.app.v2.activity.question.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.news.NewsTab;
import net.oschina.app.v2.activity.news.fragment.NewsFragment;
import net.oschina.app.v2.activity.question.QuestionTab;
import net.oschina.app.v2.activity.question.fragment.QuestionFragment;
import net.oschina.app.v2.base.BaseViewPagerAdapter;

public final class QuestionTabPagerAdapter extends BaseViewPagerAdapter {

    public QuestionTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

//	public QuestionTabPagerAdapter(FragmentManager mgr, Context context,
//			ViewPager vp) {
//		super(mgr, QuestionTab.values().length,
//				context.getApplicationContext(), vp);
//		QuestionTab[] values = QuestionTab.values();
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
//				args.putInt(QuestionFragment.BUNDLE_KEY_CATALOG,
//						values[i].getCatalog());
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
		return QuestionTab.values().length;
	}

	public final CharSequence getPageTitle(int i) {
		QuestionTab tab = QuestionTab.getTabByIdx(i);
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
        final int pattern = position % 5;
        QuestionTab[] values = QuestionTab.values();
        Fragment f = null;
        try {
            f = (Fragment) values[pattern].getClz().newInstance();

            Bundle args = new Bundle();
            args.putInt(QuestionFragment.BUNDLE_KEY_CATALOG, values[pattern].getCatalog());

            f.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return f;
    }
}
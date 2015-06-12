package net.oschina.app.v2.activity.active.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.IMainTab;
import net.oschina.app.v2.activity.IPagerFragment;
import net.oschina.app.v2.activity.active.adapter.ActiveTabPagerAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.BaseViewPagerAdapter;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.ui.tab.SlidingTabLayout;
import net.oschina.app.v2.utils.TLog;

public class ActiveViewPagerFragment extends BaseFragment implements
        IPagerFragment {

    //private PagerSlidingTabStrip mTabStrip;
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private ActiveTabPagerAdapter mTabAdapter;
    //private BadgeView mBvAtMe,mBvComment,mBvMsg;

    private BroadcastReceiver mNoticeReceiver = new BroadcastReceiver() {

        public static final java.lang.String TAG = "NoticeReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            int atmeCount = intent.getIntExtra("atmeCount", 0);// @我
            int msgCount = intent.getIntExtra("msgCount", 0);// 留言
            int reviewCount = intent.getIntExtra("reviewCount", 0);// 评论
            int newFansCount = intent.getIntExtra("newFansCount", 0);// 新粉丝
            int activeCount = atmeCount + reviewCount + msgCount + newFansCount;// 信息总数

            Activity parentActivity = getActivity();
            if (parentActivity instanceof IMainTab) {
                int tab = ((IMainTab) parentActivity).getCurrentTab();
                if(tab != 3)
                    return;
            }

            TLog.log(TAG,"Me 收到广播 @me:" + atmeCount + " msg:" + msgCount + " review:"
                    + reviewCount + " newFans:" + newFansCount + " active:"
                    + activeCount+" from:"+ intent.getStringExtra("from"));

            mSlidingTabLayout.setMessageCount(1, atmeCount);
            if (atmeCount > 0) {
                //mBvAtMe.setText(atmeCount + "");
                //mBvAtMe.show();
            } else {
                //mBvAtMe.hide();
            }

            mSlidingTabLayout.setMessageCount(2, reviewCount);
            if (reviewCount > 0) {
                //mBvComment.setText(reviewCount + "");
                //mBvComment.show();
            } else {
                //mBvComment.hide();
            }

            mSlidingTabLayout.setMessageCount(4, msgCount);
            if (msgCount > 0) {
                //mBvMsg.setText(msgCount + "");
                //mBvMsg.show();
            } else {
                //mBvMsg.hide();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(Constants.INTENT_ACTION_NOTICE);
        getActivity().registerReceiver(mNoticeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mNoticeReceiver);
    }

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
            throw new RuntimeException(ActiveViewPagerFragment.class.getSimpleName() + "'s parent activity must be a IMainTab");
        }

        //(SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.v2_tab_indicator2, R.id.tv_name);
        mViewPager = (ViewPager) view.findViewById(R.id.main_tab_pager);

        if (mTabAdapter == null) {
            mTabAdapter = new ActiveTabPagerAdapter(getChildFragmentManager());
        }
        mViewPager.setOffscreenPageLimit(mTabAdapter.getCount());
        mViewPager.setAdapter(mTabAdapter);
        //mViewPager.setOnPageChangeListener(this);
        //mTabStrip.setViewPager(mViewPager);

        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        //mBvAtMe = new BadgeView(getActivity(), mSlidingTabLayout.getBadgeView(1));
        //mBvAtMe.setBadgeMargin(0);
        //mBvAtMe.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        //mBvAtMe.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        //mBvAtMe.setBackgroundResource(R.drawable.tab_notification_bg);

        //mBvComment = new BadgeView(getActivity(), mSlidingTabLayout.getBadgeView(2));
        //mBvComment.setBadgeMargin(0);
        //mBvComment.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        //mBvComment.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        //mBvComment.setBackgroundResource(R.drawable.tab_notification_bg);

        //mBvMsg = new BadgeView(getActivity(), mSlidingTabLayout.getBadgeView(4));
        //mBvMsg.setBadgeMargin(0);
        //mBvMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        //mBvMsg.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        //mBvMsg.setBackgroundResource(R.drawable.tab_notification_bg);
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
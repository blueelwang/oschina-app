package net.oschina.app.v2.activity.blog.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.blog.adapter.BlogAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Blog;
import net.oschina.app.v2.model.BlogList;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 博客列表
 * 
 * @author william_sim
 */
public class BlogFragment extends BaseRecycleViewFragment {

	protected static final String TAG = "MainActivity";//BlogFragment.class.getSimpleName();
	private static final String CACHE_KEY_PREFIX = "blog_list";
	private static final long MAX_CACAHE_TIME = 12 * 3600 * 1000;// 博客的缓存最长时间为12小时

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        Activity parentActivity = getActivity();

        mRecycleView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            mRecycleView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }
    }

    @Override
	protected boolean requestDataFromNetWork() {
		return System.currentTimeMillis()
				- AppContext.getRefreshTime(getCacheKey()) > MAX_CACAHE_TIME;
	}

	@Override
	protected RecycleBaseAdapter getListAdapter() {
        //View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.v2_padding, null);
		return new BlogAdapter();//headerView
	}

	@Override
	protected String getCacheKeyPrefix() {
		return CACHE_KEY_PREFIX;
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		BlogList list = BlogList.parse(is);
		return list;
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		return ((BlogList) seri);
	}

	@Override
	protected void sendRequestData() {
		NewsApi.getBlogList(
				mCatalog == BlogList.CATALOG_RECOMMEND ? "recommend" : "latest",
				mCurrentPage, getResponseHandler());
	}

	@Override
	public void onItemClick(View view,  int position) {
		Blog blog = (Blog) mAdapter.getItem(position);
		if (blog != null) {
            UIHelper.showUrlRedirect(view.getContext(), blog.getUrl());
            AppContext.addReadedBlog(blog.getId());
            notifyDataSetChanged();
        }
	}
}

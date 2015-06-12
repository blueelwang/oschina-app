package net.oschina.app.v2.activity.question.fragment;

import java.io.InputStream;
import java.io.Serializable;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.question.adapter.QuestionAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseListFragment;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.Post;
import net.oschina.app.v2.model.PostList;
import net.oschina.app.v2.utils.UIHelper;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.tonlin.osc.happy.R;

/**
 * 问答
 * 
 * @author william_sim
 */
public class QuestionFragment extends BaseRecycleViewFragment {

	protected static final String TAG = QuestionFragment.class.getSimpleName();
	private static final String CACHE_KEY_PREFIX = "post_list";

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
	protected RecycleBaseAdapter getListAdapter() {
		return new QuestionAdapter();
	}

    @Override
    protected boolean isNeedListDivider() {
        return false;
    }

    @Override
	protected String getCacheKeyPrefix() {
		return CACHE_KEY_PREFIX;
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		PostList list = PostList.parse(is);
		return list;
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		return ((PostList) seri);
	}

	@Override
	protected void sendRequestData() {
		NewsApi.getPostList(mCatalog, mCurrentPage, getResponseHandler());
	}

	@Override
	public void onItemClick(View view, int position) {
		Post post = (Post) mAdapter.getItem(position);
		if (post != null) {
            UIHelper.showQuestionDetail(view.getContext(), post.getId());
            AppContext.addReadedQuestion(post.getId());
            notifyDataSetChanged();
        }
	}
}

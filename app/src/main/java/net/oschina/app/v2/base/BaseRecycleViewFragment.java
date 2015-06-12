package net.oschina.app.v2.base;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.MySwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.cache.v2.CacheManager;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.NewsList;
import net.oschina.app.v2.ui.decorator.DividerItemDecoration;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.widget.FixedRecyclerView;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.WeakAsyncTask;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

public abstract class BaseRecycleViewFragment extends BaseTabFragment implements
        RecycleBaseAdapter.OnItemClickListener, RecycleBaseAdapter.OnItemLongClickListener {

    public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
    private static final String TAG = "BaseRecycleViewFragment";
    protected MySwipeRefreshLayout mSwipeRefresh;
    protected FixedRecyclerView mRecycleView;
    protected LinearLayoutManager mLayoutManager;
    protected RecycleBaseAdapter mAdapter;
    protected EmptyLayout mErrorLayout;
    protected int mStoreEmptyState = -1;
    protected String mStoreEmptyMessage;

    protected int mCurrentPage = 0;
    protected int mCatalog = NewsList.CATALOG_ALL;

    //private AsyncTask<String, Void, ListEntity> mCacheTask;
    private ParserTask mParserTask;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            int totalItemCount = mLayoutManager.getItemCount();
            if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                if (mState == STATE_NONE && mAdapter != null
                        && mAdapter.getDataSize() > 0) {
                    loadMore();
                }
            }
        }
    };

    protected int getLayoutRes() {
        return R.layout.v2_fragment_swipe_refresh_recyclerview;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mCatalog = args.getInt(BUNDLE_KEY_CATALOG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        initViews(view);
        return view;
    }

    protected void initViews(View view) {
        mErrorLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentPage = 0;
                mState = STATE_REFRESH;
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                requestData(true);
            }
        });

        mSwipeRefresh = (MySwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.main_green, R.color.main_gray, R.color.main_black, R.color.main_purple);
        mSwipeRefresh.setOnRefreshListener(new MySwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecycleView = (FixedRecyclerView) view.findViewById(R.id.recycleView);
        mRecycleView.setOnScrollListener(mScrollListener);

        if(isNeedListDivider()) {
            // use a linear layout manager
            mRecycleView.addItemDecoration(new DividerItemDecoration(getActivity(),
                    DividerItemDecoration.VERTICAL_LIST));
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);

        if (mAdapter != null) {
            mRecycleView.setAdapter(mAdapter);
            mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
        } else {
            mAdapter = getListAdapter();
            mAdapter.setOnItemClickListener(this);
            mAdapter.setOnItemLongClickListener(this);
            mRecycleView.setAdapter(mAdapter);

            if (requestDataIfViewCreated()) {
                mCurrentPage = 0;
                mState = STATE_REFRESH;
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                //requestData(requestDataFromNetWork());
                new ReadCacheTask(this).execute();
            } else {
                mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
            }
        }

        if (mStoreEmptyState != -1) {
            mErrorLayout.setErrorType(mStoreEmptyState);
        }
        if (!TextUtils.isEmpty(mStoreEmptyMessage)) {
            mErrorLayout.setErrorMessage(mStoreEmptyMessage);
        }
    }

    @Override
    public void onDestroyView() {
        mStoreEmptyState = mErrorLayout.getErrorState();
        mStoreEmptyMessage = mErrorLayout.getMessage();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        //cancelReadCacheTask();
        //cancelParserTask();
        super.onDestroy();
    }

    @Override
    public void onItemClick(View view) {
        onItemClick(view, mRecycleView.getChildPosition(view));
    }

    protected void onItemClick(View view, int position) {
    }

    @Override
    public boolean onItemLongClick(View view) {
        return onItemLongClick(view, mRecycleView.getChildPosition(view));
    }

    protected boolean onItemLongClick(View view, int position) {
        return false;
    }

    protected abstract RecycleBaseAdapter getListAdapter();

    @Deprecated
    protected boolean requestDataFromNetWork() {
        return false;
    }

    protected boolean requestDataIfViewCreated() {
        return true;
    }

    protected String getCacheKeyPrefix() {
        return null;
    }

    protected ListEntity parseList(InputStream is) throws Exception {
        return null;
    }

    protected ListEntity readList(Serializable seri) {
        return null;
    }

    protected AsyncHttpResponseHandler getResponseHandler() {
        return new ResponseHandler(this);
    }

    protected void notifyDataSetChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public void refresh() {
        mCurrentPage = 0;
        mState = STATE_REFRESH;
        requestData(true);
    }

    public void loadMore() {
        if (mState == STATE_NONE) {
            if (mAdapter.getState() == ListBaseAdapter.STATE_LOAD_MORE) {
                TLog.log(TAG,"begin to load more data.");
                mCurrentPage++;
                mState = STATE_LOADMORE;
                requestData(false);
            }
        }
    }

    protected String getCacheKey() {
        return new StringBuffer(getCacheKeyPrefix()).append(mCatalog)
                .append("_").append(mCurrentPage).append("_")
                .append(TDevice.getPageSize()).toString();
    }

    protected void requestData(boolean refresh) {
        sendRequestData();
    }

    protected void sendRequestData() {

    }

    public long getCacheExpire() {
        return Constants.CACHE_EXPIRE_OND_DAY;
    }

    protected boolean isNeedListDivider() {
        return true;
    }

    static class ReadCacheTask extends
            WeakAsyncTask<Void, Void, byte[], BaseRecycleViewFragment> {

        public ReadCacheTask(BaseRecycleViewFragment target) {
            super(target);
        }

        @Override
        protected byte[] doInBackground(BaseRecycleViewFragment target,
                                        Void... params) {
            if (target == null) {
                TLog.log(TAG, "weak task target is null.");
                return null;
            }
            if (TextUtils.isEmpty(target.getCacheKey())) {
                TLog.log(TAG, "unset cache key.no cache.");
                return null;
            }

            byte[] data = CacheManager.getCache(target.getCacheKey());
            if (data == null) {
                TLog.log(TAG, "cache data is empty.:" + target.getCacheKey());
                return null;
            }

            TLog.log(TAG, "exist cache:" + target.getCacheKey() + " data:"
                    + data);

            return data;
        }

        @Override
        protected void onPostExecute(BaseRecycleViewFragment target,
                                     byte[] result) {
            super.onPostExecute(target, result);
            if (target == null)
                return;
            if (result != null) {
                try {
                    target.executeParserTask(result, true);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    TLog.log(TAG, "parser cache error :" + e.getMessage());
                }
            }
//            target.requestData(true);
            target.refresh();
        }
    }

    private static class ResponseHandler extends AsyncHttpResponseHandler {
        private WeakReference<BaseRecycleViewFragment> mInstance;

        ResponseHandler(BaseRecycleViewFragment instance) {
            mInstance = new WeakReference<>(instance);
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBytes) {
            if (mInstance != null) {
                BaseRecycleViewFragment instance = mInstance.get();
                if (instance.isAdded()) {
                    //if (instance.mState == STATE_REFRESH) {
                    //    instance.onRefreshNetworkSuccess();
                        //AppContext.setRefreshTime(instance.getCacheKey(),System.currentTimeMillis());
                    //}
                    instance.executeParserTask(responseBytes, false);
                }
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            if (mInstance != null) {
                BaseRecycleViewFragment instance = mInstance.get();
                if (instance.isAdded()) {
                    //.readCacheData(instance.getCacheKey());
                    instance.executeOnLoadDataError(null);
                    instance.executeOnLoadFinish();
                }
            }
        }
    }

    // Parse model when request data success.
    private static class ParserTask extends AsyncTask<Void, Void, String> {
        private WeakReference<BaseRecycleViewFragment> mInstance;
        private byte[] responseData;
        private boolean parserError;
        private boolean fromCache;
        private List<?> list;

        public ParserTask(BaseRecycleViewFragment instance, byte[] data, boolean fromCache) {
            this.mInstance = new WeakReference<>(instance);
            this.responseData = data;
            this.fromCache = fromCache;
        }

        @Override
        protected String doInBackground(Void... params) {
            BaseRecycleViewFragment instance = mInstance.get();
            if (instance == null) return null;
            try {
                ListEntity data = instance.parseList(new ByteArrayInputStream(responseData));
                if (!fromCache){
                    UIHelper.sendNoticeBroadcast(instance.getActivity(), data);
                }
                //new SaveCacheTask(instance, data, instance.getCacheKey()).execute();
                // save the cache
                if (!fromCache && instance.mCurrentPage == 0 && !TextUtils.isEmpty(instance.getCacheKey())) {
                    CacheManager.setCache(instance.getCacheKey(), responseData,
                            instance.getCacheExpire(), CacheManager.TYPE_INTERNAL);
                }
                list = data.getList();
            } catch (Exception e) {
                e.printStackTrace();
                parserError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            BaseRecycleViewFragment instance = mInstance.get();
            if (instance != null) {
                if (parserError) {
                    //instance.readCacheData(instance.getCacheKey());
                    instance.executeOnLoadDataError(null);
                } else {
                    instance.executeOnLoadDataSuccess(list);
                    if(!fromCache) {
                        if (instance.mState == STATE_REFRESH) {
                            instance.onRefreshNetworkSuccess();
                        }
                    }
                    instance.executeOnLoadFinish();
                }
                if (fromCache) {
                    TLog.log(TAG, "key:" + instance.getCacheKey()
                            + ",set cache data finish ,begin to load network data.");
//                    instance.requestData(true);
                    instance.refresh();
                }
            }
        }
    }

    protected void executeOnLoadDataSuccess(List<?> data) {
        if (mState == STATE_REFRESH)
            mAdapter.clear();
        mAdapter.addData(data);
        mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
        if (data.size() == 0 && mState == STATE_REFRESH) {
            mErrorLayout.setErrorType(EmptyLayout.NODATA);
        } else if (data.size() < TDevice.getPageSize()) {
            if (mState == STATE_REFRESH)
                mAdapter.setState(ListBaseAdapter.STATE_LESS_ONE_PAGE);
            else
                mAdapter.setState(ListBaseAdapter.STATE_NO_MORE);
        } else {
            mAdapter.setState(ListBaseAdapter.STATE_LOAD_MORE);
        }
    }

    protected void onRefreshNetworkSuccess() {
        // TODO do nothing
    }

    protected void executeOnLoadDataError(String error) {
        if (mCurrentPage == 0) {
            if (mAdapter.getDataSize() == 0) {
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
            } else {
                mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
                String message = error;
                if (TextUtils.isEmpty(error)) {
                    if (TDevice.hasInternet()) {
                        message = getString(R.string.tip_load_data_error);
                    } else {
                        message = getString(R.string.tip_network_error);
                    }
                }
                AppContext.showToastShort(message);
            }
        } else {
            mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
            mAdapter.setState(RecycleBaseAdapter.STATE_NETWORK_ERROR);
        }
        mAdapter.notifyDataSetChanged();
    }

    protected void executeOnLoadFinish() {
        mSwipeRefresh.setRefreshing(false);
        mState = STATE_NONE;
    }

    private void executeParserTask(byte[] data, boolean fromCache) {
        cancelParserTask();
        mParserTask = new ParserTask(this, data, fromCache);
        mParserTask.execute();
    }

    private void cancelParserTask() {
        if (mParserTask != null) {
            mParserTask.cancel(true);
            mParserTask = null;
        }
    }
}

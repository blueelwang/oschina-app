package net.oschina.app.v2.activity.active.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;

//import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.active.adapter.ActiveAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Active;
import net.oschina.app.v2.model.ActiveList;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.Notice;
import net.oschina.app.v2.service.NoticeUtils;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.HTMLSpirit;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;

import java.io.InputStream;
import java.io.Serializable;

//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.AdapterView.OnItemLongClickListener;

/**
 * 动态
 *
 * @author william_sim
 */
public class ActiveFragment extends BaseRecycleViewFragment {

    protected static final String TAG = ActiveFragment.class.getSimpleName();
    private static final String CACHE_KEY_PREFIX = "active_list";
    private boolean mIsWatingLogin;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mErrorLayout != null) {
                mIsWatingLogin = true;
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
                mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(Constants.INTENT_ACTION_LOGOUT);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (mIsWatingLogin) {
            mCurrentPage = 0;
            mState = STATE_REFRESH;
            requestData(false);
        }
        super.onResume();
    }

    @Override
    protected RecycleBaseAdapter getListAdapter() {
        RecycleBaseAdapter adapter = new ActiveAdapter();
        adapter.setOnItemLongClickListener(this);
        return adapter;
    }

    @Override
    protected boolean isNeedListDivider() {
        return false;
    }

    @Override
    protected String getCacheKeyPrefix() {
        return new StringBuffer(CACHE_KEY_PREFIX).append(
                AppContext.instance().getLoginUid()).toString();
    }

    @Override
    protected ListEntity parseList(InputStream is) throws Exception {
        ActiveList list = ActiveList.parse(is);
        return list;
    }

    @Override
    protected ListEntity readList(Serializable seri) {
        return ((ActiveList) seri);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        Activity parentActivity = getActivity();

        mRecycleView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            mRecycleView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AppContext.instance().isLogin()) {
                    requestData(false);
                } else {
                    UIHelper.showLogin(getActivity());
                }
            }
        });
        if (AppContext.instance().isLogin()) {
            int type = -100;
            if (mCatalog == ActiveList.CATALOG_ATME)
                type = Notice.TYPE_ATME;
            else if (mCatalog == ActiveList.CATALOG_COMMENT)
                type = Notice.TYPE_COMMENT;
            else if (mCatalog == -1)
                type = Notice.TYPE_MESSAGE;
            if (type != -100) {
                UIHelper.sendBroadcastForNotice(getActivity());
            }
        }
    }

    @Override
    protected void requestData(boolean refresh) {
        // mErrorLayout.setErrorMessage("");
        if (AppContext.instance().isLogin()) {
            mIsWatingLogin = false;
            super.requestData(refresh);
        } else {
            mIsWatingLogin = true;
            mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
            mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
        }
    }

    @Override
    protected void sendRequestData() {
        NewsApi.getActiveList(AppContext.instance().getLoginUid(), mCatalog,
                mCurrentPage, getResponseHandler());
    }

    @Override
    protected void onRefreshNetworkSuccess() {
        if (AppContext.instance().isLogin()) {
            int type = -100;
            if (mCatalog == ActiveList.CATALOG_ATME)
                type = Notice.TYPE_ATME;
            else if (mCatalog == ActiveList.CATALOG_COMMENT)
                type = Notice.TYPE_COMMENT;
            else if (mCatalog == -1)
                type = Notice.TYPE_MESSAGE;
            if (type != -100) {
                NoticeUtils.clearNotice(type);
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Active active = (Active) mAdapter.getItem(position);
        if (active != null)
            UIHelper.showActiveRedirect(view.getContext(), active);
    }

    @Override
    protected boolean onItemLongClick(View view, int position) {
        final Active active = (Active) mAdapter.getItem(position);
        if (active == null)
            return false;
        String[] items = new String[]{
                getResources().getString(R.string.view),
                getResources().getString(R.string.copy)};
        //final CommonDialog dialog = DialogHelper
        //		.getPinterestDialogCancelable(getActivity());
        //dialog.setTitle(R.string.operation);
        //dialog.setNegativeButton(R.string.cancle, null);
        //dialog.setItemsWithoutChk(items, new AdapterView.OnItemClickListener() {
        //
        //	@Override
        //	public void onItemClick(AdapterView<?> parent, View view,
        //			int position, long id) {
        //		dialog.dismiss();
        //		if (position == 0) {
        //			UIHelper.showActiveRedirect(view.getContext(), active);
        //		} else if (position == 1) {
        //			TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(active
        //					.getMessage()));
        //		}
        //	}
        //});
        //dialog.show();

//        new MaterialDialog.Builder(getActivity())
//                .items(items)
//                .itemsCallback(new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
//                        materialDialog.dismiss();
//                        if (i == 0) {
//                            UIHelper.showActiveRedirect(view.getContext(), active);
//                        } else if (i == 1) {
//                            TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(active
//                                    .getMessage()));
//                        }
//                    }
//                })
//                .title(R.string.operation)
//                .show();

        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
                R.style.Theme_AppCompat_Light_Dialog_Alert)
        .setTitle(R.string.operation)
        .setCancelable(true)
        .setItems(items,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            UIHelper.showActiveRedirect(getActivity(), active);
                        } else if (which == 1) {
                            TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(active
                                    .getMessage()));
                        }
            }
        }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        return true;
    }
}

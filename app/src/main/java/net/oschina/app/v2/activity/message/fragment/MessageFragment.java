package net.oschina.app.v2.activity.message.fragment;

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
import net.oschina.app.v2.activity.active.fragment.ActiveFragment;
import net.oschina.app.v2.activity.message.adapter.MessageAdapter;
import net.oschina.app.v2.api.OperationResponseHandler;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.MessageList;
import net.oschina.app.v2.model.Message;
import net.oschina.app.v2.model.Notice;
import net.oschina.app.v2.model.Result;
import net.oschina.app.v2.service.NoticeUtils;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.HTMLSpirit;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

public class MessageFragment extends BaseRecycleViewFragment {
	protected static final String TAG = ActiveFragment.class.getSimpleName();
	private static final String CACHE_KEY_PREFIX = "message_list";
	private boolean mIsWatingLogin;

	private BroadcastReceiver mLogoutReceiver = new BroadcastReceiver() {

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
		getActivity().registerReceiver(mLogoutReceiver, filter);
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(mLogoutReceiver);
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
        RecycleBaseAdapter adapter = new MessageAdapter();
        return adapter;
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
		MessageList list = MessageList.parse(is);
		return list;
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		return ((MessageList) seri);
	}

	@Override
	protected void initViews(View view) {
		super.initViews(view);
        Activity parentActivity = getActivity();

        mRecycleView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            mRecycleView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }
		//mListView.setOnItemLongClickListener(this);
		//mListView.setDivider(null);
		//mListView.setDividerHeight(0);
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
			UIHelper.sendBroadcastForNotice(getActivity());
		}
	}

	@Override
	protected void requestData(boolean refresh) {
		mErrorLayout.setErrorMessage("");
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
		NewsApi.getMessageList(AppContext.instance().getLoginUid(),
				mCurrentPage, getResponseHandler());
	}

	@Override
	protected void onRefreshNetworkSuccess() {
		NoticeUtils.clearNotice(Notice.TYPE_MESSAGE);
	}

	@Override
	public void onItemClick(View view, int position) {
		Message message = (Message) mAdapter.getItem(position);
		if (message != null)
			UIHelper.showMessageDetail(getActivity(), message.getFriendId(),
					message.getFriendName());
	}

	@Override
	public boolean onItemLongClick(View view, int position) {
		final Message message = (Message) mAdapter.getItem(position);
//		final CommonDialog dialog = DialogHelper
//				.getPinterestDialogCancelable(getActivity());
//		dialog.setItemsWithoutChk(
//				getResources().getStringArray(R.array.message_list_options),
//				new OnItemClickListener() {
//
//					@Override
//					public void onItemClick(AdapterView<?> parent, View view,
//							int position, long id) {
//						dialog.dismiss();
//						switch (position) {
//						case 0:
//							UIHelper.showMessagePub(getActivity(),
//									message.getFriendId(),
//									message.getFriendName());
//							break;
//						case 1:
//							UIHelper.showMessageForward(getActivity(),
//									message.getFriendName(),
//									message.getContent());
//							break;
//						case 2:
//							handleDeleteMessage(message);
//							break;
//						default:
//							break;
//						}
//					}
//				});
//		dialog.setNegativeButton(R.string.cancle, null);
//		dialog.show();

//        new MaterialDialog.Builder(getActivity())
//                .title(R.string.operation)
//                .items(R.array.message_list_options)
//                .itemsCallback(new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        dialog.dismiss();
//						switch (which) {
//						case 0:
//							UIHelper.showMessagePub(getActivity(),
//									message.getFriendId(),
//									message.getFriendName());
//							break;
//						case 1:
//							UIHelper.showMessageForward(getActivity(),
//									message.getFriendName(),
//									message.getContent());
//							break;
//						case 2:
//							handleDeleteMessage(message);
//							break;
//						default:
//							break;
//						}
//                    }
//                })
//                .show();
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.operation)
                .setCancelable(true)
                .setItems(R.array.message_list_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                UIHelper.showMessagePub(getActivity(),
                                        message.getFriendId(),
                                        message.getFriendName());
                                break;
                            case 1:
                                UIHelper.showMessageForward(getActivity(),
                                        message.getFriendName(),
                                        message.getContent());
                                break;
                            case 2:
                                handleDeleteMessage(message);
                                break;
                            default:
                                break;
                        }
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
		return true;
	}

	private void handleDeleteMessage(final Message message) {
//		CommonDialog dialog = DialogHelper
//				.getPinterestDialogCancelable(getActivity());
//		dialog.setMessage(getString(R.string.confirm_delete_message,
//				message.getFriendName()));
//		dialog.setNegativeButton(R.string.cancle, null);
//		dialog.setPositiveButton(R.string.ok,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						showWaitDialog(R.string.progress_submit);
//
//						NewsApi.deleteMessage(AppContext.instance()
//								.getLoginUid(), message.getFriendId(),
//								new DeleteMessageOperationHandler(message));
//					}
//				});
//		dialog.show();

//        new MaterialDialog.Builder(getActivity())
//                .content(getString(R.string.confirm_delete_message,message.getFriendName()))
//                .positiveText(R.string.ok)
//                .negativeText(R.string.cancel)
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog dialog) {
//                        super.onPositive(dialog);
//                        dialog.dismiss();
//						showWaitDialog(R.string.progress_submit);
//
//						NewsApi.deleteMessage(AppContext.instance()
//								.getLoginUid(), message.getFriendId(),
//								new DeleteMessageOperationHandler(message));
//                    }
//                })
//                .show();

        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setCancelable(true)
                .setMessage(getString(R.string.confirm_delete_message,message.getFriendName()))
                .setNegativeButton(R.string.cancel,null)
                .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
						showWaitDialog(R.string.progress_submit);
						NewsApi.deleteMessage(AppContext.instance()
								.getLoginUid(), message.getFriendId(),
								new DeleteMessageOperationHandler(message));
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
	}

	class DeleteMessageOperationHandler extends OperationResponseHandler {

		public DeleteMessageOperationHandler(Object... args) {
			super(args);
		}

		@Override
		public void onSuccess(int code, ByteArrayInputStream is, Object[] args)
				throws Exception {
			Result res = Result.parse(is);
			if (res.OK()) {
				Message msg = (Message) args[0];
				mAdapter.removeItem(msg);
				hideWaitDialog();
				AppContext.showToastShort(R.string.tip_delete_success);
                UIHelper.sendNoticeBroadcast(getActivity(),res);
			} else {
				AppContext.showToastShort(res.getErrorMessage());
				hideWaitDialog();
			}
		}

		@Override
		public void onFailure(int code, String errorMessage, Object[] args) {
			AppContext.showToastShort(R.string.tip_delete_faile);
			hideWaitDialog();
		}
	}
}

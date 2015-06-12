package net.oschina.app.v2.activity.comment.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

//import com.afollestad.materialdialogs.MaterialDialog;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.comment.adapter.CommentAdapter;
import net.oschina.app.v2.activity.comment.adapter.CommentAdapter.OnOperationListener;
import net.oschina.app.v2.activity.news.fragment.NewsFragment;
import net.oschina.app.v2.api.OperationResponseHandler;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.emoji.EmojiFragment;
import net.oschina.app.v2.emoji.EmojiFragment.EmojiTextListener;
import net.oschina.app.v2.model.BlogCommentList;
import net.oschina.app.v2.model.Comment;
import net.oschina.app.v2.model.CommentList;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.Result;
import net.oschina.app.v2.service.PublicCommentTask;
import net.oschina.app.v2.service.ServerTaskUtils;
import net.oschina.app.v2.utils.HTMLSpirit;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

public class CommentFragment extends BaseRecycleViewFragment implements
		OnOperationListener, EmojiTextListener {

	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
	public static final String BUNDLE_KEY_BLOG = "BUNDLE_KEY_BLOG";
	public static final String BUNDLE_KEY_ID = "BUNDLE_KEY_ID";
	public static final String BUNDLE_KEY_OWNER_ID = "BUNDLE_KEY_OWNER_ID";
	protected static final String TAG = NewsFragment.class.getSimpleName();
	private static final String BLOG_CACHE_KEY_PREFIX = "blogcomment_list";
	private static final String CACHE_KEY_PREFIX = "comment_list";
	private static final int REQUEST_CODE = 0x10;
	private static final String COMMENT_SCREEN = "comment_screen";

	private int mId, mOwnerId;
	private boolean mIsBlogComment;

	private EmojiFragment mEmojiFragment;
    private CommentChangeReceiver mReceiver;

    class CommentChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int opt = intent.getIntExtra(Comment.BUNDLE_KEY_OPERATION, 0);
            int id = intent.getIntExtra(Comment.BUNDLE_KEY_ID, 0);
            int catalog = intent.getIntExtra(Comment.BUNDLE_KEY_CATALOG, 0);
            boolean isBlog = intent.getBooleanExtra(Comment.BUNDLE_KEY_BLOG,
                    false);
            Comment comment = intent.getParcelableExtra(Comment.BUNDLE_KEY_COMMENT);

            if (id == mId && catalog == mCatalog && !isBlog) {
                if (Comment.OPT_ADD == opt) {
                    refresh();
                }
            } else {
				if (Comment.OPT_ADD == opt) {
					refresh();
				}
			}
        }
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		BaseActivity act = ((BaseActivity) activity);
		FragmentTransaction trans = act.getSupportFragmentManager()
				.beginTransaction();
		mEmojiFragment = new EmojiFragment();
		mEmojiFragment.setEmojiTextListener(this);
		trans.replace(R.id.emoji_container, mEmojiFragment);
		trans.commit();
		activity.findViewById(R.id.emoji_container).setVisibility(View.VISIBLE);
	}

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mCatalog = args.getInt(BUNDLE_KEY_CATALOG, 0);
			mId = args.getInt(BUNDLE_KEY_ID, 0);
			mOwnerId = args.getInt(BUNDLE_KEY_OWNER_ID, 0);
			mIsBlogComment = args.getBoolean(BUNDLE_KEY_BLOG, false);
		}

		if (!mIsBlogComment && mCatalog == CommentList.CATALOG_POST) {
			((BaseActivity) getActivity())
					.setActionBarTitle(R.string.post_answer);
		}

		int mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		getActivity().getWindow().setSoftInputMode(mode);

        mReceiver = new CommentChangeReceiver();
        IntentFilter filter = new IntentFilter(
                Constants.INTENT_ACTION_COMMENT_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			// if (mState == STATE_NONE) {
			// mCurrentPage = 0;
			// mState = STATE_REFRESH;
			// requestData(true);
			// }
			Comment comment = data
					.getParcelableExtra(Comment.BUNDLE_KEY_COMMENT);
			if (comment != null) {
				mAdapter.addItem(0, comment);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected RecycleBaseAdapter getListAdapter() {
        return new CommentAdapter(this,true);
	}

    @Override
    protected boolean isNeedListDivider() {
        return false;
    }

    @Override
	protected String getCacheKeyPrefix() {
		String str = mIsBlogComment ? BLOG_CACHE_KEY_PREFIX : CACHE_KEY_PREFIX;
		return new StringBuilder(str).append("_").append(mId).append("_Owner")
				.append(mOwnerId).toString();
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		if (mIsBlogComment) {
			return BlogCommentList.parse(is);
		} else {
			return CommentList.parse(is);
		}
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		if (mIsBlogComment)
			return ((BlogCommentList) seri);
		return ((CommentList) seri);
	}

	@Override
	public boolean onBackPressed() {
		if (mEmojiFragment != null) {
			return mEmojiFragment.onBackPressed();
		}
		return super.onBackPressed();
	}

	@Override
	protected void sendRequestData() {
		if (mIsBlogComment) {
			NewsApi.getBlogCommentList(mId, mCurrentPage, getResponseHandler());
		} else {
			NewsApi.getCommentList(mId, mCatalog, mCurrentPage, getResponseHandler());
		}
	}

	@Override
	public void onItemClick(View view, int position) {
		final Comment comment = (Comment) mAdapter.getItem(position);
		if (comment == null)
			return;
		handleReplyComment(comment);
	}

	@Override
	public boolean onItemLongClick(View view,int position) {
		final Comment item = (Comment) mAdapter.getItem(position);
		if (item == null)
			return false;
		String[] items;
		if (AppContext.instance().isLogin()
				&& AppContext.instance().getLoginUid() == item.getAuthorId()) {
			items = new String[] {
					getResources().getString(R.string.reply),
					getResources().getString(R.string.copy),
					getResources().getString(R.string.delete)};
		} else {
			items = new String[] {
					getResources().getString(R.string.reply),
					getResources().getString(R.string.copy) };
		}
		
		//final CommonDialog dialog = DialogHelper
		//		.getPinterestDialogCancelable(getActivity());
		//dialog.setTitle(R.string.operation);
		//dialog.setNegativeButton(R.string.cancle, null);
		//dialog.setItemsWithoutChk(items, new OnItemClickListener() {
        //
		//	@Override
		//	public void onItemClick(AdapterView<?> parent, View view,
		//			int position, long id) {
		//		dialog.dismiss();
		//		if (position == 0) {
		//			handleReplyComment(item);
		//		} else if (position == 1) {
		//			TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(item
		//					.getContent()));
		//		} else if(position==2){
		//			handleDeleteComment(item);
		//		}
		//	}
		//});
		//dialog.show();

//        new MaterialDialog.Builder(getActivity())
//                .title(R.string.operation)
//                .items(items)
//                .itemsCallback(new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View view, int position, CharSequence text) {
//                        dialog.dismiss();
//                        if (position == 0) {
//                            handleReplyComment(item);
//                        } else if (position == 1) {
//                            TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(item
//                                    .getContent()));
//                        } else if (position == 2) {
//                            handleDeleteComment(item);
//                        }
//                    }
//                })
//                .show();

        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.operation)
                .setCancelable(true)
                .setItems(items,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        dialog.dismiss();
                        if (position == 0) {
                            handleReplyComment(item);
                        } else if (position == 1) {
                            TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(item
                                    .getContent()));
                        } else if (position == 2) {
                            handleDeleteComment(item);
                        }
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
		return true;
	}

	private void handleReplyComment(Comment comment) {
		if (!AppContext.instance().isLogin()) {
			UIHelper.showLogin(getActivity());
			return;
		}
		UIHelper.showReplyCommentForResult(this, REQUEST_CODE, mIsBlogComment,
				mId, mCatalog, comment);

	}

	private void handleDeleteComment(Comment comment) {
		if (!AppContext.instance().isLogin()) {
			UIHelper.showLogin(getActivity());
			return;
		}
		AppContext.showToastShort(R.string.deleting);
		if (mIsBlogComment) {
			NewsApi.deleteBlogComment(AppContext.instance().getLoginUid(), mId,
					comment.getId(), comment.getAuthorId(), mOwnerId,
					new DeleteOperationResponseHandler(comment));
		} else {
			NewsApi.deleteComment(mId, mCatalog, comment.getId(), comment
					.getAuthorId(), new DeleteOperationResponseHandler(comment));
		}
	}

	@Override
	public void onMoreClick(final Comment comment) {
	}

	@Override
	public void onSendClick(String text) {
        if (!TDevice.hasInternet()) {
            AppContext.showToastShort(R.string.tip_network_error);
            return;
        }
        if (!AppContext.instance().isLogin()) {
            UIHelper.showLogin(getActivity());
            return;
        }
        if (TextUtils.isEmpty(text)) {
            AppContext.showToastShort(R.string.tip_comment_content_empty);
            mEmojiFragment.requestFocusInput();
            return;
        }
        PublicCommentTask task = new PublicCommentTask();
        task.setId(mId);
		task.setContent(text);
		task.setUid(AppContext.instance().getLoginUid());
		if(!mIsBlogComment) {
			task.setCatalog(CommentList.CATALOG_NEWS);
			task.setIsPostToMyZone(0);
			ServerTaskUtils.publicComment(getActivity(), task);
		} else{
			ServerTaskUtils.publicBlogComment(getActivity(), task);
		}
        mEmojiFragment.reset();
	}

	class DeleteOperationResponseHandler extends OperationResponseHandler {

		DeleteOperationResponseHandler(Object... args) {
			super(args);
		}

		@Override
		public void onSuccess(int code, ByteArrayInputStream is, Object[] args) {
			try {
				Result res = Result.parse(is);
				if (res.OK()) {
					AppContext.showToastShort(R.string.delete_success);
                    UIHelper.sendNoticeBroadcast(getActivity(),res);
					mAdapter.removeItem(args[0]);
				} else {
					AppContext.showToastShort(res.getErrorMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(code, e.getMessage(), args);
			}
		}

		@Override
		public void onFailure(int code, String errorMessage, Object[] args) {
			AppContext.showToastShort(R.string.delete_faile);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(COMMENT_SCREEN);
		MobclickAgent.onResume(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(COMMENT_SCREEN);
		MobclickAgent.onPause(getActivity());
	}
}

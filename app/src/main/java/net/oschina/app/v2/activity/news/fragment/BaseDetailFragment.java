package net.oschina.app.v2.activity.news.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.MySwipeRefreshLayout;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ZoomButtonsController;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.utils.OauthHelper;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.news.view.ShareDialog;
import net.oschina.app.v2.activity.news.view.ShareDialog.OnSharePlatformClick;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.cache.v2.CacheManager;
import net.oschina.app.v2.model.Comment;
import net.oschina.app.v2.model.Entity;
import net.oschina.app.v2.model.Result;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.widget.WebView;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.WeakAsyncTask;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

//import android.webkit.WebView;

public class BaseDetailFragment extends BaseFragment implements
        View.OnTouchListener, MySwipeRefreshLayout.OnRefreshListener {// OnItemClickListener
    private static final String TAG = "BaseDetailFragment";

    final UMSocialService mController = UMServiceFactory
            .getUMSocialService("com.umeng.share");

    protected MySwipeRefreshLayout mRefreshView;
    protected EmptyLayout mEmptyLayout;
    protected WebView mWebView;

    private float mDownX, mDownY;
    private boolean mIsFavorited;
    private boolean mLoadCacheSuccess;

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
            onCommentChanged(opt, id, catalog, isBlog, comment);
        }
    }

    static class AddFavoriteResponseHandler extends AsyncHttpResponseHandler {

        private WeakReference<BaseDetailFragment> mInstance;

        AddFavoriteResponseHandler(BaseDetailFragment instance) {
            mInstance = new WeakReference<>(instance);
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            if (mInstance == null)
                return;
            BaseDetailFragment instance = mInstance.get();
            if (instance == null)
                return;
            try {
                Result res = Result.parse(new ByteArrayInputStream(arg2));
                if (res.OK()) {
                    AppContext.showToastShort(R.string.add_favorite_success);
                    instance.mIsFavorited = true;
                    instance.sendRequestData();
                    instance.onFavoriteChanged(true);
                } else {
                    AppContext.showToastShort(res.getErrorMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(arg0, arg1, arg2, e);
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            AppContext.showToastShort(R.string.add_favorite_faile);
        }
    }

    static class DelFavoriteResponseHandler extends AsyncHttpResponseHandler {

        private WeakReference<BaseDetailFragment> mInstance;

        DelFavoriteResponseHandler(BaseDetailFragment instance) {
            mInstance = new WeakReference<>(instance);
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            if (mInstance == null)
                return;
            BaseDetailFragment instance = mInstance.get();
            if (instance == null)
                return;
            try {
                Result res = Result.parse(new ByteArrayInputStream(bytes));
                if (res.OK()) {
                    AppContext.showToastShort(R.string.del_favorite_success);
                    instance.mIsFavorited = false;
                    instance.sendRequestData();
                    instance.onFavoriteChanged(false);
                } else {
                    AppContext.showToastShort(res.getErrorMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(i, headers, bytes, e);
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            AppContext.showToastShort(R.string.del_favorite_faile);
        }
    }

    protected AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            try {
                Entity entity = parseData(new ByteArrayInputStream(arg2));
                if (entity != null && entity.getId() > 0) {
                    UIHelper.sendNoticeBroadcast(getActivity(), entity);
                    executeOnLoadDataSuccess(entity);
                    executeOnLoadFinish();
                    CacheManager.setCache(getCacheKey(), arg2, getCacheExpore(), CacheManager.TYPE_INTERNAL);
                } else {
                    throw new RuntimeException("load detail error");
                }
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(arg0, arg1, arg2, e);
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            executeOnLoadDataError(arg3.getMessage());
            executeOnLoadFinish();
        }
    };


    static class ReadCacheTask extends
            WeakAsyncTask<Void, Void, byte[], BaseDetailFragment> {

        public ReadCacheTask(BaseDetailFragment target) {
            super(target);
        }

        @Override
        protected byte[] doInBackground(BaseDetailFragment target,
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

            TLog.log(TAG, "exist cache:" + target.getCacheKey() + " data:" + data);
            return data;
        }

        @Override
        protected void onPostExecute(BaseDetailFragment target,
                                     byte[] result) {
            super.onPostExecute(target, result);
            if (target == null)
                return;
            if (result != null) {
                try {
                    Entity entity = target.parseData(new ByteArrayInputStream(result));
                    if (entity != null && entity.getId() > 0) {
                        target.executeOnLoadDataSuccess(entity);
                        target.mLoadCacheSuccess = true;
                        //return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            target.sendRequestData();
        }
    }

    protected WebViewClient mWebClient = new WebViewClient() {

        private boolean receivedError = false;

        @Override
        public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
            receivedError = false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
            UIHelper.showUrlRedirect(view.getContext(), url);
            return true;
        }

        @Override
        public void onPageFinished(android.webkit.WebView view, String url) {
            if (mEmptyLayout != null) {
                if (receivedError) {
                    mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
                } else {
                    mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
                }
            }
        }

        @Override
        public void onReceivedError(android.webkit.WebView view, int errorCode,
                                    String description, String failingUrl) {
            receivedError = true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (shouldRegisterCommentChangedReceiver()) {
            mReceiver = new CommentChangeReceiver();
            IntentFilter filter = new IntentFilter(
                    Constants.INTENT_ACTION_COMMENT_CHANGED);
            getActivity().registerReceiver(mReceiver, filter);
        }

        mController.getConfig().closeToast();
    }

    @Override
    public void onDestroyView() {
        recycleWebView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        //cancelReadCache();
        recycleWebView();
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshView = (MySwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        if (mRefreshView != null) {
            mRefreshView.setColorSchemeResources(R.color.main_green, R.color.main_gray, R.color.main_black, R.color.main_purple);
            mRefreshView.setOnRefreshListener(this);
        }

        requestData();

        mWebView = (WebView) view.findViewById(R.id.webview);
        mWebView.setOnTouchListener(this);
    }

    @Override
    public void onRefresh() {
        sendRequestData();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                float currentX = event.getX();
                float currentY = event.getY();
                if (Math.abs(mDownY - currentY) < TDevice.dpToPixel(40) && currentX - mDownX > TDevice.dpToPixel(30)) {
                    if (!mWebView.canScrollHorizontally(-1)) {
                        getActivity().finish();
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void initWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setDefaultFontSize(15);
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        int sysVersion = Build.VERSION.SDK_INT;
        if (sysVersion >= 11) {
            settings.setDisplayZoomControls(false);
        } else {
            ZoomButtonsController zbc = new ZoomButtonsController(webView);
            zbc.getZoomControls().setVisibility(View.GONE);
        }
        UIHelper.addWebImageShow(getActivity(), webView);
    }

    protected void recycleWebView() {
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    protected void requestData() {
        mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
        new ReadCacheTask(this).execute();
    }

    protected boolean shouldRegisterCommentChangedReceiver() {
        return true;
    }

    protected String getCacheKey() {
        return null;
    }

    protected long getCacheExpore() {
        return Constants.CACHE_EXPIRE_OND_DAY;
    }

    protected Entity parseData(InputStream is) throws Exception {
        return null;
    }

    protected void onCommentChanged(int opt, int id, int catalog,
                                    boolean isBlog, Comment comment) {
    }

    protected void sendRequestData() {
    }

    protected void executeOnLoadDataSuccess(Entity entity) {

    }

    protected boolean hasReportMenu() {
        return true;
    }

    protected void onReportMenuClick() {
    }

    protected void executeOnLoadDataError(String object) {
        if (mLoadCacheSuccess) {
            if (TDevice.hasInternet())
                AppContext.showToastShort(R.string.tip_load_data_error);
            else
                AppContext.showToastShort(R.string.tip_network_error);
        } else {
            mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
            mEmptyLayout.setOnLayoutClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mState = STATE_REFRESH;
                    mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                    //requestData(true);
                    sendRequestData();
                }
            });
        }
    }

    protected void executeOnLoadFinish() {
        if (mRefreshView != null)
            mRefreshView.setRefreshing(false);
    }

    protected void onFavoriteChanged(boolean flag) {
    }

    protected void handleFavoriteOrNot() {
        if (!TDevice.hasInternet()) {
            AppContext.showToastShort(R.string.tip_no_internet);
            return;
        }
        if (!AppContext.instance().isLogin()) {
            UIHelper.showLogin(getActivity());
            return;
        }
        if (getFavoriteTargetId() == -1 || getFavoriteTargetType() == -1) {
            return;
        }
        int uid = AppContext.instance().getLoginUid();
        if (mIsFavorited) {//mMenuAdapter.isFavorite()
            NewsApi.delFavorite(uid, getFavoriteTargetId(),
                    getFavoriteTargetType(), new DelFavoriteResponseHandler(this));
        } else {
            NewsApi.addFavorite(uid, getFavoriteTargetId(),
                    getFavoriteTargetType(), new AddFavoriteResponseHandler(this));
        }
    }

    protected void notifyFavorite(boolean favorite) {
        mIsFavorited = favorite;
        onFavoriteChanged(favorite);
    }

    protected int getFavoriteTargetId() {
        return -1;
    }

    protected int getFavoriteTargetType() {
        return -1;
    }

    protected String getShareUrl() {
        return "";
    }

    protected String getShareTitle() {
        return getString(R.string.share_title);
    }

    protected String getShareContent() {
        return "";
    }

    protected void handleShare() {
        if (TextUtils.isEmpty(getShareContent())
                || TextUtils.isEmpty(getShareUrl())) {
            return;
        }
        final ShareDialog dialog = new ShareDialog(getActivity());
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(R.string.share_to);
        dialog.setOnPlatformClickListener(new OnSharePlatformClick() {

            @Override
            public void onPlatformClick(SHARE_MEDIA media) {
                switch (media) {
                    case QQ:
                        shareToQQ(media);
                        break;
                    case QZONE:
                        shareToQZone();
                        break;
                    case TENCENT:
                        shareToTencentWeibo();
                        break;
                    case SINA:
                        shareToSinaWeibo();
                        break;
                    case WEIXIN:
                        shareToWeiChat();
                        break;
                    case WEIXIN_CIRCLE:
                        shareToWeiChatCircle();
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }

    private void shareToWeiChatCircle() {
        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(getActivity(),
                Constants.WEICHAT_APPID, Constants.WEICHAT_SECRET);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
        // 设置微信朋友圈分享内容
        CircleShareContent circleMedia = new CircleShareContent();
        circleMedia.setShareContent(getShareContent());
        // 设置朋友圈title
        circleMedia.setTitle(getShareTitle());
        circleMedia.setShareImage(new UMImage(getActivity(), R.drawable.ima_app_icon));
        circleMedia.setTargetUrl(getShareUrl());
        mController.setShareMedia(circleMedia);
        mController.postShare(getActivity(), SHARE_MEDIA.WEIXIN_CIRCLE,
                new SnsPostListener() {

                    @Override
                    public void onStart() {
                        AppContext.showToastShort(R.string.tip_start_share);
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA arg0, int arg1,
                                           SocializeEntity arg2) {
                        AppContext.showToastShort(R.string.tip_share_done);
                    }
                });
    }

    private void shareToWeiChat() {
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(getActivity(),
                Constants.WEICHAT_APPID, Constants.WEICHAT_SECRET);
        wxHandler.addToSocialSDK();
        // 设置微信好友分享内容
        WeiXinShareContent weixinContent = new WeiXinShareContent();
        // 设置分享文字
        weixinContent.setShareContent(getShareContent());
        // 设置title
        weixinContent.setTitle(getShareTitle());
        // 设置分享内容跳转URL
        weixinContent.setTargetUrl(getShareUrl());
        // 设置分享图片
        weixinContent.setShareImage(new UMImage(getActivity(), R.drawable.ima_app_icon));
        mController.setShareMedia(weixinContent);
        mController.postShare(getActivity(), SHARE_MEDIA.WEIXIN,
                new SnsPostListener() {

                    @Override
                    public void onStart() {
                        AppContext.showToastShort(R.string.tip_start_share);
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA arg0, int arg1,
                                           SocializeEntity arg2) {
                        AppContext.showToastShort(R.string.tip_share_done);
                    }
                });
    }

    private void shareToSinaWeibo() {
        // 设置腾讯微博SSO handler
        mController.getConfig().setSsoHandler(new SinaSsoHandler());
        if (OauthHelper.isAuthenticated(getActivity(), SHARE_MEDIA.SINA)) {
            shareContent(SHARE_MEDIA.SINA);
        } else {
            mController.doOauthVerify(getActivity(), SHARE_MEDIA.SINA,
                    new UMAuthListener() {

                        @Override
                        public void onStart(SHARE_MEDIA arg0) {
                        }

                        @Override
                        public void onError(SocializeException arg0,
                                            SHARE_MEDIA arg1) {
                        }

                        @Override
                        public void onComplete(Bundle arg0, SHARE_MEDIA arg1) {
                            shareContent(SHARE_MEDIA.SINA);
                        }

                        @Override
                        public void onCancel(SHARE_MEDIA arg0) {
                        }
                    });
        }
    }

    private void shareToTencentWeibo() {
        // 设置腾讯微博SSO handler
        mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
        if (OauthHelper.isAuthenticated(getActivity(), SHARE_MEDIA.TENCENT)) {
            shareContent(SHARE_MEDIA.TENCENT);
        } else {
            mController.doOauthVerify(getActivity(), SHARE_MEDIA.TENCENT,
                    new UMAuthListener() {

                        @Override
                        public void onStart(SHARE_MEDIA arg0) {
                        }

                        @Override
                        public void onError(SocializeException arg0,
                                            SHARE_MEDIA arg1) {
                        }

                        @Override
                        public void onComplete(Bundle arg0, SHARE_MEDIA arg1) {
                            shareContent(SHARE_MEDIA.TENCENT);
                        }

                        @Override
                        public void onCancel(SHARE_MEDIA arg0) {
                        }
                    });
        }
    }

    private void shareContent(SHARE_MEDIA media) {
        mController.setShareContent(getShareContent() + getShareUrl());
        mController.directShare(getActivity(), media, new SnsPostListener() {

            @Override
            public void onStart() {
                AppContext.showToastShort(R.string.tip_start_share);
            }

            @Override
            public void onComplete(SHARE_MEDIA arg0, int arg1,
                                   SocializeEntity arg2) {
                AppContext.showToastShort(R.string.tip_share_done);
            }
        });
    }

    private void shareToQZone() {
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(getActivity(),
                Constants.QQ_APPID, Constants.QQ_APPKEY);
        qZoneSsoHandler.addToSocialSDK();
        QZoneShareContent qzone = new QZoneShareContent();
        // 设置分享文字
        qzone.setShareContent(getShareContent());
        // 设置点击消息的跳转URL
        qzone.setTargetUrl(getShareUrl());
        // 设置分享内容的标题
        qzone.setTitle(getShareTitle());
        // 设置分享图片
        // qzone.setShareImage(urlImage);
        mController.setShareMedia(qzone);
        mController.postShare(getActivity(), SHARE_MEDIA.QZONE,
                new SnsPostListener() {

                    @Override
                    public void onStart() {
                        AppContext.showToastShort(R.string.tip_start_share);
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA arg0, int arg1,
                                           SocializeEntity arg2) {
                        AppContext.showToastShort(R.string.tip_share_done);
                    }
                });
    }

    protected void shareToQQ(SHARE_MEDIA media) {
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(getActivity(),
                Constants.QQ_APPID, Constants.QQ_APPKEY);
        qqSsoHandler.setTargetUrl(getShareUrl());
        qqSsoHandler.setTitle(getShareContent());
        qqSsoHandler.addToSocialSDK();
        mController.setShareContent(getShareContent());
        mController.postShare(getActivity(), media, new SnsPostListener() {

            @Override
            public void onStart() {
                AppContext.showToastShort(R.string.tip_start_share);
            }

            @Override
            public void onComplete(SHARE_MEDIA arg0, int arg1,
                                   SocializeEntity arg2) {
                AppContext.showToastShort(R.string.tip_share_done);
            }
        });
    }
}

package net.oschina.app.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import net.oschina.app.v2.api.ApiHttpClient;
import net.oschina.app.v2.api.remote.OtherApi;
import net.oschina.app.v2.base.BaseApplication;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.cache.v2.CacheManager;
import net.oschina.app.v2.content.DBHelper;
import net.oschina.app.v2.emoji.EmojiHelper;
import net.oschina.app.v2.model.DailyEnglish;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.utils.CircleBitmapDisplayer;
import net.oschina.app.v2.utils.CyptoUtils;
import net.oschina.app.v2.utils.DateUtil;
import net.oschina.app.v2.utils.FileUtils;
import net.oschina.app.v2.utils.ImageUtils;
import net.oschina.app.v2.utils.MethodsCompat;
import net.oschina.app.v2.utils.StringUtils;
import net.oschina.app.v2.utils.TLog;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UpdateConfig;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class AppContext extends BaseApplication {

    private static final String KEY_SOFTKEYBOARD_HEIGHT = "KEY_SOFTKEYBOARD_HEIGHT";
    private static final String KEY_LOAD_IMAGE = "KEY_LOAD_IMAGE";
    private static final String KEY_NOTIFICATION_SOUND = "KEY_NOTIFICATION_SOUND";
    private static final String LAST_QUESTION_CATEGORY_IDX = "LAST_QUESTION_CATEGORY_IDX";
    private static final String KEY_DAILY_ENGLISH = "KEY_DAILY_ENGLISH";
    private static final String KEY_GET_LAST_DAILY_ENG = "KEY_GET_LAST_DAILY_ENG";
    private static final String KEY_NOTIFICATION_DISABLE_WHEN_EXIT = "KEY_NOTIFICATION_DISABLE_WHEN_EXIT";
    private static final String KEY_TWEET_DRAFT = "key_tweet_draft";
    private static final String KEY_QUESTION_TITLE_DRAFT = "key_question_title_draft";
    private static final String KEY_QUESTION_CONTENT_DRAFT = "key_question_content_draft";
    private static final String KEY_QUESTION_TYPE_DRAFT = "key_question_type_draft";
    private static final String KEY_QUESTION_LMK_DRAFT = "key_question_lmk_draft";
    private static final String KEY_NEWS_READED = "key_readed_news_2";
    private static final String KEY_QUESTION_READED = "key_readed_question_2";
    private static final String KEY_BLOG_READED = "key_readed_blog_2";
    private static final String KEY_NOTICE_ATME_COUNT = "key_notice_atme_count";
    private static final String KEY_NOTICE_MESSAGE_COUNT = "key_notice_message_count";
    private static final String KEY_NOTICE_REVIEW_COUNT = "key_notice_review_count";
    private static final String KEY_NOTICE_NEWFANS_COUNT = "key_notice_newfans_count";
    private static final String KEY_LOGIN_ID = "key_login_id";
    private static final String KEY_COOKIE = "key_cookie";
    private static final String KEY_APP_ID = "key_app_id";
    private static final String KEY_DETAIL_FONT_SIZE = "key_font_size";

    private static Set<String> mReadedNewsIds, mReadedQuestionIds, mReadedBlogIds; //已读IDS

    private static AppContext instance;


    @Override
    public void onCreate() {
        super.onCreate();
        // 注册App异常崩溃处理器
        // Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());

        CacheManager.initCacheDir(Constants.CACHE_DIR, getApplicationContext(),
                new DBHelper(getApplicationContext(), 1, "app_cache", null, null));

        instance = this;

        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        ApiHttpClient.setHttpClient(client);
        ApiHttpClient.setCookie(ApiHttpClient.getCookie(this));

        EmojiHelper.initEmojis();
        initImageLoader(this);

        MobclickAgent.setDebugMode(false);
        UpdateConfig.setDebug(false);

        requestDailyEnglish();
    }

    public static void requestDailyEnglish() {
        String lastDate = getLastGetDailyEngDate();
        if (lastDate != null) {
            if (DateUtil.getNow("yyy-MM-dd").compareTo(lastDate) <= 0) {
                DailyEnglish de = getDailyEnglish();
                if (de != null) {
                    TLog.log("today english has ready");
                    return;
                }
            }
        }
        OtherApi.getDailyEnglish(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                AppContext.setLastGetDailyEngDate(DateUtil.getNow("yyy-MM-dd"));
                AppContext.setDailyEnglish(response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  String responseString, Throwable throwable) {
                TLog.log(responseString + "");
                throwable.printStackTrace();
            }
        });
    }

    public static void setLastGetDailyEngDate(String date) {
        Editor editor = getPreferences().edit();
        editor.putString(KEY_GET_LAST_DAILY_ENG, date);
        apply(editor);
    }

    public static String getLastGetDailyEngDate() {
        return getPreferences().getString(KEY_GET_LAST_DAILY_ENG, null);
    }

    public static void setDailyEnglish(String daily) {
        Editor editor = getPreferences().edit();
        editor.putString(KEY_DAILY_ENGLISH, daily);
        apply(editor);
    }

    public static DailyEnglish getDailyEnglish() {
        String dailyEnglish = getPreferences().getString(KEY_DAILY_ENGLISH,
                null);
        if (!TextUtils.isEmpty(dailyEnglish)) {
            return DailyEnglish.make(dailyEnglish);
        }
        return null;
    }

    public static void initImageLoader(Context context) {
        DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                .displayer(new CircleBitmapDisplayer())
                .cacheInMemory(true).cacheOnDisk(true)
                .bitmapConfig(Config.RGB_565).build();
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024)
                        // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        //.writeDebugLogs() // Remove for release app
                .defaultDisplayImageOptions(displayOptions).build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public static void setSoftKeyboardHeight(int height) {
        Editor editor = getPreferences().edit();
        editor.putInt(KEY_SOFTKEYBOARD_HEIGHT, height);
        apply(editor);
    }

    public static int getSoftKeyboardHeight() {
        return getPreferences().getInt(KEY_SOFTKEYBOARD_HEIGHT, 0);
    }

    public static boolean shouldLoadImage() {
        return getPreferences().getBoolean(KEY_LOAD_IMAGE, true);
    }

    public static void setLoadImage(boolean flag) {
        Editor editor = getPreferences().edit();
        editor.putBoolean(KEY_LOAD_IMAGE, flag);
        apply(editor);
    }

    /**
     * 设置字体大小可选值[0,1,2]
     * @param size
     */
    public static void setDetailFontSize(int size) {
        Editor editor = getPreferences().edit();
        editor.putInt(KEY_DETAIL_FONT_SIZE, size);
        apply(editor);
    }

    /**
     * 获取字体大小[0,1,2]
     * @return
     */
    public static int getDetailFontSize() {
        return getPreferences().getInt(KEY_DETAIL_FONT_SIZE, 1);
    }

    public static String getDetailFontSizeStr() {
        return resources().getStringArray(R.array.font_size)[getDetailFontSize()];
    }

    public static int getDetailFontSizePx() {
        return resources().getIntArray(R.array.font_size_value)[getDetailFontSize()];
    }

    /**
     * 判断当前版本是否兼容目标版本的方法
     *
     * @param VersionCode
     * @return
     */
    public static boolean isMethodsCompat(int VersionCode) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    /**
     * 获取App唯一标识
     *
     * @return
     */
    public String getAppId() {
        String uniqueID = getProperty(KEY_APP_ID);
        if (StringUtils.isEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            setProperty(KEY_APP_ID, uniqueID);
        }
        return uniqueID;
    }

    /**
     * 用户是否登录
     *
     * @return
     */
    public boolean isLogin() {
        return getLoginUid() != 0;
    }

    /**
     * 获取登录用户id
     *
     * @return
     */
    public static int getLoginUid() {
        return getPreferences().getInt(KEY_LOGIN_ID, 0);
    }

    public static void setLoginUid(int uid) {
        Editor editor = getPreferences().edit();
        editor.putInt(KEY_LOGIN_ID, uid);
        apply(editor);
    }

    /**
     * 用户注销
     */
    public void Logout() {
        ApiHttpClient.cleanCookie();
        this.cleanCookie();
        //this.login = false;
        //this.loginUid = 0;

        Intent intent = new Intent(Constants.INTENT_ACTION_LOGOUT);
        sendBroadcast(intent);
    }

    /**
     * 初始化用户登录信息
     */
    public void initLoginInfo() {
        User loginUser = getLoginInfo();
        if (loginUser != null && loginUser.getUid() > 0
                && !TextUtils.isEmpty(ApiHttpClient.getCookie(this))) {// &&
            // loginUser.isRememberMe()
        } else {
            this.Logout();
        }
    }

    /**
     * 保存登录信息
     *
     * @param user
     */
    public static void saveLoginInfo(final User user) {
        setLoginUid(user.getUid());

        setProperty("user.uid", String.valueOf(user.getUid()));
        setProperty("user.name", user.getName());
        setProperty("user.face", FileUtils.getFileName(user.getFace()));// 用户头像-文件名
        setProperty("user.account", user.getAccount());
        setProperty("user.pwd",
                CyptoUtils.encode("oschinaApp", user.getPwd()));
        setProperty("user.location", user.getLocation());
        setProperty("user.followers",
                String.valueOf(user.getFollowers()));
        setProperty("user.fans", String.valueOf(user.getFans()));
        setProperty("user.score", String.valueOf(user.getScore()));
        setProperty("user.isRememberMe", String.valueOf(user.isRememberMe()));// 是否记住我的信息
    }

    /**
     * 清除登录信息
     */
    public static void cleanLoginInfo() {
        removeProperty("user.uid", "user.name", "user.face", "user.account",
                "user.pwd", "user.location", "user.followers", "user.fans",
                "user.score", "user.isRememberMe");
        setLoginUid(0);
    }

    /**
     * 获取登录信息
     *
     * @return
     */
    public static User getLoginInfo() {
        User lu = new User();
        lu.setUid(StringUtils.toInt(getProperty("user.uid"), 0));
        lu.setName(getProperty("user.name"));
        lu.setFace(getProperty("user.face"));
        lu.setAccount(getProperty("user.account"));
        lu.setPwd(CyptoUtils.decode("oschinaApp", getProperty("user.pwd")));
        lu.setLocation(getProperty("user.location"));
        lu.setFollowers(StringUtils.toInt(getProperty("user.followers"), 0));
        lu.setFans(StringUtils.toInt(getProperty("user.fans"), 0));
        lu.setScore(StringUtils.toInt(getProperty("user.score"), 0));
        lu.setRememberMe(StringUtils.toBool(getProperty("user.isRememberMe")));
        return lu;
    }

    public void cleanCookie() {
        removeProperty(KEY_COOKIE);
    }

    public static String getCookie(){
        return getProperty(KEY_COOKIE);
    }

    public static void setCookie(String cookie){
        setProperty(KEY_COOKIE, cookie);
    }

    /**
     * 清除app缓存
     */
    public void clearAppCache() {
        deleteDatabase("webview.db");
        deleteDatabase("webview.db-shm");
        deleteDatabase("webview.db-wal");
        deleteDatabase("webviewCache.db");
        deleteDatabase("webviewCache.db-shm");
        deleteDatabase("webviewCache.db-wal");
        // 清除数据缓存
        clearCacheFolder(getFilesDir(), System.currentTimeMillis());
        clearCacheFolder(getCacheDir(), System.currentTimeMillis());
        // 2.2版本才有将应用缓存转移到sd卡的功能
        if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
            clearCacheFolder(MethodsCompat.getExternalCacheDir(this),
                    System.currentTimeMillis());
        }
    }

    /**
     * 清除缓存目录
     *
     * @param dir     目录
     * @param curTime 当前系统时间
     * @return
     */
    private int clearCacheFolder(File dir, long curTime) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, curTime);
                    }
                    if (child.lastModified() < curTime) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    public static void setProperty(String key, String value) {
        Editor editor = getPreferences().edit();
        editor.putString(key, value);
        apply(editor);
    }

    public static String getProperty(String key) {
        return getPreferences().getString(key, null);
    }

    public static void removeProperty(String... keys) {
        for (String key : keys) {
            Editor editor = getPreferences().edit();
            editor.putString(key, null);
            apply(editor);
        }
    }

    public static AppContext instance() {
        return instance;
    }

    public static int getLastQuestionCategoryIdx() {
        return getPreferences().getInt(LAST_QUESTION_CATEGORY_IDX, 0);
    }

    public static String getLastQuestionCategory() {
        int idx = getLastQuestionCategoryIdx();
        return resources().getStringArray(R.array.post_pub_options)[idx];
    }

    public static boolean isNotificationSoundEnable() {
        return getPreferences().getBoolean(KEY_NOTIFICATION_SOUND, true);
    }

    public static void setNotificationSoundEnable(boolean enable) {
        Editor editor = getPreferences().edit();
        editor.putBoolean(KEY_NOTIFICATION_SOUND, enable);
        apply(editor);
    }

    public static boolean isNotificationDisableWhenExit() {
        return getPreferences().getBoolean(KEY_NOTIFICATION_DISABLE_WHEN_EXIT,
                false);
    }

    public static void setNotificationDisableWhenExit(boolean enable) {
        Editor editor = getPreferences().edit();
        editor.putBoolean(KEY_NOTIFICATION_DISABLE_WHEN_EXIT, enable);
        apply(editor);
    }

    public static String getTweetDraft() {
        return getPreferences().getString(
                KEY_TWEET_DRAFT + instance().getLoginUid(), "");
    }

    public static void setTweetDraft(String draft) {
        Editor editor = getPreferences().edit();
        editor.putString(KEY_TWEET_DRAFT + instance().getLoginUid(), draft);
        apply(editor);
    }

    public static String getQuestionTitleDraft() {
        return getPreferences().getString(
                KEY_QUESTION_TITLE_DRAFT + instance().getLoginUid(), "");
    }

    public static void setQuestionTitleDraft(String draft) {
        Editor editor = getPreferences().edit();
        editor.putString(KEY_QUESTION_TITLE_DRAFT + instance().getLoginUid(),
                draft);
        apply(editor);
    }

    public static String getQuestionContentDraft() {
        return getPreferences().getString(
                KEY_QUESTION_CONTENT_DRAFT + instance().getLoginUid(), "");
    }

    public static void setQuestionContentDraft(String draft) {
        Editor editor = getPreferences().edit();
        editor.putString(KEY_QUESTION_CONTENT_DRAFT + instance().getLoginUid(),
                draft);
        apply(editor);
    }

    public static int getQuestionTypeDraft() {
        return getPreferences().getInt(
                KEY_QUESTION_TYPE_DRAFT + instance().getLoginUid(), 0);
    }

    public static void setQuestionTypeDraft(int draft) {
        Editor editor = getPreferences().edit();
        editor.putInt(KEY_QUESTION_TYPE_DRAFT + instance().getLoginUid(), draft);
        apply(editor);
    }

    public static boolean getQuestionLetMeKnowDraft() {
        return getPreferences().getBoolean(
                KEY_QUESTION_LMK_DRAFT + instance().getLoginUid(), false);
    }

    public static void setQuestionLetMeKnowDraft(boolean draft) {
        Editor editor = getPreferences().edit();
        editor.putBoolean(KEY_QUESTION_LMK_DRAFT + instance().getLoginUid(),
                draft);
        apply(editor);
    }

    public static void setRefreshTime(String cacheKey, long time) {
        Editor editor = getPreferences().edit();
        editor.putLong(cacheKey, time);
        apply(editor);
    }

    public static long getRefreshTime(String cacheKey) {
        return getPreferences().getLong(cacheKey, 0);
    }

    public static Set<String> getNewsReadIdSet() {
        if (mReadedNewsIds == null) {
            mReadedNewsIds = getStringSet(KEY_NEWS_READED);
        }
        return mReadedNewsIds;
    }

    public static void addReadedNews(int id) {
        Set<String> set = getNewsReadIdSet();
        set.add(id + "");
        putStringSet(KEY_NEWS_READED, set);
    }

    public static boolean isReadedNews(int id) {
        if (mReadedNewsIds == null) {
            mReadedNewsIds = getNewsReadIdSet();
        }
        if (mReadedNewsIds != null && mReadedNewsIds.contains(id + ""))
            return true;
        return false;
    }

    public static Set<String> getQuestionReadIdSet() {
        if (mReadedQuestionIds == null) {
            mReadedQuestionIds = getStringSet(KEY_QUESTION_READED);
        }
        return mReadedQuestionIds;
    }

    public static void addReadedQuestion(int id) {
        Set<String> set = getQuestionReadIdSet();
        set.add(id + "");
        putStringSet(KEY_QUESTION_READED, set);
    }

    public static boolean isReadedQuestion(int id) {
        if (mReadedQuestionIds == null) {
            Set<String> ids = getQuestionReadIdSet();
            mReadedQuestionIds = ids;
        }
        if (mReadedQuestionIds != null && mReadedQuestionIds.contains(id + ""))
            return true;
        return false;
    }

    public static Set<String> getBlogReadIdSet() {
        if (mReadedBlogIds == null) {
            mReadedBlogIds = getStringSet(KEY_BLOG_READED);
        }
        return mReadedBlogIds;
    }

    public static void addReadedBlog(int id) {
        Set<String> set = getBlogReadIdSet();
        set.add(id + "");
        putStringSet(KEY_BLOG_READED, set);
    }

    public static boolean isReadedBlog(int id) {
        if (mReadedBlogIds == null) {
            Set<String> ids = getBlogReadIdSet();
            mReadedBlogIds = ids;
        }
        if (mReadedBlogIds != null && mReadedBlogIds.contains(id + ""))
            return true;
        return false;
    }

    public static void setNoticeAtMeCount(int noticeAtMeCount) {
        Editor editor = getPreferences().edit();
        editor.putInt(KEY_NOTICE_ATME_COUNT + instance().getLoginUid(),
                noticeAtMeCount);
        apply(editor);
    }

    public static int getNoticeAtMeCount() {
        return getPreferences().getInt(KEY_NOTICE_ATME_COUNT + instance().getLoginUid(), 0);
    }

    public static void setNoticeMessageCount(int noticeMessageCount) {
        Editor editor = getPreferences().edit();
        editor.putInt(KEY_NOTICE_MESSAGE_COUNT + instance().getLoginUid(),
                noticeMessageCount);
        apply(editor);
    }

    public static int getNoticeMessageCount() {
        return getPreferences().getInt(KEY_NOTICE_MESSAGE_COUNT + instance().getLoginUid(), 0);
    }

    public static void setNoticeReviewCount(int reviewCount) {
        Editor editor = getPreferences().edit();
        editor.putInt(KEY_NOTICE_REVIEW_COUNT + instance().getLoginUid(),
                reviewCount);
        apply(editor);
    }

    public static int getNoticeReviewCount() {
        return getPreferences().getInt(KEY_NOTICE_REVIEW_COUNT + instance().getLoginUid(), 0);
    }

    public static void setNoticeNewFansCount(int newFansCount) {
        Editor editor = getPreferences().edit();
        editor.putInt(KEY_NOTICE_NEWFANS_COUNT + instance().getLoginUid(),
                newFansCount);
        apply(editor);
    }

    public static int getNoticeNewFansCount() {
        return getPreferences().getInt(KEY_NOTICE_NEWFANS_COUNT + instance().getLoginUid(), 0);
    }
}

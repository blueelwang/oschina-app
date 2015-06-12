package net.oschina.app.v2.cache.v2;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import net.oschina.app.v2.content.DBHelper;
import net.oschina.app.v2.utils.FileUtils;
import net.oschina.app.v2.utils.TLog;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

@SuppressLint("SimpleDateFormat")
public class CacheManager {
    private static final String TAG = CacheManager.class.getSimpleName();
    public static final int TYPE_INTERNAL = 1;
    public static final int TYPE_EXTERNAL = 2;

    //public static File downloadDir;
    //public static File externalFileDir;
    //public static File logDir;
    //public static File offlineZip;
    //public static File userAvatar;
    public static File offlineUnZip;
    public static File cacheDirExternal;
    public static File cacheDirInternal;
    public static File cachePhotoDirExternal;
    private static ArrayList<CacheTask> cacheTasks = new ArrayList<>();
    private static Thread cacheThread;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    public static DBHelper dbHelper = null;

    //public static final int dataCacheExpire = 3600;
    //public static final int imageCacheExpire = 0x127500;
    private static long maxExternalCacheSize = 0x1f400000L;
    private static long maxInternalCacheSize = 0x1400000L;
    private static long minExternalStorageAvailableSize = 0xa00000L;
    private static long minInternalStorageAvailableSize = 0x100000L;

    public static File tempCacheDirExternal;
    public static File tempCacheDirInternal;

    static {
        cacheThread = new Thread() {

            @Override
            public void run() {
                synchronized (cacheThread) {
                    while (true) {
                        if (cacheTasks != null && !cacheTasks.isEmpty()
                                && cacheTasks.size() > 0) {
                            TLog.log(TAG, "begin cache new task :" + cacheTasks.get(0).getKey());
                            saveCache(cacheTasks.get(0));
                            if (cacheTasks != null && !cacheTasks.isEmpty())
                                cacheTasks.remove(0);
                        } else {
                            try {
                                TLog.log(TAG, "waitting new cache task...");
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        cacheThread.start();
    }

    public CacheManager() {
    }

    /**
     * 删除所有缓存
     */
    public static synchronized void clearAllCache() {
        clearAllCache(TYPE_INTERNAL);
        clearAllCache(TYPE_EXTERNAL);
    }

    /**
     * 删除缓存
     *
     * @param type
     */
    public static synchronized void clearAllCache(int type) {
        dbHelper.getWritableDatabase().delete("app_cache", "status=" + type,
                null);
        clearTempCache(type);
        if (type == TYPE_INTERNAL && cacheDirInternal.exists()
                && cacheDirInternal.isDirectory()) {
            FileUtils.deleteDirectory(cacheDirInternal, false);
            tempCacheDirInternal = new File(cacheDirInternal, "temp");
            if (!tempCacheDirInternal.exists() || tempCacheDirInternal.isFile())
                tempCacheDirInternal.mkdirs();
        } else if (type == TYPE_EXTERNAL && cacheDirExternal.exists()
                && cacheDirExternal.isDirectory()) {
            FileUtils.deleteDirectory(cacheDirExternal, false);
            tempCacheDirExternal = new File(cacheDirExternal, "temp");
            if (!tempCacheDirExternal.exists() || tempCacheDirExternal.isFile())
                tempCacheDirExternal.mkdirs();
        }
    }

    public static synchronized void clearNeedClearCache(int status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from app_cache where expire<="
                + System.currentTimeMillis() + " and status = " + status, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
            Cache cache = new Cache();
            try {
                cache.parse(cursor);
                if (cache.getFile() != null && cache.getFile().exists()
                        && cache.getFile().delete())
                    db.delete("app_cache", "id=" + cache.getId(), null);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (cursor != null)
            cursor.close();
        if (needClear(TYPE_INTERNAL)) {
            TLog.log(TAG, "The internal cache space is not enough, clear cache...");
            clearProportionCache(TYPE_INTERNAL);
        }
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)
                && needClear(TYPE_EXTERNAL)) {
            TLog.log(TAG, "The external cache space is not enough, clear cache...");
            clearProportionCache(TYPE_EXTERNAL);
        }
    }

    public static synchronized void clearProportionCache() {
        if (needClear(TYPE_INTERNAL))
            clearProportionCache(TYPE_INTERNAL);
        if (needClear(TYPE_EXTERNAL))
            clearProportionCache(TYPE_EXTERNAL);
    }

    public static synchronized void clearProportionCache(int i) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from app_cache  where status = "
                + i + " order by expire", null);
        if (cursor != null && cursor.getCount() > 0) {
            try {
                long cacheSize = 0;
                long usedCacheSize = getUsedCacheSize(i);
                while (cursor.moveToNext()) {
                    Cache cache = new Cache();
                    cache.parse(cursor);
                    if (cache.getFile().delete()) {
                        db.delete("app_cache", "id=" + cache.getId(), null);
                        cacheSize += cache.getSize();
                        if ((float) cacheSize < 0.5F * (float) usedCacheSize) {
                            break;
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (cursor != null)
            cursor.close();
    }

    public static synchronized void clearTempCache() {
        clearTempCache(TYPE_INTERNAL);
        clearTempCache(TYPE_EXTERNAL);
    }

    private static synchronized void clearTempCache(int type) {
        if (type == TYPE_INTERNAL && tempCacheDirInternal.exists()
                && tempCacheDirInternal.isDirectory()) {
            FileUtils.deleteDirectory(tempCacheDirInternal, false);
        } else if (type == TYPE_EXTERNAL && tempCacheDirExternal.exists()
                && tempCacheDirExternal.isDirectory()) {
            FileUtils.deleteDirectory(tempCacheDirExternal, false);
        }
    }

    public static synchronized void clearUnUserFile() {
        FileUtils.deleteDirectoryByTime(offlineUnZip, 10);
        if (FileUtils.deleteDirectoryByTime(cacheDirExternal, 30)) {
            tempCacheDirExternal = new File(cacheDirExternal, "temp");
            if (!tempCacheDirExternal.exists() || tempCacheDirExternal.isFile())
                tempCacheDirExternal.mkdirs();
        }
    }

    private static synchronized long getAvailableCacheSize(int type) {
        File file = type != TYPE_INTERNAL ? cachePhotoDirExternal
                : cacheDirInternal;
        return FileUtils.getAvailableStorageSize(file);
    }

    public static byte[] getCache(String key) {
        Cache cache = getCacheFile(key);
        if (cache != null && cache.getExpire() > System.currentTimeMillis()
                && cache.getFile() != null && cache.getFile().exists()
                && cache.getFile().isFile()) {
            try {
                byte[] data = FileUtils.readFileToByte(cache.getFile());
                TLog.log(TAG, "get cache data: " + key);
                return data;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "get cache data ignore expire fail: " + key);
            }
        } else {
            TLog.log(TAG, "get cache data fail: " + key);
        }
        return null;
    }

    public static String getCacheData(String key) {
        byte[] data = getCache(key);
        if (data != null) {
            return new String(data);
        }
        return null;
    }

    public static synchronized Cache getCacheFile(String name) {
        Cache cache = null;
        String key = getCacheKey(name);
        if (dbHelper != null) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from app_cache where key = '"
                    + key + "'", null);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    try {
                        cache = new Cache();
                        cache.parse(cursor);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    TLog.log(TAG, "cache cursor is no data with:" + key);
                }
            } else {
                TLog.log(TAG, "cache cursor is empty with key:" + key);
            }
            if (cursor != null)
                cursor.close();
        } else {
            TLog.log(TAG, "db helper is null");
        }
        TLog.log(TAG, "get cache file :" + cache);
        return cache;
    }

    private static String getCacheFileName(String name) {
        return getCacheKey(name);
    }

    public static byte[] getCacheIgnoreExpire(String key) {
        Cache cache = getCacheFile(key);
        if (cache != null) {
            try {
                return FileUtils.readFileToByte(cache.getFile());
            } catch (IOException e) {
                Log.e(TAG, "get cache data ignore expire fail: " + key);
            }
        }
        return null;
    }

    private static String getCacheKey(String key) {
        if (key != null && key.trim().length() > 0)
            return UUID.nameUUIDFromBytes(key.getBytes()).toString();
        return null;
    }

    public static long getMaxExternalCacheSize() {
        return maxExternalCacheSize;
    }

    public static long getMaxInternalCacheSize() {
        return maxInternalCacheSize;
    }

    public static long getMinExternalStorageAvailableSize() {
        return minExternalStorageAvailableSize;
    }

    public static long getMinInternalStorageAvailableSize() {
        return minInternalStorageAvailableSize;
    }

    public static synchronized long getUsedCacheSize(int i) {
        File file = i != 1 ? cachePhotoDirExternal : cacheDirInternal;
        if (file != null) {
            if (file.exists()) {
                return FileUtils.getDirSize(file);
            }
        }
        return 0;
    }

    public static void initCacheDir(String baseFolder, Context context,
                                    DBHelper dbhelper) {
        int memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        // int _tmp = (int) (Runtime.getRuntime().maxMemory() / 1024L);
        Cache.memoryCacheSize = (0x100000 * memoryClass) / 10;
        dbHelper = dbhelper;
        cacheDirInternal = new File(context.getCacheDir(), "app");
        File file = new File(Environment.getExternalStorageDirectory(),
                baseFolder + "/cache");
        cacheDirExternal = file;
        cachePhotoDirExternal = file;
        cacheDirExternal = new File(cachePhotoDirExternal, "app");
        if (!cacheDirInternal.exists() || !cacheDirInternal.isDirectory())
            cacheDirExternal.mkdirs();
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            if (!cacheDirExternal.exists() || !cacheDirExternal.isDirectory())
                cacheDirExternal.mkdirs();
            if (!cachePhotoDirExternal.exists()
                    || !cachePhotoDirExternal.isDirectory())
                cachePhotoDirExternal.mkdirs();
        }
        tempCacheDirInternal = new File(cacheDirInternal, "temp");
        tempCacheDirExternal = new File(cacheDirExternal, "temp");
        if (!tempCacheDirInternal.exists() || tempCacheDirInternal.isFile())
            tempCacheDirInternal.mkdirs();
        if (!tempCacheDirExternal.exists() || tempCacheDirExternal.isFile())
            tempCacheDirExternal.mkdirs();
    }

    public static boolean isSDCardCanUse() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    private static synchronized boolean needClear(int type) {
        if (type == TYPE_INTERNAL
                && (getAvailableCacheSize(1) <= minInternalStorageAvailableSize || getUsedCacheSize(1) > maxInternalCacheSize)) {
            TLog.log(TAG, "The cache space [" + type + "] need clear");
            return true;
        }
        if (type == TYPE_EXTERNAL
                && (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)
                && getAvailableCacheSize(2) <= minExternalStorageAvailableSize || getUsedCacheSize(2) <= maxExternalCacheSize)) {
            TLog.log(TAG, "The cache space [" + type + "] need clear");
            return true;
        }
        return false;
    }

    private static File saveByteToFile(CacheTask task) {
        // 缓存任务为空直接返回
        if (task == null)
            return null;
        // 如果存储在SD卡上去没有装载SD卡 则直接返回
        if (Environment.getExternalStorageState() != null
                && !Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)
                && task.getType() == TYPE_EXTERNAL) {
            return null;
        }
        // 如果缓存任务数据不完整直接返回
        if (task.getKey() == null || task.getContent() == null
                || task.getContent().length <= 0) {
            TLog.log(TAG, "set cache data: cache task is null");
            return null;
        }
        // 如果无法生成缓存文件名直接返回
        String fileName = getCacheFileName(task.getKey());
        if (fileName == null) {
            TLog.log(TAG, "set cache data: cache name is null");
            return null;
        }
        String dateName = dateFormat.format(new Date());

        File cacheFile = null;
        File tmpCacheDir = null;
        File cacheDir = null;
        if (task.getType() == TYPE_INTERNAL) {
            if (cacheDirInternal != null && cacheDirInternal.exists()
                    && tempCacheDirInternal != null
                    && tempCacheDirInternal.exists()) {
                cacheDir = new File(cacheDirInternal, dateName);
                cacheFile = new File(cacheDir, fileName);
                tmpCacheDir = new File(tempCacheDirInternal, fileName);
            } else {
                TLog.log(TAG, "set cache data: cache internal dir is not exists");
                return null;
            }
        } else if (task.getType() == TYPE_EXTERNAL) {
            if (cacheDirExternal != null && cacheDirExternal.exists()
                    && tempCacheDirExternal != null
                    && tempCacheDirExternal.exists()) {
                cacheDir = new File(cacheDirExternal, dateName);
                cacheFile = new File(cacheDir, fileName);
                tmpCacheDir = new File(tempCacheDirExternal, fileName);
            } else {
                TLog.log(TAG, "set cache data: cache external dir is not exists");
                return null;
            }
        }
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        try {
            FileUtils.writeFile(tmpCacheDir, task.getContent());
            if (tmpCacheDir != null && tmpCacheDir.exists()
                    && tmpCacheDir.isFile()) {
                if (cacheFile != null && cacheFile.exists()
                        && cacheFile.isFile())
                    cacheFile.delete();
                FileUtils.move(tmpCacheDir, cacheFile);
            }
        } catch (Exception e) {
            TLog.log(TAG, "set cache data: move cache file exception " + e);
            e.printStackTrace();
        }
        return cacheFile;
    }

    private static synchronized void saveCache(CacheTask task) {
        File file = saveByteToFile(task);
        if (file != null && file.exists()) {
            if (file.isFile()) {
                String key = getCacheKey(task.getKey());
                try {
                    long fileSize = file.length();
                    long currentTime = System.currentTimeMillis();
                    ContentValues cv = new ContentValues();
                    cv.put("key", key);
                    cv.put("file", file.getAbsolutePath());
                    cv.put("size", Long.valueOf(fileSize));
                    cv.put("status", Integer.valueOf(task.getType()));
                    cv.put("time", Long.valueOf(currentTime));
                    if (task.getExpire() <= 0L) {
                        cv.put("expire", Long.valueOf(currentTime));
                    } else {
                        cv.put("expire",
                                Long.valueOf(currentTime + 1000L
                                        * task.getExpire()));
                    }

                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    while (db.isDbLockedByCurrentThread()) {
                        Thread.sleep(10);
                    }

                    Cache cache = null;
                    Cursor cursor = db
                            .rawQuery("select * from app_cache where key = '"
                                    + key + "'", null);
                    if (cursor != null && cursor.getCount() > 0
                            && cursor.moveToNext()) {
                        cache = new Cache();
                        cache.parse(cursor);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    db = dbHelper.getWritableDatabase();
                    while (db.isDbLockedByCurrentThread()) {
                        Thread.sleep(10);
                    }
                    if (cache == null) {
                        db.insert("app_cache", null, cv);
                        TLog.log(TAG, "insert db cache :" + task.getKey());
                    } else {
                        cv.put("id", Long.valueOf(cache.getId()));
                        db.update("app_cache", cv, "id=?",
                                new String[]{Long.toString(cache.getId())});
                        TLog.log(TAG, "update db cache :" + task.getKey());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "set cache data failed: " + key);
                }
            }
        }
    }

    public static void setCache(String key, byte[] content, long expire,
                                int type) {
        TLog.log(TAG, "add cache task:" + key + " expire:" + expire);
        CacheTask task = new CacheTask();
        task.setKey(key);
        task.setContent(content);
        task.setExpire(expire);
        task.setType(type);
        cacheTasks.add(task);
        synchronized (cacheThread) {
            cacheThread.notify();
        }
    }

    public static void setMaxExternalCacheSize(long size) {
        maxExternalCacheSize = size;
    }

    public static void setMaxInternalCacheSize(long size) {
        maxInternalCacheSize = size;
    }

    public static void setMinExternalStorageAvailableSize(long size) {
        minExternalStorageAvailableSize = size;
    }

    public static void setMinInternalStorageAvailableSize(long size) {
        minInternalStorageAvailableSize = size;
    }

    private static class CacheTask {
        private byte[] content;
        private long expire;
        private String key;
        private int type;

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        public long getExpire() {
            return expire;
        }

        public void setExpire(long expire) {
            this.expire = expire;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}

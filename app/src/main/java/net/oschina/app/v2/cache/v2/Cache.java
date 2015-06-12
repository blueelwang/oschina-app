package net.oschina.app.v2.cache.v2;

import android.database.Cursor;

import java.io.File;
import java.text.ParseException;

public class Cache {

    public static int memoryCacheSize = 0x200000;
    private long expire;
    private File file;
    private long id;
    private String key;
    private long size;
    private int status;
    private long time;

    public Cache() {
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void parse(Cursor cursor) throws ParseException {
        setId(cursor.getLong(cursor.getColumnIndex("id")));
        if (cursor.getString(cursor.getColumnIndex("key")) != null)
            setKey(cursor.getString(cursor.getColumnIndex("key")));
        if (cursor.getString(cursor.getColumnIndex("file")) != null)
            setFile(new File(cursor.getString(cursor.getColumnIndex("file"))));
        if (cursor.getString(cursor.getColumnIndex("size")) != null)
            setSize(cursor.getLong(cursor.getColumnIndex("size")));
        setStatus(cursor.getInt(cursor.getColumnIndex("status")));
        setTime(cursor.getLong(cursor.getColumnIndex("time")));
        setExpire(cursor.getLong(cursor.getColumnIndex("expire")));
    }
}

package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 我的个人信息实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class MyInformation extends Entity {

    private static final String NODE_USER = "user";
    private static final String NODE_NAME = "name";
    private static final String NODE_PORTRAIT = "portrait";
    private static final String NODE_JOIN_TIME = "jointime";
    private static final String NODE_GENDER = "gender";
    private static final String NODE_FROM = "from";
    private static final String NODE_DEV_PLATFORM = "devplatform";
    private static final String NODE_EXPERTISE = "expertise";
    private static final String NODE_FAVORITE_COUNT = "favoritecount";
    private static final String NODE_FANS = "fans";
    private static final String NODE_FOLLOWERS = "followers";

    private String name;
    private String face;
    private String jointime;
    private int gender;
    private String from;
    private String devplatform;
    private String expertise;
    private int favoritecount;
    private int fanscount;
    private int followerscount;

    public String getJointime() {
        return jointime;
    }

    public void setJointime(String jointime) {
        this.jointime = jointime;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getDevplatform() {
        return devplatform;
    }

    public void setDevplatform(String devplatform) {
        this.devplatform = devplatform;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int getFavoritecount() {
        return favoritecount;
    }

    public void setFavoritecount(int favoritecount) {
        this.favoritecount = favoritecount;
    }

    public int getFanscount() {
        return fanscount;
    }

    public void setFanscount(int fanscount) {
        this.fanscount = fanscount;
    }

    public int getFollowerscount() {
        return followerscount;
    }

    public void setFollowerscount(int followerscount) {
        this.followerscount = followerscount;
    }

    public static MyInformation parse(InputStream stream) throws IOException, AppException {
        MyInformation user = null;
        // 获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {
            xmlParser.setInput(stream, Base.UTF8);
            // 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType = xmlParser.getEventType();
            // 一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlParser.getName();
                switch (evtType) {

                    case XmlPullParser.START_TAG:
                        // 如果是标签开始，则说明需要实例化对象了
                        if (tag.equalsIgnoreCase(NODE_USER)) {
                            user = new MyInformation();
                        } else if (user != null) {
                            if (tag.equalsIgnoreCase(NODE_NAME)) {
                                user.setName(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_PORTRAIT)) {
                                user.setFace(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_JOIN_TIME)) {
                                user.setJointime(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_GENDER)) {
                                user.setGender(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_FROM)) {
                                user.setFrom(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_DEV_PLATFORM)) {
                                user.setDevplatform(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_EXPERTISE)) {
                                user.setExpertise(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_FAVORITE_COUNT)) {
                                user.setFavoritecount(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_FANS)) {
                                user.setFanscount(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_FOLLOWERS)) {
                                user.setFollowerscount(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                                user.setNotice(new Notice());
                            } else if (user.getNotice() != null) {
                                if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                    user.getNotice().setAtmeCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                    user.getNotice().setMsgCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                    user.getNotice().setReviewCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                    user.getNotice().setNewFansCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 如果xml没有结束，则导航到下一个节点
                evtType = xmlParser.next();
            }
        } catch (XmlPullParserException e) {
            throw AppException.xml(e);
        } finally {
            stream.close();
        }
        return user;
    }
}

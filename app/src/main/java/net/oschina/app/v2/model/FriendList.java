package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 好友列表实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class FriendList extends Entity implements ListEntity {

    public final static int TYPE_FANS = 0x00;
    public final static int TYPE_FOLLOWER = 0x01;
    private static final String NODE_FRIEND = "friend";
    private static final String NODE_USER_ID = "userid";
    private static final String NODE_NAME = "name";
    private static final String NODE_PORTRAIT = "portrait";
    private static final String NODE_EXPERTISE = "expertise";
    private static final String NODE_GENDER = "gender";

    private List<Friend> friendlist = new ArrayList<Friend>();

    /**
     * 好友实体类
     */
    public static class Friend implements Serializable {
        private int userid;
        private String name;
        private String face;
        private String expertise;
        private int gender;

        public int getUserid() {
            return userid;
        }

        public void setUserid(int userid) {
            this.userid = userid;
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

        public String getExpertise() {
            return expertise;
        }

        public void setExpertise(String expertise) {
            this.expertise = expertise;
        }

        public int getGender() {
            return gender;
        }

        public void setGender(int gender) {
            this.gender = gender;
        }
    }

    public List<Friend> getFriendlist() {
        return friendlist;
    }

    public void setFriendlist(List<Friend> resultlist) {
        this.friendlist = resultlist;
    }

    public static FriendList parse(InputStream inputStream) throws IOException,
            AppException {
        FriendList friendlist = new FriendList();
        Friend friend = null;
        // 获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {
            xmlParser.setInput(inputStream, UTF8);
            // 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType = xmlParser.getEventType();
            // 一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlParser.getName();
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase(NODE_FRIEND)) {
                            friend = new Friend();
                        } else if (friend != null) {
                            if (tag.equalsIgnoreCase(NODE_USER_ID)) {
                                friend.userid = StringUtils.toInt(
                                        xmlParser.nextText(), 0);
                            } else if (tag.equalsIgnoreCase(NODE_NAME)) {
                                friend.name = xmlParser.nextText();
                            } else if (tag.equalsIgnoreCase(NODE_PORTRAIT)) {
                                friend.face = xmlParser.nextText();
                            } else if (tag.equalsIgnoreCase(NODE_EXPERTISE)) {
                                friend.expertise = xmlParser.nextText();
                            } else if (tag.equalsIgnoreCase(NODE_GENDER)) {
                                friend.gender = StringUtils.toInt(
                                        xmlParser.nextText(), 0);
                            }
                        } else if (friendlist.getNotice() != null) {
                            if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                friendlist.getNotice().setAtmeCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                friendlist.getNotice().setMsgCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                friendlist.getNotice().setReviewCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                friendlist.getNotice().setNewFansCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        // 如果遇到标签结束，则把对象添加进集合中
                        if (tag.equalsIgnoreCase(NODE_FRIEND) && friend != null) {
                            friendlist.getFriendlist().add(friend);
                            friend = null;
                        }
                        break;
                }
                // 如果xml没有结束，则导航到下一个节点
                evtType = xmlParser.next();
            }
        } catch (XmlPullParserException e) {
            throw AppException.xml(e);
        } finally {
            inputStream.close();
        }
        return friendlist;
    }

    @Override
    public List<?> getList() {
        return friendlist;
    }
}

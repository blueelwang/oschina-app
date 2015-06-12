package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 消息实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Message extends Entity {

    public final static int CLIENT_MOBILE = 2;
    public final static int CLIENT_ANDROID = 3;
    public final static int CLIENT_IPHONE = 4;
    public final static int CLIENT_WINDOWS_PHONE = 5;

    public static final String NODE_MESSAGE = "message";
    public static final String NODE_ID = "id";
    public static final String NODE_PORTRAIT = "portrait";
    public static final String NODE_FRIEND_ID = "friendid";
    public static final String NODE_FRIEND_NAME = "friendname";
    public static final String NODE_CONTENT = "content";
    public static final String NODE_SENDER = "sender";
    public static final String NODE_SENDER_ID = "senderid";
    public static final String NODE_MESSAGE_COUNT = "messageCount";
    public static final String NODE_PUB_DATE = "pubDate";
    public static final String NODE_APP_CLIENT = "appclient";

    private String face;
    private int friendId;
    private String friendName;
    private String sender;
    private int senderId;
    private String content;
    private int messageCount;
    private String pubDate;
    private int appClient;

    public int getAppClient() {
        return appClient;
    }

    public void setAppClient(int appClient) {
        this.appClient = appClient;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public static Message parse(InputStream inputStream) throws IOException, AppException {
        Message msg = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType = xmlParser.getEventType();
            //一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlParser.getName();
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase(NODE_MESSAGE)) {
                            msg = new Message();
                        } else if (msg != null) {
                            if (tag.equalsIgnoreCase(NODE_ID)) {
                                msg.id = StringUtils.toInt(xmlParser.nextText(), 0);
                            } else if (tag.equalsIgnoreCase(NODE_PORTRAIT)) {
                                msg.setFace(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_FRIEND_ID)) {
                                msg.setFriendId(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_FRIEND_NAME)) {
                                msg.setFriendName(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_CONTENT)) {
                                msg.setContent(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_SENDER)) {
                                msg.setSender(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_SENDER_ID)) {
                                msg.setSenderId(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_MESSAGE_COUNT)) {
                                msg.setMessageCount(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_PUB_DATE)) {
                                msg.setPubDate(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_APP_CLIENT)) {
                                msg.setAppClient(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                                msg.setNotice(new Notice());
                            } else if (msg.getNotice() != null) {
                                if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                    msg.getNotice().setAtmeCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                    msg.getNotice().setMsgCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                    msg.getNotice().setReviewCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                    msg.getNotice().setNewFansCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                //如果xml没有结束，则导航到下一个节点
                evtType = xmlParser.next();
            }
        } catch (XmlPullParserException e) {
            throw AppException.xml(e);
        } finally {
            inputStream.close();
        }
        return msg;
    }
}

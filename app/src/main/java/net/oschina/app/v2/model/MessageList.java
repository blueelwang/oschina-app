package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 消息列表实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class MessageList extends Entity implements ListEntity {

    private static final String NODE_MESSAGE_COUNT = "messageCount";
    private int pageSize;
    private int messageCount;
    private List<Message> messagelist = new ArrayList<>();

    public int getPageSize() {
        return pageSize;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public List<Message> getMessagelist() {
        return messagelist;
    }

    public static MessageList parse(InputStream inputStream)
            throws IOException, AppException {
        MessageList msglist = new MessageList();
        Message msg = null;
        // 获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {
            xmlParser.setInput(inputStream, UTF8);
            // 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType = xmlParser.getEventType();
            // 一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlParser.getName();
                int depth = xmlParser.getDepth();
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        if (depth == 2 && tag.equalsIgnoreCase(NODE_MESSAGE_COUNT)) {
                            msglist.messageCount = StringUtils.toInt(
                                    xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(NODE_PAGE_SIZE)) {
                            msglist.pageSize = StringUtils.toInt(
                                    xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(Message.NODE_MESSAGE)) {
                            msg = new Message();
                        } else if (msg != null) {
                            if (tag.equalsIgnoreCase(Message.NODE_ID)) {
                                msg.id = StringUtils.toInt(xmlParser.nextText(), 0);
                            } else if (tag.equalsIgnoreCase(Message.NODE_PORTRAIT)) {
                                msg.setFace(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Message.NODE_FRIEND_ID)) {
                                msg.setFriendId(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Message.NODE_FRIEND_NAME)) {
                                msg.setFriendName(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Message.NODE_CONTENT)) {
                                msg.setContent(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Message.NODE_SENDER)) {
                                msg.setSender(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Message.NODE_SENDER_ID)) {
                                msg.setSenderId(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Message.NODE_MESSAGE_COUNT)) {
                                msg.setMessageCount(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Message.NODE_PUB_DATE)) {
                                msg.setPubDate(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Message.NODE_APP_CLIENT)) {
                                msg.setAppClient(StringUtils.toInt(xmlParser.nextText(), 0));
                            }
                        } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                            msglist.setNotice(new Notice());
                        } else if (msglist.getNotice() != null) {
                            if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                msglist.getNotice().setAtmeCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                msglist.getNotice().setMsgCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                msglist.getNotice().setReviewCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                msglist.getNotice().setNewFansCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        // 如果遇到标签结束，则把对象添加进集合中
                        if (tag.equalsIgnoreCase("message") && msg != null) {
                            msglist.getMessagelist().add(msg);
                            msg = null;
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
        return msglist;
    }

    @Override
    public List<?> getList() {
        return messagelist;
    }
}

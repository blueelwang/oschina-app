package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.model.Active.ObjectReply;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 用户专页信息实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UserInformation extends Entity {

    private static final String NODE_USER = "user";
    private static final String NODE_ACTIVE = "active";
    private static final String NODE_UID = "uid";
    private static final String NODE_FROM = "from";
    private static final String NODE_NAME = "name";
    private static final String NODE_PORTRAIT = "portrait";
    private static final String NODE_JOIN_TIME = "jointime";
    private static final String NODE_GENDER = "gender";
    private static final String NODE_DEV_PLATFORM = "devplatform";
    private static final String NODE_EXPERTISE = "expertise";
    private static final String NODE_RELATION = "relation";
    private static final String NODE_LATEST_ONLINE = "latestonline";


    private static final String NODE_ID = "id";
    private static final String NODE_MESSAGE = "message";
    private static final String NODE_AUTHOR = "author";
    private static final String NODE_AUTHOR_ID = "authorid";
    private static final String NODE_OBJECT_ID = "objectID";
    private static final String NODE_OBJECT_TYPE = "objecttype";
    private static final String NODE_OBJECT_CATALOG = "objectcatalog";
    private static final String NODE_OBJECT_TITLE = "objecttitle";
    private static final String NODE_OBJECT_REPLY = "objectreply";
    private static final String NODE_OBJECT_NAME = "objectname";
    private static final String NODE_OBJECT_BODY = "objectbody";
    private static final String NODE_COMMENT_COUNT = "commentCount";
    private static final String NODE_PUB_DATE = "pubDate";
    private static final String NODE_TWEET_IMAGE = "tweetimage";
    private static final String NODE_APP_CLIENT = "appclient";
    private static final String NODE_URL = "url";

    private int pageSize;
    private User user = new User();
    private List<Active> activelist = new ArrayList<Active>();

    public int getPageSize() {
        return pageSize;
    }

    public User getUser() {
        return user;
    }

    public List<Active> getActivelist() {
        return activelist;
    }

    public static UserInformation parse(InputStream inputStream) throws IOException, AppException {
        UserInformation uinfo = new UserInformation();
        User user = null;
        Active active = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType = xmlParser.getEventType();
            //一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlParser.getName();
                int depth = xmlParser.getDepth();
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase(NODE_USER)) {
                            user = new User();
                        } else if (tag.equalsIgnoreCase(NODE_PAGE_SIZE)) {
                            uinfo.pageSize = StringUtils.toInt(xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(NODE_ACTIVE)) {
                            active = new Active();
                        } else if (user != null) {
                            if (tag.equalsIgnoreCase(NODE_UID)) {
                                user.setUid(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_FROM)) {
                                user.setLocation(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_NAME)) {
                                user.setName(xmlParser.nextText());
                            } else if (depth == 3 && tag.equalsIgnoreCase(NODE_PORTRAIT)) {
                                user.setFace(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_JOIN_TIME)) {
                                user.setJointime(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_GENDER)) {
                                user.setGender(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_DEV_PLATFORM)) {
                                user.setDevplatform(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_EXPERTISE)) {
                                user.setExpertise(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_RELATION)) {
                                user.setRelation(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_LATEST_ONLINE)) {
                                user.setLatestonline(xmlParser.nextText());
                            }
                        } else if (active != null) {
                            if (tag.equalsIgnoreCase(NODE_ID)) {
                                active.id = StringUtils.toInt(xmlParser.nextText(), 0);
                            } else if (depth == 4 && tag.equalsIgnoreCase(NODE_PORTRAIT)) {
                                active.setFace(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_MESSAGE)) {
                                active.setMessage(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_AUTHOR)) {
                                active.setAuthor(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_AUTHOR_ID)) {
                                active.setAuthorId(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_CATALOG)) {
                                active.setActiveType(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_OBJECT_ID)) {
                                active.setObjectId(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_OBJECT_TYPE)) {
                                active.setObjectType(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_OBJECT_CATALOG)) {
                                active.setObjectCatalog(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_OBJECT_TITLE)) {
                                active.setObjectTitle(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_OBJECT_REPLY)) {
                                active.setObjectReply(new ObjectReply());
                            } else if (active.getObjectReply() != null && tag.equalsIgnoreCase(NODE_OBJECT_NAME)) {
                                active.getObjectReply().objectName = xmlParser.nextText();
                            } else if (active.getObjectReply() != null && tag.equalsIgnoreCase(NODE_OBJECT_BODY)) {
                                active.getObjectReply().objectBody = xmlParser.nextText();
                            } else if (tag.equalsIgnoreCase(NODE_COMMENT_COUNT)) {
                                active.setCommentCount(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_PUB_DATE)) {
                                active.setPubDate(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_TWEET_IMAGE)) {
                                active.setTweetimage(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_APP_CLIENT)) {
                                active.setAppClient(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_URL)) {
                                active.setUrl(xmlParser.nextText());
                            }
                        } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                            uinfo.setNotice(new Notice());
                        } else if (uinfo.getNotice() != null) {
                            if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                uinfo.getNotice().setAtmeCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                uinfo.getNotice().setMsgCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                uinfo.getNotice().setReviewCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                uinfo.getNotice().setNewFansCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //如果遇到标签结束，则把对象添加进集合中
                        if (tag.equalsIgnoreCase(NODE_USER) && user != null) {
                            uinfo.user = user;
                            user = null;
                        } else if (tag.equalsIgnoreCase(NODE_ACTIVE) && active != null) {
                            uinfo.getActivelist().add(active);
                            active = null;
                        }
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
        return uinfo;
    }
}

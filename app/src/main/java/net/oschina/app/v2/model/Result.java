package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.model.Comment.Refer;
import net.oschina.app.v2.model.Comment.Reply;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 数据操作结果实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Result extends Base {

    private static final String NODE_RESULT = "result";
    private static final String NODE_ERROR_CODE = "errorCode";
    private static final String NODE_ERROR_MESSAGE = "errorMessage";
    private static final String NODE_COMMENT = "comment";
    private static final String NODE_ID = "id";
    private static final String NODE_PORTRAIT = "portrait";
    private static final String NODE_AUTHOR = "author";
    private static final String NODE_AUTHOR_ID = "authorid";
    private static final String NODE_CONTENT = "content";
    private static final String NODE_PUB_DATE = "pubDate";
    private static final String NODE_APP_CLIENT = "appclient";
    private static final String NODE_REPLY = "reply";
    private static final String NODE_RAUTHOR = "rauthor";
    private static final String NODE_RPUB_DATE = "rpubDate";
    private static final String NODE_RCONTENT = "rcontent";
    private static final String NODE_REFER = "refer";
    private static final String NODE_REFER_TITLE = "refertitle";
    private static final String NODE_REFER_BODY = "referbody";
    private int errorCode;
    private String errorMessage;

    private Comment comment;

    public boolean OK() {
        return errorCode == 1;
    }

    /**
     * 解析调用结果
     *
     * @param stream
     * @return
     * @throws java.io.IOException
     * @throws org.xmlpull.v1.XmlPullParserException
     */
    public static Result parse(InputStream stream) throws IOException, AppException {
        Result res = null;
        Reply reply = null;
        Refer refer = null;
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
                        if (tag.equalsIgnoreCase(NODE_RESULT)) {
                            res = new Result();
                        } else if (res != null) {
                            if (tag.equalsIgnoreCase(NODE_ERROR_CODE)) {
                                res.errorCode = StringUtils.toInt(xmlParser.nextText(), -1);
                            } else if (tag.equalsIgnoreCase(NODE_ERROR_MESSAGE)) {
                                res.errorMessage = xmlParser.nextText().trim();
                            } else if (tag.equalsIgnoreCase(NODE_COMMENT)) {
                                res.comment = new Comment();
                            } else if (res.comment != null) {
                                if (tag.equalsIgnoreCase(NODE_ID)) {
                                    res.comment.id = StringUtils.toInt(xmlParser.nextText(), 0);
                                } else if (tag.equalsIgnoreCase(NODE_PORTRAIT)) {
                                    res.comment.setFace(xmlParser.nextText());
                                } else if (tag.equalsIgnoreCase(NODE_AUTHOR)) {
                                    res.comment.setAuthor(xmlParser.nextText());
                                } else if (tag.equalsIgnoreCase(NODE_AUTHOR_ID)) {
                                    res.comment.setAuthorId(StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(NODE_CONTENT)) {
                                    res.comment.setContent(xmlParser.nextText());
                                } else if (tag.equalsIgnoreCase(NODE_PUB_DATE)) {
                                    res.comment.setPubDate(xmlParser.nextText());
                                } else if (tag.equalsIgnoreCase(NODE_APP_CLIENT)) {
                                    res.comment.setAppClient(StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(NODE_REPLY)) {
                                    reply = new Reply();
                                } else if (reply != null && tag.equalsIgnoreCase(NODE_RAUTHOR)) {
                                    reply.rauthor = xmlParser.nextText();
                                } else if (reply != null && tag.equalsIgnoreCase(NODE_RPUB_DATE)) {
                                    reply.rpubDate = xmlParser.nextText();
                                } else if (reply != null && tag.equalsIgnoreCase(NODE_RCONTENT)) {
                                    reply.rcontent = xmlParser.nextText();
                                } else if (tag.equalsIgnoreCase(NODE_REFER)) {
                                    refer = new Refer();
                                } else if (refer != null && tag.equalsIgnoreCase(NODE_REFER_TITLE)) {
                                    refer.refertitle = xmlParser.nextText();
                                } else if (refer != null && tag.equalsIgnoreCase(NODE_REFER_BODY)) {
                                    refer.referbody = xmlParser.nextText();
                                }
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                                res.setNotice(new Notice());
                            }  else if (res.getNotice() != null) {
                                if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                    res.getNotice().setAtmeCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                    res.getNotice().setMsgCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                    res.getNotice().setReviewCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                    res.getNotice().setNewFansCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //如果遇到标签结束，则把对象添加进集合中
                        if (tag.equalsIgnoreCase(NODE_REPLY) && res.comment != null && reply != null) {
                            res.comment.getReplies().add(reply);
                            reply = null;
                        } else if (tag.equalsIgnoreCase(NODE_REFER) && res.comment != null && refer != null) {
                            res.comment.getRefers().add(refer);
                            refer = null;
                        }
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
        return res;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return String.format("RESULT: CODE:%d,MSG:%s", errorCode, errorMessage);
    }
}

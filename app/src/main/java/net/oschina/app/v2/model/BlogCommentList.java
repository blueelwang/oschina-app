package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.model.Comment.Refer;
import net.oschina.app.v2.model.Comment.Reply;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 博客评论列表实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class BlogCommentList extends Entity implements ListEntity {

    private int pageSize;
    private int allCount;
    private List<Comment> commentlist = new ArrayList<>();

    private static final String NODE_ALL_COUNT = "allCount";

    public int getPageSize() {
        return pageSize;
    }

    public int getAllCount() {
        return allCount;
    }

    public List<Comment> getCommentlist() {
        return commentlist;
    }

    public static BlogCommentList parse(InputStream inputStream)
            throws IOException, AppException {
        BlogCommentList commlist = new BlogCommentList();
        Comment comm = null;
        Reply reply = null;
        Refer refer = null;
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
                        if (tag.equalsIgnoreCase(NODE_ALL_COUNT)) {
                            commlist.allCount = StringUtils.toInt(
                                    xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(NODE_PAGE_SIZE)) {
                            commlist.pageSize = StringUtils.toInt(
                                    xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(Comment.NODE_COMMENT)) {
                            comm = new Comment();
                        } else if (comm != null) {
                            if (tag.equalsIgnoreCase(Comment.NODE_ID)) {
                                comm.id = StringUtils
                                        .toInt(xmlParser.nextText(), 0);
                            } else if (tag.equalsIgnoreCase(Comment.NODE_PORTRAIT)) {
                                comm.setFace(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Comment.NODE_AUTHOR)) {
                                comm.setAuthor(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Comment.NODE_AUTHOR_ID)) {
                                comm.setAuthorId(StringUtils.toInt(
                                        xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Comment.NODE_CONTENT)) {
                                comm.setContent(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Comment.NODE_PUB_DATE)) {
                                comm.setPubDate(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Comment.NODE_APP_CLIENT)) {
                                comm.setAppClient(StringUtils.toInt(
                                        xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Comment.NODE_REPLY)) {
                                reply = new Reply();
                            } else if (reply != null
                                    && tag.equalsIgnoreCase(Comment.NODE_RAUTHOR)) {
                                reply.rauthor = xmlParser.nextText();
                            } else if (reply != null
                                    && tag.equalsIgnoreCase(Comment.NODE_RPUB_DATE)) {
                                reply.rpubDate = xmlParser.nextText();
                            } else if (reply != null
                                    && tag.equalsIgnoreCase(Comment.NODE_RCONTENT)) {
                                reply.rcontent = xmlParser.nextText();
                            } else if (tag.equalsIgnoreCase(Comment.NODE_REFER)) {
                                refer = new Refer();
                            } else if (refer != null
                                    && tag.equalsIgnoreCase(Comment.NODE_REFER_TITLE)) {
                                refer.refertitle = xmlParser.nextText();
                            } else if (refer != null
                                    && tag.equalsIgnoreCase(Comment.NODE_REFER_BODY)) {
                                refer.referbody = xmlParser.nextText();
                            }
                        } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {// 通知信息
                            commlist.setNotice(new Notice());
                        } else if (commlist.getNotice() != null) {
                            if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                commlist.getNotice().setAtmeCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                commlist.getNotice().setMsgCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                commlist.getNotice().setReviewCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                commlist.getNotice().setNewFansCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        // 如果遇到标签结束，则把对象添加进集合中
                        if (tag.equalsIgnoreCase(Comment.NODE_COMMENT) && comm != null) {
                            commlist.getCommentlist().add(comm);
                            comm = null;
                        } else if (tag.equalsIgnoreCase(Comment.NODE_REPLY) && comm != null
                                && reply != null) {
                            comm.getReplies().add(reply);
                            reply = null;
                        } else if (tag.equalsIgnoreCase(Comment.NODE_REFER) && comm != null
                                && refer != null) {
                            comm.getRefers().add(refer);
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
            inputStream.close();
        }
        return commlist;
    }

    @Override
    public List<?> getList() {
        return commentlist;
    }
}

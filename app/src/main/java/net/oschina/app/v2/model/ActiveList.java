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
 * 动态列表实体类
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ActiveList extends Entity implements ListEntity {

	public final static int CATALOG_LASTEST = 1;// 最新
	public final static int CATALOG_ATME = 2;// @我
	public final static int CATALOG_COMMENT = 3;// 评论
	public final static int CATALOG_MYSELF = 4;// 我自己

    private static final String NODE_ACTIVE_COUNT = "activeCount";

    private int pageSize;
	private int activeCount;
	private List<Active> activelist = new ArrayList<Active>();

	public int getPageSize() {
		return pageSize;
	}

	public int getActiveCount() {
		return activeCount;
	}

	public List<Active> getActivelist() {
		return activelist;
	}

	public static ActiveList parse(InputStream inputStream) throws IOException,
			AppException {
		ActiveList activelist = new ActiveList();
		Active active = null;
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
					if (tag.equalsIgnoreCase(NODE_ACTIVE_COUNT)) {
						activelist.activeCount = StringUtils.toInt(
								xmlParser.nextText(), 0);
					} else if (tag.equalsIgnoreCase(NODE_PAGE_SIZE)) {
						activelist.pageSize = StringUtils.toInt(
								xmlParser.nextText(), 0);
					} else if (tag.equalsIgnoreCase(Active.NODE_ACTIVE)) {
						active = new Active();
					} else if (active != null) {
                        if (tag.equalsIgnoreCase(Active.NODE_ID)) {
                            active.id = StringUtils.toInt(xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(Active.NODE_PORTRAIT)) {
                            active.setFace(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Active.NODE_MESSAGE)) {
                            active.setMessage(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Active.NODE_AUTHOR)) {
                            active.setAuthor(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Active.NODE_AUTHOR_ID)) {
                            active.setAuthorId(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(NODE_CATALOG)) {
                            active.setActiveType(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Active.NODE_OBJECT_ID)) {
                            active.setObjectId(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Active.NODE_OBJECT_TYPE)) {
                            active.setObjectType(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Active.NODE_OBJECT_CATALOG)){
                            active.setObjectCatalog(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Active.NODE_OBJECT_TITLE)) {
                            active.setObjectTitle(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Active.NODE_OBJECT_REPLY)) {
                            active.setObjectReply(new ObjectReply());
                        } else if (active.getObjectReply() != null && tag.equalsIgnoreCase(Active.NODE_OBJECT_NAME)) {
                            active.getObjectReply().objectName = xmlParser.nextText();
                        } else if (active.getObjectReply() != null && tag.equalsIgnoreCase(Active.NODE_OBJECT_BODY)) {
                            active.getObjectReply().objectBody = xmlParser.nextText();
                        } else if (tag.equalsIgnoreCase(Active.NODE_COMMENT_COUNT)) {
                            active.setCommentCount(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Active.NODE_PUB_DATE)) {
                            active.setPubDate(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Active.NODE_TWEET_IMAGE)) {
                            active.setTweetimage(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Active.NODE_APP_CLIENT)) {
                            active.setAppClient(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Active.NODE_URL)) {
                            active.setUrl(xmlParser.nextText());
                        }
					} else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {// 通知信息
                        activelist.setNotice(new Notice());
                    } else if (activelist.getNotice() != null) {
                        if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                            activelist.getNotice().setAtmeCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                            activelist.getNotice().setMsgCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                            activelist.getNotice().setReviewCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                            activelist.getNotice().setNewFansCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        }
                    }
					break;
				case XmlPullParser.END_TAG:
					// 如果遇到标签结束，则把对象添加进集合中
					if (tag.equalsIgnoreCase(Active.NODE_ACTIVE) && active != null) {
						activelist.getActivelist().add(active);
						active = null;
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
		return activelist;
	}

	@Override
	public List<?> getList() {
		return activelist;
	}
}

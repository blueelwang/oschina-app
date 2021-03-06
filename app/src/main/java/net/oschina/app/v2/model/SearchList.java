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
 * 搜索列表实体类
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SearchList extends Entity implements ListEntity {

	public final static String CATALOG_ALL = "all";
	public final static String CATALOG_NEWS = "news";
	public final static String CATALOG_POST = "post";
	public final static String CATALOG_SOFTWARE = "software";
	public final static String CATALOG_BLOG = "blog";
	public final static String CATALOG_CODE = "code";
    private static final String NODE_RESULT = "result";
    private static final String NODE_OBJ_ID = "objid";
    private static final String NODE_TYPE = "type";
    private static final String NODE_TITLE = "title";
    private static final String NODE_URL = "url";
    private static final String NODE_PUB_DATE = "pubDate";
    private static final String NODE_AUTHOR = "author";

    private int pageSize;
	private List<Result> resultlist = new ArrayList<Result>();

	/**
	 * 搜索结果实体类
	 */
	public static class Result implements Serializable {
		private int objid;
		private int type;
		private String title;
		private String url;
		private String pubDate;
		private String author;

		public int getObjid() {
			return objid;
		}

		public void setObjid(int objid) {
			this.objid = objid;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getPubDate() {
			return pubDate;
		}

		public void setPubDate(String pubDate) {
			this.pubDate = pubDate;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public List<Result> getResultlist() {
		return resultlist;
	}

	public void setResultlist(List<Result> resultlist) {
		this.resultlist = resultlist;
	}

	public static SearchList parse(InputStream inputStream) throws IOException,
			AppException {
		SearchList searchList = new SearchList();
		Result res = null;
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
					if (tag.equalsIgnoreCase(NODE_PAGE_SIZE)) {
						searchList.pageSize = StringUtils.toInt(
								xmlParser.nextText(), 0);
					} else if (tag.equalsIgnoreCase(NODE_RESULT)) {
						res = new Result();
					} else if (res != null) {
						if (tag.equalsIgnoreCase(NODE_OBJ_ID)) {
							res.objid = StringUtils.toInt(xmlParser.nextText(),
                                    0);
						} else if (tag.equalsIgnoreCase(NODE_TYPE)) {
							res.type = StringUtils.toInt(xmlParser.nextText(),
                                    0);
						} else if (tag.equalsIgnoreCase(NODE_TITLE)) {
							res.title = xmlParser.nextText();
						} else if (tag.equalsIgnoreCase(NODE_URL)){
							res.url = xmlParser.nextText();
						} else if (tag.equalsIgnoreCase(NODE_PUB_DATE)) {
							res.pubDate = xmlParser.nextText();
						} else if (tag.equalsIgnoreCase(NODE_AUTHOR)) {
							res.author = xmlParser.nextText();
						}
					} else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                        searchList.setNotice(new Notice());
                    }  else if (searchList.getNotice() != null) {
                        if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                            searchList.getNotice().setAtmeCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                            searchList.getNotice().setMsgCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                            searchList.getNotice().setReviewCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                            searchList.getNotice().setNewFansCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        }
                    }
					break;
				case XmlPullParser.END_TAG:
					// 如果遇到标签结束，则把对象添加进集合中
					if (tag.equalsIgnoreCase(NODE_RESULT) && res != null) {
						searchList.getResultlist().add(res);
						res = null;
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
		return searchList;
	}

	@Override
	public List<?> getList() {
		return resultlist;
	}
}

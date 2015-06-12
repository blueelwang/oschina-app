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
 * 博客列表实体类
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class BlogList extends Entity implements ListEntity {

	public static final int CATALOG_USER = 1;// 用户博客
	public static final int CATALOG_LATEST = 2;// 最新博客
	public static final int CATALOG_RECOMMEND = 3;// 推荐博客

	public static final String TYPE_LATEST = "latest";
	public static final String TYPE_RECOMMEND = "recommend";

    private static final String NODE_BLOG_COUNT = "blogsCount";

    private int blogsCount;
	private int pageSize;
	private List<Blog> bloglist = new ArrayList<Blog>();

	public int getBlogsCount() {
		return blogsCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public List<Blog> getBloglist() {
		return bloglist;
	}

	public static BlogList parse(InputStream inputStream) throws IOException,
			AppException {
		BlogList bloglist = new BlogList();
		Blog blog = null;
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
					if (tag.equalsIgnoreCase(NODE_BLOG_COUNT)) {
						bloglist.blogsCount = StringUtils.toInt(
								xmlParser.nextText(), 0);
					} else if (tag.equalsIgnoreCase(NODE_PAGE_SIZE)) {
						bloglist.pageSize = StringUtils.toInt(
								xmlParser.nextText(), 0);
					} else if (tag.equalsIgnoreCase(Blog.NODE_BLOG)) {
						blog = new Blog();
					} else if (blog != null) {
                        if (tag.equalsIgnoreCase(Blog.NODE_ID)) {
                            blog.id = StringUtils.toInt(xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(Blog.NODE_TITLE)) {
                            blog.setTitle(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Blog.NODE_WHERE)) {
                            blog.setWhere(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Blog.NODE_BODY)) {
                            blog.setBody(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Blog.NODE_AUTHOR)) {
                            blog.setAuthor(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Blog.NODE_AUTHOR_ID)) {
                            blog.setAuthorId(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Blog.NODE_DOCUMENT_TYPE)) {
                            blog.setDocumentType(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Blog.NODE_PUB_DATE)) {
                            blog.setPubDate(xmlParser.nextText());
                        } else if (tag.equalsIgnoreCase(Blog.NODE_FAVORITE)) {
                            blog.setFavorite(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Blog.NODE_COMMENT_COUNT)) {
                            blog.setCommentCount(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Blog.NODE_URL)) {
                            blog.setUrl(xmlParser.nextText());
                        }
					} else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {// 通知信息
                        bloglist.setNotice(new Notice());
                    }  else if (bloglist.getNotice() != null) {
                        if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                            bloglist.getNotice().setAtmeCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                            bloglist.getNotice().setMsgCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                            bloglist.getNotice().setReviewCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                            bloglist.getNotice().setNewFansCount(
                                    StringUtils.toInt(xmlParser.nextText(), 0));
                        }
                    }
					break;
				case XmlPullParser.END_TAG:
					// 如果遇到标签结束，则把对象添加进集合中
					if (tag.equalsIgnoreCase(Blog.NODE_BLOG) && blog != null) {
						bloglist.getBloglist().add(blog);
						blog = null;
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
		return bloglist;
	}

	@Override
	public List<?> getList() {
		return bloglist;
	}
}

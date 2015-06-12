package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 软件实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Software extends Entity {

    private static final String NODE_SOFEWARE = "software";
    private static final String NODE_ID = "id";
    private static final String NODE_URL = "url";
    private static final String NODE_TITLE = "title";
    private static final String NODE_EXTENSION_TITLE = "extensionTitle";
    private static final String NODE_LICENSE = "license";
    private static final String NODE_BODY = "body";
    private static final String NODE_HOME_PAGE = "homepage";
    private static final String NODE_DOCUMENT = "document";
    private static final String NODE_DOWNLOAD = "download";
    private static final String NODE_LOGO = "logo";
    private static final String NODE_LANGUAGE = "language";
    private static final String NODE_OS = "os";
    private static final String NODE_RECORD_TIME = "recordtime";
    private static final String NODE_FAVORITE = "favorite";
    private String title;
    private String extensionTitle;
    private String license;
    private String body;
    private String homepage;
    private String document;
    private String download;
    private String logo;
    private String language;
    private String os;
    private String recordtime;
    private String url;
    private int favorite;

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExtensionTitle() {
        return extensionTitle;
    }

    public void setExtensionTitle(String extensionTitle) {
        this.extensionTitle = extensionTitle;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getRecordtime() {
        return recordtime;
    }

    public void setRecordtime(String recordtime) {
        this.recordtime = recordtime;
    }

    public static Software parse(InputStream inputStream) throws IOException,
            AppException {
        Software sw = null;
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
                        if (tag.equalsIgnoreCase(NODE_SOFEWARE)) {
                            sw = new Software();
                        } else if (sw != null) {
                            if (tag.equalsIgnoreCase(NODE_ID)) {
                                sw.id = StringUtils.toInt(xmlParser.nextText(), 0);
                            } else if (tag.equalsIgnoreCase(NODE_URL)) {
                                sw.setUrl(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_TITLE)) {
                                sw.setTitle(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_EXTENSION_TITLE)) {
                                sw.setExtensionTitle(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_LICENSE)) {
                                sw.setLicense(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_BODY)) {
                                sw.setBody(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_HOME_PAGE)) {
                                sw.setHomepage(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_DOCUMENT)) {
                                sw.setDocument(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_DOWNLOAD)) {
                                sw.setDownload(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_LOGO)) {
                                sw.setLogo(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_LANGUAGE)) {
                                sw.setLanguage(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_OS)) {
                                sw.setOs(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_RECORD_TIME)) {
                                sw.setRecordtime(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_FAVORITE)) {
                                sw.setFavorite(StringUtils.toInt(
                                        xmlParser.nextText(), 0));

                            } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                                sw.setNotice(new Notice());
                            }  else if (sw.getNotice() != null) {
                                if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                    sw.getNotice().setAtmeCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                    sw.getNotice().setMsgCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                    sw.getNotice().setReviewCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                    sw.getNotice().setNewFansCount(
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
            inputStream.close();
        }
        return sw;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

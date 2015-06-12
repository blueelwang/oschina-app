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
 * 软件分类列表实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SoftwareCatalogList extends Entity {

    private static final String NODE_SOFTWARE_COUNT = "softwarecount";
    private static final String NODE_SOFTWARE_TYPE = "softwareType";
    private static final String NODE_NAME = "name";
    private static final String NODE_TAG = "tag";
    private int softwarecount;
    private List<SoftwareType> softwaretypelist = new ArrayList<SoftwareType>();

    public static class SoftwareType implements Serializable {
        public String name;
        public int tag;
    }

    public int getSoftwarecount() {
        return softwarecount;
    }

    public void setSoftwarecount(int softwarecount) {
        this.softwarecount = softwarecount;
    }

    public List<SoftwareType> getSoftwareTypelist() {
        return softwaretypelist;
    }

    public void setSoftwareTypelist(List<SoftwareType> softwaretypelist) {
        this.softwaretypelist = softwaretypelist;
    }

    public static SoftwareCatalogList parse(InputStream inputStream) throws IOException, AppException {
        SoftwareCatalogList softwarecatalogList = new SoftwareCatalogList();
        SoftwareType softwaretype = null;
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
                        if (tag.equalsIgnoreCase(NODE_SOFTWARE_COUNT)) {
                            softwarecatalogList.setSoftwarecount(StringUtils.toInt(xmlParser.nextText(), 0));
                        } else if (tag.equalsIgnoreCase(NODE_SOFTWARE_TYPE)) {
                            softwaretype = new SoftwareType();
                        } else if (softwaretype != null) {
                            if (tag.equalsIgnoreCase(NODE_NAME)) {
                                softwaretype.name = xmlParser.nextText();
                            } else if (tag.equalsIgnoreCase(NODE_TAG)) {
                                softwaretype.tag = StringUtils.toInt(xmlParser.nextText(), 0);
                            }

                        } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                            softwarecatalogList.setNotice(new Notice());
                        } else if (softwarecatalogList.getNotice() != null) {
                            if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                softwarecatalogList.getNotice().setAtmeCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                softwarecatalogList.getNotice().setMsgCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                softwarecatalogList.getNotice().setReviewCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                softwarecatalogList.getNotice().setNewFansCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //如果遇到标签结束，则把对象添加进集合中
                        if (tag.equalsIgnoreCase(NODE_SOFTWARE_TYPE) && softwaretype != null) {
                            softwarecatalogList.getSoftwareTypelist().add(softwaretype);
                            softwaretype = null;
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
        return softwarecatalogList;
    }
}

package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.MessageSafeConfigFile;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MessageSafeConfigFileParser {
    private static final String LOG_TAG = "MessageSafeConfigFileParser";
    private static final String MESSAGE_NO_TAG = "messageNos";
    private static final String RPOVIDER_ATTR = "name";
    private static final String RPOVIDER_TAG = "provider";
    private static final String SECURE_LINK_TAG = "secureLinks";
    private static final String SUB_MESSAGE_NO_ATTR = "value";
    private static final String SUB_MESSAGE_NO_TAG = "messageNo";
    private static final String SUB_SECURE_LINK_ATTR = "value";
    private static final String SUB_SECURE_LINK_TAG = "secureLink";

    public static void initConfigTable(Context context, String configPath) {
        try {
            parseRootElement(context, XmlParsers.assetXmlRootElement(context, configPath));
        } catch (XmlParserException XmlEx) {
            HwLog.e(LOG_TAG, XmlEx.getMessage());
        } catch (Exception ex) {
            HwLog.e(LOG_TAG, ex.getMessage());
        }
    }

    private static void parseRootElement(Context context, Element element) {
        if (element == null) {
            HwLog.e(LOG_TAG, "The root element is null");
            return;
        }
        List<ContentValues> mesageNoContentValuesList = new ArrayList();
        List<ContentValues> secureLinkContentValuesList = new ArrayList();
        ContentValues mesageNoContentValues = new ContentValues();
        ContentValues secureLinkContentValues = new ContentValues();
        NodeList nodeList = element.getElementsByTagName(RPOVIDER_TAG);
        int size = nodeList.getLength();
        for (int i = 0; i < size; i++) {
            int j;
            Element providerElement = (Element) nodeList.item(i);
            String provider = providerElement.getAttribute("name");
            NodeList linkNodeList = providerElement.getElementsByTagName("messageNo");
            int linkSize = linkNodeList.getLength();
            for (j = 0; j < linkSize; j++) {
                String messageNo = ((Element) linkNodeList.item(j)).getAttribute(DBHelper.VALUE);
                if (!TextUtils.isEmpty(messageNo)) {
                    mesageNoContentValues.clear();
                    mesageNoContentValues.put("messageNo", messageNo);
                    mesageNoContentValues.put(MessageSafeConfigFile.COL_PARTNER, provider);
                    mesageNoContentValuesList.add(new ContentValues(mesageNoContentValues));
                }
            }
            linkNodeList = providerElement.getElementsByTagName("secureLink");
            linkSize = linkNodeList.getLength();
            secureLinkContentValues.clear();
            secureLinkContentValues.put(MessageSafeConfigFile.COL_PARTNER, provider);
            StringBuilder sb = new StringBuilder();
            for (j = 0; j < linkSize; j++) {
                String secureLink = ((Element) linkNodeList.item(j)).getAttribute(DBHelper.VALUE);
                if (!TextUtils.isEmpty(secureLink)) {
                    sb.append(secureLink);
                    sb.append(SqlMarker.SQL_END);
                }
            }
            secureLinkContentValues.put("secureLink", sb.toString());
            secureLinkContentValuesList.add(new ContentValues(secureLinkContentValues));
        }
        insertXmlDataIntoTable(context, MessageSafeConfigFile.CONTENT_OUTERTABLE_NUMBER_URI, mesageNoContentValuesList);
        insertXmlDataIntoTable(context, MessageSafeConfigFile.CONTENT_OUTERTABLE_LINK_URI, secureLinkContentValuesList);
    }

    private static void insertXmlDataIntoTable(Context context, Uri uri, List<ContentValues> contentList) {
        if (!(context == null || contentList == null || contentList.isEmpty())) {
            context.getContentResolver().bulkInsert(uri, (ContentValues[]) contentList.toArray(new ContentValues[contentList.size()]));
        }
    }
}

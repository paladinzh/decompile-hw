package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.rainbow.vaguerule.VagueNameMatchUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigFileParser {
    private static final String LOG_TAG = "ConfigFileParser";
    private static final String PACKAGE_NAME_ATTR = "name";
    private static final String PACKAGE_TAG = "package";
    private static final String SUB_ITEM_ATTR = "name";
    private static final String SUB_ITEM_TAG = "item";
    private static Uri sTableUri = null;

    public static void initConfigTable(Context context, String configPath, Uri uri) {
        HwLog.d(LOG_TAG, "configPath " + configPath);
        try {
            sTableUri = uri;
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
        ContentValues contentValues = new ContentValues();
        List<ContentValues> contentValuesList = new ArrayList();
        NodeList nodeList = element.getElementsByTagName("package");
        int size = nodeList.getLength();
        for (int i = 0; i < size; i++) {
            Element packageElement = (Element) nodeList.item(i);
            String pkgName = packageElement.getAttribute("name");
            if (!VagueNameMatchUtil.isVaguePkgName(pkgName)) {
                contentValues.clear();
                contentValues.put("packageName", pkgName);
                NodeList permissionNodeList = packageElement.getElementsByTagName(SUB_ITEM_TAG);
                int permissionSize = permissionNodeList.getLength();
                for (int j = 0; j < permissionSize; j++) {
                    Element permissionElement = (Element) permissionNodeList.item(j);
                    contentValues.put(permissionElement.getAttribute("name"), permissionElement.getTextContent());
                }
                contentValuesList.add(new ContentValues(contentValues));
            }
        }
        insertXmlDataIntoTable(context, contentValuesList);
    }

    private static void insertXmlDataIntoTable(Context context, List<ContentValues> contentList) {
        if (!(context == null || contentList == null || contentList.isEmpty())) {
            context.getContentResolver().bulkInsert(sTableUri, (ContentValues[]) contentList.toArray(new ContentValues[contentList.size()]));
        }
    }
}

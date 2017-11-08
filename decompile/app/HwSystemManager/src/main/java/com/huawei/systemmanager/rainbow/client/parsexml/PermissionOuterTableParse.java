package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import com.huawei.systemmanager.rainbow.vaguerule.VagueNameMatchUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PermissionOuterTableParse {
    private static final String LOG_TAG = "PermissioinOuterTableParse";
    private static final String PACKAGE_NAME_ATTR = "packageName";
    private static final String PACKAGE_TAG = "package";
    private static final String PERMISSION_OUTER_XML_FILE_NAME = "cloud/permission/permissionouter.xml";
    private static final String SUB_PERMISSION_ATTR = "name";
    private static final String SUB_PERMISSION_TAG = "subPermission";

    public static void initPermissionOuterTable(Context context) {
        try {
            parseRootElement(context, XmlParsers.assetXmlRootElement(context, PERMISSION_OUTER_XML_FILE_NAME));
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
        List<ContentValues> contentValuesList = new ArrayList();
        NodeList nodeList = element.getElementsByTagName("package");
        ContentValues contentValues = new ContentValues();
        int size = nodeList.getLength();
        for (int i = 0; i < size; i++) {
            Element packageElement = (Element) nodeList.item(i);
            String pkgName = packageElement.getAttribute("packageName");
            if (!VagueNameMatchUtil.isVaguePkgName(pkgName)) {
                contentValues.clear();
                contentValues.put("packageName", pkgName);
                NodeList permissionNodeList = packageElement.getElementsByTagName(SUB_PERMISSION_TAG);
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
            context.getContentResolver().bulkInsert(PermissionValues.PERMISSION_OUTERTABLE_URI, (ContentValues[]) contentList.toArray(new ContentValues[contentList.size()]));
        }
    }
}

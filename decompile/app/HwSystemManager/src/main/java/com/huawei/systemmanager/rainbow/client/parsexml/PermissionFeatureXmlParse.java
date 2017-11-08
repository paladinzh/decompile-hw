package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureCvt;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderUtils;
import com.huawei.systemmanager.rainbow.vaguerule.VagueNameMatchUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PermissionFeatureXmlParse {
    private static final String PACKAGE_NAME_ATTR = "packageName";
    private static final String PACKAGE_TAG = "package";
    private static final String PERMISSION_FEATURE_XML_FILE_NAME = "cloud/permission/permission.xml";
    private static final String SUB_PERMISSION_ATTR = "name";
    private static final String SUB_PERMISSION_TAG = "subPermission";
    private static final String TAG = "PermissionFeatureXmlParse";

    public static void initPermissionXml(Context context) {
        try {
            parseRootElement(context, XmlParsers.assetXmlRootElement(context, PERMISSION_FEATURE_XML_FILE_NAME));
        } catch (XmlParserException ex) {
            HwLog.e(TAG, ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, ex2.getMessage());
        }
    }

    private static void parseRootElement(Context context, Element element) {
        if (element == null) {
            HwLog.e(TAG, "The root element is null");
            return;
        }
        List<ContentValues> contentValuesList = new ArrayList();
        ArrayList<ContentValues> vagueContentValuesList = new ArrayList();
        NodeList nodeList = element.getElementsByTagName("package");
        int size = nodeList.getLength();
        for (int i = 0; i < size; i++) {
            Element packageElement = (Element) nodeList.item(i);
            String pkgName = packageElement.getAttribute("packageName");
            NodeList permissionNodeList = packageElement.getElementsByTagName(SUB_PERMISSION_TAG);
            int permissionSize = permissionNodeList.getLength();
            boolean vagueName = VagueNameMatchUtil.isVaguePkgName(pkgName);
            for (int j = 0; j < permissionSize; j++) {
                Element permissionElement = (Element) permissionNodeList.item(j);
                ContentValues contentValues = GFeatureCvt.cvtToStdContentValue(pkgName, permissionElement.getAttribute("name"), permissionElement.getTextContent());
                if (contentValues != null) {
                    if (vagueName) {
                        vagueContentValuesList.add(new ContentValues(contentValues));
                    } else {
                        contentValuesList.add(new ContentValues(contentValues));
                    }
                }
            }
            List<ContentValues> def = new ArrayList();
            def.add(GFeatureCvt.cvtToStdContentValue(pkgName, "31", String.valueOf(1)));
            List<ContentValues> re = GFeatureCvt.getDefaultContentValues(contentValuesList, def);
            if (re != null && re.size() > 0) {
                contentValuesList.addAll(re);
            }
            List<ContentValues> r = GFeatureCvt.getDefaultContentValues(vagueContentValuesList, def);
            if (r != null && r.size() > 0) {
                vagueContentValuesList.addAll(r);
            }
        }
        insertXmlDataIntoTable(context, contentValuesList, vagueContentValuesList);
    }

    private static void insertXmlDataIntoTable(Context context, List<ContentValues> precisionList, List<ContentValues> vagueList) {
        if (context != null) {
            if (!(precisionList == null || precisionList.isEmpty())) {
                context.getContentResolver().bulkInsert(CloudProviderUtils.generateGFeatureUri("CloudPermission"), (ContentValues[]) precisionList.toArray(new ContentValues[precisionList.size()]));
            }
            if (!(vagueList == null || vagueList.isEmpty())) {
                context.getContentResolver().bulkInsert(CloudProviderUtils.generateGFeatureUri("CloudVaguePermission"), (ContentValues[]) vagueList.toArray(new ContentValues[vagueList.size()]));
            }
        }
    }
}

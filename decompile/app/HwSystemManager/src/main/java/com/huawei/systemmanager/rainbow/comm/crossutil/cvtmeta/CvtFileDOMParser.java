package com.huawei.systemmanager.rainbow.comm.crossutil.cvtmeta;

import android.content.Context;
import android.util.SparseArray;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.util.HwLog;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class CvtFileDOMParser {
    private static final String ASSERT_PATH = "cloud/cvt/CloudCvtPermission.xml";
    private static final String CLOUD_ID_KEY = "cloudId";
    private static final String ITEM_TAG_KEY = "Item";
    private static final String MASK_KEY = "permissionMask";
    private static final String NAME_KEY = "name";
    private static final String PERMISSION_TAG_KEY = "androidPermission";
    private static final String POWER_KEY = "power";
    private static final String TAG = CvtFileDOMParser.class.getSimpleName();
    private SparseArray<PermissionCvtData> mDataList = new SparseArray();

    CvtFileDOMParser() {
    }

    public void parseXml(Context ctx) {
        NodeList itemList = XmlParsers.assetXmlRootElement(ctx, ASSERT_PATH).getElementsByTagName(ITEM_TAG_KEY);
        for (int i = 0; i < itemList.getLength(); i++) {
            Element element = (Element) itemList.item(i);
            int itemId = Integer.parseInt(element.getAttribute(CLOUD_ID_KEY));
            PermissionCvtData data = new PermissionCvtData(itemId, Integer.parseInt(element.getAttribute(MASK_KEY)), Integer.parseInt(element.getAttribute("power")));
            parsePermission(element, data);
            if (data.valid()) {
                this.mDataList.put(itemId, data);
            } else {
                HwLog.w(TAG, "parseXml invalid item: " + data);
            }
        }
    }

    public boolean subOfPermissionSet(int itemId, Set<String> applySet) {
        PermissionCvtData data = (PermissionCvtData) this.mDataList.get(itemId);
        if (data != null) {
            return data.subOfPermissionSet(applySet);
        }
        return false;
    }

    public int getPermissionConfigType(int itemId, int pCode, int pCfg) {
        int mask = ((PermissionCvtData) this.mDataList.get(itemId)).mPermissionMask;
        if ((mask & pCode) == 0) {
            return 2;
        }
        if ((mask & pCfg) == 0) {
            return 0;
        }
        return 1;
    }

    private void parsePermission(Element element, PermissionCvtData data) {
        NodeList pList = element.getElementsByTagName(PERMISSION_TAG_KEY);
        for (int i = 0; i < pList.getLength(); i++) {
            data.appendPermission(((Element) pList.item(i)).getAttribute("name"));
        }
    }
}

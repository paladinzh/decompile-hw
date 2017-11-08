package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import java.util.Map.Entry;

public class VersionMapXmlParse extends MapXmlParseHelper {
    private static final String LOG_TAG = "VersionMapXmlParse";
    private static final String VERSION_XML_ATTR_KEY_NAME = "name";
    private static final String VERSION_XML_ATTR_VALUE_NAME = "value";
    private static final String VERSION_XML_FILENAME = "cloud/version/version.xml";
    private static final String VERSION_XML_TAG_NAME = "version";
    private Context mContext = null;

    public VersionMapXmlParse(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    protected String getAssertFileName() {
        return VERSION_XML_FILENAME;
    }

    protected Uri getInsertTableUri() {
        return null;
    }

    protected String getColumnKeyName() {
        return null;
    }

    protected String getColumnValueName() {
        return null;
    }

    protected String getTagName() {
        return "version";
    }

    protected String getAttrKeyName() {
        return "name";
    }

    protected String getAttrValueName() {
        return "value";
    }

    protected void dealWithAppMap(Map<String, String> pkgMap) {
        if (pkgMap == null || pkgMap.isEmpty()) {
            HwLog.e(LOG_TAG, "pkgMap info is null!");
            return;
        }
        LocalSharedPrefrenceHelper sharedService = new LocalSharedPrefrenceHelper(this.mContext);
        try {
            for (Entry<String, String> entry : pkgMap.entrySet()) {
                sharedService.putLong((String) entry.getKey(), Long.parseLong((String) entry.getValue()));
            }
        } catch (NumberFormatException ex) {
            HwLog.e(LOG_TAG, "NumberFormatException is: " + ex.getMessage());
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "Exception is: " + e.getMessage());
        }
    }

    public void updateFeatureVersionFlag() {
        Map<String, String> versionMap = initMapXml(this.mContext, getAssertFileName(), getTagName(), getAttrKeyName(), getAttrValueName());
        if (versionMap == null || versionMap.isEmpty()) {
            HwLog.e(LOG_TAG, "updateFeatureVersionFlag with versionMap invalid!");
            return;
        }
        LocalSharedPrefrenceHelper sharedService = new LocalSharedPrefrenceHelper(this.mContext);
        try {
            for (Entry<String, String> entry : versionMap.entrySet()) {
                String featureName = (String) entry.getKey();
                long featrueVersion = Long.parseLong((String) entry.getValue());
                String updateFlag = XmlParseConst.getUpdateFlagForSpfKey(featureName);
                sharedService.putBoolean(updateFlag, false);
                if (featrueVersion > sharedService.getLong(featureName, -1)) {
                    sharedService.putBoolean(updateFlag, true);
                    sharedService.putLong(featureName, featrueVersion);
                }
            }
        } catch (NumberFormatException ex) {
            HwLog.e(LOG_TAG, "NumberFormatException is: " + ex.getMessage());
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "Exception is: " + e.getMessage());
        }
    }
}

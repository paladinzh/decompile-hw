package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeBlackList;

public class ControlBlackAppListXmlParse extends SimpleXmlParseHelper {
    public ControlBlackAppListXmlParse(Context context) {
        super(context);
    }

    protected String getAssertFileName() {
        return "cloud/simple/control_black.xml";
    }

    protected Uri getInsertTableUri() {
        return ControlRangeBlackList.CONTENT_OUTERTABLE_URI;
    }

    protected String getFeatureVersionName() {
        return CloudReqVerSpfKeys.CONTROL_BLACK_LIST_VERSION_SPF;
    }
}

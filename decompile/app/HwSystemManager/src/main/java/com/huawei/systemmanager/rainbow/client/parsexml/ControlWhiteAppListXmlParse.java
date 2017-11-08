package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;

public class ControlWhiteAppListXmlParse extends SimpleXmlParseHelper {
    public ControlWhiteAppListXmlParse(Context context) {
        super(context);
    }

    protected String getAssertFileName() {
        return "cloud/simple/control_white.xml";
    }

    protected Uri getInsertTableUri() {
        return ControlRangeWhiteList.CONTENT_OUTERTABLE_URI;
    }

    protected String getFeatureVersionName() {
        return CloudReqVerSpfKeys.CONTROL_WHITE_LIST_VERSION_SPF;
    }
}

package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackgroundValues;

public class BackgroundXmlParse extends SimpleXmlParseHelper {
    public BackgroundXmlParse(Context context) {
        super(context);
    }

    protected String getAssertFileName() {
        return "cloud/simple/background.xml";
    }

    protected Uri getInsertTableUri() {
        return BackgroundValues.CONTENT_OUTERTABLE_URI;
    }

    protected String getFeatureVersionName() {
        return CloudReqVerSpfKeys.BACKGROUND_LIST_VERSION_SPF;
    }
}

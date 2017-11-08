package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PushBlackList;

public class PushappXmlParse extends SimpleXmlParseHelper {
    public PushappXmlParse(Context context) {
        super(context);
    }

    protected String getAssertFileName() {
        return "cloud/simple/push.xml";
    }

    protected Uri getInsertTableUri() {
        return PushBlackList.CONTENT_OUTERTABLE_URI;
    }

    protected String getFeatureVersionName() {
        return CloudReqVerSpfKeys.PUSH_LIST_VERSION_SPF;
    }
}

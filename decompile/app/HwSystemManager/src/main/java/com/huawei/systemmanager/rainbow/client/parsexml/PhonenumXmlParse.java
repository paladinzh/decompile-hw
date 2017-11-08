package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PhoneNumberList;

public class PhonenumXmlParse extends SimpleXmlParseHelper {
    public PhonenumXmlParse(Context context) {
        super(context);
    }

    protected String getAssertFileName() {
        return "cloud/simple/phone.xml";
    }

    protected Uri getInsertTableUri() {
        return PhoneNumberList.CONTENT_OUTERTABLE_URI;
    }

    protected String getFeatureVersionName() {
        return CloudReqVerSpfKeys.PHONE_LIST_VERSION_SPF;
    }
}

package com.android.mms.transaction;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.android.mms.HwCustMmsConfigImpl;

public class HwCustWapPushReceiverImpl extends HwCustWapPushReceiver {
    public void handleSlWapPushMessageOpenBrowser(Context context, Intent intent, WapPushMsg pushMsg) {
        if (HwCustMmsConfigImpl.getEnableSlWapPushMessageOpenBrowser() && "application/vnd.wap.slc".equals(intent.getType())) {
            String url = pushMsg.getAttributeValueString(1);
            if (!TextUtils.isEmpty(url)) {
                Intent it = new Intent();
                it.setAction("android.intent.action.VIEW");
                it.addFlags(268435456);
                it.setData(Uri.parse(url));
                context.startActivity(it);
            }
        }
    }
}

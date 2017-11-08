package com.android.contacts.hap.rcs.list;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.list.ContactsRequest;
import com.autonavi.amap.mapcore.VTMCDataCache;

public class RcsContactsIntentResolver {
    private boolean isRcsOn = EmuiFeatureManager.isRcsFeatureEnable();

    public void resolveIntent(Intent intent, ContactsRequest request, Activity mContext) {
        if (this.isRcsOn) {
            String action = intent.getAction();
            if (TextUtils.equals("com.huawei.contacts.rcs.ACTION_REDIRECT", action)) {
                String actionExtra = intent.getStringExtra("Action");
                if (!TextUtils.isEmpty(actionExtra)) {
                    intent.setAction(actionExtra);
                    action = actionExtra;
                }
            }
            if ("android.intent.action.SEND".equals(action) || "android.intent.action.SEND_MULTIPLE".equals(action)) {
                if (!"vnd.android.cursor.dir/contact".equals(intent.resolveType(mContext))) {
                    request.setActionCode(60);
                } else if (intent.getBooleanExtra("com.huawei.android.contacts.SelectAndCreate", false)) {
                    request.setActionCode(70);
                } else {
                    request.setActionCode(60);
                }
            }
        }
    }

    public boolean isRcsContact4MsgMimeType(String type) {
        return this.isRcsOn ? "vnd.android.cursor.item/rcs_contacts_for_message".equals(type) : false;
    }

    public void setHAPActionCode(ContactsRequest aRequest) {
        aRequest.setActionCode(VTMCDataCache.MAX_EXPIREDTIME);
    }

    public boolean isAnyMimeType(String type) {
        if (this.isRcsOn) {
            return type.equals("*/*");
        }
        return false;
    }

    public void setFtVcardRequest(ContactsRequest request) {
        if (this.isRcsOn) {
            request.setActionCode(60);
        }
    }
}

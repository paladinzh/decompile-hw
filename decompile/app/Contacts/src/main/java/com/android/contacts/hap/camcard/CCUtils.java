package com.android.contacts.hap.camcard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.bcr.CCSaveService;

public class CCUtils {
    public static final Uri CAMCARD_URL = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "camcard");
    public static final boolean REQUEST_PRECISE_ENABLE = EmuiFeatureManager.isChinaArea();

    public static Intent createCCActivityIntent() {
        return new Intent("com.huawei.android.intent.action.CAMCARD_CONTACT");
    }

    public static void startCCardActivity(Activity activity) {
        if (activity != null) {
            activity.startService(CCSaveService.createMultiUpdateIntent(activity));
            activity.startActivity(createCCActivityIntent());
        }
    }

    public static Intent createLangDialogActivityIntent() {
        Intent intent = new Intent();
        intent.setPackage("com.huawei.contactscamcard");
        intent.setClassName("com.huawei.contactscamcard", "com.huawei.contactscamcard.LangDialogActivity");
        return intent;
    }
}

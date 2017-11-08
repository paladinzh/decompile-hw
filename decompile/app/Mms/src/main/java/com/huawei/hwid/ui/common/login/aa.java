package com.huawei.hwid.ui.common.login;

import java.io.File;
import java.io.FilenameFilter;

/* compiled from: PrivacyPolicyActivity */
class aa implements FilenameFilter {
    final /* synthetic */ String a;
    final /* synthetic */ PrivacyPolicyActivity b;

    aa(PrivacyPolicyActivity privacyPolicyActivity, String str) {
        this.b = privacyPolicyActivity;
        this.a = str;
    }

    public boolean accept(File file, String str) {
        return str.startsWith(this.a) && str.endsWith(".html");
    }
}

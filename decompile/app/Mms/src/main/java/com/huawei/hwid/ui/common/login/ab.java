package com.huawei.hwid.ui.common.login;

import java.io.File;
import java.io.FilenameFilter;

/* compiled from: PrivacyPolicyActivity */
class ab implements FilenameFilter {
    final /* synthetic */ String a;
    final /* synthetic */ String b;
    final /* synthetic */ PrivacyPolicyActivity c;

    ab(PrivacyPolicyActivity privacyPolicyActivity, String str, String str2) {
        this.c = privacyPolicyActivity;
        this.a = str;
        this.b = str2;
    }

    public boolean accept(File file, String str) {
        return str.startsWith(this.a) && str.endsWith(this.b);
    }
}

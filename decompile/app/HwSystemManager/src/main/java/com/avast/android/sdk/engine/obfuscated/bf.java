package com.avast.android.sdk.engine.obfuscated;

import android.text.TextUtils;
import com.avast.android.sdk.engine.UrlCheckResultStructure;
import com.avast.android.sdk.engine.UrlCheckResultStructure.UrlCheckResult;
import com.avast.android.sdk.shield.webshield.ScannedUrlAction;
import java.util.List;

/* compiled from: Unknown */
public final class bf {
    private final String a;
    private final ScannedUrlAction b;
    private final List<UrlCheckResultStructure> c;

    public bf(String str, ScannedUrlAction scannedUrlAction, List<UrlCheckResultStructure> list) {
        this.a = str;
        this.b = scannedUrlAction;
        this.c = list;
    }

    public String a() {
        return this.a;
    }

    public boolean b() {
        return this.b == ScannedUrlAction.TYPOSQUATTING_AUTOCORRECT;
    }

    public String c() {
        if (this.c == null || this.c.isEmpty()) {
            return null;
        }
        for (UrlCheckResultStructure urlCheckResultStructure : this.c) {
            if (UrlCheckResult.RESULT_TYPO_SQUATTING.equals(urlCheckResultStructure.result) && !TextUtils.isEmpty(urlCheckResultStructure.desiredSite)) {
                return urlCheckResultStructure.desiredSite;
            }
        }
        return null;
    }
}

package com.avast.android.sdk.engine.internal;

import java.io.File;
import java.io.FilenameFilter;

/* compiled from: Unknown */
class r implements FilenameFilter {
    r() {
    }

    public boolean accept(File file, String str) {
        return str.endsWith(".apk");
    }
}

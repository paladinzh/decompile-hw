package com.avast.android.sdk.engine.internal;

import java.io.File;
import java.io.FilenameFilter;

/* compiled from: Unknown */
class s implements FilenameFilter {
    s() {
    }

    public boolean accept(File file, String str) {
        return str.endsWith(".so");
    }
}

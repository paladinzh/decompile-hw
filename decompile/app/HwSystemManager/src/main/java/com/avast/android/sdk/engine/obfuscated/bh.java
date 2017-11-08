package com.avast.android.sdk.engine.obfuscated;

import java.io.File;
import java.io.IOException;

/* compiled from: Unknown */
public class bh {
    public static boolean a(File file) throws IOException {
        if (file.getParent() != null) {
            file = new File(file.getParentFile().getCanonicalFile(), file.getName());
        }
        return !file.getCanonicalFile().equals(file.getAbsoluteFile());
    }
}

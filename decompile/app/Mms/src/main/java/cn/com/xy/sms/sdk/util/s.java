package cn.com.xy.sms.sdk.util;

import java.io.File;
import java.io.FileFilter;

/* compiled from: Unknown */
public final class s implements FileFilter {
    private String a = "";
    private String b = "";

    public s(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public final boolean accept(File file) {
        String name = file.getName();
        return name.startsWith(this.a) && name.endsWith(this.b);
    }
}

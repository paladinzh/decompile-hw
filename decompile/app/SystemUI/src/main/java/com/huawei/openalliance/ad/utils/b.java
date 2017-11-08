package com.huawei.openalliance.ad.utils;

import com.huawei.openalliance.ad.utils.b.d;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class b {
    public static String a(String str) {
        String a = j.a(str, "img", "src");
        return (a != null && a.startsWith("file:///")) ? a.substring(8) : a;
    }

    public static void a(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                d.a("FileUtil", "IOException", e);
            }
        }
    }

    public static void a(String str, List<String> list) {
        if (!j.a(str)) {
            List arrayList = new ArrayList(4);
            if (list != null) {
                try {
                    for (String file : list) {
                        arrayList.add(new File(file).getCanonicalPath());
                    }
                } catch (Exception e) {
                    d.c("FileUtil", "get image canonical path fail");
                }
            }
            try {
                File file2 = new File(str);
                if (file2.isDirectory()) {
                    File[] listFiles = file2.listFiles();
                    if (listFiles != null && listFiles.length > 0) {
                        for (File file3 : listFiles) {
                            String canonicalPath = file3.getCanonicalPath();
                            if (!(file3.isDirectory() || arrayList.contains(canonicalPath))) {
                                a(file3);
                            }
                        }
                    }
                }
            } catch (Exception e2) {
                d.c("FileUtil", "delete image fail");
            }
        }
    }

    public static boolean a(File file) {
        File file2 = new File(file.getAbsolutePath() + System.currentTimeMillis());
        return !file.renameTo(file2) ? false : file2.delete();
    }

    public static boolean b(File file) {
        if (!file.exists()) {
            return false;
        }
        return !((file.length() > 0 ? 1 : (file.length() == 0 ? 0 : -1)) <= 0);
    }
}

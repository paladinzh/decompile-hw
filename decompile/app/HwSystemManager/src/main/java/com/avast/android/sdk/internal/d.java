package com.avast.android.sdk.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/* compiled from: Unknown */
public class d {

    /* compiled from: Unknown */
    public static class a {
        public boolean a(String str, String str2) {
            return false;
        }
    }

    public static void a(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            byte[] bArr = new byte[1024];
            while (true) {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                outputStream.write(bArr, 0, read);
            }
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    public static String[] a(File file, ZipFile zipFile, a aVar) throws IOException {
        List arrayList = new ArrayList();
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            if (!zipEntry.isDirectory()) {
                String[] split = zipEntry.getName().split(File.separator);
                String str = split[split.length - 1];
                if (aVar.a(split.length <= 1 ? null : split[split.length - 2], str)) {
                    File file2 = new File(file, str);
                    a(zipFile.getInputStream(zipEntry), new BufferedOutputStream(new FileOutputStream(file2, false)));
                    arrayList.add(file2.getName());
                }
            }
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }
}

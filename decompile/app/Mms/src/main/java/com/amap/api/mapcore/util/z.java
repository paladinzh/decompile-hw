package com.amap.api.mapcore.util;

import com.google.android.gms.location.places.Place;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* compiled from: FileCopy */
public class z {

    /* compiled from: FileCopy */
    public interface a {
        void a(String str, String str2);

        void a(String str, String str2, float f);

        void a(String str, String str2, int i);

        void b(String str, String str2);
    }

    public long a(File file, File file2, long j, long j2, a aVar) {
        Exception e;
        if (j == 0) {
            System.err.println("sizeOfDirectory is the total Size,  must be a positive number");
            if (aVar != null) {
                aVar.a("", "", -1);
            }
            return 0;
        }
        String absolutePath = file.getAbsolutePath();
        String absolutePath2 = file2.getAbsolutePath();
        try {
            if (file.isDirectory()) {
                if (!file2.exists()) {
                    if (!file2.mkdirs()) {
                        throw new IOException("Cannot create dir " + file2.getAbsolutePath());
                    }
                }
                String[] list = file.list();
                if (list != null) {
                    int i = 0;
                    long j3 = j;
                    while (i < list.length) {
                        try {
                            j3 = a(new File(file, list[i]), new File(file2, list[i]), j3, j2, aVar);
                            i++;
                        } catch (Exception e2) {
                            e = e2;
                            j = j3;
                        }
                    }
                    j = j3;
                }
                return j;
            }
            File parentFile = file2.getParentFile();
            if (parentFile != null) {
                if (!(parentFile.exists() || parentFile.mkdirs())) {
                    throw new IOException("Cannot create dir " + parentFile.getAbsolutePath());
                }
            }
            if (aVar != null) {
                if ((j > 0 ? 1 : null) == null) {
                    aVar.a(absolutePath, absolutePath2);
                }
            }
            InputStream fileInputStream = new FileInputStream(file);
            OutputStream fileOutputStream = new FileOutputStream(file2);
            byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read <= 0) {
                    break;
                }
                fileOutputStream.write(bArr, 0, read);
                j += (long) read;
                if (aVar != null) {
                    aVar.a(absolutePath, absolutePath2, a(j, j2));
                }
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            if (aVar != null) {
                if ((j < j2 - 1 ? 1 : null) == null) {
                    aVar.b(absolutePath, absolutePath2);
                }
            }
            return j;
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (aVar != null) {
                aVar.a(absolutePath, absolutePath2, -1);
            }
            return j;
        }
    }

    private float a(long j, long j2) {
        return (((float) j) / ((float) j2)) * 100.0f;
    }
}

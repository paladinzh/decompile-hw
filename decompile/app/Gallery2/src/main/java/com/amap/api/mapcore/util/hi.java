package com.amap.api.mapcore.util;

import android.content.Context;
import android.text.TextUtils;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/* compiled from: StatisticsEntity */
public class hi {
    private Context a;
    private String b;
    private String c;
    private String d;
    private String e;

    public hi(Context context, String str, String str2, String str3) throws ex {
        if (!TextUtils.isEmpty(str3) && str3.length() <= 256) {
            this.a = context.getApplicationContext();
            this.c = str;
            this.d = str2;
            this.b = str3;
            return;
        }
        throw new ex("无效的参数 - IllegalArgumentException");
    }

    public void a(String str) throws ex {
        if (!TextUtils.isEmpty(str) && str.length() <= HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT) {
            this.e = str;
            return;
        }
        throw new ex("无效的参数 - IllegalArgumentException");
    }

    public byte[] a(int i) {
        return new byte[]{(byte) ((byte) ((i >> 24) & 255)), (byte) ((byte) ((i >> 16) & 255)), (byte) ((byte) ((i >> 8) & 255)), (byte) ((byte) (i & 255))};
    }

    public byte[] b(String str) {
        if (TextUtils.isEmpty(str)) {
            return new byte[]{(byte) 0, (byte) 0};
        }
        byte length = (byte) (str.length() % 256);
        return new byte[]{(byte) ((byte) (str.length() / 256)), (byte) length};
    }

    public byte[] a() {
        ByteArrayOutputStream byteArrayOutputStream;
        Throwable th;
        byte[] bArr = new byte[0];
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                fi.a(byteArrayOutputStream, this.c);
                fi.a(byteArrayOutputStream, this.d);
                fi.a(byteArrayOutputStream, this.b);
                fi.a(byteArrayOutputStream, String.valueOf(fc.m(this.a)));
                new SimpleDateFormat("SSS").format(new Date());
                byteArrayOutputStream.write(a(Calendar.getInstance().get(14)));
                byteArrayOutputStream.write(b(this.e));
                byteArrayOutputStream.write(fi.a(this.e));
                byte[] toByteArray = byteArrayOutputStream.toByteArray();
                if (byteArrayOutputStream == null) {
                    return toByteArray;
                }
                try {
                    byteArrayOutputStream.close();
                    return toByteArray;
                } catch (Throwable th2) {
                    th2.printStackTrace();
                    return toByteArray;
                }
            } catch (Throwable th3) {
                th = th3;
                try {
                    fl.a(th, "StatisticsEntity", "toDatas");
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable th4) {
                            th4.printStackTrace();
                        }
                    }
                    return bArr;
                } catch (Throwable th5) {
                    th4 = th5;
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable th22) {
                            th22.printStackTrace();
                        }
                    }
                    throw th4;
                }
            }
        } catch (Throwable th6) {
            th4 = th6;
            byteArrayOutputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            throw th4;
        }
    }
}

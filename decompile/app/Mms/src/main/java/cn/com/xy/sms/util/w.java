package cn.com.xy.sms.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.net.a;
import cn.com.xy.sms.sdk.net.util.m;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.Random;
import java.util.UUID;

/* compiled from: Unknown */
public final class w {
    private static String a = null;
    private static char[] b = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static short c = (short) 20;
    private static String d = null;

    public static String a() {
        try {
            String deviceId = a.getDeviceId(false);
            return (deviceId == null || "".equals(deviceId.trim())) ? System.nanoTime() + String.valueOf(new Random().nextInt(1000000000)) : new StringBuilder(String.valueOf(deviceId)).append(System.nanoTime()).append(String.valueOf(new Random().nextInt(1000000000))).toString();
        } catch (Throwable th) {
            return "";
        }
    }

    private static String a(String str, String str2) {
        try {
            return (String) Class.forName("android.os.SystemProperties").getMethod("get", new Class[]{String.class, String.class}).invoke(null, new Object[]{str, null});
        } catch (Throwable th) {
            return null;
        }
    }

    public static String b() {
        if (!StringUtils.isNull(d)) {
            return d;
        }
        String deviceId = a.getDeviceId(true);
        if (StringUtils.isNull(deviceId)) {
            deviceId = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.UNIQUE_CODE);
            if (StringUtils.isNull(deviceId)) {
                deviceId = m.a(UUID.randomUUID().toString().toUpperCase());
                SysParamEntityManager.setParam(Constant.UNIQUE_CODE, deviceId);
            }
        }
        d = deviceId;
        return deviceId;
    }

    public static String c() {
        String str = "";
        try {
            return new UUID((long) ("35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.DISPLAY.length() % 10) + (Build.HOST.length() % 10) + (Build.ID.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10) + (Build.TAGS.length() % 10) + (Build.TYPE.length() % 10) + (Build.USER.length() % 10)).hashCode(), (long) Build.class.getField("SERIAL").get(null).toString().hashCode()).toString();
        } catch (Throwable th) {
            return "";
        }
    }

    public static String d() {
        String str = "microfountain";
        try {
            Context context = Constant.getContext();
            if (context != null) {
                str = Secure.getString(context.getContentResolver(), "android_id");
            }
        } catch (Throwable th) {
        }
        return str;
    }

    private static String e() {
        if (a != null) {
            return a;
        }
        String a = a("ro.aliyun.clouduuid", null);
        a = a;
        if (a == null || a.trim().length() == 0) {
            a = a("ro.sys.aliyun.clouduuid", null);
        }
        return a;
    }

    private static String f() {
        int i = 1;
        char[] cArr = new char[c];
        long currentTimeMillis = (System.currentTimeMillis() - 936748800000L) >> 1;
        for (int i2 = 7; i2 > 0; i2--) {
            cArr[i2] = (char) b[(int) (currentTimeMillis % 36)];
            currentTimeMillis /= 36;
        }
        cArr[0] = (char) b[((int) (currentTimeMillis % 26)) + 10];
        UUID randomUUID = UUID.randomUUID();
        long mostSignificantBits = randomUUID.getMostSignificantBits() ^ randomUUID.getLeastSignificantBits();
        if (mostSignificantBits < 0) {
            i = 0;
        }
        if (i == 0) {
            mostSignificantBits = -mostSignificantBits;
        }
        long j = mostSignificantBits;
        for (short s = (short) 8; s < c; s++) {
            cArr[s] = (char) b[(int) (j % 36)];
            j /= 36;
        }
        return new String(cArr);
    }
}

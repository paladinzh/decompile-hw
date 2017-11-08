package com.huawei.hwid.simchange.a;

import android.content.Context;
import com.huawei.hwid.core.c.b.a;
import java.lang.reflect.InvocationTargetException;

/* compiled from: MTKMutilSim */
public class b implements a {
    private Object a = b();

    public boolean a() {
        return true;
    }

    public String a(Context context, int i) {
        String str;
        String str2 = "";
        try {
            str = (String) Class.forName("com.mediatek.telephony.TelephonyManagerEx").getMethod("getSubscriberId", new Class[]{Integer.TYPE}).invoke(this.a, new Object[]{Integer.valueOf(i)});
        } catch (ClassNotFoundException e) {
            a.a("MTKMutilSim", "getSubscriberId ClassNotFoundException exception:");
            str = str2;
        } catch (NoSuchMethodException e2) {
            a.a("MTKMutilSim", "getSubscriberId NoSuchMethodException exception:");
            str = str2;
        } catch (IllegalAccessException e3) {
            a.a("MTKMutilSim", "getSubscriberId IllegalAccessException exception:");
            str = str2;
        } catch (IllegalArgumentException e4) {
            a.a("MTKMutilSim", "getSubscriberId IllegalArgumentException exception:");
            str = str2;
        } catch (InvocationTargetException e5) {
            a.a("MTKMutilSim", "getSubscriberId InvocationTargetException exception:");
            str = str2;
        }
        if (str != null) {
            return str;
        }
        return "";
    }

    public int b(Context context, int i) {
        try {
            return ((Integer) Class.forName("com.mediatek.telephony.TelephonyManagerEx").getMethod("getSimState", new Class[]{Integer.TYPE}).invoke(this.a, new Object[]{Integer.valueOf(i)})).intValue();
        } catch (ClassNotFoundException e) {
            a.a("MTKMutilSim", "getSimState ClassNotFoundException exception:");
            return -1;
        } catch (NoSuchMethodException e2) {
            a.a("MTKMutilSim", "getSimState NoSuchMethodException exception:");
            return -1;
        } catch (IllegalAccessException e3) {
            a.a("MTKMutilSim", "getSimState IllegalAccessException exception:");
            return -1;
        } catch (IllegalArgumentException e4) {
            a.a("MTKMutilSim", "getSimState IllegalArgumentException exception:");
            return -1;
        } catch (InvocationTargetException e5) {
            a.a("MTKMutilSim", "getSimState InvocationTargetException exception:");
            return -1;
        }
    }

    private Object b() {
        Object obj = null;
        try {
            Class cls = Class.forName("com.mediatek.telephony.TelephonyManagerEx");
            obj = cls.getDeclaredMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
        } catch (Exception e) {
            a.a("MTKMutilSim", " getDegaultTelephonyManagerEx wrong ");
        }
        return obj;
    }
}

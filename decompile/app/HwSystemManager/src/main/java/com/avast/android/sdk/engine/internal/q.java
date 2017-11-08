package com.avast.android.sdk.engine.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.VpsClassLoaderFactory;
import com.avast.android.sdk.engine.internal.vps.ObjectStorage;
import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.internal.SystemUtils;
import com.avast.android.sdk.internal.d;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

/* compiled from: Unknown */
public class q {
    public static final String a = "dex";
    private static VpsClassLoaderFactory b = new p();
    private static final Map<c, Method> c = new HashMap();
    private static final String d = "i-4";
    private static final ClassLoader e = q.class.getClassLoader();
    private static final String f = "com.avast.android.mobilesecurity.vps.Interface";
    private static final Object g = new Object();
    private static ClassLoader h;
    private static Class<?> i;
    private static Object j;
    private static final Map<a, Boolean> k = new HashMap();

    /* compiled from: Unknown */
    public enum a {
        RESULT_OK,
        RESULT_UNKNOWN_FAILURE,
        RESULT_ALREADY_REGISTERED,
        RESULT_ERROR_APK,
        RESULT_SO_NOT_FOUND,
        RESULT_OLD_INTERFACE_VERSION,
        RESULT_DIFFERENT_NAMES
    }

    /* compiled from: Unknown */
    public enum b {
        RESULT_OK,
        RESULT_UNKNOWN_ERROR,
        RESULT_ALREADY_DEREGISTERED
    }

    /* compiled from: Unknown */
    public enum c {
        ACQUIRE_VPS_CONTEXT("acquireVpsContext"),
        RELEASE_VPS_CONTEXT("releaseVpsContext"),
        GET_VERSION("getVersion"),
        CONTAINS_LIBRARY("containsLibrary"),
        SCAN("scan"),
        CHECK_URL("checkUrl"),
        GET_VPS_INFORMATION("getVpsInformation"),
        SCAN_MESSAGE("scanMessage"),
        UNLOAD_VPS("unloadVps"),
        UPDATE_DETECTION_INFO_WITH_ACTION("updateActionOnInfectedFile"),
        GET_DETECTION_PREFIXES("getDetectionPrefixes"),
        GET_PRIVACY_INFORMATION("getPrivacyInformation"),
        CLOUD_SCAN("cloudScan");
        
        private static final Map<String, c> n = null;
        private final String o;

        static {
            n = new HashMap();
            Iterator it = EnumSet.allOf(c.class).iterator();
            while (it.hasNext()) {
                c cVar = (c) it.next();
                n.put(cVar.a(), cVar);
            }
        }

        private c(String str) {
            this.o = str;
        }

        public final String a() {
            return this.o;
        }
    }

    static {
        try {
            System.loadLibrary("avast-vps-interface");
        } catch (UnsatisfiedLinkError e) {
        }
        for (Object put : a.values()) {
            k.put(put, Boolean.valueOf(false));
        }
    }

    public static a a(Context context, String str, String[] strArr) {
        a b;
        synchronized (g) {
            b = b(context, str, strArr);
        }
        return b;
    }

    public static b a(Context context) {
        if (j == null) {
            return b.RESULT_ALREADY_DEREGISTERED;
        }
        try {
            Map hashMap = new HashMap();
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.b.CONTEXT_CONTEXT_ID.a()), context);
            a(context, c.UNLOAD_VPS, hashMap);
            if (NativeRegistration.a(j) != 0) {
                return b.RESULT_UNKNOWN_ERROR;
            }
            b();
            return b.RESULT_OK;
        } catch (UnsatisfiedLinkError e) {
            return b.RESULT_UNKNOWN_ERROR;
        }
    }

    private static Boolean a(byte[] bArr) {
        Boolean valueOf = Boolean.valueOf(false);
        try {
            if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                Short.valueOf((short) -1);
                int i = 4;
                while (i < bArr.length) {
                    int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                    i += 4;
                    if (bArr[(i + intValue) - 1] == (byte) -1) {
                        Boolean bool;
                        Short sh = (Short) al.a(bArr, null, Short.TYPE, i);
                        if (sh != null) {
                            switch (sh.shortValue()) {
                                case (short) 0:
                                    bool = (Boolean) al.a(bArr, null, Boolean.TYPE, i + 2);
                                    continue;
                                default:
                                    break;
                            }
                        }
                        bool = valueOf;
                        i += intValue;
                        valueOf = bool;
                    } else {
                        throw new IllegalArgumentException("Invalid payload length");
                    }
                }
                return valueOf;
            }
            throw new IllegalArgumentException("Invalid structure length");
        } catch (Throwable e) {
            ao.a("Exception parsing contains library result", e);
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Object a(Context context, c cVar, Map<Short, Object> map) {
        Object obj = 1;
        synchronized (g) {
            c[] values = c.values();
            for (Object obj2 : values) {
                if (c.get(obj2) == null) {
                    obj = null;
                    break;
                }
            }
            if (j == null || r0 == null) {
                a b = b(context, null, null);
                if (!a.RESULT_OK.equals(b)) {
                    if (!((Boolean) k.get(b)).booleanValue()) {
                        ao.d("[ContextIDIssue] VPS registration failed with result " + b);
                        k.put(b, Boolean.valueOf(true));
                    }
                }
            }
        }
    }

    public static String a() {
        return d;
    }

    private static String a(Context context, File file) {
        try {
            String str;
            AssetManager assets = context.getAssets();
            String[] list = assets.list("");
            for (int i = 0; i < list.length; i++) {
                if (list[i].endsWith(".apk")) {
                    str = list[i];
                    break;
                }
            }
            str = null;
            if (str != null) {
                File file2 = new File(file, str);
                d.a(new BufferedInputStream(assets.open(str)), new FileOutputStream(file2));
                str = SystemUtils.a();
                ZipFile zipFile = new ZipFile(file2);
                String[] a = d.a(file, zipFile, new t(str));
                if (a == null || a.length == 0) {
                    ao.a("VPS native library for the given cpu architecture not found.");
                    d.a(file, zipFile, new u());
                }
                zipFile.close();
                ao.a("Unpacked VPS is at " + file2.getName());
                return file2.getName();
            }
            ao.a("Can't find internal VPS");
            return null;
        } catch (Throwable e) {
            ao.d("[ContextIDIssue] IOException during VPS unpack", e);
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressLint({"NewApi"})
    public static a b(Context context, String str, String[] strArr) {
        c[] values;
        int i;
        int i2;
        int i3 = 0;
        if (j != null) {
            values = c.values();
            i = 1;
            for (Object obj : values) {
                if (c.get(obj) == null) {
                    i = 0;
                }
            }
            if (i != 0) {
                return a.RESULT_ALREADY_REGISTERED;
            }
        }
        i = 2;
        int i4 = 0;
        String str2 = str;
        while (true) {
            int i5 = i - 1;
            if (i != 0) {
                if (str2 == null || str2.equals("")) {
                    str2 = b(context);
                    if (str2 == null) {
                        ao.a("Can't find any VPS .apk file");
                        return a.RESULT_ERROR_APK;
                    }
                }
                h = c().createVpsClassLoader(context.getDir(a, 0).getAbsolutePath() + File.separator + str2, context.getDir(a, 0).getAbsolutePath(), e);
                i = h.loadClass(f);
                String str3 = "";
                try {
                    Constructor constructor = i.getConstructor(new Class[]{Map.class});
                    Map hashMap = new HashMap();
                    hashMap.put(com.avast.android.sdk.engine.internal.vps.a.c.CONTEXT_CONTEXT_ID.a(), context);
                    hashMap.put(com.avast.android.sdk.engine.internal.vps.a.c.VPS_PATH_STRING_ID.a(), context.getDir(a, 0).getAbsolutePath() + File.separator + str2);
                    hashMap.put(com.avast.android.sdk.engine.internal.vps.a.c.OBJECT_STORAGE_CLASS_ID.a(), ObjectStorage.class);
                    hashMap.put(com.avast.android.sdk.engine.internal.vps.a.c.SDK_API_KEY_STRING_ID.a(), EngineInterface.getEngineConfig().getApiKey());
                    j = constructor.newInstance(new Object[]{hashMap});
                    hashMap.clear();
                    String str4 = new String((byte[]) i.getMethod(c.GET_VERSION.a(), new Class[]{Map.class}).invoke(j, new Object[]{hashMap}));
                    values = c.values();
                    for (c a : values) {
                        i.getMethod(a.a(), new Class[]{Map.class});
                    }
                    i2 = 0;
                    if (i2 == 0 && r2.equals(a())) {
                        break;
                    } else if (i4 == 0) {
                        h = null;
                        i = null;
                        j = null;
                        File[] listFiles = context.getDir(a, 0).listFiles();
                        i2 = 0;
                        while (i2 < listFiles.length) {
                            if (listFiles[i2].delete()) {
                                i2++;
                            } else {
                                ao.a("Couldn't delete " + listFiles[i2]);
                                return a.RESULT_UNKNOWN_FAILURE;
                            }
                        }
                        i = i5;
                        i4 = 1;
                        str2 = null;
                    } else {
                        ao.a("Built-in VPS incompatible");
                        return a.RESULT_OLD_INTERFACE_VERSION;
                    }
                } catch (Throwable e) {
                    ao.a("ClassNotFoundException", e);
                    b();
                    return a.RESULT_DIFFERENT_NAMES;
                } catch (Throwable e2) {
                    ao.a("InstantiationException", e2);
                    b();
                    return a.RESULT_UNKNOWN_FAILURE;
                } catch (Throwable e22) {
                    ao.a("IllegalAccessException", e22);
                    b();
                    return a.RESULT_UNKNOWN_FAILURE;
                } catch (Throwable e222) {
                    ao.a("SecurityException", e222);
                    b();
                    return a.RESULT_UNKNOWN_FAILURE;
                } catch (Throwable e2222) {
                    ao.a("NoSuchMethodException", e2222);
                    b();
                    return a.RESULT_DIFFERENT_NAMES;
                } catch (Throwable e22222) {
                    ao.a("IllegalArgumentException", e22222);
                    b();
                    return a.RESULT_OLD_INTERFACE_VERSION;
                } catch (Throwable e222222) {
                    ao.a("InvocationTargetException", e222222);
                    b();
                    return a.RESULT_OLD_INTERFACE_VERSION;
                } catch (Throwable e2222222) {
                    ao.a("UnsatisfiedLinkError", e2222222);
                    b();
                    return a.RESULT_UNKNOWN_FAILURE;
                }
            }
            break;
        }
        c[] values2 = c.values();
        while (i3 < values2.length) {
            c.put(values2[i3], i.getMethod(values2[i3].a(), new Class[]{Map.class}));
            i3++;
        }
        new HashMap().put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.b.CONTEXT_CONTEXT_ID.a()), context);
        if (!a((byte[]) i.getMethod(c.CONTAINS_LIBRARY.a(), new Class[]{Map.class}).invoke(j, new Object[]{new HashMap()})).booleanValue()) {
            return a.RESULT_OK;
        }
        if (strArr == null || strArr.length == 0) {
            strArr = c(context);
            if (strArr != null) {
                if (strArr.length != 0) {
                }
            }
            ao.a("VPS should contain a lib, but it was not found");
            return a.RESULT_SO_NOT_FOUND;
        }
        if (NativeRegistration.a(i, j, context.getDir(a, 0).getAbsolutePath(), strArr).equals(com.avast.android.sdk.engine.internal.NativeRegistration.a.REGISTRATION_OK)) {
            return a.RESULT_OK;
        }
        ao.a("Native registration failed");
        return a.RESULT_UNKNOWN_FAILURE;
    }

    public static String b(Context context) {
        File dir = context.getDir(a, 0);
        File[] listFiles = dir.listFiles(new r());
        if (listFiles == null || listFiles.length != 1) {
            return a(context, dir);
        }
        ao.a("Found VPS at " + listFiles[0].getAbsolutePath());
        return listFiles[0].getName();
    }

    private static void b() {
        j = null;
        c.clear();
    }

    private static VpsClassLoaderFactory c() {
        VpsClassLoaderFactory customVpsClassLoaderFactory = EngineInterface.getEngineConfig().getCustomVpsClassLoaderFactory();
        return customVpsClassLoaderFactory != null ? customVpsClassLoaderFactory : b;
    }

    private static String[] c(Context context) {
        int i = 0;
        List arrayList = new ArrayList();
        File[] listFiles = context.getDir(a, 0).listFiles(new s());
        if (listFiles != null && listFiles.length > 0) {
            int length = listFiles.length;
            while (i < length) {
                arrayList.add(listFiles[i].getName());
                i++;
            }
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }
}

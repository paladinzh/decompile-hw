package com.avast.android.sdk.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import com.avast.android.sdk.engine.MessageScanResultContainer.MessageScanResult;
import com.avast.android.sdk.engine.MessageScanResultContainer.MessageScanResultStructure;
import com.avast.android.sdk.engine.ScanResultStructure.ScanResult;
import com.avast.android.sdk.engine.UpdateResultStructure.UpdateResult;
import com.avast.android.sdk.engine.UrlCheckResultStructure.UrlCheckResult;
import com.avast.android.sdk.engine.internal.e;
import com.avast.android.sdk.engine.internal.f;
import com.avast.android.sdk.engine.internal.g;
import com.avast.android.sdk.engine.internal.l;
import com.avast.android.sdk.engine.internal.n;
import com.avast.android.sdk.engine.internal.q;
import com.avast.android.sdk.engine.internal.q.c;
import com.avast.android.sdk.engine.internal.v;
import com.avast.android.sdk.engine.internal.vps.a.b;
import com.avast.android.sdk.engine.internal.vps.a.d;
import com.avast.android.sdk.engine.internal.vps.a.j;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.bg;
import com.avast.android.sdk.internal.a;
import com.avast.android.sdk.internal.h;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* compiled from: Unknown */
public class EngineInterface {
    public static final int DEFAULT_WHITELIST_SECS = 900;
    public static final int FLAG_MESSAGE_SCAN_LOCAL = 1;
    public static final int FLAG_MESSAGE_SCAN_REMOTE = 2;
    public static final int FLAG_SCAN_ADDONS = 64;
    public static final int FLAG_SCAN_AUTOMATIC = 8;
    public static final int FLAG_SCAN_MALWARE = 32;
    public static final int FLAG_SCAN_MESSAGE_SHIELD = 16;
    public static final int FLAG_SCAN_ON_DEMAND = 1;
    public static final int FLAG_SCAN_ON_INSTALL = 2;
    public static final int FLAG_SCAN_REALTIME_SHIELD = 4;
    public static final int FLAG_SCAN_STORAGE_SHIELD_READ = 128;
    public static final int FLAG_SCAN_STORAGE_SHIELD_WRITE = 256;
    private static EngineConfig a;
    private static VpsInformation b;
    private static boolean c = false;
    private static a d;

    private EngineInterface() {
    }

    private static synchronized void a() {
        synchronized (EngineInterface.class) {
            if (c) {
            } else {
                throw new IllegalStateException("Engine was not yet initialized");
            }
        }
    }

    private static synchronized void a(Context context, EngineConfig engineConfig, boolean z) throws InvalidConfigException {
        synchronized (EngineInterface.class) {
            if (!z) {
                a();
            }
            if (engineConfig != null) {
                EngineConfig build = EngineConfig.newBuilder(engineConfig).build();
                new f(context).a(build);
                a = build;
                ao.a(a.getEngineLogger());
                bg.a(context, engineConfig);
                h.a(context).a(a.getGuid());
            } else {
                throw new IllegalArgumentException("Configuration can't be null");
            }
        }
    }

    public static Integer acquireVpsContextId(Context context) {
        a();
        d.a();
        return com.avast.android.sdk.engine.internal.a.a(context);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<UrlCheckResultStructure> checkUrl(Context context, Integer num, String str, UrlSource urlSource) {
        UrlCheckResultStructure urlCheckResultStructure = null;
        a();
        d.a();
        if (TextUtils.isEmpty(getEngineConfig().getUrlInfoApiKey())) {
            throw new UnsupportedOperationException("This operation is not supported without valid caller ID and API key");
        } else if (str != null) {
            Object obj;
            if (num != null && num.intValue() >= 0) {
                obj = null;
            } else {
                num = acquireVpsContextId(context);
                obj = 1;
            }
            if (num != null && num.intValue() >= 0) {
                try {
                    Map hashMap = new HashMap();
                    hashMap.put(Short.valueOf(b.STRUCTURE_VERSION_INT_ID.a()), UrlCheckResultStructure.getVersionCode());
                    hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
                    hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
                    hashMap.put(Short.valueOf(j.URL_STRING_ID.a()), str);
                    List<UrlCheckResultStructure> parseResultList = UrlCheckResultStructure.parseResultList((byte[]) q.a(context, c.CHECK_URL, hashMap));
                    if (parseResultList != null) {
                        if (!parseResultList.isEmpty()) {
                            switch (c.a[((UrlCheckResultStructure) parseResultList.get(0)).result.ordinal()]) {
                                case 1:
                                    List<UrlCheckResultStructure> a = n.a(context, num, str, urlSource);
                                    if (!(a == null || a.isEmpty())) {
                                        urlCheckResultStructure = (UrlCheckResultStructure) a.get(0);
                                    }
                                    if (!(urlCheckResultStructure == null || urlCheckResultStructure.result == UrlCheckResult.RESULT_OK || urlCheckResultStructure.result == UrlCheckResult.RESULT_UNKNOWN_ERROR)) {
                                        r0 = a;
                                        break;
                                    }
                                case 2:
                                case 3:
                                    r0 = n.a(context, num, str, urlSource);
                                    break;
                                default:
                            }
                        }
                    }
                    r0 = parseResultList;
                    if (obj != null) {
                        releaseVpsContextId(context, num.intValue());
                    }
                } catch (Throwable th) {
                    if (obj != null) {
                        releaseVpsContextId(context, num.intValue());
                    }
                }
            } else {
                r0 = null;
            }
            if (r0 == null) {
                r0 = n.a(context, num, str, urlSource);
            }
            return r0;
        } else {
            r0 = new LinkedList();
            r0.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_UNKNOWN_ERROR));
            return r0;
        }
    }

    public static Map<String, CloudScanResultStructure> cloudScan(Context context, Integer num, List<ApplicationInfo> list, List<File> list2) {
        Object obj;
        a();
        d.a();
        Map<String, CloudScanResultStructure> hashMap = new HashMap();
        if (num != null && num.intValue() >= 0) {
            obj = null;
        } else {
            num = acquireVpsContextId(context);
            obj = 1;
        }
        if (num == null || num.intValue() < 0) {
            return hashMap;
        }
        List linkedList = new LinkedList();
        if (list != null) {
            for (ApplicationInfo applicationInfo : list) {
                linkedList.add(applicationInfo.sourceDir);
            }
        }
        if (list2 != null) {
            for (File absolutePath : list2) {
                linkedList.add(absolutePath.getAbsolutePath());
            }
        }
        if (linkedList.isEmpty()) {
            return hashMap;
        }
        try {
            Map hashMap2 = new HashMap();
            hashMap2.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
            hashMap2.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
            hashMap2.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.COMMUNITY_IQ_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().getScanReportingEnabled()));
            hashMap2.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.a.FILES_TO_SCAN_LIST_OF_STRINGS_ID.a()), linkedList);
            byte[] bArr = (byte[]) q.a(context, c.CLOUD_SCAN, hashMap2);
            if (bArr != null) {
                hashMap.putAll(CloudScanResultStructure.a(linkedList, bArr));
                return hashMap;
            }
            if (obj != null) {
                releaseVpsContextId(context, num.intValue());
            }
            return null;
        } finally {
            if (obj != null) {
                releaseVpsContextId(context, num.intValue());
            }
        }
    }

    public static void confirmTypoSquattingAction(Context context, Integer num, String str, UrlCheckResultStructure urlCheckResultStructure, boolean z, boolean z2) {
        Integer num2;
        if (urlCheckResultStructure != null && urlCheckResultStructure.result != null && UrlCheckResult.RESULT_TYPO_SQUATTING.equals(urlCheckResultStructure.result)) {
            Object obj;
            if (num != null && num.intValue() >= 0) {
                obj = null;
                num2 = num;
            } else {
                num2 = acquireVpsContextId(context);
                obj = 1;
            }
            if (num2 != null && num2.intValue() >= 0) {
                try {
                    n.a(context, num2, str, urlCheckResultStructure, z, z2);
                    if (obj != null) {
                        releaseVpsContextId(context, num2.intValue());
                    }
                } catch (Throwable th) {
                    if (obj != null) {
                        releaseVpsContextId(context, num2.intValue());
                    }
                }
            }
        }
    }

    public static void detectedFileActionPerformed(Context context, Integer num, String str, String str2, DetectionAction detectionAction) {
        Object obj;
        a();
        d.a();
        if (num != null && num.intValue() >= 0) {
            obj = null;
        } else {
            num = acquireVpsContextId(context);
            obj = 1;
        }
        if (num != null && num.intValue() >= 0) {
            try {
                Map hashMap = new HashMap();
                hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
                hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
                hashMap.put(Short.valueOf(d.DETECTION_PACKAGE_NAME_STRING_ID.a()), str);
                hashMap.put(Short.valueOf(d.DETECTION_FILE_PATH_STRING_ID.a()), str2);
                hashMap.put(Short.valueOf(d.DETECTION_ACTION_SHORT_ID.a()), Short.valueOf(detectionAction.getId()));
                q.a(context, c.UPDATE_DETECTION_INFO_WITH_ACTION, hashMap);
            } finally {
                if (obj != null) {
                    releaseVpsContextId(context, num.intValue());
                }
            }
        }
    }

    public static synchronized EngineConfig getEngineConfig() {
        EngineConfig build;
        synchronized (EngineInterface.class) {
            a();
            build = EngineConfig.newBuilder(a).build();
        }
        return build;
    }

    public static PrivacyScanResult getPrivacyInformation(Context context, Integer num, String str, File file) {
        Object obj;
        a();
        d.a();
        if (num != null && num.intValue() >= 0) {
            obj = null;
        } else {
            num = acquireVpsContextId(context);
            obj = 1;
        }
        if (num == null || num.intValue() < 0) {
            return null;
        }
        try {
            Map hashMap = new HashMap();
            hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
            hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.f.PACKAGE_NAME_STRING_ID.a()), str);
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.f.FILE_FILE_ID.a()), file);
            byte[] bArr = (byte[]) q.a(context, c.GET_PRIVACY_INFORMATION, hashMap);
            if (bArr != null) {
                PrivacyScanResult a = PrivacyScanResult.a(bArr);
                return a;
            }
            if (obj != null) {
                releaseVpsContextId(context, num.intValue());
            }
            return null;
        } finally {
            if (obj != null) {
                releaseVpsContextId(context, num.intValue());
            }
        }
    }

    public static VpsInformation getVpsInformation(Context context, Integer num) {
        a();
        d.a();
        if (b != null) {
            return b;
        }
        Object obj;
        if (num != null && num.intValue() >= 0) {
            obj = null;
        } else {
            num = acquireVpsContextId(context);
            obj = 1;
        }
        if (num == null || num.intValue() < 0) {
            return null;
        }
        try {
            Map hashMap = new HashMap();
            hashMap.put(Short.valueOf(b.STRUCTURE_VERSION_INT_ID.a()), VpsInformation.getVersionCode());
            hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
            hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
            List parseResultList = VpsInformation.parseResultList((byte[]) q.a(context, c.GET_VPS_INFORMATION, hashMap));
            b = null;
            if (parseResultList != null) {
                if (parseResultList.size() > 0) {
                    b = (VpsInformation) parseResultList.get(0);
                    if (b != null) {
                        d.a(b.version);
                    }
                }
            }
            if (obj != null) {
                releaseVpsContextId(context, num.intValue());
            }
            return b;
        } catch (Throwable th) {
            if (obj != null) {
                releaseVpsContextId(context, num.intValue());
            }
        }
    }

    public static synchronized void init(Context context, EngineConfig engineConfig) throws InvalidConfigException {
        synchronized (EngineInterface.class) {
            if (c) {
                throw new IllegalStateException("Engine already initialized");
            }
            com.avast.android.sdk.internal.c.a(context);
            a(context, engineConfig, true);
            d = a.a(context, engineConfig);
            c = true;
        }
    }

    public static UpdateCheckResultStructure isUpdateAvailable(Context context) {
        a();
        d.a();
        return v.a(context);
    }

    public static void releaseVpsContextId(Context context, int i) {
        a();
        d.a();
        com.avast.android.sdk.engine.internal.a.a(context, i);
    }

    public static List<ScanResultStructure> scan(Context context, Integer num, File file, PackageInfo packageInfo, long j) {
        Object obj = 1;
        a();
        d.a();
        if ((1 & j) != 0) {
        }
        if ((2 & j) != 0) {
        }
        Object obj2 = (4 & j) != 0 ? 1 : null;
        if ((8 & j) != 0) {
        }
        if ((16 & j) != 0) {
        }
        Object obj3 = (32 & j) != 0 ? 1 : null;
        Object obj4 = (64 & j) != 0 ? 1 : null;
        Object obj5 = (128 & j) != 0 ? 1 : null;
        List<ScanResultStructure> a;
        ScanResultStructure scanResultStructure;
        List<ScanResultStructure> linkedList;
        List<ScanResultStructure> a2;
        Map hashMap;
        List parseResultList;
        List<ScanResultStructure> a3;
        if ((256 & j) != 0) {
            if (!(obj2 == null || packageInfo == null)) {
                a = com.avast.android.sdk.engine.internal.j.a(context, num, packageInfo);
                if (a != null) {
                    return a;
                }
            }
            if (file != null) {
                if (file.exists() && file.canRead()) {
                    if (file.length() == 0) {
                    }
                }
                scanResultStructure = new ScanResultStructure();
                scanResultStructure.result = ScanResult.RESULT_OK;
                linkedList = new LinkedList();
                linkedList.add(scanResultStructure);
                return linkedList;
            }
            if (obj5 != null) {
                a2 = com.avast.android.sdk.engine.internal.j.a(context, num, file);
                if (a2 != null) {
                    return a2;
                }
            }
            if (num != null && num.intValue() >= 0) {
                obj = null;
            } else {
                num = acquireVpsContextId(context);
            }
            if (num != null && num.intValue() >= 0) {
                try {
                    hashMap = new HashMap();
                    hashMap.put(Short.valueOf(b.STRUCTURE_VERSION_INT_ID.a()), ScanResultStructure.getVersionCode());
                    hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
                    hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.FILE_FILE_ID.a()), file);
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.FLAGS_LONG_ID.a()), Long.valueOf(j));
                    if (packageInfo == null) {
                        hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PACKAGE_NAME_STRING_ID.a()), (String) null);
                    } else {
                        hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PACKAGE_NAME_STRING_ID.a()), packageInfo.packageName);
                    }
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PUP_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().getScanPupsEnabled()));
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.LANGUAGE_STRING_ID.a()), Locale.getDefault().getLanguage());
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.GUID_STRING_ID.a()), getEngineConfig().getGuid());
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.COMMUNITY_IQ_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().getScanReportingEnabled()));
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.CLOUD_SCANNING_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().isFileCloudScanningEnabled()));
                    parseResultList = ScanResultStructure.parseResultList((byte[]) q.a(context, c.SCAN, hashMap));
                    if (packageInfo == null) {
                        if (obj3 != null && obj4 == null) {
                            e.a(packageInfo.packageName, parseResultList);
                        }
                        if (obj3 == null && obj4 != null) {
                            e.b(packageInfo.packageName, parseResultList);
                        }
                        if (!(obj3 == null || obj4 == null)) {
                            List a4 = com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList);
                            List b = com.avast.android.sdk.engine.internal.j.b(context, num, parseResultList);
                            e.a(packageInfo.packageName, a4);
                            e.b(packageInfo.packageName, b);
                        }
                    } else if (file != null) {
                        e.c(file.getAbsolutePath(), com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList));
                    }
                    if (obj != null) {
                        releaseVpsContextId(context, num.intValue());
                    }
                    if (parseResultList == null) {
                        parseResultList = new LinkedList();
                    }
                    a3 = com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList, file, packageInfo);
                    if (file != null) {
                        if (file.exists() && file.canRead()) {
                            if (file.length() == 0) {
                            }
                        }
                        a3.clear();
                        a3.add(new ScanResultStructure());
                    }
                    return a3;
                } catch (Throwable th) {
                    if (obj != null) {
                        releaseVpsContextId(context, num.intValue());
                    }
                }
            } else {
                ao.e("Invalid context during scan");
                scanResultStructure = new ScanResultStructure();
                scanResultStructure.result = ScanResult.RESULT_ERROR_SCAN_INVALID_CONTEXT;
                linkedList = new LinkedList();
                linkedList.add(scanResultStructure);
                return linkedList;
            }
        }
        a = com.avast.android.sdk.engine.internal.j.a(context, num, packageInfo);
        if (a != null) {
            return a;
        }
        if (file != null) {
            if (file.length() == 0) {
                scanResultStructure = new ScanResultStructure();
                scanResultStructure.result = ScanResult.RESULT_OK;
                linkedList = new LinkedList();
                linkedList.add(scanResultStructure);
                return linkedList;
            }
        }
        if (obj5 != null) {
            a2 = com.avast.android.sdk.engine.internal.j.a(context, num, file);
            if (a2 != null) {
                return a2;
            }
        }
        if (num != null) {
            obj = null;
            if (num != null) {
                hashMap = new HashMap();
                hashMap.put(Short.valueOf(b.STRUCTURE_VERSION_INT_ID.a()), ScanResultStructure.getVersionCode());
                hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
                hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.FILE_FILE_ID.a()), file);
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.FLAGS_LONG_ID.a()), Long.valueOf(j));
                if (packageInfo == null) {
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PACKAGE_NAME_STRING_ID.a()), packageInfo.packageName);
                } else {
                    hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PACKAGE_NAME_STRING_ID.a()), (String) null);
                }
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PUP_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().getScanPupsEnabled()));
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.LANGUAGE_STRING_ID.a()), Locale.getDefault().getLanguage());
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.GUID_STRING_ID.a()), getEngineConfig().getGuid());
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.COMMUNITY_IQ_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().getScanReportingEnabled()));
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.CLOUD_SCANNING_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().isFileCloudScanningEnabled()));
                parseResultList = ScanResultStructure.parseResultList((byte[]) q.a(context, c.SCAN, hashMap));
                if (packageInfo == null) {
                    e.a(packageInfo.packageName, parseResultList);
                    e.b(packageInfo.packageName, parseResultList);
                    List a42 = com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList);
                    List b2 = com.avast.android.sdk.engine.internal.j.b(context, num, parseResultList);
                    e.a(packageInfo.packageName, a42);
                    e.b(packageInfo.packageName, b2);
                } else if (file != null) {
                    e.c(file.getAbsolutePath(), com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList));
                }
                if (obj != null) {
                    releaseVpsContextId(context, num.intValue());
                }
                if (parseResultList == null) {
                    parseResultList = new LinkedList();
                }
                a3 = com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList, file, packageInfo);
                if (file != null) {
                    if (file.length() == 0) {
                        a3.clear();
                        a3.add(new ScanResultStructure());
                    }
                }
                return a3;
            }
            ao.e("Invalid context during scan");
            scanResultStructure = new ScanResultStructure();
            scanResultStructure.result = ScanResult.RESULT_ERROR_SCAN_INVALID_CONTEXT;
            linkedList = new LinkedList();
            linkedList.add(scanResultStructure);
            return linkedList;
        }
        num = acquireVpsContextId(context);
        if (num != null) {
            hashMap = new HashMap();
            hashMap.put(Short.valueOf(b.STRUCTURE_VERSION_INT_ID.a()), ScanResultStructure.getVersionCode());
            hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
            hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.FILE_FILE_ID.a()), file);
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.FLAGS_LONG_ID.a()), Long.valueOf(j));
            if (packageInfo == null) {
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PACKAGE_NAME_STRING_ID.a()), (String) null);
            } else {
                hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PACKAGE_NAME_STRING_ID.a()), packageInfo.packageName);
            }
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.PUP_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().getScanPupsEnabled()));
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.LANGUAGE_STRING_ID.a()), Locale.getDefault().getLanguage());
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.GUID_STRING_ID.a()), getEngineConfig().getGuid());
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.COMMUNITY_IQ_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().getScanReportingEnabled()));
            hashMap.put(Short.valueOf(com.avast.android.sdk.engine.internal.vps.a.h.CLOUD_SCANNING_ENABLED_BOOLEAN_ID.a()), Boolean.valueOf(getEngineConfig().isFileCloudScanningEnabled()));
            parseResultList = ScanResultStructure.parseResultList((byte[]) q.a(context, c.SCAN, hashMap));
            if (packageInfo == null) {
                e.a(packageInfo.packageName, parseResultList);
                e.b(packageInfo.packageName, parseResultList);
                List a422 = com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList);
                List b22 = com.avast.android.sdk.engine.internal.j.b(context, num, parseResultList);
                e.a(packageInfo.packageName, a422);
                e.b(packageInfo.packageName, b22);
            } else if (file != null) {
                e.c(file.getAbsolutePath(), com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList));
            }
            if (obj != null) {
                releaseVpsContextId(context, num.intValue());
            }
            if (parseResultList == null) {
                parseResultList = new LinkedList();
            }
            a3 = com.avast.android.sdk.engine.internal.j.a(context, num, parseResultList, file, packageInfo);
            if (file != null) {
                if (file.length() == 0) {
                    a3.clear();
                    a3.add(new ScanResultStructure());
                }
            }
            return a3;
        }
        ao.e("Invalid context during scan");
        scanResultStructure = new ScanResultStructure();
        scanResultStructure.result = ScanResult.RESULT_ERROR_SCAN_INVALID_CONTEXT;
        linkedList = new LinkedList();
        linkedList.add(scanResultStructure);
        return linkedList;
    }

    public static MessageScanResultContainer scanMessage(Context context, Integer num, MessageType messageType, String str, String str2, Map<String, File> map, int i) {
        Object obj;
        Integer num2;
        a();
        d.a();
        MessageScanResultContainer messageScanResultContainer = new MessageScanResultContainer();
        messageScanResultContainer.messageScanResults = new LinkedList();
        if (num != null && num.intValue() >= 0) {
            obj = null;
            num2 = num;
        } else {
            num2 = acquireVpsContextId(context);
            obj = 1;
        }
        if (num2 != null && num2.intValue() >= 0) {
            Object obj2;
            Object obj3;
            if ((i & 1) == 0) {
                obj2 = null;
            } else {
                int i2 = 1;
            }
            if ((i & 2) == 0) {
                obj3 = null;
            } else {
                int i3 = 1;
            }
            if (obj2 != null) {
                try {
                    messageScanResultContainer.messageScanResults = com.avast.android.sdk.engine.internal.h.a(context, num2, str, messageType, str2, map);
                    if (messageScanResultContainer.messageScanResults.size() < 1) {
                        messageScanResultContainer.messageScanResults.add(new MessageScanResultStructure(MessageScanResult.RESULT_UNKNOWN_ERROR, ""));
                    }
                    if (map != null) {
                        messageScanResultContainer.additionalFilesDetections = com.avast.android.sdk.engine.internal.h.a(context, num2, (Map) map);
                    }
                } catch (Throwable th) {
                    if (obj != null) {
                        releaseVpsContextId(context, num2.intValue());
                    }
                }
            }
            if (obj3 != null) {
                if (messageScanResultContainer.messageScanResults.size() < 1) {
                    if (obj2 == null) {
                        messageScanResultContainer.messageScanResults.add(new MessageScanResultStructure(MessageScanResult.RESULT_OK, ""));
                    } else {
                        messageScanResultContainer.messageScanResults.add(new MessageScanResultStructure(MessageScanResult.RESULT_UNKNOWN_ERROR, ""));
                    }
                }
                messageScanResultContainer.urlDetections = com.avast.android.sdk.engine.internal.h.a(context, num2, str2);
            }
            if (obj != null) {
                releaseVpsContextId(context, num2.intValue());
            }
            return messageScanResultContainer;
        }
        MessageScanResultStructure messageScanResultStructure = new MessageScanResultStructure();
        messageScanResultStructure.result = MessageScanResult.RESULT_ERROR_SCAN_INVALID_CONTEXT;
        messageScanResultContainer.messageScanResults.add(messageScanResultStructure);
        return messageScanResultContainer;
    }

    public static SubmitResult sendFalsePositive(Context context, File file, PackageInfo packageInfo, ScanResultStructure scanResultStructure, SubmitInformation submitInformation, ProgressObserver progressObserver) {
        a();
        d.a();
        SubmitResult a = g.a(context, null, file, packageInfo, scanResultStructure, submitInformation, progressObserver);
        if (SubmitResult.RESULT_DONE.equals(a)) {
            detectedFileActionPerformed(context, null, packageInfo == null ? null : packageInfo.packageName, file.getAbsolutePath(), DetectionAction.FP_REPORT);
        }
        return a;
    }

    public static synchronized void setEngineConfig(Context context, EngineConfig engineConfig) throws InvalidConfigException {
        synchronized (EngineInterface.class) {
            a(context, engineConfig, false);
        }
    }

    public static UpdateResultStructure update(Context context, ProgressObserver progressObserver) {
        a();
        d.a();
        UpdateResultStructure a = l.a(context, progressObserver);
        try {
            n.a(context, null);
        } catch (Exception e) {
        }
        if (UpdateResult.RESULT_UPDATED.equals(a.result)) {
            b = a.newVps;
            if (b != null) {
                d.a(b.version);
            }
            e.a();
        }
        return a;
    }

    public static void whitelistUrl(String str) {
        a();
        d.a();
        n.a(str, Integer.valueOf(DEFAULT_WHITELIST_SECS));
    }
}

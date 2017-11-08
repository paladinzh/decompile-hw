package com.avast.android.sdk.engine.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.avast.android.sdk.engine.ScanResultStructure.ScanResult;
import com.avast.android.sdk.engine.internal.c.b;
import com.avast.android.sdk.engine.internal.q.c;
import com.avast.android.sdk.engine.internal.vps.a;
import com.avast.android.sdk.engine.internal.vps.a.e;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* compiled from: Unknown */
public class j {
    public static List<ScanResultStructure> a(Context context, Integer num, PackageInfo packageInfo) {
        if (packageInfo == null) {
            return null;
        }
        List a = e.a(packageInfo.packageName);
        return a == null ? null : a(context, num, a, null, packageInfo);
    }

    public static List<c> a(Context context, Integer num, b bVar) {
        Object obj;
        Collection a = e.a(bVar);
        if (a != null && !a.isEmpty()) {
            return new LinkedList(a);
        }
        LinkedList linkedList = new LinkedList();
        if (num != null && num.intValue() >= 0) {
            obj = null;
        } else {
            num = a.a(context);
            obj = 1;
        }
        if (num == null || num.intValue() < 0) {
            return new LinkedList();
        }
        try {
            Map hashMap = new HashMap();
            hashMap.put(Short.valueOf(a.b.CONTEXT_CONTEXT_ID.a()), context);
            hashMap.put(Short.valueOf(a.b.CONTEXT_ID_INTEGER_ID.a()), num);
            hashMap.put(Short.valueOf(a.b.STRUCTURE_VERSION_INT_ID.a()), c.a());
            hashMap.put(Short.valueOf(e.DETECTION_PREFIX_GROUP_ENUM_STRING_ID.a()), bVar.name());
            Collection a2 = c.a((byte[]) q.a(context, c.GET_DETECTION_PREFIXES, hashMap));
            if (!(a2 == null || a2.isEmpty())) {
                e.a(bVar, new LinkedList(a2));
            }
            return a2;
        } finally {
            if (obj != null) {
                a.a(context, num.intValue());
            }
        }
    }

    public static List<ScanResultStructure> a(Context context, Integer num, File file) {
        if (file == null) {
            return null;
        }
        List b = e.b(file.getAbsolutePath());
        return b == null ? null : a(context, num, b, null, null);
    }

    public static List<ScanResultStructure> a(Context context, Integer num, List<ScanResultStructure> list) {
        return a((List) list, a(context, num, b.MALWARE));
    }

    @SuppressLint({"DefaultLocale"})
    public static List<ScanResultStructure> a(Context context, Integer num, List<ScanResultStructure> list, File file, PackageInfo packageInfo) {
        Iterator listIterator;
        ScanResultStructure scanResultStructure;
        if (packageInfo == null || context == null || !context.getApplicationContext().getPackageName().equals(packageInfo.packageName)) {
            if (list == null) {
                return list;
            }
            Iterator listIterator2;
            ScanResultStructure scanResultStructure2;
            if (!EngineInterface.getEngineConfig().getScanPupsEnabled()) {
                listIterator2 = list.listIterator();
                while (listIterator2.hasNext()) {
                    scanResultStructure2 = (ScanResultStructure) listIterator2.next();
                    if (scanResultStructure2.infectionType != null && scanResultStructure2.infectionType.toUpperCase().contains(" [PUP]")) {
                        listIterator2.remove();
                    }
                }
            }
            if (list.size() >= 2) {
                listIterator2 = list.listIterator();
                while (listIterator2.hasNext()) {
                    scanResultStructure2 = (ScanResultStructure) listIterator2.next();
                    if (scanResultStructure2 == null || scanResultStructure2.result == null) {
                        listIterator2.remove();
                    }
                }
                scanResultStructure2 = new ScanResultStructure();
                listIterator = list.listIterator();
                loop1:
                while (true) {
                    scanResultStructure = scanResultStructure2;
                    while (listIterator.hasNext()) {
                        scanResultStructure2 = (ScanResultStructure) listIterator.next();
                        if (scanResultStructure2.result.getResult() <= scanResultStructure.result.getResult()) {
                        }
                    }
                    break loop1;
                }
                listIterator = list.listIterator();
                while (listIterator.hasNext()) {
                    if (((ScanResultStructure) listIterator.next()).result.getResult() < scanResultStructure.result.getResult()) {
                        listIterator.remove();
                    }
                }
                Set hashSet;
                if (scanResultStructure.infectionType == null) {
                    hashSet = new HashSet();
                    listIterator = list.listIterator();
                    while (listIterator.hasNext()) {
                        scanResultStructure2 = (ScanResultStructure) listIterator.next();
                        if (hashSet.contains(scanResultStructure2.result)) {
                            listIterator.remove();
                        } else {
                            hashSet.add(scanResultStructure2.result);
                        }
                    }
                } else {
                    hashSet = new HashSet();
                    listIterator = list.listIterator();
                    while (listIterator.hasNext()) {
                        scanResultStructure2 = (ScanResultStructure) listIterator.next();
                        if (scanResultStructure2.infectionType == null) {
                            listIterator.remove();
                        } else if (hashSet.contains(scanResultStructure2.infectionType.toLowerCase())) {
                            listIterator.remove();
                        } else {
                            hashSet.add(scanResultStructure2.infectionType.toLowerCase());
                        }
                    }
                }
                if (list.size() < 1) {
                    list.add(new ScanResultStructure());
                }
                return list;
            }
            if (list.size() == 0) {
                list.add(new ScanResultStructure());
            }
            return list;
        } else if (list == null) {
            return list;
        } else {
            for (c cVar : a(context, num, b.ADDONS)) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    scanResultStructure = (ScanResultStructure) it.next();
                    if (!(scanResultStructure.infectionType == null || scanResultStructure.infectionType.toLowerCase().startsWith(cVar.a.toLowerCase()))) {
                        it.remove();
                    }
                }
            }
            if (list.isEmpty()) {
                list.add(new ScanResultStructure());
            }
            return list;
        }
    }

    private static List<ScanResultStructure> a(List<ScanResultStructure> list, List<c> list2) {
        List<ScanResultStructure> linkedList = new LinkedList();
        if (!(list == null || list.isEmpty())) {
            for (ScanResultStructure scanResultStructure : list) {
                for (c cVar : list2) {
                    if (scanResultStructure.infectionType != null && scanResultStructure.infectionType.toLowerCase().startsWith(cVar.a.toLowerCase())) {
                        linkedList.add(scanResultStructure);
                        break;
                    }
                }
            }
        }
        if (linkedList.isEmpty()) {
            linkedList.add(new ScanResultStructure(ScanResult.RESULT_OK, null));
        }
        return linkedList;
    }

    public static boolean a(Context context, ScanResultStructure scanResultStructure) {
        List linkedList = new LinkedList();
        linkedList.add(scanResultStructure);
        linkedList = b(context, null, linkedList);
        return !linkedList.isEmpty() && ((ScanResultStructure) linkedList.get(0)).equals(scanResultStructure);
    }

    public static List<ScanResultStructure> b(Context context, Integer num, List<ScanResultStructure> list) {
        return a((List) list, a(context, num, b.ADDONS));
    }
}

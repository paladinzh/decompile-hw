package com.avast.android.sdk.engine;

import android.util.SparseArray;
import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class CloudScanResultStructure {
    private CloudScanResult a = CloudScanResult.RESULT_UNKNOWN_ERROR;
    private String b = null;

    /* compiled from: Unknown */
    public enum CloudScanResult {
        RESULT_UNKNOWN_ERROR(0),
        RESULT_ERROR_PRIVATE_FILE(1),
        RESULT_OUTDATED_APPLICATION(2),
        RESULT_INCOMPATIBLE_VPS(3),
        RESULT_ERROR_SCAN_INVALID_CONTEXT(4),
        RESULT_ERROR_SCAN_INTERNAL_ERROR(5),
        RESULT_OK(100),
        RESULT_INCONCLUSIVE(150),
        RESULT_SUSPICIOUS(175),
        RESULT_INFECTED(200);
        
        private static final SparseArray<CloudScanResult> a = null;
        private final int b;

        static {
            a = new SparseArray();
            Iterator it = EnumSet.allOf(CloudScanResult.class).iterator();
            while (it.hasNext()) {
                CloudScanResult cloudScanResult = (CloudScanResult) it.next();
                a.put(cloudScanResult.getResult(), cloudScanResult);
            }
        }

        private CloudScanResult(int i) {
            this.b = i;
        }

        public static CloudScanResult get(int i) {
            return (CloudScanResult) a.get(i);
        }

        public final int getResult() {
            return this.b;
        }
    }

    /* compiled from: Unknown */
    private enum a {
        PAYLOAD_SCAN_RESULT((short) 0),
        PAYLOAD_INFECTION_NAME((short) 1);
        
        private static final SparseArray<a> c = null;
        private final short d;

        static {
            c = new SparseArray();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                c.put(aVar.a(), aVar);
            }
        }

        private a(short s) {
            this.d = (short) s;
        }

        public static a a(short s) {
            return (a) c.get(s);
        }

        public final short a() {
            return this.d;
        }
    }

    static CloudScanResultStructure a(byte[] bArr) {
        CloudScanResultStructure cloudScanResultStructure = new CloudScanResultStructure();
        if (bArr == null) {
            return cloudScanResultStructure;
        }
        try {
            if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                int i = 4;
                while (i < bArr.length) {
                    int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                    i += 4;
                    if (bArr[(i + intValue) - 1] == (byte) -1) {
                        a a = a.a(((Short) al.a(bArr, null, Short.TYPE, i)).shortValue());
                        if (a != null) {
                            switch (a.a[a.ordinal()]) {
                                case 1:
                                    Short sh = (Short) al.a(bArr, null, Short.TYPE, i + 2);
                                    if (sh == null) {
                                        break;
                                    }
                                    cloudScanResultStructure.a = CloudScanResult.get(sh.shortValue());
                                    if (cloudScanResultStructure.a != null) {
                                        break;
                                    }
                                    cloudScanResultStructure.a = CloudScanResult.RESULT_UNKNOWN_ERROR;
                                    break;
                                case 2:
                                    cloudScanResultStructure.b = new String(bArr, i + 2, intValue - 3);
                                    break;
                                default:
                                    break;
                            }
                        }
                        i += intValue;
                    } else {
                        throw new IllegalArgumentException("Invalid payload length");
                    }
                }
                return cloudScanResultStructure;
            }
            throw new IllegalArgumentException("Invalid structure length");
        } catch (Throwable e) {
            ao.d("Exception parsing VPS cloud scan result", e);
        }
    }

    static Map<String, CloudScanResultStructure> a(List<String> list, byte[] bArr) {
        Map<String, CloudScanResultStructure> hashMap = new HashMap();
        if (bArr == null) {
            return hashMap;
        }
        int i = 0;
        int i2 = 0;
        while (i2 < bArr.length) {
            int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i2)).intValue() + 4;
            Object obj = new byte[intValue];
            System.arraycopy(bArr, i2, obj, 0, intValue);
            int i3 = i2 + intValue;
            CloudScanResultStructure a = a(obj);
            if (i < list.size()) {
                i2 = i + 1;
                String str = (String) list.get(i);
                ao.a("CloudScanResultStructure.parseMap - " + str + " = " + a.getResult());
                hashMap.put(str, a);
                i = i2;
                i2 = i3;
            } else {
                ao.d("CloudScanResultStructure.parseMap there are more results than scanned files");
                hashMap.clear();
                return hashMap;
            }
        }
        if (i < list.size()) {
            ao.d("CloudScanResultStructure.parseMap there are less results than scanned files");
            hashMap.clear();
        }
        return hashMap;
    }

    public String getInfectionName() {
        return this.b;
    }

    public CloudScanResult getResult() {
        return this.a;
    }
}

package com.avast.android.sdk.engine;

import android.content.Context;
import com.avast.android.sdk.engine.internal.j;
import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class ScanResultStructure {
    public List<AddonCategory> addonCategories;
    public boolean cachedResult;
    public DetectionCategory detectionCategory;
    public DetectionType detectionType;
    public String infectionType;
    public ScanResult result;

    /* compiled from: Unknown */
    public enum AddonCategory {
        CATEGORY_COLLECTS_LOCATION(0),
        CATEGORY_COLLECTS_INFO_DEVICE_OR_NETWORK(1),
        CATEGORY_COLLECTS_INFO_PERSONAL(2),
        CATEGORY_ANALYTICS(3),
        CATEGORY_AD_BANNERS_INAPP(4),
        CATEGORY_AD_FULLSCREEN_INAPP(5),
        CATEGORY_NON_MARKET_APP_DOWNLOADS(6),
        CATEGORY_CALL_ON_AD_TOUCH(7),
        CATEGORY_REPLACES_DIALER_RING(8),
        CATEGORY_AD_IN_NOTIFICATION_BAR(9),
        CATEGORY_ADDS_HOME_SCREEN_ICON(10),
        CATEGORY_MODIFY_DEFAULT_BROWSER_BOOKMARKS(11);
        
        private static final Map<Integer, AddonCategory> a = null;
        private final int b;

        static {
            a = new HashMap();
            Iterator it = EnumSet.allOf(AddonCategory.class).iterator();
            while (it.hasNext()) {
                AddonCategory addonCategory = (AddonCategory) it.next();
                a.put(Integer.valueOf(addonCategory.getCategoryId()), addonCategory);
            }
        }

        private AddonCategory(int i) {
            this.b = i;
        }

        public static AddonCategory get(int i) {
            return (AddonCategory) a.get(Integer.valueOf(i));
        }

        public final int getCategoryId() {
            return this.b;
        }
    }

    /* compiled from: Unknown */
    public enum DetectionCategory {
        CATEGORY_UNKNOWN(0),
        CATEGORY_MALWARE(1),
        CATEGORY_SUSPICOUS(2),
        CATEGORY_PUP(3);
        
        private static final Map<Integer, DetectionCategory> a = null;
        private final int b;

        static {
            a = new HashMap();
            Iterator it = EnumSet.allOf(DetectionCategory.class).iterator();
            while (it.hasNext()) {
                DetectionCategory detectionCategory = (DetectionCategory) it.next();
                a.put(Integer.valueOf(detectionCategory.getCategory()), detectionCategory);
            }
        }

        private DetectionCategory(int i) {
            this.b = i;
        }

        public static DetectionCategory get(int i) {
            return (DetectionCategory) a.get(Integer.valueOf(i));
        }

        public final int getCategory() {
            return this.b;
        }
    }

    /* compiled from: Unknown */
    public enum DetectionType {
        TYPE_UNKNOWN(0),
        TYPE_DIALER(1),
        TYPE_ADWARE(2),
        TYPE_CRYPTOR(3),
        TYPE_DROPPER(4),
        TYPE_EXPLOIT(5),
        TYPE_VIRUS_MAKING_KIT(6),
        TYPE_ROOTKIT(7),
        TYPE_SPYWARE(8),
        TYPE_TROJAN(9),
        TYPE_WORM(10),
        TYPE_PUP(11),
        TYPE_JOKE(12),
        TYPE_TOOL(13),
        TYPE_HEURISTICS(14),
        TYPE_SUSPICIOUS(15);
        
        private static final Map<Integer, DetectionType> a = null;
        private final int b;

        static {
            a = new HashMap();
            Iterator it = EnumSet.allOf(DetectionType.class).iterator();
            while (it.hasNext()) {
                DetectionType detectionType = (DetectionType) it.next();
                a.put(Integer.valueOf(detectionType.getType()), detectionType);
            }
        }

        private DetectionType(int i) {
            this.b = i;
        }

        public static DetectionType get(int i) {
            return (DetectionType) a.get(Integer.valueOf(i));
        }

        public final int getType() {
            return this.b;
        }
    }

    /* compiled from: Unknown */
    public enum ScanResult {
        RESULT_UNKNOWN_ERROR(0),
        RESULT_ERROR_INSUFFICIENT_SPACE(1),
        RESULT_ERROR_PRIVATE_FILE(2),
        RESULT_ERROR_SKIP(3),
        RESULT_OUTDATED_APPLICATION(4),
        RESULT_INCOMPATIBLE_VPS(5),
        RESULT_ERROR_SCAN_INVALID_CONTEXT(6),
        RESULT_ERROR_UNNAMED_VIRUS(7),
        RESULT_ERROR_SCAN_INTERNAL_ERROR(8),
        RESULT_OK(100),
        RESULT_SUSPICIOUS(150),
        RESULT_INFECTED(200);
        
        private static final Map<Integer, ScanResult> a = null;
        private final int b;

        static {
            a = new HashMap();
            Iterator it = EnumSet.allOf(ScanResult.class).iterator();
            while (it.hasNext()) {
                ScanResult scanResult = (ScanResult) it.next();
                a.put(Integer.valueOf(scanResult.getResult()), scanResult);
            }
        }

        private ScanResult(int i) {
            this.b = i;
        }

        public static ScanResult get(int i) {
            return (ScanResult) a.get(Integer.valueOf(i));
        }

        public final int getResult() {
            return this.b;
        }
    }

    /* compiled from: Unknown */
    private enum a {
        PAYLOAD_RESULT((short) 0),
        PAYLOAD_INFECTION_TYPE((short) 1),
        PAYLOAD_ADDON_CATEGORIES((short) 2);
        
        private static final Map<Short, a> d = null;
        private final short e;

        static {
            d = new HashMap();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                d.put(Short.valueOf(aVar.a()), aVar);
            }
        }

        private a(short s) {
            this.e = (short) s;
        }

        public static a a(short s) {
            return (a) d.get(Short.valueOf(s));
        }

        public final short a() {
            return this.e;
        }
    }

    public ScanResultStructure() {
        this.result = null;
        this.infectionType = null;
        this.detectionType = null;
        this.detectionCategory = null;
        this.addonCategories = null;
        this.cachedResult = false;
        this.result = ScanResult.RESULT_OK;
    }

    public ScanResultStructure(ScanResult scanResult, String str) {
        this.result = null;
        this.infectionType = null;
        this.detectionType = null;
        this.detectionCategory = null;
        this.addonCategories = null;
        this.cachedResult = false;
        if (!ScanResult.RESULT_OK.equals(scanResult) && str == null) {
            throw new IllegalArgumentException("Infection description must be passed if the scan result is not RESULT_OK");
        }
        this.result = scanResult;
        this.infectionType = str;
    }

    private static DetectionCategory a(DetectionType detectionType) {
        switch (f.b[detectionType.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                return DetectionCategory.CATEGORY_MALWARE;
            case 11:
            case 12:
            case 13:
                return DetectionCategory.CATEGORY_PUP;
            case 14:
            case 15:
                return DetectionCategory.CATEGORY_SUSPICOUS;
            default:
                return DetectionCategory.CATEGORY_UNKNOWN;
        }
    }

    private static DetectionType a(String str) {
        if (str == null || "".equals(str)) {
            return DetectionType.TYPE_UNKNOWN;
        }
        String toUpperCase = str.toUpperCase();
        return !toUpperCase.contains(" [DIALER]") ? !toUpperCase.contains(" [ADW]") ? !toUpperCase.contains(" [CRYP]") ? !toUpperCase.contains(" [DRP]") ? !toUpperCase.contains(" [EXPL]") ? !toUpperCase.contains(" [KIT]") ? !toUpperCase.contains(" [RTK]") ? !toUpperCase.contains(" [SPY]") ? !toUpperCase.contains(" [TRJ]") ? !toUpperCase.contains(" [WRM]") ? !toUpperCase.contains(" [PUP]") ? !toUpperCase.contains(" [JOKE]") ? !toUpperCase.contains(" [TOOL]") ? !toUpperCase.contains(" [HEUR]") ? !toUpperCase.contains(" [SUSP]") ? DetectionType.TYPE_TROJAN : DetectionType.TYPE_SUSPICIOUS : DetectionType.TYPE_HEURISTICS : DetectionType.TYPE_TOOL : DetectionType.TYPE_JOKE : DetectionType.TYPE_PUP : DetectionType.TYPE_WORM : DetectionType.TYPE_TROJAN : DetectionType.TYPE_SPYWARE : DetectionType.TYPE_ROOTKIT : DetectionType.TYPE_VIRUS_MAKING_KIT : DetectionType.TYPE_EXPLOIT : DetectionType.TYPE_DROPPER : DetectionType.TYPE_CRYPTOR : DetectionType.TYPE_ADWARE : DetectionType.TYPE_DIALER;
    }

    private static ScanResult a(byte[] bArr, int i) {
        return ScanResult.get((((Byte) al.a(bArr, null, Byte.TYPE, i)).intValue() + 256) % 256);
    }

    private static List<AddonCategory> a(byte[] bArr, int i, int i2) {
        List<AddonCategory> linkedList = new LinkedList();
        while (i <= i2) {
            try {
                linkedList.add(AddonCategory.get(((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue()));
            } catch (Throwable e) {
                ao.d("Exception parsing addon categories", e);
            }
            i += 4;
        }
        return linkedList;
    }

    public static String getVersion() {
        return "srs-2";
    }

    public static Integer getVersionCode() {
        return Integer.valueOf(Integer.parseInt("srs-2".substring("srs-2".indexOf("-") + 1)));
    }

    public static ScanResultStructure parse(byte[] bArr) {
        ScanResultStructure scanResultStructure = new ScanResultStructure();
        if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
            int i = 4;
            while (i < bArr.length) {
                try {
                    int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                    i += 4;
                    if (bArr[(i + intValue) - 1] == (byte) -1) {
                        a a = a.a(((Short) al.a(bArr, null, Short.TYPE, i)).shortValue());
                        if (a != null) {
                            switch (f.a[a.ordinal()]) {
                                case 1:
                                    scanResultStructure.result = a(bArr, i + 2);
                                    if (scanResultStructure.result != null) {
                                        break;
                                    }
                                    scanResultStructure.result = ScanResult.RESULT_OK;
                                    break;
                                case 2:
                                    scanResultStructure.infectionType = new String(bArr, i + 2, (intValue - 2) - 1);
                                    break;
                                case 3:
                                    try {
                                        scanResultStructure.addonCategories = a(bArr, i + 2, ((i + intValue) - 2) - 1);
                                        break;
                                    } catch (Throwable e) {
                                        ao.d("Exception parsing addon categories", e);
                                        break;
                                    }
                                default:
                                    break;
                            }
                        }
                        i += intValue;
                    } else {
                        throw new IllegalArgumentException("Invalid payload length");
                    }
                } catch (Throwable e2) {
                    ao.d("Exception parsing scan result", e2);
                    scanResultStructure.result = ScanResult.RESULT_UNKNOWN_ERROR;
                    scanResultStructure.infectionType = "";
                }
            }
            if (scanResultStructure.result.getResult() > ScanResult.RESULT_OK.getResult()) {
                scanResultStructure.detectionType = a(scanResultStructure.infectionType);
                scanResultStructure.detectionCategory = a(scanResultStructure.detectionType);
            }
            return scanResultStructure;
        }
        throw new IllegalArgumentException("Invalid structure length");
    }

    public static List<ScanResultStructure> parseResultList(byte[] bArr) {
        List<ScanResultStructure> linkedList = new LinkedList();
        if (bArr == null) {
            return linkedList;
        }
        ao.a(al.a(bArr));
        int i = 0;
        while (i < bArr.length) {
            int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue() + 4;
            ao.a("ScanResultStructure.parseResultList - numResultBytes=" + intValue);
            Object obj = new byte[intValue];
            System.arraycopy(bArr, i, obj, 0, intValue);
            intValue += i;
            ScanResultStructure parse = parse(obj);
            ao.a("ScanResultStructure.parseResultList - " + parse.infectionType);
            linkedList.add(parse);
            i = intValue;
        }
        return linkedList;
    }

    public boolean isAddonResult(Context context) {
        return (this.addonCategories == null || this.addonCategories.isEmpty()) ? j.a(context, this) : true;
    }
}

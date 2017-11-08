package com.avast.android.sdk.engine;

import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/* compiled from: Unknown */
public class UrlCheckResultStructure {
    private String a;
    public String brandDomain;
    public String desiredSite;
    public String infection;
    public UrlCheckResult result;

    /* compiled from: Unknown */
    public enum UrlCheckResult {
        RESULT_OK(0),
        RESULT_MALWARE(1),
        RESULT_PHISHING(2),
        RESULT_UNKNOWN_ERROR(3),
        RESULT_SUSPICIOUS(4),
        RESULT_TYPO_SQUATTING(5);
        
        private static final Map<Integer, UrlCheckResult> a = null;
        private final int b;

        static {
            a = new HashMap();
            Iterator it = EnumSet.allOf(UrlCheckResult.class).iterator();
            while (it.hasNext()) {
                UrlCheckResult urlCheckResult = (UrlCheckResult) it.next();
                a.put(Integer.valueOf(urlCheckResult.getResult()), urlCheckResult);
            }
        }

        private UrlCheckResult(int i) {
            this.b = i;
        }

        public static UrlCheckResult get(int i) {
            return (UrlCheckResult) a.get(Integer.valueOf(i));
        }

        public final int getResult() {
            return this.b;
        }
    }

    /* compiled from: Unknown */
    private enum a {
        PAYLOAD_RESULT((short) 0),
        PAYLOAD_INFECTION_TYPE((short) 1),
        PAYLOAD_TYPO_DESIRED_SITE((short) 2),
        PAYLOAD_TYPO_REDIRECT_ID((short) 3),
        PAYLOAD_TYPO_BRAND_DOMAIN((short) 4);
        
        private static final Map<Short, a> f = null;
        private final short g;

        static {
            f = new HashMap();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                f.put(Short.valueOf(aVar.a()), aVar);
            }
        }

        private a(short s) {
            this.g = (short) s;
        }

        public static a a(short s) {
            return (a) f.get(Short.valueOf(s));
        }

        public final short a() {
            return this.g;
        }
    }

    public UrlCheckResultStructure() {
        this.result = null;
        this.infection = null;
        this.desiredSite = null;
        this.a = null;
        this.brandDomain = null;
        this.result = UrlCheckResult.RESULT_OK;
        this.a = UUID.randomUUID().toString();
    }

    public UrlCheckResultStructure(UrlCheckResult urlCheckResult) {
        this.result = null;
        this.infection = null;
        this.desiredSite = null;
        this.a = null;
        this.brandDomain = null;
        this.result = urlCheckResult;
        this.a = UUID.randomUUID().toString();
    }

    public UrlCheckResultStructure(UrlCheckResult urlCheckResult, String str, String str2) {
        this.result = null;
        this.infection = null;
        this.desiredSite = null;
        this.a = null;
        this.brandDomain = null;
        this.result = urlCheckResult;
        this.infection = str;
        this.desiredSite = str2;
        this.a = UUID.randomUUID().toString();
    }

    private static UrlCheckResult a(byte[] bArr, int i) {
        return UrlCheckResult.get((((Byte) al.a(bArr, null, Byte.TYPE, i)).intValue() + 256) % 256);
    }

    public static String getVersion() {
        return "uchrs-2";
    }

    public static Integer getVersionCode() {
        return Integer.valueOf(Integer.parseInt("uchrs-2".substring("uchrs-2".indexOf("-") + 1)));
    }

    public static UrlCheckResultStructure parse(byte[] bArr) {
        UrlCheckResultStructure urlCheckResultStructure = new UrlCheckResultStructure();
        try {
            if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                int i = 4;
                while (i < bArr.length) {
                    int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                    i += 4;
                    if (bArr[(i + intValue) - 1] == (byte) -1) {
                        a a = a.a(((Short) al.a(bArr, null, Short.TYPE, i)).shortValue());
                        if (a != null) {
                            switch (h.a[a.ordinal()]) {
                                case 1:
                                    urlCheckResultStructure.result = a(bArr, i + 2);
                                    if (urlCheckResultStructure.result != null) {
                                        break;
                                    }
                                    urlCheckResultStructure.result = UrlCheckResult.RESULT_OK;
                                    break;
                                case 2:
                                    urlCheckResultStructure.infection = new String(bArr, i + 2, (intValue - 2) - 1);
                                    break;
                                case 3:
                                    urlCheckResultStructure.desiredSite = new String(bArr, i + 2, (intValue - 2) - 1);
                                    break;
                                case 4:
                                    urlCheckResultStructure.a = new String(bArr, i + 2, (intValue - 2) - 1);
                                    break;
                                case 5:
                                    urlCheckResultStructure.brandDomain = new String(bArr, i + 2, (intValue - 2) - 1);
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
                return urlCheckResultStructure;
            }
            throw new IllegalArgumentException("Invalid structure length");
        } catch (Throwable e) {
            ao.d("Exception parsing url check result", e);
            urlCheckResultStructure.result = UrlCheckResult.RESULT_UNKNOWN_ERROR;
        }
    }

    public static List<UrlCheckResultStructure> parseResultList(byte[] bArr) {
        List<UrlCheckResultStructure> linkedList = new LinkedList();
        if (bArr == null) {
            return linkedList;
        }
        ao.a(al.a(bArr));
        int i = 0;
        while (i < bArr.length) {
            int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue() + 4;
            Object obj = new byte[intValue];
            System.arraycopy(bArr, i, obj, 0, intValue);
            intValue += i;
            linkedList.add(parse(obj));
            i = intValue;
        }
        return linkedList;
    }

    public String getRedirectId() {
        return this.a;
    }
}

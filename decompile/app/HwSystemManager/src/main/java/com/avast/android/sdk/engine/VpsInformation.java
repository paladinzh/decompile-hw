package com.avast.android.sdk.engine;

import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

/* compiled from: Unknown */
public class VpsInformation {
    public long adsDefinitionCount;
    public Date adsLastModifiedTimestamp = null;
    public long definitionCount;
    public Date gmtCreateTime;
    public int privacyInformationAlgorithmVersion = -1;
    public String version;

    /* compiled from: Unknown */
    private enum a {
        PAYLOAD_VERSION((short) 0),
        PAYLOAD_BUILD_YEAR((short) 1),
        PAYLOAD_BUILD_MONTH((short) 2),
        PAYLOAD_BUILD_DAY((short) 3),
        PAYLOAD_BUILD_HOUR((short) 4),
        PAYLOAD_BUILD_MINUTE((short) 5),
        PAYLOAD_DEFINITION_COUNT((short) 6),
        PAYLOAD_ADS_DEFINITION_COUNT((short) 7),
        PAYLOAD_ADS_LAST_MODIFIED_TIMESTAMP((short) 8),
        PAYLOAD_PRIVACY_SCAN_ALGORITHM_VERSION((short) 9);
        
        private static final Map<Short, a> k = null;
        private final short l;

        static {
            k = new HashMap();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                k.put(Short.valueOf(aVar.a()), aVar);
            }
        }

        private a(short s) {
            this.l = (short) s;
        }

        public static a a(short s) {
            return (a) k.get(Short.valueOf(s));
        }

        public final short a() {
            return this.l;
        }
    }

    public VpsInformation() {
        Random random = new Random(System.currentTimeMillis());
        Date date = new Date();
        String str = "" + (date.getYear() % 100);
        if (date.getMonth() + 1 < 10) {
            str = str + "0";
        }
        str = str + (date.getMonth() + 1);
        if (date.getDate() < 10) {
            str = str + "0";
        }
        this.version = (str + date.getDate()) + "-00";
        long j = 1000000;
        if (random.nextFloat() < 0.5f) {
            j = 1000000000;
        }
        this.gmtCreateTime = new Date(System.currentTimeMillis() - j);
        this.definitionCount = (long) random.nextInt(10000);
        this.adsDefinitionCount = (long) random.nextInt(10000);
    }

    public VpsInformation(String str, Date date, long j, long j2) {
        this.version = str;
        this.gmtCreateTime = date;
        this.definitionCount = j;
        this.adsDefinitionCount = j2;
    }

    public static String getVersion() {
        return "vpsi-2";
    }

    public static Integer getVersionCode() {
        return Integer.valueOf(Integer.parseInt("vpsi-2".substring("vpsi-2".indexOf("-") + 1)));
    }

    public static VpsInformation parse(byte[] bArr) {
        VpsInformation vpsInformation = new VpsInformation();
        try {
            if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
                int i = 4;
                while (i < bArr.length) {
                    int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                    i += 4;
                    if (bArr[(i + intValue) - 1] == (byte) -1) {
                        a a = a.a(((Short) al.a(bArr, null, Short.TYPE, i)).shortValue());
                        if (a != null) {
                            Short sh;
                            Long l;
                            switch (i.a[a.ordinal()]) {
                                case 1:
                                    vpsInformation.version = new String(bArr, i + 2, (intValue - 2) - 1);
                                    break;
                                case 2:
                                    sh = (Short) al.a(bArr, null, Short.TYPE, i + 2);
                                    if (sh == null) {
                                        break;
                                    }
                                    instance.set(1, sh.shortValue());
                                    break;
                                case 3:
                                    sh = (Short) al.a(bArr, null, Short.TYPE, i + 2);
                                    if (sh == null) {
                                        break;
                                    }
                                    instance.set(2, sh.shortValue());
                                    break;
                                case 4:
                                    sh = (Short) al.a(bArr, null, Short.TYPE, i + 2);
                                    if (sh == null) {
                                        break;
                                    }
                                    instance.set(5, sh.shortValue());
                                    break;
                                case 5:
                                    sh = (Short) al.a(bArr, null, Short.TYPE, i + 2);
                                    if (sh == null) {
                                        break;
                                    }
                                    instance.set(11, sh.shortValue());
                                    break;
                                case 6:
                                    sh = (Short) al.a(bArr, null, Short.TYPE, i + 2);
                                    if (sh == null) {
                                        break;
                                    }
                                    instance.set(12, sh.shortValue());
                                    break;
                                case 7:
                                    l = (Long) al.a(bArr, null, Long.TYPE, i + 2);
                                    if (l == null) {
                                        break;
                                    }
                                    vpsInformation.definitionCount = l.longValue();
                                    break;
                                case 8:
                                    l = (Long) al.a(bArr, null, Long.TYPE, i + 2);
                                    if (l == null) {
                                        break;
                                    }
                                    vpsInformation.adsDefinitionCount = l.longValue();
                                    break;
                                case 9:
                                    l = (Long) al.a(bArr, null, Long.TYPE, i + 2);
                                    if (l == null) {
                                        break;
                                    }
                                    Calendar instance2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
                                    instance2.setTimeInMillis(l.longValue());
                                    vpsInformation.adsLastModifiedTimestamp = instance2.getTime();
                                    break;
                                case 10:
                                    Integer num = (Integer) al.a(bArr, null, Integer.TYPE, i + 2);
                                    if (num == null) {
                                        break;
                                    }
                                    vpsInformation.privacyInformationAlgorithmVersion = num.intValue();
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
                vpsInformation.gmtCreateTime = instance.getTime();
                return vpsInformation;
            }
            throw new IllegalArgumentException("Invalid structure length");
        } catch (Throwable e) {
            ao.d("Exception parsing VPS information", e);
            return null;
        }
    }

    public static List<VpsInformation> parseResultList(byte[] bArr) {
        List<VpsInformation> linkedList = new LinkedList();
        if (bArr == null) {
            return linkedList;
        }
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
}

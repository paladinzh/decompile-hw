package com.avast.android.sdk.engine;

import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class MessageScanResultContainer {
    public Map<String, List<ScanResultStructure>> additionalFilesDetections = null;
    public List<MessageScanResultStructure> messageScanResults = null;
    public Map<String, List<UrlCheckResultStructure>> urlDetections = null;

    /* compiled from: Unknown */
    public enum MessageScanResult {
        RESULT_UNKNOWN_ERROR(0),
        RESULT_OUTDATED_APPLICATION(1),
        RESULT_INCOMPATIBLE_VPS(2),
        RESULT_ERROR_SCAN_INVALID_CONTEXT(3),
        RESULT_ERROR_UNNAMED_DETECTION(4),
        RESULT_ERROR_SCAN_INTERNAL_ERROR(5),
        RESULT_OK(100),
        RESULT_SUSPICIOUS(150),
        RESULT_SENDER_BLACKLIST(175),
        RESULT_EXPLOIT_MESSAGE_FORMAT(176),
        RESULT_GENERIC_DETECTION(200);
        
        private static final Map<Integer, MessageScanResult> a = null;
        private final int b;

        static {
            a = new HashMap();
            Iterator it = EnumSet.allOf(MessageScanResult.class).iterator();
            while (it.hasNext()) {
                MessageScanResult messageScanResult = (MessageScanResult) it.next();
                a.put(Integer.valueOf(messageScanResult.getResult()), messageScanResult);
            }
        }

        private MessageScanResult(int i) {
            this.b = i;
        }

        public static MessageScanResult get(int i) {
            return (MessageScanResult) a.get(Integer.valueOf(i));
        }

        public final int getResult() {
            return this.b;
        }
    }

    /* compiled from: Unknown */
    public static class MessageScanResultStructure {
        public String infectionType;
        public MessageScanResult result;

        /* compiled from: Unknown */
        private enum a {
            PAYLOAD_RESULT((short) 0),
            PAYLOAD_INFECTION_TYPE((short) 1);
            
            private static final Map<Short, a> c = null;
            private final short d;

            static {
                c = new HashMap();
                Iterator it = EnumSet.allOf(a.class).iterator();
                while (it.hasNext()) {
                    a aVar = (a) it.next();
                    c.put(Short.valueOf(aVar.a()), aVar);
                }
            }

            private a(short s) {
                this.d = (short) s;
            }

            public static a a(short s) {
                return (a) c.get(Short.valueOf(s));
            }

            public final short a() {
                return this.d;
            }
        }

        public MessageScanResultStructure() {
            this.result = null;
            this.infectionType = null;
            this.result = MessageScanResult.RESULT_OK;
        }

        public MessageScanResultStructure(MessageScanResult messageScanResult, String str) {
            this.result = null;
            this.infectionType = null;
            if (!MessageScanResult.RESULT_OK.equals(messageScanResult) && str == null) {
                throw new IllegalArgumentException("Infection description must be passed if the scan result is not RESULT_OK");
            }
            this.result = messageScanResult;
            this.infectionType = str;
        }

        private static MessageScanResult a(byte[] bArr, int i) {
            return MessageScanResult.get((((Byte) al.a(bArr, null, Byte.TYPE, i)).intValue() + 256) % 256);
        }

        public static String getVersion() {
            return "msrs-1";
        }

        public static Integer getVersionCode() {
            return Integer.valueOf(Integer.parseInt("msrs-1".substring("msrs-1".indexOf("-") + 1)));
        }

        public static MessageScanResultStructure parse(byte[] bArr) {
            MessageScanResultStructure messageScanResultStructure = new MessageScanResultStructure();
            try {
                if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                    int i = 4;
                    while (i < bArr.length) {
                        int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                        i += 4;
                        if (bArr[(i + intValue) - 1] == (byte) -1) {
                            a a = a.a(((Short) al.a(bArr, null, Short.TYPE, i)).shortValue());
                            if (a != null) {
                                switch (d.a[a.ordinal()]) {
                                    case 1:
                                        messageScanResultStructure.result = a(bArr, i + 2);
                                        if (messageScanResultStructure.result != null) {
                                            break;
                                        }
                                        messageScanResultStructure.result = MessageScanResult.RESULT_OK;
                                        break;
                                    case 2:
                                        messageScanResultStructure.infectionType = new String(bArr, i + 2, (intValue - 2) - 1);
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
                    return messageScanResultStructure;
                }
                throw new IllegalArgumentException("Invalid structure length");
            } catch (Throwable e) {
                ao.d("Exception parsing message scan result", e);
                messageScanResultStructure.result = MessageScanResult.RESULT_UNKNOWN_ERROR;
                messageScanResultStructure.infectionType = "";
            }
        }

        public static List<MessageScanResultStructure> parseResultList(byte[] bArr) {
            List<MessageScanResultStructure> linkedList = new LinkedList();
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
    }
}

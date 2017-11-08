package sun.security.ssl;

import java.io.IOException;
import java.security.spec.ECParameterSpec;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLProtocolException;

/* compiled from: HelloExtensions */
final class SupportedEllipticCurvesExtension extends HelloExtension {
    private static final int ARBITRARY_CHAR2 = 65282;
    private static final int ARBITRARY_PRIME = 65281;
    static final SupportedEllipticCurvesExtension DEFAULT;
    private static final String[] NAMED_CURVE_OID_TABLE = new String[]{null, "1.3.132.0.1", "1.3.132.0.2", "1.3.132.0.15", "1.3.132.0.24", "1.3.132.0.25", "1.3.132.0.26", "1.3.132.0.27", "1.3.132.0.3", "1.3.132.0.16", "1.3.132.0.17", "1.3.132.0.36", "1.3.132.0.37", "1.3.132.0.38", "1.3.132.0.39", "1.3.132.0.9", "1.3.132.0.8", "1.3.132.0.30", "1.3.132.0.31", "1.2.840.10045.3.1.1", "1.3.132.0.32", "1.3.132.0.33", "1.3.132.0.10", "1.2.840.10045.3.1.7", "1.3.132.0.34", "1.3.132.0.35"};
    private static final Map<String, Integer> curveIndices = new HashMap();
    private static final boolean fips = SunJSSE.isFIPS();
    private final int[] curveIds;

    static {
        int[] ids;
        if (fips) {
            ids = new int[]{23, 1, 3, 19, 21, 6, 7, 9, 10, 24, 11, 12, 25, 13, 14};
        } else {
            ids = new int[]{23, 1, 3, 19, 21, 6, 7, 9, 10, 24, 11, 12, 25, 13, 14, 15, 16, 17, 2, 18, 4, 5, 20, 8, 22};
        }
        DEFAULT = new SupportedEllipticCurvesExtension(ids);
        for (int i = 1; i < NAMED_CURVE_OID_TABLE.length; i++) {
            curveIndices.put(NAMED_CURVE_OID_TABLE[i], Integer.valueOf(i));
        }
    }

    private SupportedEllipticCurvesExtension(int[] curveIds) {
        super(ExtensionType.EXT_ELLIPTIC_CURVES);
        this.curveIds = curveIds;
    }

    SupportedEllipticCurvesExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_ELLIPTIC_CURVES);
        int k = s.getInt16();
        if ((len & 1) == 0 && k + 2 == len) {
            this.curveIds = new int[(k >> 1)];
            for (int i = 0; i < this.curveIds.length; i++) {
                this.curveIds[i] = s.getInt16();
            }
            return;
        }
        throw new SSLProtocolException("Invalid " + this.type + " extension");
    }

    boolean contains(int index) {
        for (int curveId : this.curveIds) {
            if (index == curveId) {
                return true;
            }
        }
        return false;
    }

    int[] curveIds() {
        return this.curveIds;
    }

    int length() {
        return (this.curveIds.length << 1) + 6;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(this.type.id);
        int k = this.curveIds.length << 1;
        s.putInt16(k + 2);
        s.putInt16(k);
        for (int curveId : this.curveIds) {
            s.putInt16(curveId);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Extension ").append(this.type).append(", curve names: {");
        boolean first = true;
        for (int curveId : this.curveIds) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            String oid = getCurveOid(curveId);
            if (oid != null) {
                ECParameterSpec spec = JsseJce.getECParameterSpec(oid);
                if (spec != null) {
                    sb.append(spec.toString().split(" ")[0]);
                } else {
                    sb.append(oid);
                }
            } else if (curveId == ARBITRARY_PRIME) {
                sb.append("arbitrary_explicit_prime_curves");
            } else if (curveId == ARBITRARY_CHAR2) {
                sb.append("arbitrary_explicit_char2_curves");
            } else {
                sb.append("unknown curve ").append(curveId);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    static boolean isSupported(int index) {
        if (index <= 0 || index >= NAMED_CURVE_OID_TABLE.length) {
            return false;
        }
        if (fips) {
            return DEFAULT.contains(index);
        }
        return true;
    }

    static int getCurveIndex(ECParameterSpec params) {
        int i = -1;
        String oid = JsseJce.getNamedCurveOid(params);
        if (oid == null) {
            return -1;
        }
        Integer n = (Integer) curveIndices.get(oid);
        if (n != null) {
            i = n.intValue();
        }
        return i;
    }

    static String getCurveOid(int index) {
        if (index <= 0 || index >= NAMED_CURVE_OID_TABLE.length) {
            return null;
        }
        return NAMED_CURVE_OID_TABLE[index];
    }
}

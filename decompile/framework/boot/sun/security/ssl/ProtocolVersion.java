package sun.security.ssl;

public final class ProtocolVersion implements Comparable<ProtocolVersion> {
    static final ProtocolVersion DEFAULT = TLS10;
    static final ProtocolVersion DEFAULT_HELLO;
    private static final boolean FIPS = SunJSSE.isFIPS();
    static final int LIMIT_MAX_VALUE = 65535;
    static final int LIMIT_MIN_VALUE = 0;
    static final ProtocolVersion MAX = TLS12;
    static final ProtocolVersion MIN = (FIPS ? TLS10 : SSL30);
    static final ProtocolVersion NONE = new ProtocolVersion(-1, "NONE");
    static final ProtocolVersion SSL20Hello = new ProtocolVersion(2, "SSLv2Hello");
    static final ProtocolVersion SSL30 = new ProtocolVersion(768, "SSLv3");
    static final ProtocolVersion TLS10 = new ProtocolVersion(769, "TLSv1");
    static final ProtocolVersion TLS11 = new ProtocolVersion(770, "TLSv1.1");
    static final ProtocolVersion TLS12 = new ProtocolVersion(771, "TLSv1.2");
    public final byte major;
    public final byte minor;
    final String name;
    public final int v;

    static {
        ProtocolVersion protocolVersion;
        if (FIPS) {
            protocolVersion = TLS10;
        } else {
            protocolVersion = SSL30;
        }
        DEFAULT_HELLO = protocolVersion;
    }

    private ProtocolVersion(int v, String name) {
        this.v = v;
        this.name = name;
        this.major = (byte) (v >>> 8);
        this.minor = (byte) (v & 255);
    }

    private static ProtocolVersion valueOf(int v) {
        if (v == SSL30.v) {
            return SSL30;
        }
        if (v == TLS10.v) {
            return TLS10;
        }
        if (v == TLS11.v) {
            return TLS11;
        }
        if (v == TLS12.v) {
            return TLS12;
        }
        if (v == SSL20Hello.v) {
            return SSL20Hello;
        }
        return new ProtocolVersion(v, "Unknown-" + ((v >>> 8) & 255) + "." + (v & 255));
    }

    public static ProtocolVersion valueOf(int major, int minor) {
        return valueOf(((major & 255) << 8) | (minor & 255));
    }

    static ProtocolVersion valueOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Protocol cannot be null");
        } else if (FIPS && (name.equals(SSL30.name) || name.equals(SSL20Hello.name))) {
            throw new IllegalArgumentException("Only TLS 1.0 or later allowed in FIPS mode");
        } else if (name.equals(SSL30.name)) {
            return SSL30;
        } else {
            if (name.equals(TLS10.name)) {
                return TLS10;
            }
            if (name.equals(TLS11.name)) {
                return TLS11;
            }
            if (name.equals(TLS12.name)) {
                return TLS12;
            }
            if (name.equals(SSL20Hello.name)) {
                return SSL20Hello;
            }
            throw new IllegalArgumentException(name);
        }
    }

    public String toString() {
        return this.name;
    }

    public int compareTo(ProtocolVersion protocolVersion) {
        return this.v - protocolVersion.v;
    }
}

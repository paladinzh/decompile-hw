package sun.security.ssl;

import java.io.IOException;
import java.util.ArrayList;
import javax.net.ssl.SSLProtocolException;

/* compiled from: HelloExtensions */
final class SupportedEllipticPointFormatsExtension extends HelloExtension {
    static final HelloExtension DEFAULT = new SupportedEllipticPointFormatsExtension(new byte[]{(byte) 0});
    static final int FMT_ANSIX962_COMPRESSED_CHAR2 = 2;
    static final int FMT_ANSIX962_COMPRESSED_PRIME = 1;
    static final int FMT_UNCOMPRESSED = 0;
    private final byte[] formats;

    private SupportedEllipticPointFormatsExtension(byte[] formats) {
        super(ExtensionType.EXT_EC_POINT_FORMATS);
        this.formats = formats;
    }

    SupportedEllipticPointFormatsExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_EC_POINT_FORMATS);
        this.formats = s.getBytes8();
        boolean uncompressed = false;
        for (int format : this.formats) {
            if (format == 0) {
                uncompressed = true;
                break;
            }
        }
        if (!uncompressed) {
            throw new SSLProtocolException("Peer does not support uncompressed points");
        }
    }

    int length() {
        return this.formats.length + 5;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(this.type.id);
        s.putInt16(this.formats.length + 1);
        s.putBytes8(this.formats);
    }

    private static String toString(byte format) {
        int f = format & 255;
        switch (f) {
            case 0:
                return "uncompressed";
            case 1:
                return "ansiX962_compressed_prime";
            case 2:
                return "ansiX962_compressed_char2";
            default:
                return "unknown-" + f;
        }
    }

    public String toString() {
        Object list = new ArrayList();
        for (byte format : this.formats) {
            list.add(toString(format));
        }
        return "Extension " + this.type + ", formats: " + list;
    }
}

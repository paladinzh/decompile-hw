package sun.security.ssl;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

final class MAC {
    private static final int BLOCK_OFFSET_TYPE = 8;
    private static final int BLOCK_OFFSET_VERSION = 9;
    private static final int BLOCK_SIZE_SSL = 11;
    private static final int BLOCK_SIZE_TLS = 13;
    static final MAC NULL = new MAC();
    private static final byte[] nullMAC = new byte[0];
    private final byte[] block;
    private final Mac mac;
    private final MacAlg macAlg;
    private final int macSize;

    private MAC() {
        this.macSize = 0;
        this.macAlg = CipherSuite.M_NULL;
        this.mac = null;
        this.block = null;
    }

    MAC(MacAlg macAlg, ProtocolVersion protocolVersion, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm;
        this.macAlg = macAlg;
        this.macSize = macAlg.size;
        boolean tls = protocolVersion.v >= ProtocolVersion.TLS10.v;
        if (macAlg == CipherSuite.M_MD5) {
            algorithm = tls ? "HmacMD5" : "SslMacMD5";
        } else if (macAlg == CipherSuite.M_SHA) {
            algorithm = tls ? "HmacSHA1" : "SslMacSHA1";
        } else if (macAlg == CipherSuite.M_SHA256) {
            algorithm = "HmacSHA256";
        } else if (macAlg == CipherSuite.M_SHA384) {
            algorithm = "HmacSHA384";
        } else {
            throw new RuntimeException("Unknown Mac " + macAlg);
        }
        this.mac = JsseJce.getMac(algorithm);
        this.mac.init(key);
        if (tls) {
            this.block = new byte[13];
            this.block[9] = protocolVersion.major;
            this.block[10] = protocolVersion.minor;
            return;
        }
        this.block = new byte[11];
    }

    int MAClen() {
        return this.macSize;
    }

    int hashBlockLen() {
        return this.macAlg.hashBlockSize;
    }

    int minimalPaddingLen() {
        return this.macAlg.minimalPaddingSize;
    }

    final byte[] compute(byte type, byte[] buf, int offset, int len, boolean isSimulated) {
        return compute(type, null, buf, offset, len, isSimulated);
    }

    final byte[] compute(byte type, ByteBuffer bb, boolean isSimulated) {
        return compute(type, bb, null, 0, bb.remaining(), isSimulated);
    }

    final boolean seqNumOverflow() {
        if (this.block != null && this.mac != null && this.block[0] == (byte) -1 && this.block[1] == (byte) -1 && this.block[2] == (byte) -1 && this.block[3] == (byte) -1 && this.block[4] == (byte) -1 && this.block[5] == (byte) -1) {
            return this.block[6] == (byte) -1;
        } else {
            return false;
        }
    }

    final boolean seqNumIsHuge() {
        if (this.block == null || this.mac == null || this.block[0] != (byte) -1) {
            return false;
        }
        return this.block[1] == (byte) -1;
    }

    private void incrementSequenceNumber() {
        int k = 7;
        while (k >= 0) {
            byte[] bArr = this.block;
            byte b = (byte) (bArr[k] + 1);
            bArr[k] = b;
            if (b == (byte) 0) {
                k--;
            } else {
                return;
            }
        }
    }

    private byte[] compute(byte type, ByteBuffer bb, byte[] buf, int offset, int len, boolean isSimulated) {
        if (this.macSize == 0) {
            return nullMAC;
        }
        if (!isSimulated) {
            this.block[8] = type;
            this.block[this.block.length - 2] = (byte) (len >> 8);
            this.block[this.block.length - 1] = (byte) len;
            this.mac.update(this.block);
            incrementSequenceNumber();
        }
        if (bb != null) {
            this.mac.update(bb);
        } else {
            this.mac.update(buf, offset, len);
        }
        return this.mac.doFinal();
    }
}

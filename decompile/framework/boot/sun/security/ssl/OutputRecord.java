package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.net.ssl.SSLException;
import sun.misc.HexDumpEncoder;

class OutputRecord extends ByteArrayOutputStream implements Record {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static int[] V3toV2CipherMap1 = new int[]{-1, -1, -1, 2, 1, -1, 4, 5, -1, 6, 7};
    private static int[] V3toV2CipherMap3 = new int[]{-1, -1, -1, 128, 128, -1, 128, 128, -1, 64, 192};
    static final Debug debug = Debug.getInstance("ssl");
    private final byte contentType;
    private boolean firstMessage;
    private HandshakeHash handshakeHash;
    private ProtocolVersion helloVersion;
    private int lastHashed;
    ProtocolVersion protocolVersion;

    static {
        boolean z;
        if (OutputRecord.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    OutputRecord(byte type, int size) {
        super(size);
        this.protocolVersion = ProtocolVersion.DEFAULT;
        this.helloVersion = ProtocolVersion.DEFAULT_HELLO;
        this.firstMessage = true;
        this.count = 5;
        this.contentType = type;
        this.lastHashed = this.count;
    }

    OutputRecord(byte type) {
        this(type, recordSize(type));
    }

    private static int recordSize(byte type) {
        if (type == (byte) 20 || type == (byte) 21) {
            return Record.maxAlertRecordSize;
        }
        return Record.maxRecordSize;
    }

    synchronized void setVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    synchronized void setHelloVersion(ProtocolVersion helloVersion) {
        this.helloVersion = helloVersion;
    }

    public synchronized void reset() {
        super.reset();
        this.count = 5;
        this.lastHashed = this.count;
    }

    void setHandshakeHash(HandshakeHash handshakeHash) {
        if (!-assertionsDisabled) {
            if ((this.contentType == (byte) 22 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        this.handshakeHash = handshakeHash;
    }

    void doHashes() {
        int len = this.count - this.lastHashed;
        if (len > 0) {
            hashInternal(this.buf, this.lastHashed, len);
            this.lastHashed = this.count;
        }
    }

    private void hashInternal(byte[] buf, int offset, int len) {
        if (debug != null && Debug.isOn("data")) {
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                System.out.println("[write] MD5 and SHA1 hashes:  len = " + len);
                hd.encodeBuffer(new ByteArrayInputStream(buf, this.lastHashed, len), System.out);
            } catch (IOException e) {
            }
        }
        this.handshakeHash.update(buf, this.lastHashed, len);
        this.lastHashed = this.count;
    }

    boolean isEmpty() {
        return this.count == 5;
    }

    boolean isAlert(byte description) {
        boolean z = false;
        if (this.count <= 6 || this.contentType != (byte) 21) {
            return false;
        }
        if (this.buf[6] == description) {
            z = true;
        }
        return z;
    }

    void addMAC(MAC signer) throws IOException {
        if (this.contentType == (byte) 22) {
            doHashes();
        }
        if (signer.MAClen() != 0) {
            write(signer.compute(this.contentType, this.buf, 5, this.count - 5, false));
        }
    }

    void encrypt(CipherBox box) {
        this.count = box.encrypt(this.buf, 5, this.count - 5) + 5;
    }

    final int availableDataBytes() {
        return 16384 - (this.count - 5);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > this.buf.length) {
            this.buf = Arrays.copyOf(this.buf, minCapacity);
        }
    }

    final byte contentType() {
        return this.contentType;
    }

    void write(OutputStream s, boolean holdRecord, ByteArrayOutputStream heldRecordBuffer) throws IOException {
        if (this.count != 5) {
            int length = this.count - 5;
            if (length < 0) {
                throw new SSLException("output record size too small: " + length);
            }
            if (debug != null && ((Debug.isOn("record") || Debug.isOn("handshake")) && ((debug != null && Debug.isOn("record")) || contentType() == (byte) 20))) {
                System.out.println(Thread.currentThread().getName() + ", WRITE: " + this.protocolVersion + " " + InputRecord.contentName(contentType()) + ", length = " + length);
            }
            if (this.firstMessage && useV2Hello()) {
                byte[] v3Msg = new byte[(length - 4)];
                System.arraycopy(this.buf, 9, v3Msg, 0, v3Msg.length);
                V3toV2ClientHello(v3Msg);
                this.handshakeHash.reset();
                this.lastHashed = 2;
                doHashes();
                if (debug != null && Debug.isOn("record")) {
                    System.out.println(Thread.currentThread().getName() + ", WRITE: SSLv2 client hello message" + ", length = " + (this.count - 2));
                }
            } else {
                this.buf[0] = this.contentType;
                this.buf[1] = this.protocolVersion.major;
                this.buf[2] = this.protocolVersion.minor;
                this.buf[3] = (byte) (length >> 8);
                this.buf[4] = (byte) length;
            }
            this.firstMessage = false;
            int debugOffset = 0;
            if (holdRecord) {
                writeBuffer(heldRecordBuffer, this.buf, 0, this.count, 0);
            } else {
                if (heldRecordBuffer != null && heldRecordBuffer.size() > 0) {
                    int heldLen = heldRecordBuffer.size();
                    ensureCapacity(this.count + heldLen);
                    System.arraycopy(this.buf, 0, this.buf, heldLen, this.count);
                    System.arraycopy(heldRecordBuffer.toByteArray(), 0, this.buf, 0, heldLen);
                    this.count += heldLen;
                    heldRecordBuffer.reset();
                    debugOffset = heldLen;
                }
                writeBuffer(s, this.buf, 0, this.count, debugOffset);
            }
            reset();
        }
    }

    void writeBuffer(OutputStream s, byte[] buf, int off, int len, int debugOffset) throws IOException {
        s.write(buf, off, len);
        s.flush();
        if (debug != null && Debug.isOn("packet")) {
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                ByteBuffer bb = ByteBuffer.wrap(buf, off + debugOffset, len - debugOffset);
                System.out.println("[Raw write]: length = " + bb.remaining());
                hd.encodeBuffer(bb, System.out);
            } catch (IOException e) {
            }
        }
    }

    private boolean useV2Hello() {
        if (this.firstMessage && this.helloVersion == ProtocolVersion.SSL20Hello && this.contentType == (byte) 22 && this.buf[5] == (byte) 1) {
            return this.buf[43] == (byte) 0;
        } else {
            return false;
        }
    }

    private void V3toV2ClientHello(byte[] v3Msg) throws SSLException {
        int v3CipherSpecLenOffset = v3Msg[34] + 35;
        int cipherSpecs = (((v3Msg[v3CipherSpecLenOffset] & 255) << 8) + (v3Msg[v3CipherSpecLenOffset + 1] & 255)) / 2;
        int v3CipherSpecOffset = v3CipherSpecLenOffset + 2;
        int v2CipherSpecLen = 0;
        this.count = 11;
        boolean containsRenegoInfoSCSV = false;
        int v3CipherSpecOffset2 = v3CipherSpecOffset;
        for (int i = 0; i < cipherSpecs; i++) {
            v3CipherSpecOffset = v3CipherSpecOffset2 + 1;
            byte byte1 = v3Msg[v3CipherSpecOffset2];
            v3CipherSpecOffset2 = v3CipherSpecOffset + 1;
            byte byte2 = v3Msg[v3CipherSpecOffset];
            v2CipherSpecLen += V3toV2CipherSuite(byte1, byte2);
            if (!containsRenegoInfoSCSV && byte1 == (byte) 0 && byte2 == (byte) -1) {
                containsRenegoInfoSCSV = true;
            }
        }
        if (!containsRenegoInfoSCSV) {
            v2CipherSpecLen += V3toV2CipherSuite((byte) 0, (byte) -1);
        }
        this.buf[2] = (byte) 1;
        this.buf[3] = v3Msg[0];
        this.buf[4] = v3Msg[1];
        this.buf[5] = (byte) (v2CipherSpecLen >>> 8);
        this.buf[6] = (byte) v2CipherSpecLen;
        this.buf[7] = (byte) 0;
        this.buf[8] = (byte) 0;
        this.buf[9] = (byte) 0;
        this.buf[10] = (byte) 32;
        System.arraycopy(v3Msg, 2, this.buf, this.count, 32);
        this.count += 32;
        this.count -= 2;
        this.buf[0] = (byte) (this.count >>> 8);
        byte[] bArr = this.buf;
        bArr[0] = (byte) (bArr[0] | 128);
        this.buf[1] = (byte) this.count;
        this.count += 2;
    }

    private int V3toV2CipherSuite(byte byte1, byte byte2) {
        byte[] bArr = this.buf;
        int i = this.count;
        this.count = i + 1;
        bArr[i] = (byte) 0;
        bArr = this.buf;
        i = this.count;
        this.count = i + 1;
        bArr[i] = byte1;
        bArr = this.buf;
        i = this.count;
        this.count = i + 1;
        bArr[i] = byte2;
        if ((byte2 & 255) > 10 || V3toV2CipherMap1[byte2] == -1) {
            return 3;
        }
        bArr = this.buf;
        i = this.count;
        this.count = i + 1;
        bArr[i] = (byte) V3toV2CipherMap1[byte2];
        bArr = this.buf;
        i = this.count;
        this.count = i + 1;
        bArr[i] = (byte) 0;
        bArr = this.buf;
        i = this.count;
        this.count = i + 1;
        bArr[i] = (byte) V3toV2CipherMap3[byte2];
        return 6;
    }
}

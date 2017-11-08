package sun.security.ssl;

import java.io.IOException;
import java.io.OutputStream;

public class HandshakeOutStream extends OutputStream {
    static final /* synthetic */ boolean -assertionsDisabled = (!HandshakeOutStream.class.desiredAssertionStatus());
    private SSLEngineImpl engine;
    OutputRecord r;
    private SSLSocketImpl socket;

    HandshakeOutStream(ProtocolVersion protocolVersion, ProtocolVersion helloVersion, HandshakeHash handshakeHash, SSLSocketImpl socket) {
        this.socket = socket;
        this.r = new OutputRecord((byte) 22);
        init(protocolVersion, helloVersion, handshakeHash);
    }

    HandshakeOutStream(ProtocolVersion protocolVersion, ProtocolVersion helloVersion, HandshakeHash handshakeHash, SSLEngineImpl engine) {
        this.engine = engine;
        this.r = new EngineOutputRecord((byte) 22, engine);
        init(protocolVersion, helloVersion, handshakeHash);
    }

    private void init(ProtocolVersion protocolVersion, ProtocolVersion helloVersion, HandshakeHash handshakeHash) {
        this.r.setVersion(protocolVersion);
        this.r.setHelloVersion(helloVersion);
        this.r.setHandshakeHash(handshakeHash);
    }

    void doHashes() {
        this.r.doHashes();
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        while (len > 0) {
            int howmuch = Math.min(len, this.r.availableDataBytes());
            if (howmuch == 0) {
                flush();
            } else {
                this.r.write(buf, off, howmuch);
                off += howmuch;
                len -= howmuch;
            }
        }
    }

    public void write(int i) throws IOException {
        if (this.r.availableDataBytes() < 1) {
            flush();
        }
        this.r.write(i);
    }

    public void flush() throws IOException {
        if (this.socket != null) {
            try {
                this.socket.writeRecord(this.r);
                return;
            } catch (IOException e) {
                this.socket.waitForClose(true);
                throw e;
            }
        }
        this.engine.writeRecord((EngineOutputRecord) this.r);
    }

    void setFinishedMsg() {
        if (!-assertionsDisabled) {
            if ((this.socket == null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        ((EngineOutputRecord) this.r).setFinishedMsg();
    }

    void putInt8(int i) throws IOException {
        checkOverflow(i, 256);
        this.r.write(i);
    }

    void putInt16(int i) throws IOException {
        checkOverflow(i, 65536);
        if (this.r.availableDataBytes() < 2) {
            flush();
        }
        this.r.write(i >> 8);
        this.r.write(i);
    }

    void putInt24(int i) throws IOException {
        checkOverflow(i, Record.OVERFLOW_OF_INT24);
        if (this.r.availableDataBytes() < 3) {
            flush();
        }
        this.r.write(i >> 16);
        this.r.write(i >> 8);
        this.r.write(i);
    }

    void putInt32(int i) throws IOException {
        if (this.r.availableDataBytes() < 4) {
            flush();
        }
        this.r.write(i >> 24);
        this.r.write(i >> 16);
        this.r.write(i >> 8);
        this.r.write(i);
    }

    void putBytes8(byte[] b) throws IOException {
        if (b == null) {
            putInt8(0);
            return;
        }
        checkOverflow(b.length, 256);
        putInt8(b.length);
        write(b, 0, b.length);
    }

    public void putBytes16(byte[] b) throws IOException {
        if (b == null) {
            putInt16(0);
            return;
        }
        checkOverflow(b.length, 65536);
        putInt16(b.length);
        write(b, 0, b.length);
    }

    void putBytes24(byte[] b) throws IOException {
        if (b == null) {
            putInt24(0);
            return;
        }
        checkOverflow(b.length, Record.OVERFLOW_OF_INT24);
        putInt24(b.length);
        write(b, 0, b.length);
    }

    private void checkOverflow(int length, int overflow) {
        if (length >= overflow) {
            throw new RuntimeException("Field length overflow, the field length (" + length + ") should be less than " + overflow);
        }
    }
}

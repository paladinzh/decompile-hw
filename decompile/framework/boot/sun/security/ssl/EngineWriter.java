package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import sun.misc.HexDumpEncoder;

final class EngineWriter {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static final Debug debug = Debug.getInstance("ssl");
    private boolean outboundClosed = false;
    private LinkedList<Object> outboundList = new LinkedList();

    static {
        boolean z;
        if (EngineWriter.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    EngineWriter() {
    }

    private HandshakeStatus getOutboundData(ByteBuffer dstBB) {
        ByteBuffer msg = this.outboundList.removeFirst();
        if (-assertionsDisabled || (msg instanceof ByteBuffer)) {
            ByteBuffer bbIn = msg;
            if (!-assertionsDisabled) {
                if ((dstBB.remaining() >= bbIn.remaining() ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            dstBB.put(bbIn);
            if (!hasOutboundDataInternal()) {
                return null;
            }
            if (this.outboundList.getFirst() != HandshakeStatus.FINISHED) {
                return HandshakeStatus.NEED_WRAP;
            }
            this.outboundList.removeFirst();
            return HandshakeStatus.FINISHED;
        }
        throw new AssertionError();
    }

    synchronized void writeRecord(EngineOutputRecord outputRecord, MAC writeMAC, CipherBox writeCipher) throws IOException {
        if (this.outboundClosed) {
            throw new IOException("writer side was already closed.");
        }
        outputRecord.write(writeMAC, writeCipher);
        if (outputRecord.isFinishedMsg()) {
            this.outboundList.addLast(HandshakeStatus.FINISHED);
        }
    }

    private void dumpPacket(EngineArgs ea, boolean hsData) {
        try {
            HexDumpEncoder hd = new HexDumpEncoder();
            ByteBuffer bb = ea.netData.duplicate();
            int pos = bb.position();
            bb.position(pos - ea.deltaNet());
            bb.limit(pos);
            System.out.println("[Raw write" + (hsData ? "" : " (bb)") + "]: length = " + bb.remaining());
            hd.encodeBuffer(bb, System.out);
        } catch (IOException e) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized HandshakeStatus writeRecord(EngineOutputRecord outputRecord, EngineArgs ea, MAC writeMAC, CipherBox writeCipher) throws IOException {
        if (hasOutboundDataInternal()) {
            HandshakeStatus hss = getOutboundData(ea.netData);
            if (debug != null && Debug.isOn("packet")) {
                dumpPacket(ea, true);
            }
        } else if (this.outboundClosed) {
            throw new IOException("The write side was already closed");
        } else {
            outputRecord.write(ea, writeMAC, writeCipher);
            if (debug != null && Debug.isOn("packet")) {
                dumpPacket(ea, false);
            }
        }
    }

    void putOutboundData(ByteBuffer bytes) {
        this.outboundList.addLast(bytes);
    }

    synchronized void putOutboundDataSync(ByteBuffer bytes) throws IOException {
        if (this.outboundClosed) {
            throw new IOException("Write side already closed");
        }
        this.outboundList.addLast(bytes);
    }

    private boolean hasOutboundDataInternal() {
        return this.outboundList.size() != 0;
    }

    synchronized boolean hasOutboundData() {
        return hasOutboundDataInternal();
    }

    synchronized boolean isOutboundDone() {
        boolean z = false;
        synchronized (this) {
            if (this.outboundClosed && !hasOutboundDataInternal()) {
                z = true;
            }
        }
        return z;
    }

    synchronized void closeOutbound() {
        this.outboundClosed = true;
    }
}

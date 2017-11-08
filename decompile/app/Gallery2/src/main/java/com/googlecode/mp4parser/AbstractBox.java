package com.googlecode.mp4parser;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.ChannelHelper;
import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.googlecode.mp4parser.annotations.DoNotParseDetail;
import com.googlecode.mp4parser.util.CastUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Logger;

public abstract class AbstractBox implements Box {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static Logger LOG = Logger.getLogger(AbstractBox.class.getName());
    public static int MEM_MAP_THRESHOLD = 102400;
    private ByteBuffer content;
    private ByteBuffer deadBytes = null;
    private ContainerBox parent;
    protected String type;
    private byte[] userType;

    protected abstract void _parseDetails(ByteBuffer byteBuffer);

    protected abstract void getContent(ByteBuffer byteBuffer);

    protected abstract long getContentSize();

    static {
        boolean z;
        if (AbstractBox.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    protected AbstractBox(String type) {
        this.type = type;
    }

    protected AbstractBox(String type, byte[] userType) {
        this.type = type;
        this.userType = userType;
    }

    @DoNotParseDetail
    public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        if (!(readableByteChannel instanceof FileChannel) || contentSize <= ((long) MEM_MAP_THRESHOLD)) {
            if (!-assertionsDisabled) {
                if ((contentSize < 2147483647L ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            this.content = ChannelHelper.readFully(readableByteChannel, contentSize);
        } else {
            this.content = ((FileChannel) readableByteChannel).map(MapMode.READ_ONLY, ((FileChannel) readableByteChannel).position(), contentSize);
            ((FileChannel) readableByteChannel).position(((FileChannel) readableByteChannel).position() + contentSize);
        }
        if (!isParsed()) {
            parseDetails();
        }
    }

    public void getBox(WritableByteChannel os) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(CastUtils.l2i(getSize()));
        getHeader(bb);
        if (this.content == null) {
            getContent(bb);
            if (this.deadBytes != null) {
                this.deadBytes.rewind();
                while (this.deadBytes.remaining() > 0) {
                    bb.put(this.deadBytes);
                }
            }
        } else {
            this.content.rewind();
            bb.put(this.content);
        }
        bb.rewind();
        os.write(bb);
    }

    final synchronized void parseDetails() {
        if (this.content != null) {
            ByteBuffer content = this.content;
            this.content = null;
            content.rewind();
            _parseDetails(content);
            if (content.remaining() > 0) {
                this.deadBytes = content.slice();
            }
            if (!(-assertionsDisabled || verify(content))) {
                throw new AssertionError();
            }
        }
    }

    protected void setDeadBytes(ByteBuffer newDeadBytes) {
        this.deadBytes = newDeadBytes;
    }

    public long getSize() {
        int i = 8;
        int i2 = 0;
        long size = this.content == null ? getContentSize() : (long) this.content.limit();
        if (size < 4294967288L) {
            i = 0;
        }
        int i3 = i + 8;
        if ("uuid".equals(getType())) {
            i = 16;
        } else {
            i = 0;
        }
        size += (long) (i + i3);
        if (this.deadBytes != null) {
            i2 = this.deadBytes.limit();
        }
        return size + ((long) i2);
    }

    @DoNotParseDetail
    public String getType() {
        return this.type;
    }

    @DoNotParseDetail
    public byte[] getUserType() {
        return this.userType;
    }

    @DoNotParseDetail
    public ContainerBox getParent() {
        return this.parent;
    }

    @DoNotParseDetail
    public void setParent(ContainerBox parent) {
        this.parent = parent;
    }

    @DoNotParseDetail
    public IsoFile getIsoFile() {
        return this.parent.getIsoFile();
    }

    public boolean isParsed() {
        return this.content == null;
    }

    private boolean verify(ByteBuffer content) {
        ByteBuffer bb = ByteBuffer.allocate(CastUtils.l2i(((long) (this.deadBytes != null ? this.deadBytes.limit() : 0)) + getContentSize()));
        getContent(bb);
        if (this.deadBytes != null) {
            this.deadBytes.rewind();
            while (this.deadBytes.remaining() > 0) {
                bb.put(this.deadBytes);
            }
        }
        content.rewind();
        bb.rewind();
        if (content.remaining() != bb.remaining()) {
            LOG.severe(getType() + ": remaining differs " + content.remaining() + " vs. " + bb.remaining());
            return false;
        }
        int p = content.position();
        int i = content.limit() - 1;
        int j = bb.limit() - 1;
        while (i >= p) {
            if (content.get(i) != bb.get(j)) {
                LOG.severe(String.format("%s: buffers differ at %d: %2X/%2X", new Object[]{getType(), Integer.valueOf(i), Byte.valueOf(content.get(i)), Byte.valueOf(bb.get(j))}));
                byte[] b1 = new byte[content.remaining()];
                byte[] b2 = new byte[bb.remaining()];
                content.get(b1);
                bb.get(b2);
                System.err.println("original      : " + Hex.encodeHex(b1, 4));
                System.err.println("reconstructed : " + Hex.encodeHex(b2, 4));
                return false;
            }
            i--;
            j--;
        }
        return true;
    }

    private boolean isSmallBox() {
        long contentSize;
        if (this.content == null) {
            contentSize = (getContentSize() + ((long) (this.deadBytes != null ? this.deadBytes.limit() : 0))) + 8;
        } else {
            contentSize = (long) this.content.limit();
        }
        return contentSize < 4294967296L;
    }

    private void getHeader(ByteBuffer byteBuffer) {
        if (isSmallBox()) {
            IsoTypeWriter.writeUInt32(byteBuffer, getSize());
            byteBuffer.put(IsoFile.fourCCtoBytes(getType()));
        } else {
            IsoTypeWriter.writeUInt32(byteBuffer, 1);
            byteBuffer.put(IsoFile.fourCCtoBytes(getType()));
            IsoTypeWriter.writeUInt64(byteBuffer, getSize());
        }
        if ("uuid".equals(getType())) {
            byteBuffer.put(getUserType());
        }
    }
}

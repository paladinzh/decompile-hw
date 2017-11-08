package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.ChannelHelper;
import com.coremedia.iso.IsoTypeWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;

public class FreeBox implements Box {
    static final /* synthetic */ boolean -assertionsDisabled = (!FreeBox.class.desiredAssertionStatus());
    ByteBuffer data;
    private ContainerBox parent;
    List<Box> replacers = new LinkedList();

    public void getBox(WritableByteChannel os) throws IOException {
        for (Box replacer : this.replacers) {
            replacer.getBox(os);
        }
        ByteBuffer header = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt32(header, (long) (this.data.limit() + 8));
        header.put("free".getBytes());
        header.rewind();
        os.write(header);
        this.data.rewind();
        os.write(this.data);
    }

    public ContainerBox getParent() {
        return this.parent;
    }

    public void setParent(ContainerBox parent) {
        this.parent = parent;
    }

    public long getSize() {
        long size = 8;
        for (Box replacer : this.replacers) {
            size += replacer.getSize();
        }
        return size + ((long) this.data.limit());
    }

    public String getType() {
        return "free";
    }

    public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        if (!(readableByteChannel instanceof FileChannel) || contentSize <= 1048576) {
            if (!-assertionsDisabled) {
                if ((contentSize < 2147483647L ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            this.data = ChannelHelper.readFully(readableByteChannel, contentSize);
            return;
        }
        this.data = ((FileChannel) readableByteChannel).map(MapMode.READ_ONLY, ((FileChannel) readableByteChannel).position(), contentSize);
        ((FileChannel) readableByteChannel).position(((FileChannel) readableByteChannel).position() + contentSize);
    }
}

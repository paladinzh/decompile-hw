package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractContainerBox;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MetaBox extends AbstractContainerBox {
    private int flags = 0;
    private int version = 0;

    public MetaBox() {
        super("meta");
    }

    public long getContentSize() {
        if (isMp4Box()) {
            return super.getContentSize() + 4;
        }
        return super.getContentSize();
    }

    public void _parseDetails(ByteBuffer content) {
        int pos = content.position();
        content.get(new byte[4]);
        if ("hdlr".equals(IsoTypeReader.read4cc(content))) {
            content.position(pos);
            this.version = -1;
            this.flags = -1;
        } else {
            content.position(pos);
            this.version = IsoTypeReader.readUInt8(content);
            this.flags = IsoTypeReader.readUInt24(content);
        }
        while (content.remaining() >= 8) {
            try {
                this.boxes.add(this.boxParser.parseBox(new ByteBufferByteChannel(content), this));
            } catch (IOException e) {
                throw new RuntimeException("Sebastian needs to fix 7518765283");
            }
        }
        if (content.remaining() > 0) {
            throw new RuntimeException("Sebastian needs to fix it 90732r26537");
        }
    }

    protected void getContent(ByteBuffer byteBuffer) {
        if (isMp4Box()) {
            IsoTypeWriter.writeUInt8(byteBuffer, this.version);
            IsoTypeWriter.writeUInt24(byteBuffer, this.flags);
        }
        writeChildBoxes(byteBuffer);
    }

    public boolean isMp4Box() {
        return (this.version == -1 || this.flags == -1) ? false : true;
    }
}

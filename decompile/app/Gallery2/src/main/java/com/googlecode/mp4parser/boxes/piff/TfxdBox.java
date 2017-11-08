package com.googlecode.mp4parser.boxes.piff;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class TfxdBox extends AbstractFullBox {
    public long fragmentAbsoluteDuration;
    public long fragmentAbsoluteTime;

    public TfxdBox() {
        super("uuid");
    }

    public byte[] getUserType() {
        return new byte[]{(byte) 109, (byte) 29, (byte) -101, (byte) 5, (byte) 66, (byte) -43, (byte) 68, (byte) -26, Byte.MIN_VALUE, (byte) -30, (byte) 20, (byte) 29, (byte) -81, (byte) -9, (byte) 87, (byte) -78};
    }

    protected long getContentSize() {
        return (long) (getVersion() == 1 ? 20 : 12);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if (getVersion() == 1) {
            this.fragmentAbsoluteTime = IsoTypeReader.readUInt64(content);
            this.fragmentAbsoluteDuration = IsoTypeReader.readUInt64(content);
            return;
        }
        this.fragmentAbsoluteTime = IsoTypeReader.readUInt32(content);
        this.fragmentAbsoluteDuration = IsoTypeReader.readUInt32(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        if (getVersion() == 1) {
            IsoTypeWriter.writeUInt64(byteBuffer, this.fragmentAbsoluteTime);
            IsoTypeWriter.writeUInt64(byteBuffer, this.fragmentAbsoluteDuration);
            return;
        }
        IsoTypeWriter.writeUInt32(byteBuffer, this.fragmentAbsoluteTime);
        IsoTypeWriter.writeUInt32(byteBuffer, this.fragmentAbsoluteDuration);
    }
}

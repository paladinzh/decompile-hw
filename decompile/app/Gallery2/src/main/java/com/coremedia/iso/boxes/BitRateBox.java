package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractBox;
import java.nio.ByteBuffer;

public final class BitRateBox extends AbstractBox {
    private long avgBitrate;
    private long bufferSizeDb;
    private long maxBitrate;

    public BitRateBox() {
        super("btrt");
    }

    protected long getContentSize() {
        return 12;
    }

    public void _parseDetails(ByteBuffer content) {
        this.bufferSizeDb = IsoTypeReader.readUInt32(content);
        this.maxBitrate = IsoTypeReader.readUInt32(content);
        this.avgBitrate = IsoTypeReader.readUInt32(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeUInt32(byteBuffer, this.bufferSizeDb);
        IsoTypeWriter.writeUInt32(byteBuffer, this.maxBitrate);
        IsoTypeWriter.writeUInt32(byteBuffer, this.avgBitrate);
    }
}

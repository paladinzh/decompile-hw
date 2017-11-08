package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public final class AppleLosslessSpecificBox extends AbstractFullBox {
    private long bitRate;
    private int channels;
    private int historyMult;
    private int initialHistory;
    private int kModifier;
    private long maxCodedFrameSize;
    private long maxSamplePerFrame;
    private long sampleRate;
    private int sampleSize;
    private int unknown1;
    private int unknown2;

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.maxSamplePerFrame = IsoTypeReader.readUInt32(content);
        this.unknown1 = IsoTypeReader.readUInt8(content);
        this.sampleSize = IsoTypeReader.readUInt8(content);
        this.historyMult = IsoTypeReader.readUInt8(content);
        this.initialHistory = IsoTypeReader.readUInt8(content);
        this.kModifier = IsoTypeReader.readUInt8(content);
        this.channels = IsoTypeReader.readUInt8(content);
        this.unknown2 = IsoTypeReader.readUInt16(content);
        this.maxCodedFrameSize = IsoTypeReader.readUInt32(content);
        this.bitRate = IsoTypeReader.readUInt32(content);
        this.sampleRate = IsoTypeReader.readUInt32(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, this.maxSamplePerFrame);
        IsoTypeWriter.writeUInt8(byteBuffer, this.unknown1);
        IsoTypeWriter.writeUInt8(byteBuffer, this.sampleSize);
        IsoTypeWriter.writeUInt8(byteBuffer, this.historyMult);
        IsoTypeWriter.writeUInt8(byteBuffer, this.initialHistory);
        IsoTypeWriter.writeUInt8(byteBuffer, this.kModifier);
        IsoTypeWriter.writeUInt8(byteBuffer, this.channels);
        IsoTypeWriter.writeUInt16(byteBuffer, this.unknown2);
        IsoTypeWriter.writeUInt32(byteBuffer, this.maxCodedFrameSize);
        IsoTypeWriter.writeUInt32(byteBuffer, this.bitRate);
        IsoTypeWriter.writeUInt32(byteBuffer, this.sampleRate);
    }

    public AppleLosslessSpecificBox() {
        super("alac");
    }

    protected long getContentSize() {
        return 28;
    }
}

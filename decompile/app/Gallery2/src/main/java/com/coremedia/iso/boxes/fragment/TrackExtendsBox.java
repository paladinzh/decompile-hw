package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class TrackExtendsBox extends AbstractFullBox {
    private long defaultSampleDescriptionIndex;
    private long defaultSampleDuration;
    private SampleFlags defaultSampleFlags;
    private long defaultSampleSize;
    private long trackId;

    public TrackExtendsBox() {
        super("trex");
    }

    protected long getContentSize() {
        return 24;
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, this.trackId);
        IsoTypeWriter.writeUInt32(byteBuffer, this.defaultSampleDescriptionIndex);
        IsoTypeWriter.writeUInt32(byteBuffer, this.defaultSampleDuration);
        IsoTypeWriter.writeUInt32(byteBuffer, this.defaultSampleSize);
        this.defaultSampleFlags.getContent(byteBuffer);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.trackId = IsoTypeReader.readUInt32(content);
        this.defaultSampleDescriptionIndex = IsoTypeReader.readUInt32(content);
        this.defaultSampleDuration = IsoTypeReader.readUInt32(content);
        this.defaultSampleSize = IsoTypeReader.readUInt32(content);
        this.defaultSampleFlags = new SampleFlags(content);
    }

    public long getTrackId() {
        return this.trackId;
    }

    public long getDefaultSampleDuration() {
        return this.defaultSampleDuration;
    }

    public long getDefaultSampleSize() {
        return this.defaultSampleSize;
    }

    public SampleFlags getDefaultSampleFlags() {
        return this.defaultSampleFlags;
    }
}

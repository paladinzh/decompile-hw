package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import java.nio.ByteBuffer;

public class AudioSampleEntry extends SampleEntry implements ContainerBox {
    private long bytesPerFrame;
    private long bytesPerPacket;
    private long bytesPerSample;
    private int channelCount;
    private int compressionId;
    private int packetSize;
    private int reserved1;
    private long reserved2;
    private long sampleRate;
    private int sampleSize;
    private long samplesPerPacket;
    private int soundVersion;
    private byte[] soundVersion2Data;

    public long getSampleRate() {
        return this.sampleRate;
    }

    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        this.soundVersion = IsoTypeReader.readUInt16(content);
        this.reserved1 = IsoTypeReader.readUInt16(content);
        this.reserved2 = IsoTypeReader.readUInt32(content);
        this.channelCount = IsoTypeReader.readUInt16(content);
        this.sampleSize = IsoTypeReader.readUInt16(content);
        this.compressionId = IsoTypeReader.readUInt16(content);
        this.packetSize = IsoTypeReader.readUInt16(content);
        this.sampleRate = IsoTypeReader.readUInt32(content);
        if (!this.type.equals("mlpa")) {
            this.sampleRate >>>= 16;
        }
        if (this.soundVersion > 0) {
            this.samplesPerPacket = IsoTypeReader.readUInt32(content);
            this.bytesPerPacket = IsoTypeReader.readUInt32(content);
            this.bytesPerFrame = IsoTypeReader.readUInt32(content);
            this.bytesPerSample = IsoTypeReader.readUInt32(content);
        }
        if (this.soundVersion == 2) {
            this.soundVersion2Data = new byte[20];
            content.get(20);
        }
        _parseChildBoxes(content);
    }

    protected long getContentSize() {
        int i;
        int i2 = 0;
        if (this.soundVersion > 0) {
            i = 16;
        } else {
            i = 0;
        }
        long contentSize = 28 + ((long) i);
        if (this.soundVersion == 2) {
            i2 = 20;
        }
        contentSize += (long) i2;
        for (Box boxe : this.boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public String toString() {
        return "AudioSampleEntry{bytesPerSample=" + this.bytesPerSample + ", bytesPerFrame=" + this.bytesPerFrame + ", bytesPerPacket=" + this.bytesPerPacket + ", samplesPerPacket=" + this.samplesPerPacket + ", packetSize=" + this.packetSize + ", compressionId=" + this.compressionId + ", soundVersion=" + this.soundVersion + ", sampleRate=" + this.sampleRate + ", sampleSize=" + this.sampleSize + ", channelCount=" + this.channelCount + ", boxes=" + getBoxes() + '}';
    }

    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, this.soundVersion);
        IsoTypeWriter.writeUInt16(byteBuffer, this.reserved1);
        IsoTypeWriter.writeUInt32(byteBuffer, this.reserved2);
        IsoTypeWriter.writeUInt16(byteBuffer, this.channelCount);
        IsoTypeWriter.writeUInt16(byteBuffer, this.sampleSize);
        IsoTypeWriter.writeUInt16(byteBuffer, this.compressionId);
        IsoTypeWriter.writeUInt16(byteBuffer, this.packetSize);
        if (this.type.equals("mlpa")) {
            IsoTypeWriter.writeUInt32(byteBuffer, getSampleRate());
        } else {
            IsoTypeWriter.writeUInt32(byteBuffer, getSampleRate() << 16);
        }
        if (this.soundVersion > 0) {
            IsoTypeWriter.writeUInt32(byteBuffer, this.samplesPerPacket);
            IsoTypeWriter.writeUInt32(byteBuffer, this.bytesPerPacket);
            IsoTypeWriter.writeUInt32(byteBuffer, this.bytesPerFrame);
            IsoTypeWriter.writeUInt32(byteBuffer, this.bytesPerSample);
        }
        if (this.soundVersion == 2) {
            byteBuffer.put(this.soundVersion2Data);
        }
        _writeChildBoxes(byteBuffer);
    }
}

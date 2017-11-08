package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitWriterBuffer;
import java.nio.ByteBuffer;

public class DTSSpecificBox extends AbstractBox {
    long DTSSamplingFrequency;
    int LBRDurationMod;
    long avgBitRate;
    int channelLayout;
    int coreLFEPresent;
    int coreLayout;
    int coreSize;
    int frameDuration;
    long maxBitRate;
    int multiAssetFlag;
    int pcmSampleDepth;
    int representationType;
    int reserved;
    int reservedBoxPresent;
    int stereoDownmix;
    int streamConstruction;

    public DTSSpecificBox() {
        super("ddts");
    }

    protected long getContentSize() {
        return 20;
    }

    public void _parseDetails(ByteBuffer content) {
        this.DTSSamplingFrequency = IsoTypeReader.readUInt32(content);
        this.maxBitRate = IsoTypeReader.readUInt32(content);
        this.avgBitRate = IsoTypeReader.readUInt32(content);
        this.pcmSampleDepth = IsoTypeReader.readUInt8(content);
        BitReaderBuffer brb = new BitReaderBuffer(content);
        this.frameDuration = brb.readBits(2);
        this.streamConstruction = brb.readBits(5);
        this.coreLFEPresent = brb.readBits(1);
        this.coreLayout = brb.readBits(6);
        this.coreSize = brb.readBits(14);
        this.stereoDownmix = brb.readBits(1);
        this.representationType = brb.readBits(3);
        this.channelLayout = brb.readBits(16);
        this.multiAssetFlag = brb.readBits(1);
        this.LBRDurationMod = brb.readBits(1);
        this.reservedBoxPresent = brb.readBits(1);
        this.reserved = brb.readBits(5);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeUInt32(byteBuffer, this.DTSSamplingFrequency);
        IsoTypeWriter.writeUInt32(byteBuffer, this.maxBitRate);
        IsoTypeWriter.writeUInt32(byteBuffer, this.avgBitRate);
        IsoTypeWriter.writeUInt8(byteBuffer, this.pcmSampleDepth);
        BitWriterBuffer bwb = new BitWriterBuffer(byteBuffer);
        bwb.writeBits(this.frameDuration, 2);
        bwb.writeBits(this.streamConstruction, 5);
        bwb.writeBits(this.coreLFEPresent, 1);
        bwb.writeBits(this.coreLayout, 6);
        bwb.writeBits(this.coreSize, 14);
        bwb.writeBits(this.stereoDownmix, 1);
        bwb.writeBits(this.representationType, 3);
        bwb.writeBits(this.channelLayout, 16);
        bwb.writeBits(this.multiAssetFlag, 1);
        bwb.writeBits(this.LBRDurationMod, 1);
        bwb.writeBits(this.reservedBoxPresent, 1);
        bwb.writeBits(this.reserved, 5);
    }
}

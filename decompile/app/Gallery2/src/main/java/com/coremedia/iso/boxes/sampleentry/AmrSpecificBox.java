package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractBox;
import java.nio.ByteBuffer;

public class AmrSpecificBox extends AbstractBox {
    private int decoderVersion;
    private int framesPerSample;
    private int modeChangePeriod;
    private int modeSet;
    private String vendor;

    public AmrSpecificBox() {
        super("damr");
    }

    public String getVendor() {
        return this.vendor;
    }

    public int getDecoderVersion() {
        return this.decoderVersion;
    }

    public int getModeSet() {
        return this.modeSet;
    }

    public int getModeChangePeriod() {
        return this.modeChangePeriod;
    }

    public int getFramesPerSample() {
        return this.framesPerSample;
    }

    protected long getContentSize() {
        return 9;
    }

    public void _parseDetails(ByteBuffer content) {
        byte[] v = new byte[4];
        content.get(v);
        this.vendor = IsoFile.bytesToFourCC(v);
        this.decoderVersion = IsoTypeReader.readUInt8(content);
        this.modeSet = IsoTypeReader.readUInt16(content);
        this.modeChangePeriod = IsoTypeReader.readUInt8(content);
        this.framesPerSample = IsoTypeReader.readUInt8(content);
    }

    public void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(IsoFile.fourCCtoBytes(this.vendor));
        IsoTypeWriter.writeUInt8(byteBuffer, this.decoderVersion);
        IsoTypeWriter.writeUInt16(byteBuffer, this.modeSet);
        IsoTypeWriter.writeUInt8(byteBuffer, this.modeChangePeriod);
        IsoTypeWriter.writeUInt8(byteBuffer, this.framesPerSample);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("AmrSpecificBox[vendor=").append(getVendor());
        buffer.append(";decoderVersion=").append(getDecoderVersion());
        buffer.append(";modeSet=").append(getModeSet());
        buffer.append(";modeChangePeriod=").append(getModeChangePeriod());
        buffer.append(";framesPerSample=").append(getFramesPerSample());
        buffer.append("]");
        return buffer.toString();
    }
}

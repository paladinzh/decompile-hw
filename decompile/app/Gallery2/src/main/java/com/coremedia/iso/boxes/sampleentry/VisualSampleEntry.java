package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import java.nio.ByteBuffer;

public class VisualSampleEntry extends SampleEntry implements ContainerBox {
    static final /* synthetic */ boolean -assertionsDisabled = (!VisualSampleEntry.class.desiredAssertionStatus());
    private String compressorname;
    private int depth;
    private int frameCount;
    private int height;
    private double horizresolution;
    private long[] predefined;
    private double vertresolution;
    private int width;

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public double getHorizresolution() {
        return this.horizresolution;
    }

    public double getVertresolution() {
        return this.vertresolution;
    }

    public int getFrameCount() {
        return this.frameCount;
    }

    public String getCompressorname() {
        return this.compressorname;
    }

    public int getDepth() {
        return this.depth;
    }

    public void _parseDetails(ByteBuffer content) {
        int i = 1;
        _parseReservedAndDataReferenceIndex(content);
        long tmp = (long) IsoTypeReader.readUInt16(content);
        if (!-assertionsDisabled) {
            if ((0 == tmp ? 1 : 0) == 0) {
                throw new AssertionError("reserved byte not 0");
            }
        }
        tmp = (long) IsoTypeReader.readUInt16(content);
        if (!-assertionsDisabled) {
            if ((0 == tmp ? 1 : 0) == 0) {
                throw new AssertionError("reserved byte not 0");
            }
        }
        this.predefined[0] = IsoTypeReader.readUInt32(content);
        this.predefined[1] = IsoTypeReader.readUInt32(content);
        this.predefined[2] = IsoTypeReader.readUInt32(content);
        this.width = IsoTypeReader.readUInt16(content);
        this.height = IsoTypeReader.readUInt16(content);
        this.horizresolution = IsoTypeReader.readFixedPoint1616(content);
        this.vertresolution = IsoTypeReader.readFixedPoint1616(content);
        tmp = IsoTypeReader.readUInt32(content);
        if (!-assertionsDisabled) {
            if ((0 == tmp ? 1 : 0) == 0) {
                throw new AssertionError("reserved byte not 0");
            }
        }
        this.frameCount = IsoTypeReader.readUInt16(content);
        int compressornameDisplayAbleData = IsoTypeReader.readUInt8(content);
        if (compressornameDisplayAbleData > 31) {
            System.out.println("invalid compressor name displayable data: " + compressornameDisplayAbleData);
            compressornameDisplayAbleData = 31;
        }
        byte[] bytes = new byte[compressornameDisplayAbleData];
        content.get(bytes);
        this.compressorname = Utf8.convert(bytes);
        if (compressornameDisplayAbleData < 31) {
            content.get(new byte[(31 - compressornameDisplayAbleData)]);
        }
        this.depth = IsoTypeReader.readUInt16(content);
        tmp = (long) IsoTypeReader.readUInt16(content);
        if (!-assertionsDisabled) {
            if (65535 != tmp) {
                i = 0;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        _parseChildBoxes(content);
    }

    protected long getContentSize() {
        long contentSize = 78;
        for (Box boxe : this.boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, 0);
        IsoTypeWriter.writeUInt16(byteBuffer, 0);
        IsoTypeWriter.writeUInt32(byteBuffer, this.predefined[0]);
        IsoTypeWriter.writeUInt32(byteBuffer, this.predefined[1]);
        IsoTypeWriter.writeUInt32(byteBuffer, this.predefined[2]);
        IsoTypeWriter.writeUInt16(byteBuffer, getWidth());
        IsoTypeWriter.writeUInt16(byteBuffer, getHeight());
        IsoTypeWriter.writeFixedPont1616(byteBuffer, getHorizresolution());
        IsoTypeWriter.writeFixedPont1616(byteBuffer, getVertresolution());
        IsoTypeWriter.writeUInt32(byteBuffer, 0);
        IsoTypeWriter.writeUInt16(byteBuffer, getFrameCount());
        IsoTypeWriter.writeUInt8(byteBuffer, Utf8.utf8StringLengthInBytes(getCompressorname()));
        byteBuffer.put(Utf8.convert(getCompressorname()));
        int a = Utf8.utf8StringLengthInBytes(getCompressorname());
        while (a < 31) {
            a++;
            byteBuffer.put((byte) 0);
        }
        IsoTypeWriter.writeUInt16(byteBuffer, getDepth());
        IsoTypeWriter.writeUInt16(byteBuffer, 65535);
        _writeChildBoxes(byteBuffer);
    }
}

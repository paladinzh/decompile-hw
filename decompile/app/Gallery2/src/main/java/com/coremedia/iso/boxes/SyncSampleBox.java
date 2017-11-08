package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;

public class SyncSampleBox extends AbstractFullBox {
    private long[] sampleNumber;

    public SyncSampleBox() {
        super("stss");
    }

    public long[] getSampleNumber() {
        return this.sampleNumber;
    }

    protected long getContentSize() {
        return (long) ((this.sampleNumber.length * 4) + 8);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int entryCount = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        this.sampleNumber = new long[entryCount];
        for (int i = 0; i < entryCount; i++) {
            this.sampleNumber[i] = IsoTypeReader.readUInt32(content);
        }
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.sampleNumber.length);
        for (long aSampleNumber : this.sampleNumber) {
            IsoTypeWriter.writeUInt32(byteBuffer, aSampleNumber);
        }
    }

    public String toString() {
        return "SyncSampleBox[entryCount=" + this.sampleNumber.length + "]";
    }

    public void setSampleNumber(long[] sampleNumber) {
        this.sampleNumber = sampleNumber;
    }
}

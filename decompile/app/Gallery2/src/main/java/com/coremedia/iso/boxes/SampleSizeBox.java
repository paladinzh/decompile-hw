package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;

public class SampleSizeBox extends AbstractFullBox {
    int sampleCount;
    private long sampleSize;
    private long[] sampleSizes = new long[0];

    public SampleSizeBox() {
        super("stsz");
    }

    public long getSampleSize() {
        return this.sampleSize;
    }

    public long getSampleCount() {
        if (this.sampleSize > 0) {
            return (long) this.sampleCount;
        }
        return (long) this.sampleSizes.length;
    }

    public long[] getSampleSizes() {
        return this.sampleSizes;
    }

    public void setSampleSizes(long[] sampleSizes) {
        this.sampleSizes = sampleSizes;
    }

    protected long getContentSize() {
        return (long) ((this.sampleSize == 0 ? this.sampleSizes.length * 4 : 0) + 12);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.sampleSize = IsoTypeReader.readUInt32(content);
        this.sampleCount = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        if (this.sampleSize == 0) {
            this.sampleSizes = new long[this.sampleCount];
            for (int i = 0; i < this.sampleCount; i++) {
                this.sampleSizes[i] = IsoTypeReader.readUInt32(content);
            }
        }
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, this.sampleSize);
        if (this.sampleSize == 0) {
            IsoTypeWriter.writeUInt32(byteBuffer, (long) this.sampleSizes.length);
            for (long sampleSize1 : this.sampleSizes) {
                IsoTypeWriter.writeUInt32(byteBuffer, sampleSize1);
            }
            return;
        }
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.sampleCount);
    }

    public String toString() {
        return "SampleSizeBox[sampleSize=" + getSampleSize() + ";sampleCount=" + getSampleCount() + "]";
    }
}

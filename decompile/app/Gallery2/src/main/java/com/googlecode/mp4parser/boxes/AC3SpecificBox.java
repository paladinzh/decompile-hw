package com.googlecode.mp4parser.boxes;

import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitWriterBuffer;
import java.nio.ByteBuffer;

public class AC3SpecificBox extends AbstractBox {
    int acmod;
    int bitRateCode;
    int bsid;
    int bsmod;
    int fscod;
    int lfeon;
    int reserved;

    public AC3SpecificBox() {
        super("dac3");
    }

    protected long getContentSize() {
        return 3;
    }

    public void _parseDetails(ByteBuffer content) {
        BitReaderBuffer brb = new BitReaderBuffer(content);
        this.fscod = brb.readBits(2);
        this.bsid = brb.readBits(5);
        this.bsmod = brb.readBits(3);
        this.acmod = brb.readBits(3);
        this.lfeon = brb.readBits(1);
        this.bitRateCode = brb.readBits(5);
        this.reserved = brb.readBits(5);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        BitWriterBuffer bwb = new BitWriterBuffer(byteBuffer);
        bwb.writeBits(this.fscod, 2);
        bwb.writeBits(this.bsid, 5);
        bwb.writeBits(this.bsmod, 3);
        bwb.writeBits(this.acmod, 3);
        bwb.writeBits(this.lfeon, 1);
        bwb.writeBits(this.bitRateCode, 5);
        bwb.writeBits(this.reserved, 5);
    }

    public String toString() {
        return "AC3SpecificBox{fscod=" + this.fscod + ", bsid=" + this.bsid + ", bsmod=" + this.bsmod + ", acmod=" + this.acmod + ", lfeon=" + this.lfeon + ", bitRateCode=" + this.bitRateCode + ", reserved=" + this.reserved + '}';
    }
}

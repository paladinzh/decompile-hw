package com.googlecode.mp4parser.boxes;

import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitWriterBuffer;
import java.nio.ByteBuffer;

public class MLPSpecificBox extends AbstractBox {
    int format_info;
    int peak_data_rate;
    int reserved;
    int reserved2;

    public MLPSpecificBox() {
        super("dmlp");
    }

    protected long getContentSize() {
        return 10;
    }

    public void _parseDetails(ByteBuffer content) {
        BitReaderBuffer brb = new BitReaderBuffer(content);
        this.format_info = brb.readBits(32);
        this.peak_data_rate = brb.readBits(15);
        this.reserved = brb.readBits(1);
        this.reserved2 = brb.readBits(32);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        BitWriterBuffer bwb = new BitWriterBuffer(byteBuffer);
        bwb.writeBits(this.format_info, 32);
        bwb.writeBits(this.peak_data_rate, 15);
        bwb.writeBits(this.reserved, 1);
        bwb.writeBits(this.reserved2, 32);
    }
}

package com.googlecode.mp4parser.boxes.apple;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;
import java.nio.ByteBuffer;

public class TimeCodeBox extends SampleEntry {
    byte[] data;

    public TimeCodeBox() {
        super("tmcd");
    }

    protected long getContentSize() {
        long size = 26;
        for (Box box : this.boxes) {
            size += box.getSize();
        }
        return size;
    }

    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        this.data = new byte[18];
        content.get(this.data);
        _parseChildBoxes(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        byteBuffer.put(this.data);
        _writeChildBoxes(byteBuffer);
    }
}

package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class DataEntryUrlBox extends AbstractFullBox {
    public DataEntryUrlBox() {
        super("url ");
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
    }

    protected long getContentSize() {
        return 4;
    }

    public String toString() {
        return "DataEntryUrlBox[]";
    }
}

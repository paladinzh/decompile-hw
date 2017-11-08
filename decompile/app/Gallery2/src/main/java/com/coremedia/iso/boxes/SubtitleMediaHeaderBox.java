package com.coremedia.iso.boxes;

import java.nio.ByteBuffer;

public class SubtitleMediaHeaderBox extends AbstractMediaHeaderBox {
    public SubtitleMediaHeaderBox() {
        super("sthd");
    }

    protected long getContentSize() {
        return 4;
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
    }

    public String toString() {
        return "SubtitleMediaHeaderBox";
    }
}

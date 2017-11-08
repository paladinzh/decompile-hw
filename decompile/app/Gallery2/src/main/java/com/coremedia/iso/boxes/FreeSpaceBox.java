package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.AbstractBox;
import java.nio.ByteBuffer;

public class FreeSpaceBox extends AbstractBox {
    byte[] data;

    protected long getContentSize() {
        return (long) this.data.length;
    }

    public FreeSpaceBox() {
        super("skip");
    }

    public void _parseDetails(ByteBuffer content) {
        this.data = new byte[content.remaining()];
        content.get(this.data);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(this.data);
    }

    public String toString() {
        return "FreeSpaceBox[size=" + this.data.length + ";type=" + getType() + "]";
    }
}

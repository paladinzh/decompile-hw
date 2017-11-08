package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public final class AppleNameBox extends AbstractFullBox {
    private String name;

    public AppleNameBox() {
        super("name");
    }

    protected long getContentSize() {
        return (long) (Utf8.convert(this.name).length + 4);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.name = IsoTypeReader.readString(content, content.remaining());
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf8.convert(this.name));
    }
}

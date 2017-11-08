package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public final class AppleMeanBox extends AbstractFullBox {
    private String meaning;

    public AppleMeanBox() {
        super("mean");
    }

    protected long getContentSize() {
        return (long) (Utf8.utf8StringLengthInBytes(this.meaning) + 4);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.meaning = IsoTypeReader.readString(content, content.remaining());
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf8.convert(this.meaning));
    }
}

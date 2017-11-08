package com.coremedia.iso.boxes.vodafone;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class CoverUriBox extends AbstractFullBox {
    private String coverUri;

    public CoverUriBox() {
        super("cvru");
    }

    public String getCoverUri() {
        return this.coverUri;
    }

    protected long getContentSize() {
        return (long) (Utf8.utf8StringLengthInBytes(this.coverUri) + 5);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.coverUri = IsoTypeReader.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf8.convert(this.coverUri));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        return "CoverUriBox[coverUri=" + getCoverUri() + "]";
    }
}

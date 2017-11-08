package com.coremedia.iso.boxes.vodafone;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class LyricsUriBox extends AbstractFullBox {
    private String lyricsUri;

    public LyricsUriBox() {
        super("lrcu");
    }

    public String getLyricsUri() {
        return this.lyricsUri;
    }

    protected long getContentSize() {
        return (long) (Utf8.utf8StringLengthInBytes(this.lyricsUri) + 5);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.lyricsUri = IsoTypeReader.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf8.convert(this.lyricsUri));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        return "LyricsUriBox[lyricsUri=" + getLyricsUri() + "]";
    }
}

package com.coremedia.iso.boxes.vodafone;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class AlbumArtistBox extends AbstractFullBox {
    private String albumArtist;
    private String language;

    public AlbumArtistBox() {
        super("albr");
    }

    public String getLanguage() {
        return this.language;
    }

    public String getAlbumArtist() {
        return this.albumArtist;
    }

    protected long getContentSize() {
        return (long) ((Utf8.utf8StringLengthInBytes(this.albumArtist) + 6) + 1);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.language = IsoTypeReader.readIso639(content);
        this.albumArtist = IsoTypeReader.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeIso639(byteBuffer, this.language);
        byteBuffer.put(Utf8.convert(this.albumArtist));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        return "AlbumArtistBox[language=" + getLanguage() + ";albumArtist=" + getAlbumArtist() + "]";
    }
}

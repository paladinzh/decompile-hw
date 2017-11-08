package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class AlbumBox extends AbstractFullBox {
    private String albumTitle;
    private String language;
    private int trackNumber;

    public AlbumBox() {
        super("albm");
    }

    public String getLanguage() {
        return this.language;
    }

    public String getAlbumTitle() {
        return this.albumTitle;
    }

    public int getTrackNumber() {
        return this.trackNumber;
    }

    protected long getContentSize() {
        return (long) ((this.trackNumber == -1 ? 0 : 1) + ((Utf8.utf8StringLengthInBytes(this.albumTitle) + 6) + 1));
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.language = IsoTypeReader.readIso639(content);
        this.albumTitle = IsoTypeReader.readString(content);
        if (content.remaining() > 0) {
            this.trackNumber = IsoTypeReader.readUInt8(content);
        } else {
            this.trackNumber = -1;
        }
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeIso639(byteBuffer, this.language);
        byteBuffer.put(Utf8.convert(this.albumTitle));
        byteBuffer.put((byte) 0);
        if (this.trackNumber != -1) {
            IsoTypeWriter.writeUInt8(byteBuffer, this.trackNumber);
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("AlbumBox[language=").append(getLanguage()).append(";");
        buffer.append("albumTitle=").append(getAlbumTitle());
        if (this.trackNumber >= 0) {
            buffer.append(";trackNumber=").append(getTrackNumber());
        }
        buffer.append("]");
        return buffer.toString();
    }
}

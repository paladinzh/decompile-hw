package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class PerformerBox extends AbstractFullBox {
    private String language;
    private String performer;

    public PerformerBox() {
        super("perf");
    }

    public String getLanguage() {
        return this.language;
    }

    public String getPerformer() {
        return this.performer;
    }

    protected long getContentSize() {
        return (long) ((Utf8.utf8StringLengthInBytes(this.performer) + 6) + 1);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeIso639(byteBuffer, this.language);
        byteBuffer.put(Utf8.convert(this.performer));
        byteBuffer.put((byte) 0);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.language = IsoTypeReader.readIso639(content);
        this.performer = IsoTypeReader.readString(content);
    }

    public String toString() {
        return "PerformerBox[language=" + getLanguage() + ";performer=" + getPerformer() + "]";
    }
}

package com.coremedia.iso.boxes.vodafone;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class ContentDistributorIdBox extends AbstractFullBox {
    private String contentDistributorId;
    private String language;

    public ContentDistributorIdBox() {
        super("cdis");
    }

    public String getLanguage() {
        return this.language;
    }

    public String getContentDistributorId() {
        return this.contentDistributorId;
    }

    protected long getContentSize() {
        return (long) ((Utf8.utf8StringLengthInBytes(this.contentDistributorId) + 2) + 5);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.language = IsoTypeReader.readIso639(content);
        this.contentDistributorId = IsoTypeReader.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeIso639(byteBuffer, this.language);
        byteBuffer.put(Utf8.convert(this.contentDistributorId));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        return "ContentDistributorIdBox[language=" + getLanguage() + ";contentDistributorId=" + getContentDistributorId() + "]";
    }
}

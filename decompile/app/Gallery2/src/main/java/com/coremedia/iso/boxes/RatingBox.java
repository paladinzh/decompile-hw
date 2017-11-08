package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class RatingBox extends AbstractFullBox {
    private String language;
    private String ratingCriteria;
    private String ratingEntity;
    private String ratingInfo;

    public RatingBox() {
        super("rtng");
    }

    public String getLanguage() {
        return this.language;
    }

    public String getRatingEntity() {
        return this.ratingEntity;
    }

    public String getRatingCriteria() {
        return this.ratingCriteria;
    }

    public String getRatingInfo() {
        return this.ratingInfo;
    }

    protected long getContentSize() {
        return (long) (Utf8.utf8StringLengthInBytes(this.ratingInfo) + 15);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.ratingEntity = IsoTypeReader.read4cc(content);
        this.ratingCriteria = IsoTypeReader.read4cc(content);
        this.language = IsoTypeReader.readIso639(content);
        this.ratingInfo = IsoTypeReader.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(IsoFile.fourCCtoBytes(this.ratingEntity));
        byteBuffer.put(IsoFile.fourCCtoBytes(this.ratingCriteria));
        IsoTypeWriter.writeIso639(byteBuffer, this.language);
        byteBuffer.put(Utf8.convert(this.ratingInfo));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("RatingBox[language=").append(getLanguage());
        buffer.append("ratingEntity=").append(getRatingEntity());
        buffer.append(";ratingCriteria=").append(getRatingCriteria());
        buffer.append(";language=").append(getLanguage());
        buffer.append(";ratingInfo=").append(getRatingInfo());
        buffer.append("]");
        return buffer.toString();
    }
}

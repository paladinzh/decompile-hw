package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class ClassificationBox extends AbstractFullBox {
    private String classificationEntity;
    private String classificationInfo;
    private int classificationTableIndex;
    private String language;

    public ClassificationBox() {
        super("clsf");
    }

    public String getLanguage() {
        return this.language;
    }

    public String getClassificationEntity() {
        return this.classificationEntity;
    }

    public int getClassificationTableIndex() {
        return this.classificationTableIndex;
    }

    public String getClassificationInfo() {
        return this.classificationInfo;
    }

    protected long getContentSize() {
        return (long) ((Utf8.utf8StringLengthInBytes(this.classificationInfo) + 8) + 1);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        byte[] cE = new byte[4];
        content.get(cE);
        this.classificationEntity = IsoFile.bytesToFourCC(cE);
        this.classificationTableIndex = IsoTypeReader.readUInt16(content);
        this.language = IsoTypeReader.readIso639(content);
        this.classificationInfo = IsoTypeReader.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(IsoFile.fourCCtoBytes(this.classificationEntity));
        IsoTypeWriter.writeUInt16(byteBuffer, this.classificationTableIndex);
        IsoTypeWriter.writeIso639(byteBuffer, this.language);
        byteBuffer.put(Utf8.convert(this.classificationInfo));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("ClassificationBox[language=").append(getLanguage());
        buffer.append("classificationEntity=").append(getClassificationEntity());
        buffer.append(";classificationTableIndex=").append(getClassificationTableIndex());
        buffer.append(";language=").append(getLanguage());
        buffer.append(";classificationInfo=").append(getClassificationInfo());
        buffer.append("]");
        return buffer.toString();
    }
}

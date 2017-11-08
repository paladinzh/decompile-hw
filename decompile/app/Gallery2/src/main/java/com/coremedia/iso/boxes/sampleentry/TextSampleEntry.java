package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import java.nio.ByteBuffer;

public class TextSampleEntry extends SampleEntry {
    private int[] backgroundColorRgba;
    private BoxRecord boxRecord;
    private long displayFlags;
    private int horizontalJustification;
    private StyleRecord styleRecord;
    private int verticalJustification;

    public static class BoxRecord {
        int bottom;
        int left;
        int right;
        int top;

        public void parse(ByteBuffer in) {
            this.top = IsoTypeReader.readUInt16(in);
            this.left = IsoTypeReader.readUInt16(in);
            this.bottom = IsoTypeReader.readUInt16(in);
            this.right = IsoTypeReader.readUInt16(in);
        }

        public void getContent(ByteBuffer bb) {
            IsoTypeWriter.writeUInt16(bb, this.top);
            IsoTypeWriter.writeUInt16(bb, this.left);
            IsoTypeWriter.writeUInt16(bb, this.bottom);
            IsoTypeWriter.writeUInt16(bb, this.right);
        }

        public int getSize() {
            return 8;
        }
    }

    public static class StyleRecord {
        int endChar;
        int faceStyleFlags;
        int fontId;
        int fontSize;
        int startChar;
        int[] textColor = new int[]{255, 255, 255, 255};

        public void parse(ByteBuffer in) {
            this.startChar = IsoTypeReader.readUInt16(in);
            this.endChar = IsoTypeReader.readUInt16(in);
            this.fontId = IsoTypeReader.readUInt16(in);
            this.faceStyleFlags = IsoTypeReader.readUInt8(in);
            this.fontSize = IsoTypeReader.readUInt8(in);
            this.textColor = new int[4];
            this.textColor[0] = IsoTypeReader.readUInt8(in);
            this.textColor[1] = IsoTypeReader.readUInt8(in);
            this.textColor[2] = IsoTypeReader.readUInt8(in);
            this.textColor[3] = IsoTypeReader.readUInt8(in);
        }

        public void getContent(ByteBuffer bb) {
            IsoTypeWriter.writeUInt16(bb, this.startChar);
            IsoTypeWriter.writeUInt16(bb, this.endChar);
            IsoTypeWriter.writeUInt16(bb, this.fontId);
            IsoTypeWriter.writeUInt8(bb, this.faceStyleFlags);
            IsoTypeWriter.writeUInt8(bb, this.fontSize);
            IsoTypeWriter.writeUInt8(bb, this.textColor[0]);
            IsoTypeWriter.writeUInt8(bb, this.textColor[1]);
            IsoTypeWriter.writeUInt8(bb, this.textColor[2]);
            IsoTypeWriter.writeUInt8(bb, this.textColor[3]);
        }

        public int getSize() {
            return 12;
        }
    }

    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        this.displayFlags = IsoTypeReader.readUInt32(content);
        this.horizontalJustification = IsoTypeReader.readUInt8(content);
        this.verticalJustification = IsoTypeReader.readUInt8(content);
        this.backgroundColorRgba = new int[4];
        this.backgroundColorRgba[0] = IsoTypeReader.readUInt8(content);
        this.backgroundColorRgba[1] = IsoTypeReader.readUInt8(content);
        this.backgroundColorRgba[2] = IsoTypeReader.readUInt8(content);
        this.backgroundColorRgba[3] = IsoTypeReader.readUInt8(content);
        this.boxRecord = new BoxRecord();
        this.boxRecord.parse(content);
        this.styleRecord = new StyleRecord();
        this.styleRecord.parse(content);
        _parseChildBoxes(content);
    }

    protected long getContentSize() {
        long contentSize = (18 + ((long) this.boxRecord.getSize())) + ((long) this.styleRecord.getSize());
        for (Box boxe : this.boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public String toString() {
        return "TextSampleEntry";
    }

    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, this.displayFlags);
        IsoTypeWriter.writeUInt8(byteBuffer, this.horizontalJustification);
        IsoTypeWriter.writeUInt8(byteBuffer, this.verticalJustification);
        IsoTypeWriter.writeUInt8(byteBuffer, this.backgroundColorRgba[0]);
        IsoTypeWriter.writeUInt8(byteBuffer, this.backgroundColorRgba[1]);
        IsoTypeWriter.writeUInt8(byteBuffer, this.backgroundColorRgba[2]);
        IsoTypeWriter.writeUInt8(byteBuffer, this.backgroundColorRgba[3]);
        this.boxRecord.getContent(byteBuffer);
        this.styleRecord.getContent(byteBuffer);
        _writeChildBoxes(byteBuffer);
    }
}

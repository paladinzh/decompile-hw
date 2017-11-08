package com.googlecode.mp4parser.boxes.apple;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;
import com.huawei.watermark.ui.WMEditor;
import java.nio.ByteBuffer;

public class QuicktimeTextSampleEntry extends SampleEntry {
    int backgroundB;
    int backgroundG;
    int backgroundR;
    long defaultTextBox;
    int displayFlags;
    short fontFace;
    String fontName = "";
    short fontNumber;
    int foregroundB = 65535;
    int foregroundG = 65535;
    int foregroundR = 65535;
    long reserved1;
    byte reserved2;
    short reserved3;
    int textJustification;

    public QuicktimeTextSampleEntry() {
        super(WMEditor.TYPETEXT);
    }

    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        this.displayFlags = content.getInt();
        this.textJustification = content.getInt();
        this.backgroundR = IsoTypeReader.readUInt16(content);
        this.backgroundG = IsoTypeReader.readUInt16(content);
        this.backgroundB = IsoTypeReader.readUInt16(content);
        this.defaultTextBox = IsoTypeReader.readUInt64(content);
        this.reserved1 = IsoTypeReader.readUInt64(content);
        this.fontNumber = content.getShort();
        this.fontFace = content.getShort();
        this.reserved2 = content.get();
        this.reserved3 = content.getShort();
        this.foregroundR = IsoTypeReader.readUInt16(content);
        this.foregroundG = IsoTypeReader.readUInt16(content);
        this.foregroundB = IsoTypeReader.readUInt16(content);
        if (content.remaining() > 0) {
            byte[] myFontName = new byte[IsoTypeReader.readUInt8(content)];
            content.get(myFontName);
            this.fontName = new String(myFontName);
            return;
        }
        this.fontName = null;
    }

    protected long getContentSize() {
        return (long) ((this.fontName != null ? this.fontName.length() : 0) + 52);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        byteBuffer.putInt(this.displayFlags);
        byteBuffer.putInt(this.textJustification);
        IsoTypeWriter.writeUInt16(byteBuffer, this.backgroundR);
        IsoTypeWriter.writeUInt16(byteBuffer, this.backgroundG);
        IsoTypeWriter.writeUInt16(byteBuffer, this.backgroundB);
        IsoTypeWriter.writeUInt64(byteBuffer, this.defaultTextBox);
        IsoTypeWriter.writeUInt64(byteBuffer, this.reserved1);
        byteBuffer.putShort(this.fontNumber);
        byteBuffer.putShort(this.fontFace);
        byteBuffer.put(this.reserved2);
        byteBuffer.putShort(this.reserved3);
        IsoTypeWriter.writeUInt16(byteBuffer, this.foregroundR);
        IsoTypeWriter.writeUInt16(byteBuffer, this.foregroundG);
        IsoTypeWriter.writeUInt16(byteBuffer, this.foregroundB);
        if (this.fontName != null) {
            IsoTypeWriter.writeUInt8(byteBuffer, this.fontName.length());
            byteBuffer.put(this.fontName.getBytes());
        }
    }
}

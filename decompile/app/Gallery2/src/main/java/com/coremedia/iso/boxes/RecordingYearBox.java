package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class RecordingYearBox extends AbstractFullBox {
    int recordingYear;

    public RecordingYearBox() {
        super("yrrc");
    }

    protected long getContentSize() {
        return 6;
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.recordingYear = IsoTypeReader.readUInt16(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, this.recordingYear);
    }
}

package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import java.nio.ByteBuffer;

public class SubtitleSampleEntry extends SampleEntry {
    private String imageMimeType;
    private String namespace;
    private String schemaLocation;

    protected long getContentSize() {
        return (long) ((((this.namespace.length() + 8) + this.schemaLocation.length()) + this.imageMimeType.length()) + 3);
    }

    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        this.namespace = IsoTypeReader.readString(content);
        this.schemaLocation = IsoTypeReader.readString(content);
        this.imageMimeType = IsoTypeReader.readString(content);
        _parseChildBoxes(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        IsoTypeWriter.writeUtf8String(byteBuffer, this.namespace);
        IsoTypeWriter.writeUtf8String(byteBuffer, this.schemaLocation);
        IsoTypeWriter.writeUtf8String(byteBuffer, this.imageMimeType);
    }
}

package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import java.nio.ByteBuffer;

public class Ovc1VisualSampleEntryImpl extends SampleEntry {
    private byte[] vc1Content;

    protected long getContentSize() {
        long size = 8;
        for (Box box : this.boxes) {
            size += box.getSize();
        }
        return size + ((long) this.vc1Content.length);
    }

    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        this.vc1Content = new byte[content.remaining()];
        content.get(this.vc1Content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(new byte[6]);
        IsoTypeWriter.writeUInt16(byteBuffer, getDataReferenceIndex());
        byteBuffer.put(this.vc1Content);
    }

    protected Ovc1VisualSampleEntryImpl() {
        super("ovc1");
    }
}

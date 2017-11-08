package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.FullContainerBox;
import java.nio.ByteBuffer;

public class ItemProtectionBox extends FullContainerBox {
    public ItemProtectionBox() {
        super("ipro");
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        IsoTypeReader.readUInt16(content);
        parseChildBoxes(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, getBoxes().size());
        writeChildBoxes(byteBuffer);
    }
}

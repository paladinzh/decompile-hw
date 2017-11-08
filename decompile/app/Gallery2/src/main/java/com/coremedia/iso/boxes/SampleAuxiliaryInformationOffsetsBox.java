package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class SampleAuxiliaryInformationOffsetsBox extends AbstractFullBox {
    private long auxInfoType;
    private long auxInfoTypeParameter;
    private List<Long> offsets = new LinkedList();

    public SampleAuxiliaryInformationOffsetsBox() {
        super("saio");
    }

    protected long getContentSize() {
        return (long) (((getFlags() & 1) == 1 ? 8 : 0) + ((getVersion() == 0 ? this.offsets.size() * 4 : this.offsets.size() * 8) + 8));
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        if ((getFlags() & 1) == 1) {
            IsoTypeWriter.writeUInt32(byteBuffer, this.auxInfoType);
            IsoTypeWriter.writeUInt32(byteBuffer, this.auxInfoTypeParameter);
        }
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.offsets.size());
        for (Long offset : this.offsets) {
            if (getVersion() == 0) {
                IsoTypeWriter.writeUInt32(byteBuffer, offset.longValue());
            } else {
                IsoTypeWriter.writeUInt64(byteBuffer, offset.longValue());
            }
        }
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if ((getFlags() & 1) == 1) {
            this.auxInfoType = IsoTypeReader.readUInt32(content);
            this.auxInfoTypeParameter = IsoTypeReader.readUInt32(content);
        }
        int entryCount = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        this.offsets.clear();
        for (int i = 0; i < entryCount; i++) {
            if (getVersion() == 0) {
                this.offsets.add(Long.valueOf(IsoTypeReader.readUInt32(content)));
            } else {
                this.offsets.add(Long.valueOf(IsoTypeReader.readUInt64(content)));
            }
        }
    }
}

package com.googlecode.mp4parser.boxes.piff;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TfrfBox extends AbstractFullBox {
    public List<Entry> entries = new ArrayList();

    public class Entry {
        long fragmentAbsoluteDuration;
        long fragmentAbsoluteTime;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Entry");
            sb.append("{fragmentAbsoluteTime=").append(this.fragmentAbsoluteTime);
            sb.append(", fragmentAbsoluteDuration=").append(this.fragmentAbsoluteDuration);
            sb.append('}');
            return sb.toString();
        }
    }

    public TfrfBox() {
        super("uuid");
    }

    public byte[] getUserType() {
        return new byte[]{(byte) -44, Byte.MIN_VALUE, (byte) 126, (byte) -14, (byte) -54, (byte) 57, (byte) 70, (byte) -107, (byte) -114, (byte) 84, (byte) 38, (byte) -53, (byte) -98, (byte) 70, (byte) -89, (byte) -97};
    }

    protected long getContentSize() {
        return (long) (((getVersion() == 1 ? 16 : 8) * this.entries.size()) + 5);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt8(byteBuffer, this.entries.size());
        for (Entry entry : this.entries) {
            if (getVersion() == 1) {
                IsoTypeWriter.writeUInt64(byteBuffer, entry.fragmentAbsoluteTime);
                IsoTypeWriter.writeUInt64(byteBuffer, entry.fragmentAbsoluteDuration);
            } else {
                IsoTypeWriter.writeUInt32(byteBuffer, entry.fragmentAbsoluteTime);
                IsoTypeWriter.writeUInt32(byteBuffer, entry.fragmentAbsoluteDuration);
            }
        }
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int fragmentCount = IsoTypeReader.readUInt8(content);
        for (int i = 0; i < fragmentCount; i++) {
            Entry entry = new Entry();
            if (getVersion() == 1) {
                entry.fragmentAbsoluteTime = IsoTypeReader.readUInt64(content);
                entry.fragmentAbsoluteDuration = IsoTypeReader.readUInt64(content);
            } else {
                entry.fragmentAbsoluteTime = IsoTypeReader.readUInt32(content);
                entry.fragmentAbsoluteDuration = IsoTypeReader.readUInt32(content);
            }
            this.entries.add(entry);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TfrfBox");
        sb.append("{entries=").append(this.entries);
        sb.append('}');
        return sb.toString();
    }
}

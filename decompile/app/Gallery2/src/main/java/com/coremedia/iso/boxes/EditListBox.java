package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class EditListBox extends AbstractFullBox {
    private List<Entry> entries = new LinkedList();

    public static class Entry {
        EditListBox editListBox;
        private double mediaRate;
        private long mediaTime;
        private long segmentDuration;

        public Entry(EditListBox editListBox, ByteBuffer bb) {
            if (editListBox.getVersion() == 1) {
                this.segmentDuration = IsoTypeReader.readUInt64(bb);
                this.mediaTime = IsoTypeReader.readUInt64(bb);
                this.mediaRate = IsoTypeReader.readFixedPoint1616(bb);
            } else {
                this.segmentDuration = IsoTypeReader.readUInt32(bb);
                this.mediaTime = IsoTypeReader.readUInt32(bb);
                this.mediaRate = IsoTypeReader.readFixedPoint1616(bb);
            }
            this.editListBox = editListBox;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Entry entry = (Entry) o;
            return this.mediaTime == entry.mediaTime && this.segmentDuration == entry.segmentDuration;
        }

        public int hashCode() {
            return (((int) (this.segmentDuration ^ (this.segmentDuration >>> 32))) * 31) + ((int) (this.mediaTime ^ (this.mediaTime >>> 32)));
        }

        public void getContent(ByteBuffer bb) {
            if (this.editListBox.getVersion() == 1) {
                IsoTypeWriter.writeUInt64(bb, this.segmentDuration);
                IsoTypeWriter.writeUInt64(bb, this.mediaTime);
            } else {
                IsoTypeWriter.writeUInt32(bb, (long) CastUtils.l2i(this.segmentDuration));
                bb.putInt(CastUtils.l2i(this.mediaTime));
            }
            IsoTypeWriter.writeFixedPont1616(bb, this.mediaRate);
        }

        public String toString() {
            return "Entry{segmentDuration=" + this.segmentDuration + ", mediaTime=" + this.mediaTime + ", mediaRate=" + this.mediaRate + '}';
        }
    }

    public EditListBox() {
        super("elst");
    }

    protected long getContentSize() {
        if (getVersion() == 1) {
            return 8 + ((long) (this.entries.size() * 20));
        }
        return 8 + ((long) (this.entries.size() * 12));
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int entryCount = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        this.entries = new LinkedList();
        for (int i = 0; i < entryCount; i++) {
            this.entries.add(new Entry(this, content));
        }
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.entries.size());
        for (Entry entry : this.entries) {
            entry.getContent(byteBuffer);
        }
    }

    public String toString() {
        return "EditListBox{entries=" + this.entries + '}';
    }
}

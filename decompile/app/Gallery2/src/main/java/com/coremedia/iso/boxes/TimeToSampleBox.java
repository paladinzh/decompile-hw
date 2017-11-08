package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeToSampleBox extends AbstractFullBox {
    static final /* synthetic */ boolean -assertionsDisabled = (!TimeToSampleBox.class.desiredAssertionStatus());
    List<Entry> entries = Collections.emptyList();

    public static class Entry {
        long count;
        long delta;

        public Entry(long count, long delta) {
            this.count = count;
            this.delta = delta;
        }

        public long getCount() {
            return this.count;
        }

        public long getDelta() {
            return this.delta;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public String toString() {
            return "Entry{count=" + this.count + ", delta=" + this.delta + '}';
        }
    }

    public TimeToSampleBox() {
        super("stts");
    }

    protected long getContentSize() {
        return (long) ((this.entries.size() * 8) + 8);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int entryCount = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        this.entries = new ArrayList(entryCount);
        for (int i = 0; i < entryCount; i++) {
            this.entries.add(new Entry(IsoTypeReader.readUInt32(content), IsoTypeReader.readUInt32(content)));
        }
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.entries.size());
        for (Entry entry : this.entries) {
            IsoTypeWriter.writeUInt32(byteBuffer, entry.getCount());
            IsoTypeWriter.writeUInt32(byteBuffer, entry.getDelta());
        }
    }

    public List<Entry> getEntries() {
        return this.entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String toString() {
        return "TimeToSampleBox[entryCount=" + this.entries.size() + "]";
    }

    public static long[] blowupTimeToSamples(List<Entry> entries) {
        long numOfSamples = 0;
        for (Entry entry : entries) {
            numOfSamples += entry.getCount();
        }
        if (!-assertionsDisabled) {
            if ((numOfSamples <= 2147483647L ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        long[] decodingTime = new long[((int) numOfSamples)];
        int current = 0;
        for (Entry entry2 : entries) {
            int i = 0;
            while (((long) i) < entry2.getCount()) {
                int current2 = current + 1;
                decodingTime[current] = entry2.getDelta();
                i++;
                current = current2;
            }
        }
        return decodingTime;
    }
}

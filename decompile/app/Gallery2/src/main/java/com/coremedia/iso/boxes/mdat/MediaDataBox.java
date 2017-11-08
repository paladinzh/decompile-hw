package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.ChannelHelper;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class MediaDataBox implements Box {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static Logger LOG = Logger.getLogger(MediaDataBox.class.getName());
    private Map<Long, Reference<ByteBuffer>> cache = new HashMap();
    private ByteBuffer content;
    private long contentSize;
    private FileChannel fileChannel;
    ByteBuffer header;
    ContainerBox parent;
    private long startPosition;

    static {
        boolean z;
        if (MediaDataBox.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public ContainerBox getParent() {
        return this.parent;
    }

    public void setParent(ContainerBox parent) {
        this.parent = parent;
    }

    public String getType() {
        return "mdat";
    }

    private static void transfer(FileChannel from, long position, long count, WritableByteChannel to) throws IOException {
        long offset = 0;
        while (offset < count) {
            offset += from.transferTo(position + offset, Math.min(67076096, count - offset), to);
        }
    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        if (this.fileChannel == null) {
            this.header.rewind();
            writableByteChannel.write(this.header);
            writableByteChannel.write(this.content);
        } else if (-assertionsDisabled || checkStillOk()) {
            transfer(this.fileChannel, this.startPosition - ((long) this.header.limit()), this.contentSize + ((long) this.header.limit()), writableByteChannel);
        } else {
            throw new AssertionError();
        }
    }

    private boolean checkStillOk() {
        try {
            this.fileChannel.position(this.startPosition - ((long) this.header.limit()));
            ByteBuffer h2 = ByteBuffer.allocate(this.header.limit());
            this.fileChannel.read(h2);
            this.header.rewind();
            h2.rewind();
            if (-assertionsDisabled || h2.equals(this.header)) {
                return true;
            }
            throw new AssertionError("It seems that the content I want to read has already been overwritten.");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getSize() {
        return ((long) this.header.limit()) + this.contentSize;
    }

    public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        this.header = header;
        this.contentSize = contentSize;
        if (!(readableByteChannel instanceof FileChannel) || contentSize <= ((long) AbstractBox.MEM_MAP_THRESHOLD)) {
            this.content = ChannelHelper.readFully(readableByteChannel, (long) CastUtils.l2i(contentSize));
            this.cache.put(Long.valueOf(0), new SoftReference(this.content));
            return;
        }
        this.fileChannel = (FileChannel) readableByteChannel;
        this.startPosition = ((FileChannel) readableByteChannel).position();
        ((FileChannel) readableByteChannel).position(((FileChannel) readableByteChannel).position() + contentSize);
    }

    public synchronized ByteBuffer getContent(long offset, int length) {
        ByteBuffer cacheEntry;
        for (Long chacheEntryOffset : this.cache.keySet()) {
            if (chacheEntryOffset.longValue() <= offset && offset <= chacheEntryOffset.longValue() + 10485760) {
                cacheEntry = (ByteBuffer) ((Reference) this.cache.get(chacheEntryOffset)).get();
                if (cacheEntry != null && chacheEntryOffset.longValue() + ((long) cacheEntry.limit()) >= ((long) length) + offset) {
                    cacheEntry.position((int) (offset - chacheEntryOffset.longValue()));
                    ByteBuffer cachedSample = cacheEntry.slice();
                    cachedSample.limit(length);
                    return cachedSample;
                }
            }
        }
        try {
            cacheEntry = this.fileChannel.map(MapMode.READ_ONLY, this.startPosition + offset, Math.min(10485760, this.contentSize - offset));
            this.cache.put(Long.valueOf(offset), new SoftReference(cacheEntry));
            cacheEntry.position(0);
            cachedSample = cacheEntry.slice();
            cachedSample.limit(length);
            return cachedSample;
        } catch (IOException e1) {
            LOG.fine("Even mapping just 10MB of the source file into the memory failed. " + e1);
            throw new RuntimeException("Delayed reading of mdat content failed. Make sure not to close the FileChannel that has been used to create the IsoFile!", e1);
        }
    }

    public ByteBuffer getHeader() {
        return this.header;
    }
}

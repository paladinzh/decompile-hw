package com.coremedia.iso;

import com.googlecode.mp4parser.util.CastUtils;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;

public class ChannelHelper {
    static final /* synthetic */ boolean -assertionsDisabled = (!ChannelHelper.class.desiredAssertionStatus());

    public static ByteBuffer readFully(ReadableByteChannel channel, long size) throws IOException {
        if (!(channel instanceof FileChannel) || size <= 1048576) {
            ByteBuffer buf = ByteBuffer.allocate(CastUtils.l2i(size));
            readFully(channel, buf, buf.limit());
            buf.rewind();
            if (!-assertionsDisabled) {
                if ((((long) buf.limit()) == size ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            return buf;
        }
        ByteBuffer bb = ((FileChannel) channel).map(MapMode.READ_ONLY, ((FileChannel) channel).position(), size);
        ((FileChannel) channel).position(((FileChannel) channel).position() + size);
        return bb;
    }

    public static int readFully(ReadableByteChannel channel, ByteBuffer buf, int length) throws IOException {
        int count = 0;
        do {
            int n = channel.read(buf);
            if (-1 == n) {
                break;
            }
            count += n;
        } while (count != length);
        if (n != -1) {
            return count;
        }
        throw new EOFException("End of file. No more boxes.");
    }
}

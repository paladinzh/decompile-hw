package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MovieBox;
import com.googlecode.mp4parser.AbstractContainerBox;
import com.googlecode.mp4parser.annotations.DoNotParseDetail;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@DoNotParseDetail
public class IsoFile extends AbstractContainerBox implements Closeable {
    static final /* synthetic */ boolean -assertionsDisabled = (!IsoFile.class.desiredAssertionStatus());
    protected BoxParser boxParser;
    ReadableByteChannel byteChannel;

    public IsoFile() {
        super("");
        this.boxParser = new PropertyBoxParserImpl(new String[0]);
    }

    public IsoFile(ReadableByteChannel byteChannel) throws IOException {
        super("");
        this.boxParser = new PropertyBoxParserImpl(new String[0]);
        this.byteChannel = byteChannel;
        this.boxParser = createBoxParser();
        parse();
    }

    protected BoxParser createBoxParser() {
        return new PropertyBoxParserImpl(new String[0]);
    }

    public void _parseDetails(ByteBuffer content) {
    }

    private void parse() throws IOException {
        boolean done = false;
        while (!done) {
            try {
                Box box = this.boxParser.parseBox(this.byteChannel, this);
                if (box != null) {
                    this.boxes.add(box);
                } else {
                    done = true;
                }
            } catch (EOFException e) {
                done = true;
            }
        }
    }

    @DoNotParseDetail
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("IsoFile[");
        if (this.boxes == null) {
            buffer.append("unparsed");
        } else {
            for (int i = 0; i < this.boxes.size(); i++) {
                if (i > 0) {
                    buffer.append(";");
                }
                buffer.append(((Box) this.boxes.get(i)).toString());
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    @DoNotParseDetail
    public static byte[] fourCCtoBytes(String fourCC) {
        byte[] result = new byte[4];
        if (fourCC != null) {
            for (int i = 0; i < Math.min(4, fourCC.length()); i++) {
                result[i] = (byte) fourCC.charAt(i);
            }
        }
        return result;
    }

    @DoNotParseDetail
    public static String bytesToFourCC(byte[] type) {
        byte[] result = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0};
        if (type != null) {
            System.arraycopy(type, 0, result, 0, Math.min(type.length, 4));
        }
        try {
            return new String(result, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character encoding is missing", e);
        }
    }

    public long getSize() {
        long size = 0;
        for (Box box : this.boxes) {
            size += box.getSize();
        }
        return size;
    }

    public IsoFile getIsoFile() {
        return this;
    }

    @DoNotParseDetail
    public MovieBox getMovieBox() {
        for (Box box : this.boxes) {
            if (box instanceof MovieBox) {
                return (MovieBox) box;
            }
        }
        return null;
    }

    public void getBox(WritableByteChannel os) throws IOException {
        for (Box box : this.boxes) {
            if (os instanceof FileChannel) {
                long startPos = ((FileChannel) os).position();
                box.getBox(os);
                long size = ((FileChannel) os).position() - startPos;
                if (-assertionsDisabled) {
                    continue;
                } else {
                    if ((size == box.getSize() ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
            } else {
                box.getBox(os);
            }
        }
    }

    public void close() throws IOException {
        this.byteChannel.close();
    }
}

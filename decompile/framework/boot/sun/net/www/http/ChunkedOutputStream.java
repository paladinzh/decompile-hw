package sun.net.www.http;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class ChunkedOutputStream extends PrintStream {
    private static final byte[] CRLF = new byte[]{(byte) 13, (byte) 10};
    private static final int CRLF_SIZE = CRLF.length;
    static final int DEFAULT_CHUNK_SIZE = 4096;
    private static final byte[] EMPTY_CHUNK_HEADER = getHeader(0);
    private static final int EMPTY_CHUNK_HEADER_SIZE = getHeaderSize(0);
    private static final byte[] FOOTER = CRLF;
    private static final int FOOTER_SIZE = CRLF_SIZE;
    private byte[] buf;
    private byte[] completeHeader;
    private int count;
    private PrintStream out;
    private int preferedHeaderSize;
    private int preferredChunkDataSize;
    private int preferredChunkGrossSize;
    private int size;
    private int spaceInCurrentChunk;

    private static int getHeaderSize(int size) {
        return Integer.toHexString(size).length() + CRLF_SIZE;
    }

    private static byte[] getHeader(int size) {
        try {
            byte[] hexBytes = Integer.toHexString(size).getBytes("US-ASCII");
            byte[] header = new byte[getHeaderSize(size)];
            for (int i = 0; i < hexBytes.length; i++) {
                header[i] = hexBytes[i];
            }
            header[hexBytes.length] = CRLF[0];
            header[hexBytes.length + 1] = CRLF[1];
            return header;
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.getMessage());
        }
    }

    public ChunkedOutputStream(PrintStream o) {
        this(o, 4096);
    }

    public ChunkedOutputStream(PrintStream o, int size) {
        super((OutputStream) o);
        this.out = o;
        if (size <= 0) {
            size = 4096;
        }
        if (size > 0) {
            int adjusted_size = (size - getHeaderSize(size)) - FOOTER_SIZE;
            if (getHeaderSize(adjusted_size + 1) < getHeaderSize(size)) {
                adjusted_size++;
            }
            size = adjusted_size;
        }
        if (size > 0) {
            this.preferredChunkDataSize = size;
        } else {
            this.preferredChunkDataSize = (4096 - getHeaderSize(4096)) - FOOTER_SIZE;
        }
        this.preferedHeaderSize = getHeaderSize(this.preferredChunkDataSize);
        this.preferredChunkGrossSize = (this.preferedHeaderSize + this.preferredChunkDataSize) + FOOTER_SIZE;
        this.completeHeader = getHeader(this.preferredChunkDataSize);
        this.buf = new byte[(this.preferredChunkDataSize + 32)];
        reset();
    }

    private void flush(boolean flushAll) {
        if (this.spaceInCurrentChunk == 0) {
            this.out.write(this.buf, 0, this.preferredChunkGrossSize);
            this.out.flush();
            reset();
        } else if (flushAll) {
            if (this.size > 0) {
                int adjustedHeaderStartIndex = this.preferedHeaderSize - getHeaderSize(this.size);
                System.arraycopy(getHeader(this.size), 0, this.buf, adjustedHeaderStartIndex, getHeaderSize(this.size));
                byte[] bArr = this.buf;
                int i = this.count;
                this.count = i + 1;
                bArr[i] = FOOTER[0];
                bArr = this.buf;
                i = this.count;
                this.count = i + 1;
                bArr[i] = FOOTER[1];
                this.out.write(this.buf, adjustedHeaderStartIndex, this.count - adjustedHeaderStartIndex);
            } else {
                this.out.write(EMPTY_CHUNK_HEADER, 0, EMPTY_CHUNK_HEADER_SIZE);
            }
            this.out.flush();
            reset();
        }
    }

    public boolean checkError() {
        return this.out.checkError();
    }

    private void ensureOpen() {
        if (this.out == null) {
            setError();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void write(byte[] b, int off, int len) {
        ensureOpen();
        if (off >= 0 && off <= b.length && len >= 0) {
            if (off + len <= b.length && off + len >= 0) {
                if (len != 0) {
                    int bytesToWrite = len;
                    int inputIndex = off;
                    while (true) {
                        if (bytesToWrite >= this.spaceInCurrentChunk) {
                            for (int i = 0; i < this.completeHeader.length; i++) {
                                this.buf[i] = this.completeHeader[i];
                            }
                            System.arraycopy(b, inputIndex, this.buf, this.count, this.spaceInCurrentChunk);
                            inputIndex += this.spaceInCurrentChunk;
                            bytesToWrite -= this.spaceInCurrentChunk;
                            this.count += this.spaceInCurrentChunk;
                            byte[] bArr = this.buf;
                            int i2 = this.count;
                            this.count = i2 + 1;
                            bArr[i2] = FOOTER[0];
                            bArr = this.buf;
                            i2 = this.count;
                            this.count = i2 + 1;
                            bArr[i2] = FOOTER[1];
                            this.spaceInCurrentChunk = 0;
                            flush(false);
                            if (checkError()) {
                                break;
                            }
                            if (bytesToWrite > 0) {
                                break;
                            }
                        } else {
                            System.arraycopy(b, inputIndex, this.buf, this.count, bytesToWrite);
                            this.count += bytesToWrite;
                            this.size += bytesToWrite;
                            this.spaceInCurrentChunk -= bytesToWrite;
                            bytesToWrite = 0;
                            if (bytesToWrite > 0) {
                                break;
                            }
                        }
                    }
                } else {
                    return;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public synchronized void write(int _b) {
        write(new byte[]{(byte) _b}, 0, 1);
    }

    public synchronized void reset() {
        this.count = this.preferedHeaderSize;
        this.size = 0;
        this.spaceInCurrentChunk = this.preferredChunkDataSize;
    }

    public int size() {
        return this.size;
    }

    public synchronized void close() {
        ensureOpen();
        if (this.size > 0) {
            flush(true);
        }
        flush(true);
        this.out = null;
    }

    public synchronized void flush() {
        ensureOpen();
        if (this.size > 0) {
            flush(true);
        }
    }
}

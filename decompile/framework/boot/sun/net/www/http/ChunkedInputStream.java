package sun.net.www.http;

import java.io.IOException;
import java.io.InputStream;
import sun.net.www.MessageHeader;

public class ChunkedInputStream extends InputStream implements Hurryable {
    private static final int MAX_CHUNK_HEADER_SIZE = 2050;
    static final int STATE_AWAITING_CHUNK_EOL = 3;
    static final int STATE_AWAITING_CHUNK_HEADER = 1;
    static final int STATE_AWAITING_TRAILERS = 4;
    static final int STATE_DONE = 5;
    static final int STATE_READING_CHUNK = 2;
    private int chunkCount;
    private byte[] chunkData = new byte[4096];
    private int chunkPos;
    private int chunkRead;
    private int chunkSize;
    private boolean closed;
    private boolean error;
    private HttpClient hc;
    private InputStream in;
    private int rawCount;
    private byte[] rawData = new byte[32];
    private int rawPos;
    private MessageHeader responses;
    private int state;

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("stream is closed");
        }
    }

    private void ensureRawAvailable(int size) {
        if (this.rawCount + size > this.rawData.length) {
            int used = this.rawCount - this.rawPos;
            if (used + size > this.rawData.length) {
                byte[] tmp = new byte[(used + size)];
                if (used > 0) {
                    System.arraycopy(this.rawData, this.rawPos, tmp, 0, used);
                }
                this.rawData = tmp;
            } else if (used > 0) {
                System.arraycopy(this.rawData, this.rawPos, this.rawData, 0, used);
            }
            this.rawCount = used;
            this.rawPos = 0;
        }
    }

    private void closeUnderlying() throws IOException {
        if (this.in != null) {
            if (!this.error && this.state == 5) {
                this.hc.finished();
            } else if (!hurry()) {
                this.hc.closeServer();
            }
            this.in = null;
        }
    }

    private int fastRead(byte[] b, int off, int len) throws IOException {
        int cnt;
        int remaining = this.chunkSize - this.chunkRead;
        if (remaining < len) {
            cnt = remaining;
        } else {
            cnt = len;
        }
        if (cnt <= 0) {
            return 0;
        }
        try {
            int nread = this.in.read(b, off, cnt);
            if (nread > 0) {
                this.chunkRead += nread;
                if (this.chunkRead >= this.chunkSize) {
                    this.state = 3;
                }
                return nread;
            }
            this.error = true;
            throw new IOException("Premature EOF");
        } catch (IOException e) {
            this.error = true;
            throw e;
        }
    }

    private void processRaw() throws IOException {
        while (this.state != 5) {
            int pos;
            int i;
            switch (this.state) {
                case 1:
                    pos = this.rawPos;
                    while (pos < this.rawCount && this.rawData[pos] != (byte) 10) {
                        pos++;
                        if (pos - this.rawPos >= MAX_CHUNK_HEADER_SIZE) {
                            this.error = true;
                            throw new IOException("Chunk header too long");
                        }
                    }
                    if (pos < this.rawCount) {
                        String header = new String(this.rawData, this.rawPos, (pos - this.rawPos) + 1, "US-ASCII");
                        i = 0;
                        while (i < header.length() && Character.digit(header.charAt(i), 16) != -1) {
                            i++;
                        }
                        try {
                            this.chunkSize = Integer.parseInt(header.substring(0, i), 16);
                            this.rawPos = pos + 1;
                            this.chunkRead = 0;
                            if (this.chunkSize <= 0) {
                                this.state = 4;
                                break;
                            } else {
                                this.state = 2;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            this.error = true;
                            throw new IOException("Bogus chunk size");
                        }
                    }
                    return;
                case 2:
                    if (this.rawPos < this.rawCount) {
                        int copyLen = Math.min(this.chunkSize - this.chunkRead, this.rawCount - this.rawPos);
                        if (this.chunkData.length < this.chunkCount + copyLen) {
                            int cnt = this.chunkCount - this.chunkPos;
                            if (this.chunkData.length < cnt + copyLen) {
                                byte[] tmp = new byte[(cnt + copyLen)];
                                System.arraycopy(this.chunkData, this.chunkPos, tmp, 0, cnt);
                                this.chunkData = tmp;
                            } else {
                                System.arraycopy(this.chunkData, this.chunkPos, this.chunkData, 0, cnt);
                            }
                            this.chunkPos = 0;
                            this.chunkCount = cnt;
                        }
                        System.arraycopy(this.rawData, this.rawPos, this.chunkData, this.chunkCount, copyLen);
                        this.rawPos += copyLen;
                        this.chunkCount += copyLen;
                        this.chunkRead += copyLen;
                        if (this.chunkSize - this.chunkRead <= 0) {
                            this.state = 3;
                            break;
                        }
                        return;
                    }
                    return;
                case 3:
                    if (this.rawPos + 1 < this.rawCount) {
                        if (this.rawData[this.rawPos] == (byte) 13) {
                            if (this.rawData[this.rawPos + 1] == (byte) 10) {
                                this.rawPos += 2;
                                this.state = 1;
                                break;
                            }
                            this.error = true;
                            throw new IOException("missing LF");
                        }
                        this.error = true;
                        throw new IOException("missing CR");
                    }
                    return;
                case 4:
                    pos = this.rawPos;
                    while (pos < this.rawCount && this.rawData[pos] != (byte) 10) {
                        pos++;
                    }
                    if (pos < this.rawCount) {
                        if (pos != this.rawPos) {
                            if (this.rawData[pos - 1] == (byte) 13) {
                                if (pos != this.rawPos + 1) {
                                    String trailer = new String(this.rawData, this.rawPos, pos - this.rawPos, "US-ASCII");
                                    i = trailer.indexOf(58);
                                    if (i != -1) {
                                        this.responses.add(trailer.substring(0, i).trim(), trailer.substring(i + 1, trailer.length()).trim());
                                        this.rawPos = pos + 1;
                                        break;
                                    }
                                    throw new IOException("Malformed tailer - format should be key:value");
                                }
                                this.state = 5;
                                closeUnderlying();
                                return;
                            }
                            this.error = true;
                            throw new IOException("LF should be proceeded by CR");
                        }
                        this.error = true;
                        throw new IOException("LF should be proceeded by CR");
                    }
                    return;
                    break;
                default:
                    break;
            }
        }
    }

    private int readAheadNonBlocking() throws IOException {
        int avail = this.in.available();
        if (avail > 0) {
            ensureRawAvailable(avail);
            try {
                int nread = this.in.read(this.rawData, this.rawCount, avail);
                if (nread < 0) {
                    this.error = true;
                    return -1;
                }
                this.rawCount += nread;
                processRaw();
            } catch (IOException e) {
                this.error = true;
                throw e;
            }
        }
        return this.chunkCount - this.chunkPos;
    }

    private int readAheadBlocking() throws IOException {
        while (this.state != 5) {
            ensureRawAvailable(32);
            try {
                int nread = this.in.read(this.rawData, this.rawCount, this.rawData.length - this.rawCount);
                if (nread < 0) {
                    this.error = true;
                    throw new IOException("Premature EOF");
                }
                this.rawCount += nread;
                processRaw();
                if (this.chunkCount > 0) {
                    return this.chunkCount - this.chunkPos;
                }
            } catch (IOException e) {
                this.error = true;
                throw e;
            }
        }
        return -1;
    }

    private int readAhead(boolean allowBlocking) throws IOException {
        if (this.state == 5) {
            return -1;
        }
        if (this.chunkPos >= this.chunkCount) {
            this.chunkCount = 0;
            this.chunkPos = 0;
        }
        if (allowBlocking) {
            return readAheadBlocking();
        }
        return readAheadNonBlocking();
    }

    public ChunkedInputStream(InputStream in, HttpClient hc, MessageHeader responses) throws IOException {
        this.in = in;
        this.responses = responses;
        this.hc = hc;
        this.state = 1;
    }

    public synchronized int read() throws IOException {
        ensureOpen();
        if (this.chunkPos >= this.chunkCount && readAhead(true) <= 0) {
            return -1;
        }
        byte[] bArr = this.chunkData;
        int i = this.chunkPos;
        this.chunkPos = i + 1;
        return bArr[i] & 255;
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off >= 0 && off <= b.length && len >= 0) {
            if (off + len <= b.length && off + len >= 0) {
                if (len == 0) {
                    return 0;
                }
                int avail = this.chunkCount - this.chunkPos;
                if (avail <= 0) {
                    if (this.state == 2) {
                        return fastRead(b, off, len);
                    }
                    avail = readAhead(true);
                    if (avail < 0) {
                        return -1;
                    }
                }
                int cnt = avail < len ? avail : len;
                System.arraycopy(this.chunkData, this.chunkPos, b, off, cnt);
                this.chunkPos += cnt;
                return cnt;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public synchronized int available() throws IOException {
        ensureOpen();
        int avail = this.chunkCount - this.chunkPos;
        if (avail > 0) {
            return avail;
        }
        avail = readAhead(false);
        if (avail < 0) {
            return 0;
        }
        return avail;
    }

    public synchronized void close() throws IOException {
        if (!this.closed) {
            closeUnderlying();
            this.closed = true;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean hurry() {
        boolean z = false;
        synchronized (this) {
            if (this.in == null || this.error) {
            } else {
                try {
                    readAhead(false);
                    if (this.error) {
                        return false;
                    } else if (this.state == 5) {
                        z = true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
    }
}

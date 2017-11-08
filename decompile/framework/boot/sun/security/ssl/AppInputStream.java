package sun.security.ssl;

import java.io.IOException;
import java.io.InputStream;

class AppInputStream extends InputStream {
    private static final byte[] SKIP_ARRAY = new byte[1024];
    private SSLSocketImpl c;
    private final byte[] oneByte = new byte[1];
    InputRecord r = new InputRecord();

    AppInputStream(SSLSocketImpl conn) {
        this.c = conn;
    }

    public int available() throws IOException {
        if (this.c.checkEOF() || !this.r.isAppDataValid()) {
            return 0;
        }
        return this.r.available();
    }

    public synchronized int read() throws IOException {
        if (read(this.oneByte, 0, 1) <= 0) {
            return -1;
        }
        return this.oneByte[0] & 255;
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            if (this.c.checkEOF()) {
                return -1;
            }
            do {
                try {
                    if (this.r.available() == 0) {
                        this.c.readDataRecord(this.r);
                    } else {
                        return this.r.read(b, off, Math.min(len, this.r.available()));
                    }
                } catch (Exception e) {
                    this.c.handleException(e);
                    return -1;
                }
            } while (!this.c.checkEOF());
            return -1;
        }
    }

    public synchronized long skip(long n) throws IOException {
        long skipped;
        skipped = 0;
        while (n > 0) {
            int r = read(SKIP_ARRAY, 0, (int) Math.min(n, (long) SKIP_ARRAY.length));
            if (r <= 0) {
                break;
            }
            n -= (long) r;
            skipped += (long) r;
        }
        return skipped;
    }

    public void close() throws IOException {
        this.c.close();
    }
}

package huawei.android.hwgallerycache;

import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.zip.Adler32;

public class BlobCache implements Closeable {
    private static final int BH_CHECKSUM = 8;
    private static final int BH_KEY = 0;
    private static final int BH_LENGTH = 16;
    private static final int BH_OFFSET = 12;
    private static final int BLOB_HEADER_SIZE = 20;
    private static final int DATA_HEADER_SIZE = 4;
    private static final int IH_ACTIVE_BYTES = 20;
    private static final int IH_ACTIVE_ENTRIES = 16;
    private static final int IH_ACTIVE_REGION = 12;
    private static final int IH_CHECKSUM = 28;
    private static final int IH_MAGIC = 0;
    private static final int IH_MAX_BYTES = 8;
    private static final int IH_MAX_ENTRIES = 4;
    private static final int IH_VERSION = 24;
    private static final int INDEX_HEADER_SIZE = 32;
    private static final int MAGIC_DATA_FILE = -1121680112;
    private static final int MAGIC_INDEX_FILE = -1289277392;
    private static final String TAG = "BlobCache";
    private int mActiveBytes;
    private RandomAccessFile mActiveDataFile;
    private int mActiveEntries;
    private int mActiveHashStart;
    private int mActiveRegion;
    private Adler32 mAdler32;
    private byte[] mBlobHeader;
    private RandomAccessFile mDataFile0;
    private RandomAccessFile mDataFile1;
    private int mFileOffset;
    private RandomAccessFile mInactiveDataFile;
    private int mInactiveHashStart;
    private MappedByteBuffer mIndexBuffer;
    private FileChannel mIndexChannel;
    private RandomAccessFile mIndexFile;
    private byte[] mIndexHeader;
    private LookupRequest mLookupRequest;
    private int mMaxBytes;
    private int mMaxEntries;
    private String mVersion;

    public static class LookupRequest {
        public byte[] buffer;
        public long key;
        public int length;
    }

    private boolean getBlob(java.io.RandomAccessFile r16, int r17, huawei.android.hwgallerycache.BlobCache.LookupRequest r18) throws java.io.IOException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:64:? in {6, 13, 19, 26, 35, 40, 41, 46, 53, 55, 60, 61, 63, 65, 66} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r15 = this;
        r6 = r15.mBlobHeader;
        r8 = r16.getFilePointer();
        r0 = r17;
        r12 = (long) r0;
        r0 = r16;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r0.seek(r12);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r0 = r16;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = r0.read(r6);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = 20;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r12 == r13) goto L_0x0028;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x0018:
        r12 = "BlobCache";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = "cannot read blob header";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        android.util.Log.w(r12, r13);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x0028:
        r12 = 0;
        r4 = r15.readLong(r6, r12);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;
        r12 = (r4 > r12 ? 1 : (r4 == r12 ? 0 : -1));
        if (r12 != 0) goto L_0x003a;
    L_0x0033:
        r12 = 0;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x003a:
        r0 = r18;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = r0.key;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = (r4 > r12 ? 1 : (r4 == r12 ? 0 : -1));	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r12 == 0) goto L_0x0063;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x0042:
        r12 = "BlobCache";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13.<init>();	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r14 = "blob key does not match: ";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.append(r14);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.append(r4);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.toString();	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        android.util.Log.w(r12, r13);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x0063:
        r12 = 8;
        r10 = r15.readInt(r6, r12);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 12;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r3 = r15.readInt(r6, r12);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r0 = r17;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r3 == r0) goto L_0x0094;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x0073:
        r12 = "BlobCache";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13.<init>();	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r14 = "blob offset does not match: ";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.append(r14);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.append(r3);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.toString();	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        android.util.Log.w(r12, r13);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x0094:
        r12 = 16;
        r7 = r15.readInt(r6, r12);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r7 < 0) goto L_0x00a4;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x009c:
        r12 = r15.mMaxBytes;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = r12 - r17;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = r12 + -20;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r7 <= r12) goto L_0x00c5;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x00a4:
        r12 = "BlobCache";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13.<init>();	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r14 = "invalid blob length: ";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.append(r14);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.append(r7);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.toString();	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        android.util.Log.w(r12, r13);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x00c5:
        r0 = r18;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = r0.buffer;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r12 == 0) goto L_0x00d2;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x00cb:
        r0 = r18;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = r0.buffer;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = r12.length;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r12 >= r7) goto L_0x00d8;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x00d2:
        r12 = new byte[r7];	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r0 = r18;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r0.buffer = r12;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x00d8:
        r0 = r18;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r2 = r0.buffer;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r0 = r18;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r0.length = r7;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r0 = r16;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = r0.read(r2, r12, r7);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r12 == r7) goto L_0x00f9;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x00e9:
        r12 = "BlobCache";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = "cannot read blob data";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        android.util.Log.w(r12, r13);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x00f9:
        r12 = 0;
        r12 = r15.checkSum(r2, r12, r7);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        if (r12 == r10) goto L_0x0121;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
    L_0x0100:
        r12 = "BlobCache";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13.<init>();	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r14 = "blob checksum does not match: ";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.append(r14);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.append(r10);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = r13.toString();	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        android.util.Log.w(r12, r13);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x0121:
        r12 = 1;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x0128:
        r11 = move-exception;
        r12 = "BlobCache";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r13 = "getBlob failed.";	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        android.util.Log.e(r12, r13, r11);	 Catch:{ Throwable -> 0x0128, all -> 0x0139 }
        r12 = 0;
        r0 = r16;
        r0.seek(r8);
        return r12;
    L_0x0139:
        r12 = move-exception;
        r0 = r16;
        r0.seek(r8);
        throw r12;
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwgallerycache.BlobCache.getBlob(java.io.RandomAccessFile, int, huawei.android.hwgallerycache.BlobCache$LookupRequest):boolean");
    }

    public BlobCache(String path, int maxEntries, int maxBytes, boolean reset) throws IOException {
        this(path, maxEntries, maxBytes, reset, "");
    }

    public BlobCache(String path, int maxEntries, int maxBytes, boolean reset, String cacheVersion) throws IOException {
        this.mIndexHeader = new byte[32];
        this.mBlobHeader = new byte[20];
        this.mAdler32 = new Adler32();
        this.mLookupRequest = new LookupRequest();
        this.mIndexFile = new RandomAccessFile(path + ".idx", "r");
        this.mDataFile0 = new RandomAccessFile(path + ".0", "r");
        this.mDataFile1 = new RandomAccessFile(path + ".1", "r");
        this.mVersion = cacheVersion;
        if ((reset || !loadIndex()) && !loadIndex()) {
            closeAll();
            throw new IOException("unable to load index");
        }
    }

    public void close() {
        closeAll();
    }

    private void closeAll() {
        closeSilently(this.mIndexChannel);
        closeSilently(this.mIndexFile);
        closeSilently(this.mDataFile0);
        closeSilently(this.mDataFile1);
    }

    private boolean loadIndex() {
        try {
            this.mIndexFile.seek(0);
            this.mDataFile0.seek(0);
            this.mDataFile1.seek(0);
            byte[] buf = this.mIndexHeader;
            if (this.mIndexFile.read(buf) != 32) {
                Log.w(TAG, "cannot read header");
                return false;
            } else if (readInt(buf, 0) != MAGIC_INDEX_FILE) {
                Log.w(TAG, "cannot read header magic");
                return false;
            } else if (Utils.versionInRange(readInt(buf, IH_VERSION), this.mVersion)) {
                this.mMaxEntries = readInt(buf, 4);
                this.mMaxBytes = readInt(buf, 8);
                this.mActiveRegion = readInt(buf, 12);
                this.mActiveEntries = readInt(buf, 16);
                this.mActiveBytes = readInt(buf, 20);
                if (checkSum(buf, 0, IH_CHECKSUM) != readInt(buf, IH_CHECKSUM)) {
                    Log.w(TAG, "header checksum does not match");
                    return false;
                } else if (this.mMaxEntries <= 0) {
                    Log.w(TAG, "invalid max entries");
                    return false;
                } else if (this.mMaxBytes <= 0) {
                    Log.w(TAG, "invalid max bytes");
                    return false;
                } else if (this.mActiveRegion != 0 && this.mActiveRegion != 1) {
                    Log.w(TAG, "invalid active region");
                    return false;
                } else if (this.mActiveEntries < 0 || this.mActiveEntries > this.mMaxEntries) {
                    Log.w(TAG, "invalid active entries");
                    return false;
                } else if (this.mActiveBytes < 4 || this.mActiveBytes > this.mMaxBytes) {
                    Log.w(TAG, "invalid active bytes");
                    return false;
                } else if (this.mIndexFile.length() != ((long) (((this.mMaxEntries * 12) * 2) + 32))) {
                    Log.w(TAG, "invalid index file length");
                    return false;
                } else {
                    byte[] magic = new byte[4];
                    if (this.mDataFile0.read(magic) != 4) {
                        Log.w(TAG, "cannot read data file magic");
                        return false;
                    } else if (readInt(magic, 0) != MAGIC_DATA_FILE) {
                        Log.w(TAG, "invalid data file magic");
                        return false;
                    } else if (this.mDataFile1.read(magic) != 4) {
                        Log.w(TAG, "cannot read data file magic");
                        return false;
                    } else if (readInt(magic, 0) != MAGIC_DATA_FILE) {
                        Log.w(TAG, "invalid data file magic");
                        return false;
                    } else {
                        this.mIndexChannel = this.mIndexFile.getChannel();
                        this.mIndexBuffer = this.mIndexChannel.map(MapMode.READ_ONLY, 0, this.mIndexFile.length());
                        this.mIndexBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        setActiveVariables();
                        return true;
                    }
                }
            } else {
                Log.w(TAG, "version mismatch");
                return false;
            }
        } catch (IOException ex) {
            Log.e(TAG, "loadIndex failed.", ex);
            return false;
        }
    }

    private void setActiveVariables() throws IOException {
        this.mActiveDataFile = this.mActiveRegion == 0 ? this.mDataFile0 : this.mDataFile1;
        this.mInactiveDataFile = this.mActiveRegion == 1 ? this.mDataFile0 : this.mDataFile1;
        this.mActiveDataFile.seek((long) this.mActiveBytes);
        this.mActiveHashStart = 32;
        this.mInactiveHashStart = 32;
        if (this.mActiveRegion == 0) {
            this.mInactiveHashStart += this.mMaxEntries * 12;
        } else {
            this.mActiveHashStart += this.mMaxEntries * 12;
        }
    }

    public byte[] lookup(long key) throws IOException {
        this.mLookupRequest.key = key;
        this.mLookupRequest.buffer = null;
        if (lookup(this.mLookupRequest)) {
            return this.mLookupRequest.buffer;
        }
        return new byte[4];
    }

    public boolean lookup(LookupRequest req) throws IOException {
        if (lookupInternal(req.key, this.mActiveHashStart) && getBlob(this.mActiveDataFile, this.mFileOffset, req)) {
            return true;
        }
        if (lookupInternal(req.key, this.mInactiveHashStart) && getBlob(this.mInactiveDataFile, this.mFileOffset, req)) {
            return true;
        }
        return false;
    }

    private boolean lookupInternal(long key, int hashStart) {
        int slot = (int) (key % ((long) this.mMaxEntries));
        if (slot < 0) {
            slot += this.mMaxEntries;
        }
        int slotBegin = slot;
        do {
            int offset = hashStart + (slot * 12);
            long candidateKey = this.mIndexBuffer.getLong(offset);
            int candidateOffset = this.mIndexBuffer.getInt(offset + 8);
            if (candidateOffset == 0) {
                return false;
            }
            if (candidateKey == key) {
                this.mFileOffset = candidateOffset;
                return true;
            }
            slot++;
            if (slot >= this.mMaxEntries) {
                slot = 0;
                continue;
            }
        } while (slot != slotBegin);
        return false;
    }

    private int checkSum(byte[] data, int offset, int nbytes) {
        this.mAdler32.reset();
        this.mAdler32.update(data, offset, nbytes);
        return (int) this.mAdler32.getValue();
    }

    private void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable th) {
            }
        }
    }

    private int readInt(byte[] buf, int offset) {
        return (((buf[offset] & 255) | ((buf[offset + 1] & 255) << 8)) | ((buf[offset + 2] & 255) << 16)) | ((buf[offset + 3] & 255) << IH_VERSION);
    }

    private long readLong(byte[] buf, int offset) {
        long result = (long) (buf[offset + 7] & 255);
        for (int i = 6; i >= 0; i--) {
            result = (result << 8) | ((long) (buf[offset + i] & 255));
        }
        return result;
    }
}

package com.common.imageloader.cache.disc.impl.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import com.common.imageloader.cache.disc.DiskCache;
import com.common.imageloader.cache.disc.impl.ext.DiskLruCache.Editor;
import com.common.imageloader.cache.disc.naming.FileNameGenerator;
import com.common.imageloader.utils.IoUtils;
import com.common.imageloader.utils.IoUtils.CopyListener;
import com.common.imageloader.utils.L;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LruDiskCache implements DiskCache {
    public static final int DEFAULT_BUFFER_SIZE = 32768;
    public static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
    public static final int DEFAULT_COMPRESS_QUALITY = 100;
    private static final String ERROR_ARG_NEGATIVE = " argument must be positive number";
    private static final String ERROR_ARG_NULL = " argument must be not null";
    protected int bufferSize;
    protected DiskLruCache cache;
    protected CompressFormat compressFormat;
    protected int compressQuality;
    protected final FileNameGenerator fileNameGenerator;
    private File reserveCacheDir;

    public java.io.File get(java.lang.String r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0023 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r5 = this;
        r2 = 0;
        r1 = 0;
        r3 = r5.cache;	 Catch:{ IOException -> 0x001a, all -> 0x0024 }
        r4 = r5.getKey(r6);	 Catch:{ IOException -> 0x001a, all -> 0x0024 }
        r1 = r3.get(r4);	 Catch:{ IOException -> 0x001a, all -> 0x0024 }
        if (r1 != 0) goto L_0x0014;
    L_0x000e:
        if (r1 == 0) goto L_0x0013;
    L_0x0010:
        r1.close();
    L_0x0013:
        return r2;
    L_0x0014:
        r3 = 0;
        r2 = r1.getFile(r3);	 Catch:{ IOException -> 0x001a, all -> 0x0024 }
        goto L_0x000e;
    L_0x001a:
        r0 = move-exception;
        com.common.imageloader.utils.L.e(r0);	 Catch:{ IOException -> 0x001a, all -> 0x0024 }
        if (r1 == 0) goto L_0x0023;
    L_0x0020:
        r1.close();
    L_0x0023:
        return r2;
    L_0x0024:
        r2 = move-exception;
        if (r1 == 0) goto L_0x002a;
    L_0x0027:
        r1.close();
    L_0x002a:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.common.imageloader.cache.disc.impl.ext.LruDiskCache.get(java.lang.String):java.io.File");
    }

    public LruDiskCache(File cacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize) throws IOException {
        this(cacheDir, null, fileNameGenerator, cacheMaxSize, 0);
    }

    public LruDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize, int cacheMaxFileCount) throws IOException {
        this.bufferSize = 32768;
        this.compressFormat = DEFAULT_COMPRESS_FORMAT;
        this.compressQuality = 100;
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir argument must be not null");
        } else if (cacheMaxSize < 0) {
            throw new IllegalArgumentException("cacheMaxSize argument must be positive number");
        } else if (cacheMaxFileCount < 0) {
            throw new IllegalArgumentException("cacheMaxFileCount argument must be positive number");
        } else if (fileNameGenerator == null) {
            throw new IllegalArgumentException("fileNameGenerator argument must be not null");
        } else {
            if (cacheMaxSize == 0) {
                cacheMaxSize = Long.MAX_VALUE;
            }
            if (cacheMaxFileCount == 0) {
                cacheMaxFileCount = SpaceConst.SCANNER_TYPE_ALL;
            }
            this.reserveCacheDir = reserveCacheDir;
            this.fileNameGenerator = fileNameGenerator;
            initCache(cacheDir, reserveCacheDir, cacheMaxSize, cacheMaxFileCount);
        }
    }

    private void initCache(File cacheDir, File reserveCacheDir, long cacheMaxSize, int cacheMaxFileCount) throws IOException {
        try {
            this.cache = DiskLruCache.open(cacheDir, 1, 1, cacheMaxSize, cacheMaxFileCount);
        } catch (IOException e) {
            L.e(e);
            if (reserveCacheDir != null) {
                initCache(reserveCacheDir, null, cacheMaxSize, cacheMaxFileCount);
            }
            if (this.cache == null) {
                throw e;
            }
        }
    }

    public File getDirectory() {
        return this.cache.getDirectory();
    }

    public boolean save(String imageUri, InputStream imageStream, CopyListener listener) throws IOException {
        Editor editor = this.cache.edit(getKey(imageUri));
        if (editor == null) {
            return false;
        }
        OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), this.bufferSize);
        boolean copied = false;
        try {
            copied = IoUtils.copyStream(imageStream, os, listener, this.bufferSize);
            return copied;
        } finally {
            IoUtils.closeSilently(os);
            if (copied) {
                editor.commit();
            } else {
                editor.abort();
            }
        }
    }

    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        Editor editor = this.cache.edit(getKey(imageUri));
        if (editor == null) {
            return false;
        }
        OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), this.bufferSize);
        boolean z = false;
        try {
            z = bitmap.compress(this.compressFormat, this.compressQuality, os);
            if (z) {
                editor.commit();
            } else {
                editor.abort();
            }
            return z;
        } finally {
            IoUtils.closeSilently(os);
        }
    }

    public boolean remove(String imageUri) {
        try {
            return this.cache.remove(getKey(imageUri));
        } catch (IOException e) {
            L.e(e);
            return false;
        }
    }

    public void close() {
        try {
            this.cache.close();
        } catch (IOException e) {
            L.e(e);
        }
        this.cache = null;
    }

    public void clear() {
        try {
            this.cache.delete();
        } catch (IOException e) {
            L.e(e);
        }
        try {
            initCache(this.cache.getDirectory(), this.reserveCacheDir, this.cache.getMaxSize(), this.cache.getMaxFileCount());
        } catch (IOException e2) {
            L.e(e2);
        }
    }

    private String getKey(String imageUri) {
        return this.fileNameGenerator.generate(imageUri);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setCompressFormat(CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public void setCompressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
    }
}

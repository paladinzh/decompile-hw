package com.common.imageloader.cache.disc.impl;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import com.common.imageloader.cache.disc.DiskCache;
import com.common.imageloader.cache.disc.naming.FileNameGenerator;
import com.common.imageloader.core.DefaultConfigurationFactory;
import com.common.imageloader.utils.IoUtils;
import com.common.imageloader.utils.IoUtils.CopyListener;
import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseDiskCache implements DiskCache {
    public static final int DEFAULT_BUFFER_SIZE = 32768;
    public static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
    public static final int DEFAULT_COMPRESS_QUALITY = 100;
    private static final String ERROR_ARG_NULL = " argument must be not null";
    private static final String TEMP_IMAGE_POSTFIX = ".tmp";
    protected int bufferSize;
    protected final File cacheDir;
    protected CompressFormat compressFormat;
    protected int compressQuality;
    protected final FileNameGenerator fileNameGenerator;
    protected final File reserveCacheDir;

    public BaseDiskCache(File cacheDir) {
        this(cacheDir, null);
    }

    public BaseDiskCache(File cacheDir, File reserveCacheDir) {
        this(cacheDir, reserveCacheDir, DefaultConfigurationFactory.createFileNameGenerator());
    }

    public BaseDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
        this.bufferSize = 32768;
        this.compressFormat = DEFAULT_COMPRESS_FORMAT;
        this.compressQuality = 100;
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir argument must be not null");
        } else if (fileNameGenerator == null) {
            throw new IllegalArgumentException("fileNameGenerator argument must be not null");
        } else {
            this.cacheDir = cacheDir;
            this.reserveCacheDir = reserveCacheDir;
            this.fileNameGenerator = fileNameGenerator;
        }
    }

    public File getDirectory() {
        return this.cacheDir;
    }

    public File get(String imageUri) {
        return getFile(imageUri);
    }

    public boolean save(String imageUri, InputStream imageStream, CopyListener listener) throws IOException {
        File imageFile = getFile(imageUri);
        File tmpFile = new File(imageFile.getAbsolutePath() + TEMP_IMAGE_POSTFIX);
        boolean z = false;
        OutputStream os;
        try {
            os = new BufferedOutputStream(new FileOutputStream(tmpFile), this.bufferSize);
            z = IoUtils.copyStream(imageStream, os, listener, this.bufferSize);
            IoUtils.closeSilently(os);
            if (z && !tmpFile.renameTo(imageFile)) {
                z = false;
            }
            if (!(z || tmpFile.delete())) {
                HwLog.w("BaseDiskCache", "delete file failed!");
            }
            return z;
        } catch (Throwable th) {
            if (z && !tmpFile.renameTo(imageFile)) {
                z = false;
            }
            if (!(z || tmpFile.delete())) {
                HwLog.w("BaseDiskCache", "delete file failed!");
            }
        }
    }

    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        File imageFile = getFile(imageUri);
        File tmpFile = new File(imageFile.getAbsolutePath() + TEMP_IMAGE_POSTFIX);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), this.bufferSize);
        boolean savedSuccessfully = false;
        try {
            savedSuccessfully = bitmap.compress(this.compressFormat, this.compressQuality, os);
            bitmap.recycle();
            return savedSuccessfully;
        } finally {
            IoUtils.closeSilently(os);
            if (savedSuccessfully && !tmpFile.renameTo(imageFile)) {
                savedSuccessfully = false;
            }
            if (!(savedSuccessfully || tmpFile.delete())) {
                HwLog.w("BaseDiskCache", "delete file failed!");
            }
        }
    }

    public boolean remove(String imageUri) {
        return getFile(imageUri).delete();
    }

    public void close() {
    }

    public void clear() {
        File[] files = this.cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.delete()) {
                    HwLog.w("BaseDiskCache", "delete file failed!");
                }
            }
        }
    }

    protected File getFile(String imageUri) {
        String fileName = this.fileNameGenerator.generate(imageUri);
        File dir = this.cacheDir;
        if (!(this.cacheDir.exists() || this.cacheDir.mkdirs() || this.reserveCacheDir == null || (!this.reserveCacheDir.exists() && !this.reserveCacheDir.mkdirs()))) {
            dir = this.reserveCacheDir;
        }
        return new File(dir, fileName);
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

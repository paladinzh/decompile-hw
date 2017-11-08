package com.common.imageloader.cache.disc.impl;

import android.graphics.Bitmap;
import com.common.imageloader.cache.disc.naming.FileNameGenerator;
import com.common.imageloader.core.DefaultConfigurationFactory;
import com.common.imageloader.utils.IoUtils.CopyListener;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LimitedAgeDiskCache extends BaseDiskCache {
    private final Map<File, Long> loadingDates;
    private final long maxFileAge;

    public LimitedAgeDiskCache(File cacheDir, long maxAge) {
        this(cacheDir, null, DefaultConfigurationFactory.createFileNameGenerator(), maxAge);
    }

    public LimitedAgeDiskCache(File cacheDir, File reserveCacheDir, long maxAge) {
        this(cacheDir, reserveCacheDir, DefaultConfigurationFactory.createFileNameGenerator(), maxAge);
    }

    public LimitedAgeDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator, long maxAge) {
        super(cacheDir, reserveCacheDir, fileNameGenerator);
        this.loadingDates = Collections.synchronizedMap(new HashMap());
        this.maxFileAge = 1000 * maxAge;
    }

    public File get(String imageUri) {
        File file = super.get(imageUri);
        if (file.exists()) {
            boolean cached;
            Long loadingDate = (Long) this.loadingDates.get(file);
            if (loadingDate == null) {
                cached = false;
                loadingDate = Long.valueOf(file.lastModified());
            } else {
                cached = true;
            }
            if (System.currentTimeMillis() - loadingDate.longValue() > this.maxFileAge) {
                if (!file.delete()) {
                    HwLog.i("LimitedAgeDiskCache", "delete file failed!");
                }
                this.loadingDates.remove(file);
            } else if (!cached) {
                this.loadingDates.put(file, loadingDate);
            }
        }
        return file;
    }

    public boolean save(String imageUri, InputStream imageStream, CopyListener listener) throws IOException {
        boolean saved = super.save(imageUri, imageStream, listener);
        rememberUsage(imageUri);
        return saved;
    }

    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        boolean saved = super.save(imageUri, bitmap);
        rememberUsage(imageUri);
        return saved;
    }

    public boolean remove(String imageUri) {
        this.loadingDates.remove(getFile(imageUri));
        return super.remove(imageUri);
    }

    public void clear() {
        super.clear();
        this.loadingDates.clear();
    }

    private void rememberUsage(String imageUri) {
        File file = getFile(imageUri);
        long currentTime = System.currentTimeMillis();
        if (!file.setLastModified(currentTime)) {
            HwLog.i("LimitedAgeDiskCache", "setLastModified failed!");
        }
        this.loadingDates.put(file, Long.valueOf(currentTime));
    }
}

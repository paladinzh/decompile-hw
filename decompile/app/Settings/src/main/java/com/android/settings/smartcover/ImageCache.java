package com.android.settings.smartcover;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import com.android.settings.smartcover.DiskLruCache.Editor;
import com.android.settings.smartcover.DiskLruCache.Snapshot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressLint({"NewApi"})
public class ImageCache {
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private ImageCacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private DiskLruCache mDiskLruCache;
    private LruCache<String, Bitmap> mMemoryCache;

    public static class ImageCacheParams {
        public CompressFormat compressFormat = ImageCache.DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = 70;
        public File diskCacheDir;
        public boolean diskCacheEnabled = true;
        public int diskCacheSize = 31457280;
        public boolean initDiskCacheOnCreate = false;
        public int memCacheSize = 31457280;
        public boolean memoryCacheEnabled = true;

        public ImageCacheParams(Context context, String uniqueName) {
            this.diskCacheDir = ImageCache.getDiskCacheDir(context, uniqueName);
        }

        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be between 0.01 and 0.8 (inclusive)");
            }
            this.memCacheSize = Math.round(((float) Runtime.getRuntime().maxMemory()) * percent);
        }
    }

    public static class RetainFragment extends Fragment {
        private Object mObject;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public void setObject(Object object) {
            this.mObject = object;
        }

        public Object getObject() {
            return this.mObject;
        }
    }

    public ImageCache(ImageCacheParams cacheParams) {
        init(cacheParams);
    }

    public static ImageCache findOrCreateCache(FragmentManager fragmentManager, ImageCacheParams cacheParams) {
        RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager);
        if (mRetainFragment == null) {
            return null;
        }
        ImageCache imageCache = (ImageCache) mRetainFragment.getObject();
        if (imageCache == null) {
            imageCache = new ImageCache(cacheParams);
            mRetainFragment.setObject(imageCache);
        }
        return imageCache;
    }

    private void init(ImageCacheParams cacheParams) {
        if (cacheParams != null) {
            this.mCacheParams = cacheParams;
            if (this.mCacheParams.memoryCacheEnabled) {
                this.mMemoryCache = new LruCache<String, Bitmap>(this.mCacheParams.memCacheSize) {
                    protected int sizeOf(String key, Bitmap bitmap) {
                        return ImageCache.getBitmapSize(bitmap);
                    }
                };
            }
            if (cacheParams.initDiskCacheOnCreate) {
                initDiskCache();
            }
        }
    }

    public void initDiskCache() {
        if (this.mDiskCacheLock != null && this.mCacheParams != null) {
            synchronized (this.mDiskCacheLock) {
                if (this.mDiskLruCache == null || this.mDiskLruCache.isClosed()) {
                    File diskCacheDir = this.mCacheParams.diskCacheDir;
                    if (this.mCacheParams.diskCacheEnabled && diskCacheDir != null && getUsableSpace(diskCacheDir) > ((long) this.mCacheParams.diskCacheSize)) {
                        try {
                            this.mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, (long) this.mCacheParams.diskCacheSize);
                        } catch (IOException e) {
                            this.mCacheParams.diskCacheDir = null;
                        }
                    }
                }
                this.mDiskCacheStarting = false;
                this.mDiskCacheLock.notifyAll();
            }
        }
    }

    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data != null && bitmap != null && this.mDiskCacheLock != null) {
            if (this.mMemoryCache != null && this.mMemoryCache.get(data) == null) {
                this.mMemoryCache.put(data, bitmap);
            }
            synchronized (this.mDiskCacheLock) {
                if (this.mDiskLruCache != null) {
                    String key = hashKeyForDisk(data);
                    OutputStream out = null;
                    try {
                        Snapshot snapshot = this.mDiskLruCache.get(key);
                        if (snapshot == null) {
                            Editor editor = this.mDiskLruCache.edit(key);
                            if (editor != null) {
                                out = editor.newOutputStream(0);
                                bitmap.compress(this.mCacheParams.compressFormat, this.mCacheParams.compressQuality, out);
                                editor.commit();
                                out.close();
                            }
                        } else {
                            snapshot.getInputStream(0).close();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                Log.e("ImageCache", "add to cache in error");
                            }
                        }
                    } catch (IOException e2) {
                        if (out != null) {
                            out.close();
                        }
                    } catch (Exception e3) {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e4) {
                                Log.e("ImageCache", "add to cache in error");
                            }
                        }
                    } catch (IOException e5) {
                        Log.e("ImageCache", "add to cache in error");
                    } catch (Throwable th) {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e6) {
                                Log.e("ImageCache", "add to cache in error");
                            }
                        }
                    }
                }
            }
        }
    }

    public Bitmap getBitmapFromMemCache(String data) {
        if (this.mMemoryCache != null) {
            Bitmap memBitmap = (Bitmap) this.mMemoryCache.get(data);
            if (memBitmap != null) {
                return memBitmap;
            }
        }
        return null;
    }

    public void removeBitmapFromCache() {
        if (this.mMemoryCache != null) {
            this.mMemoryCache.evictAll();
        }
    }

    public Bitmap getBitmapFromDiskCache(String data) {
        if (this.mDiskCacheLock == null) {
            return null;
        }
        String key = hashKeyForDisk(data);
        synchronized (this.mDiskCacheLock) {
            while (this.mDiskCacheStarting) {
                try {
                    this.mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (this.mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    Snapshot snapshot = this.mDiskLruCache.get(key);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(0);
                        if (inputStream != null) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            } catch (Exception e2) {
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e3) {
                                        Log.e("ImageCache", "get map in error");
                                    }
                                }
                                return null;
                            } catch (Error e4) {
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e5) {
                                        Log.e("ImageCache", "get map in error");
                                    }
                                }
                                return null;
                            }
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                            Log.e("ImageCache", "get map in error");
                        }
                    }
                } catch (IOException e7) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e8) {
                            Log.e("ImageCache", "get map in error");
                        }
                    }
                    return null;
                } catch (IOException e9) {
                    Log.e("ImageCache", "get map in error");
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e10) {
                            Log.e("ImageCache", "get map in error");
                        }
                    }
                }
            }
        }
        return bitmap;
    }

    public void removeBitmapFromDiskCache(String data) {
        if (this.mDiskCacheLock != null) {
            String key = hashKeyForDisk(data);
            synchronized (this.mDiskCacheLock) {
                if (this.mDiskLruCache != null) {
                    try {
                        this.mDiskLruCache.remove(key);
                    } catch (IOException e) {
                        Log.e("ImageCache", "remove map in error");
                    }
                }
            }
        }
    }

    public void clearCache() {
        if (this.mDiskCacheLock != null) {
            if (this.mMemoryCache != null) {
                this.mMemoryCache.evictAll();
            }
            synchronized (this.mDiskCacheLock) {
                this.mDiskCacheStarting = true;
                if (!(this.mDiskLruCache == null || this.mDiskLruCache.isClosed())) {
                    try {
                        this.mDiskLruCache.delete();
                    } catch (IOException e) {
                    }
                    this.mDiskLruCache = null;
                    initDiskCache();
                }
            }
        }
    }

    public void flush() {
        if (this.mDiskCacheLock != null) {
            synchronized (this.mDiskCacheLock) {
                if (this.mDiskLruCache != null) {
                    try {
                        this.mDiskLruCache.flush();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public void close() {
        if (this.mDiskCacheLock != null) {
            synchronized (this.mDiskCacheLock) {
                if (this.mDiskLruCache != null) {
                    try {
                        if (!this.mDiskLruCache.isClosed()) {
                            this.mDiskLruCache.close();
                            this.mDiskLruCache = null;
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        if (context == null || uniqueName == null) {
            return null;
        }
        String cachePath;
        if ("mounted".equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable()) {
            cachePath = getExternalCacheDir(context).getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public static String hashKeyForDisk(String key) {
        if (key == null) {
            return null;
        }
        String cacheKey;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes("UTF-8"));
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        } catch (Exception e2) {
            return null;
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static int getBitmapSize(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        if (Utils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static boolean isExternalStorageRemovable() {
        if (Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static File getExternalCacheDir(Context context) {
        if (context == null) {
            return null;
        }
        if (Utils.hasFroyo() && context.getExternalCacheDir() != null) {
            return context.getExternalCacheDir();
        }
        return new File(Environment.getExternalStorageDirectory().getPath() + ("/Android/data/" + context.getPackageName() + "/cache/"));
    }

    public static long getUsableSpace(File path) {
        if (path == null) {
            return 0;
        }
        if (Utils.hasGingerbread()) {
            return path.getUsableSpace();
        }
        StatFs stats = new StatFs(path.getPath());
        return ((long) stats.getBlockSize()) * ((long) stats.getAvailableBlocks());
    }

    public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        if (fm == null) {
            return null;
        }
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag("ImageCache");
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, "ImageCache").commitAllowingStateLoss();
        }
        return mRetainFragment;
    }
}

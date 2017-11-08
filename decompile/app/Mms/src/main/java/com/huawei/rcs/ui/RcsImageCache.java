package com.huawei.rcs.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.LruCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class RcsImageCache {
    private static final int DEFAULT_MEM_CACHE_SIZE = ((int) (Runtime.getRuntime().maxMemory() / 8));
    private ImageCacheParams mCacheParams;
    private LruCache<String, Bitmap> mMemoryCache;

    public static class ImageCacheParams {
        public int memCacheSize;
        public boolean memoryCacheEnabled;

        public ImageCacheParams(int size, boolean isEnabled) {
            this.memCacheSize = size;
            this.memoryCacheEnabled = isEnabled;
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

    private RcsImageCache(ImageCacheParams cacheParams) {
        init(cacheParams);
    }

    public static RcsImageCache getInstance(FragmentManager fragmentManager, Context context) {
        RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager, context);
        RcsImageCache imageCache = (RcsImageCache) mRetainFragment.getObject();
        if (imageCache != null) {
            return imageCache;
        }
        boolean isEnabled;
        int size = 0;
        try {
            size = DEFAULT_MEM_CACHE_SIZE;
            isEnabled = Boolean.valueOf(context.getResources().getString(R.string.cache_enable)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            isEnabled = true;
            MLog.i("RcsImageCache FileTrans: ", "getInstance Exception size= " + size + " isEnabled= " + true);
        }
        imageCache = new RcsImageCache(new ImageCacheParams(size, isEnabled));
        mRetainFragment.setObject(imageCache);
        return imageCache;
    }

    private int getBitmapSize(Bitmap map) {
        int bitmapSize = 0;
        if (map != null) {
            bitmapSize = map.getByteCount();
        }
        return bitmapSize == 0 ? 1 : bitmapSize;
    }

    private void entryRemovedHandle(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        MLog.d("RcsImageCache FileTrans: ", "entryRemoved -> evicted = " + evicted + ", key = " + key + ", oldValue = " + oldValue + ", newValue = " + newValue);
    }

    private void init(ImageCacheParams cacheParams) {
        this.mCacheParams = cacheParams;
        if (this.mCacheParams.memoryCacheEnabled) {
            this.mMemoryCache = new LruCache<String, Bitmap>(this.mCacheParams.memCacheSize) {
                protected int sizeOf(String key, Bitmap value) {
                    return RcsImageCache.this.getBitmapSize(value);
                }

                protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                    super.entryRemoved(evicted, key, oldValue, newValue);
                    RcsImageCache.this.entryRemovedHandle(evicted, key, oldValue, newValue);
                }
            };
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addBitmapToCache(String data, Bitmap value) {
        if (!(data == null || value == null || this.mMemoryCache == null)) {
            this.mMemoryCache.put(data, value);
        }
    }

    public void removeBitmapCache(String key) {
        if (this.mMemoryCache != null) {
            this.mMemoryCache.remove(key);
        }
    }

    public Bitmap getBitmapFromMemCache(String data) {
        if (this.mMemoryCache == null || TextUtils.isEmpty(data)) {
            return null;
        }
        return (Bitmap) this.mMemoryCache.get(data);
    }

    public void clearCache() {
        if (this.mMemoryCache != null) {
            this.mMemoryCache.evictAll();
        }
    }

    private static RetainFragment findOrCreateRetainFragment(FragmentManager fm, Context context) {
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag("RcsImageCache");
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            Activity activity = (Activity) Activity.class.cast(context);
            if (!(activity == null || activity.isFinishing() || activity.isDestroyed())) {
                fm.beginTransaction().add(mRetainFragment, "RcsImageCache").commitAllowingStateLoss();
            }
        }
        return mRetainFragment;
    }
}

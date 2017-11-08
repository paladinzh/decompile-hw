package com.android.mms.util;

import android.content.Context;
import com.autonavi.amap.mapcore.VTMCDataCache;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageCacheService {
    private static long[] sCrcTable = new long[256];
    private BlobCache mCache;
    private Context mContext;

    public static class ImageData {
        byte[] mData;
        int mOffset;

        public ImageData(byte[] data, int offset) {
            this.mData = data;
            this.mOffset = offset;
        }
    }

    static {
        for (int i = 0; i < 256; i++) {
            long part = (long) i;
            for (int j = 0; j < 8; j++) {
                part = (part >> 1) ^ ((((int) part) & 1) != 0 ? -7661587058870466123L : 0);
            }
            sCrcTable[i] = part;
        }
    }

    public ImageCacheService(Context context) {
        this.mCache = CacheManager.getCache(context, "imgcache", VTMCDataCache.MAXSIZE, 20971520, 3);
        this.mContext = context;
    }

    public ImageData getImageData(String path, int type) {
        byte[] key = makeKey(path, type);
        long cacheKey = crc64Long(key);
        try {
            byte[] value;
            synchronized (this.mCache) {
                value = this.mCache.lookup(cacheKey);
            }
            if (value != null && isSameKey(key, value)) {
                return new ImageData(value, key.length);
            }
        } catch (IOException e) {
        }
        return null;
    }

    public void putImageData(String path, int type, byte[] value) {
        byte[] key = makeKey(path, type);
        long cacheKey = crc64Long(key);
        ByteBuffer buffer = ByteBuffer.allocate(key.length + value.length);
        buffer.put(key);
        buffer.put(value);
        synchronized (this.mCache) {
            try {
                this.mCache.insert(cacheKey, buffer.array());
            } catch (IOException e) {
            }
        }
    }

    public void clear() {
        CacheManager.clear(this.mContext);
    }

    private static byte[] makeKey(String path, int type) {
        return getBytes(path + "+" + type);
    }

    private static boolean isSameKey(byte[] key, byte[] buffer) {
        int n = key.length;
        if (buffer.length < n) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (key[i] != buffer[i]) {
                return false;
            }
        }
        return true;
    }

    public static final long crc64Long(byte[] buffer) {
        long crc = -1;
        for (byte b : buffer) {
            crc = sCrcTable[(((int) crc) ^ b) & 255] ^ (crc >> 8);
        }
        return crc;
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[(in.length() * 2)];
        int output = 0;
        for (char ch : in.toCharArray()) {
            int i = output + 1;
            result[output] = (byte) (ch & 255);
            output = i + 1;
            result[i] = (byte) (ch >> 8);
        }
        return result;
    }
}

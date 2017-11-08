package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;

public class NinePatchTexture extends ResourceTexture {
    protected NinePatchChunk mChunk;
    private SmallCache<NinePatchInstance> mInstanceCache = new SmallCache();

    private static class SmallCache<V> {
        private int mCount;
        private int[] mKey;
        private V[] mValue;

        private SmallCache() {
            this.mKey = new int[16];
            this.mValue = new Object[16];
        }

        public V put(int key, V value) {
            if (this.mCount == 16) {
                V old = this.mValue[15];
                this.mKey[15] = key;
                this.mValue[15] = value;
                return old;
            }
            this.mKey[this.mCount] = key;
            this.mValue[this.mCount] = value;
            this.mCount++;
            return null;
        }

        public V get(int key) {
            int i = 0;
            while (i < this.mCount) {
                if (this.mKey[i] == key) {
                    V targetValue = this.mValue[i];
                    if (this.mCount > 8 && i > 0) {
                        int tmpKey = this.mKey[i];
                        this.mKey[i] = this.mKey[i - 1];
                        this.mKey[i - 1] = tmpKey;
                        V tmpValue = this.mValue[i];
                        this.mValue[i] = this.mValue[i - 1];
                        this.mValue[i - 1] = tmpValue;
                    }
                    return targetValue;
                }
                i++;
            }
            return null;
        }

        public void clear() {
            for (int i = 0; i < this.mCount; i++) {
                this.mValue[i] = null;
            }
            this.mCount = 0;
        }

        public int size() {
            return this.mCount;
        }

        public V valueAt(int i) {
            return this.mValue[i];
        }
    }

    public NinePatchTexture(Context context, int resId) {
        super(context, resId);
    }

    protected Bitmap onGetBitmap() {
        NinePatchChunk ninePatchChunk = null;
        if (this.mBitmap != null) {
            return this.mBitmap;
        }
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), this.mResId, options);
        this.mBitmap = bitmap;
        setSize(bitmap.getWidth(), bitmap.getHeight());
        if (bitmap.getNinePatchChunk() != null) {
            ninePatchChunk = NinePatchChunk.deserialize(bitmap.getNinePatchChunk());
        }
        this.mChunk = ninePatchChunk;
        if (this.mChunk != null) {
            return bitmap;
        }
        throw new RuntimeException("invalid nine-patch image: " + this.mResId);
    }

    public Rect getPaddings() {
        if (this.mChunk == null) {
            onGetBitmap();
        }
        return this.mChunk.mPaddings;
    }

    public NinePatchChunk getNinePatchChunk() {
        if (this.mChunk == null) {
            onGetBitmap();
        }
        return this.mChunk;
    }

    private NinePatchInstance findInstance(GLCanvas canvas, int w, int h) {
        int key = w;
        key = (w << 16) | h;
        NinePatchInstance instance = (NinePatchInstance) this.mInstanceCache.get(key);
        if (instance == null) {
            instance = new NinePatchInstance(this, w, h);
            NinePatchInstance removed = (NinePatchInstance) this.mInstanceCache.put(key, instance);
            if (removed != null) {
                removed.recycle(canvas);
            }
        }
        return instance;
    }

    public void draw(GLCanvas canvas, int x, int y, int w, int h) {
        if (!isLoaded()) {
            this.mInstanceCache.clear();
        }
        if (w != 0 && h != 0) {
            findInstance(canvas, w, h).draw(canvas, this, x, y);
        }
    }

    public void recycle() {
        super.recycle();
        GLCanvas canvas = this.mCanvasRef;
        if (canvas != null) {
            int n = this.mInstanceCache.size();
            for (int i = 0; i < n; i++) {
                ((NinePatchInstance) this.mInstanceCache.valueAt(i)).recycle(canvas);
            }
            this.mInstanceCache.clear();
        }
    }
}

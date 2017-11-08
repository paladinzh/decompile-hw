package com.huawei.gallery.editor.cache;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import com.android.gallery3d.util.GalleryLog;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedList;

public class BubbleCache {
    private LinkedList<Integer> mBubbleCacheList = new LinkedList();
    private HashMap<Integer, SoftReference<NinePatchDrawable>> mBubbleCacheMap = new HashMap(8);

    public synchronized NinePatchDrawable getBubble(int resId, Context context) {
        SoftReference<NinePatchDrawable> ninePatchDrawableSoftReference = (SoftReference) this.mBubbleCacheMap.get(Integer.valueOf(resId));
        if (ninePatchDrawableSoftReference != null) {
            NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) ninePatchDrawableSoftReference.get();
            if (ninePatchDrawable != null) {
                return ninePatchDrawable;
            }
            this.mBubbleCacheList.remove(Integer.valueOf(resId));
        }
        Drawable drawable = context.getResources().getDrawable(resId);
        if (drawable instanceof NinePatchDrawable) {
            if (this.mBubbleCacheList.size() >= 8) {
                this.mBubbleCacheMap.remove((Integer) this.mBubbleCacheList.pollFirst());
            }
            this.mBubbleCacheList.addLast(Integer.valueOf(resId));
            this.mBubbleCacheMap.put(Integer.valueOf(resId), new SoftReference((NinePatchDrawable) drawable));
            return (NinePatchDrawable) drawable;
        }
        GalleryLog.w("BubbleCache", "get bubble res failed:" + drawable + ", resId:" + resId);
        return null;
    }

    public synchronized void clear() {
        this.mBubbleCacheList.clear();
        this.mBubbleCacheMap.clear();
    }
}

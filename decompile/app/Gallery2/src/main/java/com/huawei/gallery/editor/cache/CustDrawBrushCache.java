package com.huawei.gallery.editor.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapShader;
import android.graphics.Shader.TileMode;
import com.android.gallery3d.util.GalleryLog;
import java.util.HashMap;
import java.util.LinkedList;

public class CustDrawBrushCache {
    private LinkedList<Integer> mBrushCacheList = new LinkedList();
    private HashMap<Integer, BitmapShader> mBrushCacheMap = new HashMap(8);

    public synchronized BitmapShader getBrush(int resId, Context context) {
        BitmapShader shader = (BitmapShader) this.mBrushCacheMap.get(Integer.valueOf(resId));
        if (shader != null) {
            return shader;
        }
        if (this.mBrushCacheList.size() >= 8) {
            this.mBrushCacheMap.remove((Integer) this.mBrushCacheList.pollFirst());
        }
        Options opt = new Options();
        opt.inPreferredConfig = Config.ARGB_8888;
        Bitmap resBitmap = BitmapFactory.decodeResource(context.getResources(), resId, opt);
        if (resBitmap == null) {
            GalleryLog.w("CustDrawBrushCache", "get brush res null:" + resId);
            return null;
        }
        this.mBrushCacheList.addLast(Integer.valueOf(resId));
        shader = new BitmapShader(resBitmap, TileMode.REPEAT, TileMode.REPEAT);
        this.mBrushCacheMap.put(Integer.valueOf(resId), shader);
        return shader;
    }

    public synchronized void clear() {
        this.mBrushCacheList.clear();
        this.mBrushCacheMap.clear();
    }
}

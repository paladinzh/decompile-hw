package com.huawei.gallery.ui;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.util.GalleryLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupTitlePool {
    private static Map<String, Entry> mTitleCache = new ConcurrentHashMap(20);

    private static class Entry {
        private TextPaint paint;
        private String title;

        private Entry() {
        }
    }

    public static void put(String group, String title, Paint paint) {
        Entry entry = new Entry();
        entry.title = title;
        entry.paint = new TextPaint(paint);
        mTitleCache.put(group, entry);
    }

    public static StringTexture getStringTexture(String group) {
        Entry entry = (Entry) mTitleCache.get(group);
        if (entry == null) {
            GalleryLog.w("GroupTitlePool", group + "has not been put in cache pool. ");
            return null;
        }
        TextPaint textPaint = entry.paint;
        if (textPaint == null) {
            textPaint = new TextPaint();
            if (entry.title == null) {
                entry.title = "";
            }
        }
        textPaint.setTypeface(Typeface.create("HwChinese-medium", 0));
        textPaint.setColor(-16777216);
        return StringTexture.newInstance(entry.title, textPaint);
    }
}

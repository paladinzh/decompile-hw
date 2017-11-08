package com.huawei.gallery.anim;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.animation.Interpolator;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.RawTexture;
import com.android.gallery3d.ui.SlotFilter;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;

public class PhotoFallbackEffect extends Animation implements SlotFilter {
    private static final Interpolator ANIM_INTERPOLATE = new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
    private ArrayList<Entry> mList = new ArrayList();
    private PositionProvider mPositionProvider;
    private float mProgress;
    private RectF mSource = new RectF();
    private RectF mTarget = new RectF();

    public static class Entry {
        public Rect dest;
        public int index;
        public Path path;
        public int relativeIndex;
        public Rect source;
        public RawTexture texture;

        public Entry(Path path, Rect source, RawTexture texture, int i) {
            this.path = path;
            this.source = source;
            this.texture = texture;
            this.relativeIndex = i;
        }
    }

    public interface PositionProvider {
        Rect getPosition(int i);
    }

    public PhotoFallbackEffect() {
        setDuration(250);
        setInterpolator(ANIM_INTERPOLATE);
    }

    public void addEntry(Path path, Rect rect, RawTexture texture, int i) {
        this.mList.add(new Entry(path, rect, texture, i));
    }

    public boolean draw(GLCanvas canvas) {
        boolean more = calculate(AnimationTime.get());
        int n = this.mList.size();
        for (int i = 0; i < n; i++) {
            Entry entry = (Entry) this.mList.get(i);
            if (entry.index >= 0) {
                entry.dest = this.mPositionProvider.getPosition(entry.index);
                drawEntry(canvas, entry);
            }
        }
        return more;
    }

    private void drawEntry(GLCanvas canvas, Entry entry) {
        if (entry.texture.isLoaded()) {
            float w = (float) entry.texture.getWidth();
            float h = (float) entry.texture.getHeight();
            Rect s = entry.source;
            Rect d = entry.dest;
            float p = this.mProgress;
            int max = Math.max(GalleryUtils.getWidthPixels(), GalleryUtils.getHeightPixels());
            canvas.fillRect(0.0f, 0.0f, (float) max, (float) max, Color.argb((int) ((WMElement.CAMERASIZEVALUE1B1 - this.mProgress) * 255.0f), 0, 0, 0));
            if (d != null) {
                float scale = ((((float) d.height()) / ((float) Math.min(s.width(), s.height()))) * p) + ((WMElement.CAMERASIZEVALUE1B1 - p) * WMElement.CAMERASIZEVALUE1B1);
                float cx = (((float) d.centerX()) * p) + (((float) s.centerX()) * (WMElement.CAMERASIZEVALUE1B1 - p));
                float cy = (((float) d.centerY()) * p) + (((float) s.centerY()) * (WMElement.CAMERASIZEVALUE1B1 - p));
                float ch = ((float) s.height()) * scale;
                float cw = ((float) s.width()) * scale;
                float dal;
                if (w > h) {
                    this.mTarget.set(cx - (ch / 2.0f), cy - (ch / 2.0f), (ch / 2.0f) + cx, (ch / 2.0f) + cy);
                    this.mSource.set((w - h) / 2.0f, 0.0f, (w + h) / 2.0f, h);
                    canvas.drawTexture(entry.texture, this.mSource, this.mTarget);
                    canvas.save(1);
                    dal = ((cw - ch) / 2.0f) * p;
                    this.mTarget.set((cx - (cw / 2.0f)) + dal, cy - (ch / 2.0f), cx - (ch / 2.0f), (ch / 2.0f) + cy);
                    this.mSource.set(((w - h) * p) / 2.0f, 0.0f, (w - h) / 2.0f, h);
                    canvas.drawTexture(entry.texture, this.mSource, this.mTarget);
                    this.mTarget.set((ch / 2.0f) + cx, cy - (ch / 2.0f), ((cw / 2.0f) + cx) - dal, (ch / 2.0f) + cy);
                    this.mSource.set((w + h) / 2.0f, 0.0f, w - (((w - h) / 2.0f) * p), h);
                    canvas.drawTexture(entry.texture, this.mSource, this.mTarget);
                    canvas.restore();
                } else {
                    this.mTarget.set(cx - (cw / 2.0f), cy - (cw / 2.0f), (cw / 2.0f) + cx, (cw / 2.0f) + cy);
                    this.mSource.set(0.0f, (h - w) / 2.0f, w, (h + w) / 2.0f);
                    canvas.drawTexture(entry.texture, this.mSource, this.mTarget);
                    canvas.save(1);
                    dal = ((ch - cw) / 2.0f) * p;
                    this.mTarget.set(cx - (cw / 2.0f), (cy - (ch / 2.0f)) + dal, (cw / 2.0f) + cx, cy - (cw / 2.0f));
                    this.mSource.set(0.0f, ((h - w) * p) / 2.0f, w, (h - w) / 2.0f);
                    canvas.drawTexture(entry.texture, this.mSource, this.mTarget);
                    this.mTarget.set(cx - (cw / 2.0f), (cw / 2.0f) + cy, (cw / 2.0f) + cx, ((ch / 2.0f) + cy) - dal);
                    this.mSource.set(0.0f, (w + h) / 2.0f, w, h - (((h - w) / 2.0f) * p));
                    canvas.drawTexture(entry.texture, this.mSource, this.mTarget);
                    canvas.restore();
                }
            }
        }
    }

    protected void onCalculate(float progress) {
        this.mProgress = progress;
    }

    public void setPositionProvider(int entryIndex, PositionProvider provider) {
        this.mPositionProvider = provider;
        if (this.mPositionProvider != null) {
            int n = this.mList.size();
            for (int i = 0; i < n; i++) {
                Entry entry = (Entry) this.mList.get(i);
                entry.index = entry.relativeIndex + entryIndex;
            }
        }
    }

    public boolean acceptSlot(int index) {
        int n = this.mList.size();
        for (int i = 0; i < n; i++) {
            if (((Entry) this.mList.get(i)).index == index) {
                return false;
            }
        }
        return true;
    }
}

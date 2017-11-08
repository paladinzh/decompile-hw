package com.huawei.gallery.editor.ui;

import android.text.TextPaint;
import com.autonavi.amap.mapcore.MapConfig;

public class LabelPainData {
    public boolean bold;
    public int color;
    public int iconRes;
    public boolean italic;
    public boolean shadow;
    public boolean valid;

    public LabelPainData(int iconRes, int color) {
        this(iconRes, color, false, false, false);
    }

    public LabelPainData(int iconRes, int color, boolean bold, boolean italic, boolean shadow) {
        this.iconRes = iconRes;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.shadow = shadow;
        this.valid = true;
    }

    public LabelPainData(LabelPainData painData) {
        this(painData.iconRes, painData.color, painData.bold, painData.italic, painData.shadow);
        this.valid = painData.valid;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof LabelPainData)) {
            return false;
        }
        LabelPainData painData = (LabelPainData) o;
        if (this.iconRes == painData.iconRes && this.color == painData.color && this.bold == painData.bold && this.italic == painData.italic && this.shadow == painData.shadow && this.valid == painData.valid) {
            z = true;
        }
        return z;
    }

    public void updateTextPaint(TextPaint textPaint) {
        int i;
        textPaint.setAntiAlias(true);
        textPaint.setColor(this.color);
        textPaint.setFakeBoldText(this.bold);
        textPaint.setTextSkewX(this.italic ? -0.5f : 0.0f);
        if (this.shadow) {
            i = 5;
        } else {
            i = 0;
        }
        textPaint.setShadowLayer((float) i, MapConfig.MIN_ZOOM, MapConfig.MIN_ZOOM, -7829368);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean canSelected(LabelPainData data) {
        return this.valid && this.iconRes == data.iconRes;
    }
}

package com.android.ex.editstyledtext;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.util.Log;

public class EditStyledText$EditStyledTextSpans$MarqueeSpan extends CharacterStyle {
    private int mMarqueeColor;
    private int mType;

    public EditStyledText$EditStyledTextSpans$MarqueeSpan(int type, int bgc) {
        this.mType = type;
        checkType(type);
        this.mMarqueeColor = getMarqueeColor(type, bgc);
    }

    public void resetColor(int bgc) {
        this.mMarqueeColor = getMarqueeColor(this.mType, bgc);
    }

    private int getMarqueeColor(int type, int bgc) {
        int a = Color.alpha(bgc);
        int r = Color.red(bgc);
        int g = Color.green(bgc);
        int b = Color.blue(bgc);
        if (a == 0) {
            a = 128;
        }
        switch (type) {
            case 0:
                if (r <= 128) {
                    r = (255 - r) / 2;
                    break;
                }
                r /= 2;
                break;
            case 1:
                if (g <= 128) {
                    g = (255 - g) / 2;
                    break;
                }
                g /= 2;
                break;
            case 2:
                return 16777215;
            default:
                Log.e("EditStyledText", "--- getMarqueeColor: got illigal marquee ID.");
                return 16777215;
        }
        return Color.argb(a, r, g, b);
    }

    private boolean checkType(int type) {
        if (type == 0 || type == 1) {
            return true;
        }
        Log.e("EditStyledTextSpan", "--- Invalid type of MarqueeSpan");
        return false;
    }

    public void updateDrawState(TextPaint tp) {
        tp.bgColor = this.mMarqueeColor;
    }
}

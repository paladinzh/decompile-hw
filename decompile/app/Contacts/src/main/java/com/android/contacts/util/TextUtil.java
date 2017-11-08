package com.android.contacts.util;

import android.graphics.Paint;
import android.net.Uri;

public class TextUtil {
    public static int getTextWidth(String aText, float textSize) {
        if (aText == null || aText.length() == 0) {
            return 0;
        }
        Paint p = new Paint();
        p.setTextSize(textSize);
        return (int) Math.round(((double) p.measureText(aText)) + 0.5d);
    }

    public static int getTextWidth(Paint paint, String aText, float textSize) {
        if (aText == null || aText.length() == 0) {
            return 0;
        }
        if (paint == null) {
            paint = new Paint();
        }
        paint.setTextSize(textSize);
        return (int) Math.round(((double) paint.measureText(aText)) + 0.5d);
    }

    public static boolean stringOrNullEquals(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null || !a.equals(b)) {
            return false;
        }
        return true;
    }

    public static boolean equals(Uri a, Uri b) {
        if (a == null) {
            if (b != null) {
                return false;
            }
            return true;
        } else if (b == null) {
            return false;
        } else {
            return a.toString().equals(b.toString());
        }
    }
}

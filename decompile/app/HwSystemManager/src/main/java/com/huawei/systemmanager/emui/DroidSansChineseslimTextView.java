package com.huawei.systemmanager.emui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.huawei.systemmanager.comm.widget.TypefaceUtil;
import com.huawei.systemmanager.comm.widget.TypefaceUtil.QianheiTypeface;

public class DroidSansChineseslimTextView extends TextView {
    public DroidSansChineseslimTextView(Context context) {
        super(context);
        setCustTypeface();
    }

    public DroidSansChineseslimTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustTypeface();
    }

    public DroidSansChineseslimTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustTypeface();
    }

    private void setCustTypeface() {
        Typeface typeface = TypefaceUtil.getTypefaceFromName(getContext(), QianheiTypeface.NAME);
        if (typeface != null) {
            setTypeface(typeface);
        }
    }
}

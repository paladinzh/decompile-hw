package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.huawei.systemmanager.R;

public class BaseTextView extends TextView {
    private String mCustomTypeface;

    public BaseTextView(Context context) {
        super(context);
    }

    public BaseTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        setCustomTypeFace(context);
    }

    public BaseTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(context, attrs);
        setCustomTypeFace(context);
    }

    private final void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BaseTextView);
        this.mCustomTypeface = a.getString(3);
        a.recycle();
    }

    private final void setCustomTypeFace(Context ctx) {
        Typeface tf = TypefaceUtil.getTypefaceFromName(ctx, this.mCustomTypeface);
        if (tf != null) {
            setTypeface(tf);
        }
    }
}

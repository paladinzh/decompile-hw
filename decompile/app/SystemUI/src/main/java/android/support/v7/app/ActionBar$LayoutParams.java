package android.support.v7.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

public class ActionBar$LayoutParams extends MarginLayoutParams {
    public int gravity;

    public ActionBar$LayoutParams(@NonNull Context c, AttributeSet attrs) {
        super(c, attrs);
        this.gravity = 0;
        TypedArray a = c.obtainStyledAttributes(attrs, R$styleable.ActionBarLayout);
        this.gravity = a.getInt(R$styleable.ActionBarLayout_android_layout_gravity, 0);
        a.recycle();
    }

    public ActionBar$LayoutParams(int width, int height) {
        super(width, height);
        this.gravity = 0;
        this.gravity = 8388627;
    }

    public ActionBar$LayoutParams(ActionBar$LayoutParams source) {
        super(source);
        this.gravity = 0;
        this.gravity = source.gravity;
    }

    public ActionBar$LayoutParams(LayoutParams source) {
        super(source);
        this.gravity = 0;
    }
}

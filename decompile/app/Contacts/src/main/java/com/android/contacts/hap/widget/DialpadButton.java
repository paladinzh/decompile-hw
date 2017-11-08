package com.android.contacts.hap.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.android.contacts.R$styleable;
import com.android.contacts.hap.CommonUtilMethods;

public class DialpadButton extends ImageButton {
    public DialpadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (CommonUtilMethods.isNotSupportRippleInLargeTheme(context.getResources()) && attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R$styleable.LargeTheme);
            if (typedArray != null) {
                setBackground(typedArray.getDrawable(0));
                typedArray.recycle();
            }
        }
    }
}

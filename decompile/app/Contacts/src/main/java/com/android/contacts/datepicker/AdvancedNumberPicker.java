package com.android.contacts.datepicker;

import android.content.Context;
import android.util.AttributeSet;
import huawei.android.widget.NumberPicker;
import java.util.Locale;

public class AdvancedNumberPicker extends NumberPicker {
    public static final Formatter TWO_DIGIT_FORMATTER = new Formatter() {
        final Object[] mArgs = new Object[1];
        final StringBuilder mBuilder = new StringBuilder();
        final java.util.Formatter mFmt = new java.util.Formatter(this.mBuilder, Locale.US);
    };
    private Formatter mFormatter;

    public interface Formatter {
    }

    public AdvancedNumberPicker(Context context) {
        super(context);
    }

    public AdvancedNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvancedNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFormatter(Formatter formatter) {
        if (formatter != this.mFormatter) {
            this.mFormatter = formatter;
        }
    }
}

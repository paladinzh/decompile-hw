package com.android.settings.bluetooth;

import android.content.Context;
import android.util.AttributeSet;
import com.android.settings.ProgressCategory;

public class BluetoothProgressCategory extends ProgressCategory {
    public BluetoothProgressCategory(Context context) {
        this(context, null);
    }

    public BluetoothProgressCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEmptyTextRes(2131624445);
    }

    public BluetoothProgressCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BluetoothProgressCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setEmptyTextRes(2131624445);
    }
}

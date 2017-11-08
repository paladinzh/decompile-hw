package com.android.mms.ui;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

public class EmuiSwitchPreference extends SwitchPreference {
    public EmuiSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EmuiSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmuiSwitchPreference(Context context) {
        super(context);
    }

    protected void onBindView(View view) {
        TextView title = (TextView) view.findViewById(16908310);
        if (title != null) {
            title.setSingleLine(false);
        }
        clearListenerInViewGroup((ViewGroup) view);
        super.onBindView(view);
    }

    private void clearListenerInViewGroup(ViewGroup viewGroup) {
        if (viewGroup != null) {
            int count = viewGroup.getChildCount();
            for (int n = 0; n < count; n++) {
                View childView = viewGroup.getChildAt(n);
                if (childView instanceof Switch) {
                    ((Switch) childView).setOnCheckedChangeListener(null);
                    return;
                }
                if (childView instanceof ViewGroup) {
                    clearListenerInViewGroup((ViewGroup) childView);
                }
            }
        }
    }
}

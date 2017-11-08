package com.huawei.systemmanager.emui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;

public class SectionDividerView extends View {
    private static final String SECTOR_TEXT_COLOR = "@*androidhwext:color/emui_label_primary_text";

    public SectionDividerView(Context context) {
        super(context);
        setStyleForEmui();
    }

    public SectionDividerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setStyleForEmui();
    }

    public SectionDividerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setStyleForEmui();
    }

    private void setStyleForEmui() {
        Resources resources = getResources();
        if (resources != null) {
            int themeID = resources.getIdentifier(SECTOR_TEXT_COLOR, null, null);
            if (themeID != 0) {
                setBackgroundResource(themeID);
            }
        }
    }
}

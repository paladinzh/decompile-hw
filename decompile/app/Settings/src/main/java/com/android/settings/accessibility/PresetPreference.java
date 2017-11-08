package com.android.settings.accessibility;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.CaptioningManager;
import android.widget.TextView;
import com.android.internal.widget.SubtitleView;

public class PresetPreference extends ListDialogPreference {
    private final CaptioningManager mCaptioningManager;

    public PresetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(2130968827);
        setListItemLayoutResource(2130969018);
        this.mCaptioningManager = (CaptioningManager) context.getSystemService("captioning");
    }

    public boolean shouldDisableDependents() {
        if (getValue() == -1) {
            return super.shouldDisableDependents();
        }
        return true;
    }

    protected void onBindListItem(View view, int index) {
        SubtitleView previewText = (SubtitleView) view.findViewById(2131886519);
        CaptionPropertiesFragment.applyCaptionProperties(this.mCaptioningManager, previewText, view.findViewById(2131886343), getValueAt(index));
        previewText.setTextSize(32.0f * getContext().getResources().getDisplayMetrics().density);
        CharSequence title = getTitleAt(index);
        if (title != null) {
            ((TextView) view.findViewById(2131886387)).setText(title);
        }
    }
}

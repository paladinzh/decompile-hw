package com.android.settings.accessibility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ColorPreference extends ListDialogPreference {
    private ColorDrawable mPreviewColor;
    private boolean mPreviewEnabled;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(2130968827);
        setListItemLayoutResource(2130968679);
    }

    public boolean shouldDisableDependents() {
        return Color.alpha(getValue()) != 0 ? super.shouldDisableDependents() : true;
    }

    protected CharSequence getTitleAt(int index) {
        CharSequence title = super.getTitleAt(index);
        if (title != null) {
            return title;
        }
        int value = getValueAt(index);
        int r = Color.red(value);
        int g = Color.green(value);
        int b = Color.blue(value);
        return getContext().getString(2131625908, new Object[]{Integer.valueOf(r), Integer.valueOf(g), Integer.valueOf(b)});
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mPreviewEnabled) {
            float f;
            ImageView previewImage = (ImageView) view.findViewById(2131886871);
            int argb = getValue();
            if (Color.alpha(argb) < 255) {
                previewImage.setBackgroundResource(2130838730);
            } else {
                previewImage.setBackground(null);
            }
            if (this.mPreviewColor == null) {
                this.mPreviewColor = new ColorDrawable(argb);
                previewImage.setImageDrawable(this.mPreviewColor);
            } else {
                this.mPreviewColor.setColor(argb);
            }
            CharSequence summary = getSummary();
            if (TextUtils.isEmpty(summary)) {
                previewImage.setContentDescription(null);
            } else {
                previewImage.setContentDescription(summary);
            }
            if (isEnabled()) {
                f = 1.0f;
            } else {
                f = 0.2f;
            }
            previewImage.setAlpha(f);
        }
    }

    protected void onBindListItem(View view, int index) {
        int argb = getValueAt(index);
        ImageView swatch = (ImageView) view.findViewById(2131886386);
        if (Color.alpha(argb) < 255) {
            swatch.setBackgroundResource(2130838730);
        } else {
            swatch.setBackground(null);
        }
        Drawable foreground = swatch.getDrawable();
        if (foreground instanceof ColorDrawable) {
            ((ColorDrawable) foreground).setColor(argb);
        } else {
            swatch.setImageDrawable(new ColorDrawable(argb));
        }
        CharSequence title = getTitleAt(index);
        if (title != null) {
            ((TextView) view.findViewById(2131886387)).setText(title);
        }
    }
}

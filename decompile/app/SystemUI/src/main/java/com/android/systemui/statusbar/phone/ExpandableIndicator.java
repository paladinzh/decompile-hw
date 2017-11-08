package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.R;

public class ExpandableIndicator extends ImageView {
    private boolean mExpanded;
    private boolean mIsDefaultDirection = true;

    public ExpandableIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setImageResource(getDrawableResourceId(this.mExpanded));
        setContentDescription(getContentDescription(this.mExpanded));
    }

    public void setExpanded(boolean expanded) {
        if (expanded != this.mExpanded) {
            this.mExpanded = expanded;
            setImageDrawable(getContext().getDrawable(getDrawableResourceId(!this.mExpanded)));
            setContentDescription(getContentDescription(expanded));
        }
    }

    private int getDrawableResourceId(boolean expanded) {
        int i = R.drawable.statusbar_header_arrow_up_selector;
        int i2 = R.drawable.statusbar_header_arrow_down_selector;
        if (this.mIsDefaultDirection) {
            if (!expanded) {
                i2 = R.drawable.statusbar_header_arrow_up_selector;
            }
            return i2;
        }
        if (!expanded) {
            i = R.drawable.statusbar_header_arrow_down_selector;
        }
        return i;
    }

    private String getContentDescription(boolean expanded) {
        if (expanded) {
            return this.mContext.getString(R.string.accessibility_quick_settings_collapse);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_expand);
    }
}

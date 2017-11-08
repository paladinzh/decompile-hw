package com.android.systemui.qs.customize;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.qs.QSIconView;
import com.android.systemui.qs.QSTileView;
import libcore.util.Objects;

public class CustomizeTileView extends QSTileView {
    private TextView mAppLabel;
    private int mLabelMaxLines;

    public CustomizeTileView(Context context, QSIconView icon) {
        super(context, icon);
        this.mIsCust = true;
    }

    protected void createLabel() {
        super.createLabel();
        this.mLabelMaxLines = this.mLabel.getMaxLines();
        this.mAppLabel = (TextView) findViewById(R.id.tile_app_label);
        this.mAppLabel.setAlpha(0.6f);
        this.mAppLabel.setSingleLine(true);
        this.mAppLabel.setVisibility(8);
        this.mAppLabel.setEllipsize(TruncateAt.valueOf("END"));
    }

    public void setShowAppLabel(boolean showAppLabel) {
        this.mAppLabel.setVisibility(8);
        this.mLabel.setSingleLine(showAppLabel);
        if (!showAppLabel) {
            this.mLabel.setMaxLines(this.mLabelMaxLines);
        }
        this.mLabel.setEllipsize(TruncateAt.valueOf("END"));
    }

    public void setAppLabel(CharSequence label) {
        if (!Objects.equal(label, this.mAppLabel.getText())) {
            this.mAppLabel.setText(label);
        }
    }

    public TextView getAppLabel() {
        return this.mAppLabel;
    }
}

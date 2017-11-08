package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile.State;
import libcore.util.Objects;

public class QSTileView extends QSTileBaseView {
    protected final Context mContext;
    protected boolean mIsCust;
    protected TextView mLabel;
    private ImageView mPadLock;

    public QSTileView(Context context, QSIconView icon) {
        this(context, icon, false);
    }

    public QSTileView(Context context, QSIconView icon, boolean collapsedView) {
        super(context, icon, collapsedView);
        this.mIsCust = false;
        this.mContext = context;
        setClipChildren(false);
        setClickable(true);
        updateTopPadding();
        setId(View.generateViewId());
        createLabel();
        setOrientation(1);
        setGravity(1);
    }

    private void updateTopPadding() {
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateTopPadding();
        FontSizeUtils.updateFontSize(this.mLabel, R.dimen.qs_tile_text_size);
    }

    protected void createLabel() {
        View view = LayoutInflater.from(this.mContext).inflate(R.layout.qs_tile_label, null);
        this.mLabel = (TextView) view.findViewById(R.id.tile_label);
        this.mPadLock = (ImageView) view.findViewById(R.id.restricted_padlock);
        addView(view);
    }

    protected void handleStateChanged(final State state) {
        int i = 0;
        super.handleStateChanged(state);
        if (state.textChangedDelay <= 0 || this.mIsCust) {
            updateText(state.labelTint, state.label);
        } else {
            postDelayed(new Runnable() {
                public void run() {
                    QSTileView.this.updateText(state.labelTint, state.label);
                }
            }, state.textChangedDelay);
        }
        this.mLabel.setEnabled(!state.disabledByPolicy);
        ImageView imageView = this.mPadLock;
        if (!state.disabledByPolicy) {
            i = 8;
        }
        imageView.setVisibility(i);
    }

    private void updateText(int labelTint, CharSequence label) {
        if (!Objects.equal(this.mLabel.getText(), label)) {
            this.mLabel.setText(label);
        }
        if (3 == labelTint) {
            this.mLabel.setTextColor(this.mContext.getResources().getColor(R.color.qs_tile_tint_opening));
        } else if (2 == labelTint) {
            this.mLabel.setTextColor(this.mContext.getResources().getColor(R.color.qs_tile_tint_disable));
        } else if (1 == labelTint) {
            this.mLabel.setTextColor(this.mContext.getResources().getColor(R.color.qs_tile_tint_on));
        } else {
            this.mLabel.setTextColor(this.mContext.getResources().getColor(R.color.qs_tile_tint_off));
        }
    }
}

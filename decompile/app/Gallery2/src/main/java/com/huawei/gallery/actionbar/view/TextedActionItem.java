package com.huawei.gallery.actionbar.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.ImmersionUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class TextedActionItem extends LinearLayout implements ActionItem {
    private static final float[] IMAGE_ALPHA = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.5f, 0.3f};
    private static final float[][] TEXT_ALPHA = new float[][]{new float[]{0.65f, 0.325f, 0.195f}, new float[]{0.7f, 0.35f, 0.2f}};
    private Action mAction = Action.NONE;
    private ImageView mImageView;
    protected int mStyle = 0;
    private TextView mTextView;

    public TextedActionItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(1);
        init();
    }

    protected boolean isLand(Configuration config) {
        Configuration currentConfig;
        if (config == null) {
            currentConfig = getResources().getConfiguration();
        } else {
            currentConfig = config;
        }
        return currentConfig.orientation == 2;
    }

    private void init() {
        removeAllViews();
        inflate(getContext(), R.layout.text_action_item, this);
        this.mTextView = (TextView) findViewById(R.id.texted_action_text);
        this.mTextView.setAlpha(TEXT_ALPHA[this.mStyle][0]);
        this.mImageView = (ImageView) findViewById(R.id.texted_action_image);
        this.mImageView.setAlpha(IMAGE_ALPHA[0]);
        setClickable(true);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setPadding();
        this.mTextView.setVisibility(!isLand(null) ? 0 : 8);
        applyStyle(this.mStyle);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setPadding();
        this.mTextView.setVisibility(!isLand(newConfig) ? 0 : 8);
        applyStyle(this.mStyle);
    }

    private void setPadding() {
        setPadding(0, getResources().getDimensionPixelSize(R.dimen.action_bar_textedaction_pad_top), 0, 0);
    }

    private void setItemView(boolean isEnable, boolean isPressed) {
        int state = isEnable ? isPressed ? 1 : 0 : 2;
        this.mImageView.setAlpha(IMAGE_ALPHA[state]);
        if (this.mAction.highLight) {
            this.mTextView.setAlpha(IMAGE_ALPHA[state]);
        } else {
            this.mTextView.setAlpha(TEXT_ALPHA[this.mStyle][state]);
        }
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        setItemView(isEnabled(), pressed);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mAction.disableTextResID != -1) {
            this.mTextView.setText(enabled ? getResources().getString(this.mAction.textResID) : getResources().getString(this.mAction.disableTextResID));
        }
        setItemView(enabled, isPressed());
    }

    private void setActionImage(Action action, Configuration config) {
        int style = (config.orientation != 2 || ImmersionUtils.getImmersionStyle(getContext()) == 0) ? this.mStyle : 1;
        switch (style) {
            case 1:
                this.mImageView.setImageDrawable(ColorfulUtils.mappingColorfulDrawable(getContext(), false, action.iconResID, action.iconWhiteResID));
                this.mTextView.setTextColor(-1);
                return;
            default:
                this.mImageView.setImageDrawable(ColorfulUtils.mappingColorfulDrawable(getContext(), true, action.iconResID, action.iconWhiteResID));
                this.mTextView.setTextColor(-16777216);
                return;
        }
    }

    public Action getAction() {
        return this.mAction;
    }

    public View asView() {
        return this;
    }

    public void applyStyle(int style) {
        this.mStyle = style;
        setActionImage(getAction(), getResources().getConfiguration());
    }
}

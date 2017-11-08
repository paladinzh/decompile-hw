package com.android.gallery3d.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.gallery3d.R;

public class AccountPreference extends Preference {
    private CharSequence mDescription;
    TextView mDescriptionView;
    private Drawable mHeadPortrait;
    ImageView mIcon;

    public AccountPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AccountPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccountPreference(Context context) {
        super(context);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.mDescriptionView = (TextView) view.findViewById(R.id.description);
        this.mIcon = (ImageView) view.findViewById(R.id.icon);
        updateDescriptionView();
        updateIcon();
    }

    public void setIcon(Drawable icon) {
        this.mHeadPortrait = icon;
        updateIcon();
    }

    public void setIcon(int iconResId) {
        this.mHeadPortrait = getContext().getDrawable(iconResId);
        updateIcon();
    }

    public void setDescription(CharSequence description) {
        this.mDescription = description;
        updateDescriptionView();
    }

    private void updateIcon() {
        if (this.mIcon != null) {
            this.mIcon.setImageDrawable(this.mHeadPortrait);
        }
    }

    private void updateDescriptionView() {
        if (this.mDescriptionView != null) {
            this.mDescriptionView.setText(this.mDescription);
            if (this.mDescription == null) {
                this.mDescriptionView.setVisibility(8);
            } else {
                this.mDescriptionView.setVisibility(0);
            }
        }
    }
}

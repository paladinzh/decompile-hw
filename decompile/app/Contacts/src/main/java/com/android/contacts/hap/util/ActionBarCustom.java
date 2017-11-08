package com.android.contacts.hap.util;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.R;

public class ActionBarCustom {
    private ActionBar mActionBar;
    private Context mContext;
    private View mCustTitle;
    private LinearLayout mCustomLayout;
    private Drawable mIcon1Drawable;
    private ImageView mIcon1View;
    private boolean mIcon1Visible;
    private Drawable mIcon2Drawable;
    private ImageView mIcon2View;
    private boolean mIcon2Visible;
    private OnClickListener mListener1;
    private OnClickListener mListener2;
    private TextView mSubTitle;
    private TextView mTitle;
    private LinearLayout mTitleContainer;

    public ActionBarCustom(Context context, ActionBar actionBar) {
        this.mContext = context;
        this.mActionBar = actionBar;
        init();
    }

    public void init() {
        this.mCustomLayout = (LinearLayout) LayoutInflater.from(this.mContext).inflate(R.layout.action_bar_custom, null);
        this.mIcon1View = (ImageView) this.mCustomLayout.findViewById(R.id.icon1);
        this.mIcon2View = (ImageView) this.mCustomLayout.findViewById(R.id.icon2);
        this.mTitleContainer = (LinearLayout) this.mCustomLayout.findViewById(R.id.titleContainer);
        this.mTitle = (TextView) this.mCustomLayout.findViewById(R.id.action_bar_title);
        this.mSubTitle = (TextView) this.mCustomLayout.findViewById(R.id.action_bar_subtitle);
        setStartIconVisible(this.mIcon1Visible);
        setEndIconVisible(this.mIcon2Visible);
        setStartIconImage(this.mIcon1Drawable);
        setEndIconImage(this.mIcon2Drawable);
        setStartIconListener(this.mListener1);
        setEndIconListener(this.mListener2);
        if (this.mActionBar != null) {
            this.mTitle.setText(this.mActionBar.getTitle());
            this.mSubTitle.setText(this.mActionBar.getSubtitle());
            this.mActionBar.setDisplayShowCustomEnabled(true);
            this.mActionBar.setDisplayShowTitleEnabled(false);
            this.mActionBar.setCustomView(this.mCustomLayout);
        }
    }

    public void setStartIconVisible(boolean icon1Visible) {
        this.mIcon1Visible = icon1Visible;
        triggerIconsVisible(this.mIcon1Visible, this.mIcon2Visible);
    }

    public void setEndIconVisible(boolean icon2Visible) {
        this.mIcon2Visible = icon2Visible;
        triggerIconsVisible(this.mIcon1Visible, this.mIcon2Visible);
    }

    public void triggerIconsVisible(boolean icon1Visible, boolean icon2Visible) {
        int i = 4;
        if (icon1Visible) {
            this.mIcon1View.setVisibility(0);
        } else {
            this.mIcon1View.setVisibility(icon2Visible ? 4 : 8);
        }
        if (icon2Visible) {
            this.mIcon2View.setVisibility(0);
            return;
        }
        ImageView imageView = this.mIcon2View;
        if (!icon1Visible) {
            i = 8;
        }
        imageView.setVisibility(i);
    }

    public void setStartIconImage(Drawable icon1) {
        if (icon1 != null) {
            this.mIcon1Drawable = icon1;
            this.mIcon1View.setImageDrawable(icon1);
        }
    }

    public void setEndIconImage(Drawable icon2) {
        if (icon2 != null) {
            this.mIcon2Drawable = icon2;
            this.mIcon2View.setImageDrawable(icon2);
        }
    }

    public void setStartIconListener(OnClickListener listener1) {
        if (listener1 != null) {
            this.mListener1 = listener1;
            this.mIcon1View.setOnClickListener(listener1);
        }
    }

    public void setEndIconListener(OnClickListener listener2) {
        if (listener2 != null) {
            this.mListener2 = listener2;
            this.mIcon2View.setOnClickListener(listener2);
        }
    }

    public void setStartIcon(boolean icon1Visible, Drawable icon1, OnClickListener listener1) {
        setStartIconVisible(icon1Visible);
        setStartIconImage(icon1);
        setStartIconListener(listener1);
    }

    public void setEndIcon(boolean icon2Visible, Drawable icon2, OnClickListener listener2) {
        setEndIconVisible(icon2Visible);
        setEndIconImage(icon2);
        setEndIconListener(listener2);
    }

    public void setCustomTitle(View view) {
        this.mTitleContainer.removeView(this.mTitle);
        this.mTitleContainer.removeView(this.mSubTitle);
        if (this.mCustTitle != null && this.mCustTitle.getParent() == this.mTitleContainer) {
            this.mTitleContainer.removeView(this.mCustTitle);
        }
        ViewParent vp = view.getParent();
        if (vp != null && (vp instanceof ViewGroup)) {
            ((ViewGroup) vp).removeView(view);
        }
        this.mTitleContainer.addView(view);
        this.mCustTitle = view;
    }
}

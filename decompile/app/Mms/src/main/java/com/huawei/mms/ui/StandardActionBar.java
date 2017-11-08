package com.huawei.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import com.google.android.gms.R;
import com.huawei.mms.util.ResEx;

public class StandardActionBar extends LinearLayout implements ActionBarInterface, OnMenuItemClickListener, OnDismissListener {
    Activity mActivity = null;
    private OnClickListener mClickMenuListener = new OnClickListener() {
        public void onClick(View v) {
            if (StandardActionBar.this.mPopupMenu == null) {
                StandardActionBar.this.showPopup(v);
            } else {
                StandardActionBar.this.close();
            }
        }
    };
    ImageView mEndIcon;
    int mMenuRes = 0;
    protected PopupMenu mPopupMenu;
    ImageView mStartIcon;
    TextView mTitle;
    TextView mTitleNumber;

    public StandardActionBar(Context context) {
        super(context);
    }

    public StandardActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StandardActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mStartIcon = (ImageView) findViewById(R.id.icon1);
        this.mEndIcon = (ImageView) findViewById(R.id.icon2);
        this.mTitle = (TextView) findViewById(R.id.action_bar_title);
        this.mTitle.setTypeface(Typeface.create("chnfzxh", 0));
        this.mTitleNumber = (TextView) findViewById(R.id.action_bar_title_number);
        this.mTitleNumber.setVisibility(8);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        ResEx.self().setBlurWallpaperBackground(getContext(), this);
    }

    public boolean onMenuItemClick(MenuItem menuItem) {
        this.mPopupMenu = null;
        if (this.mActivity != null) {
            return this.mActivity.onOptionsItemSelected(menuItem);
        }
        return false;
    }

    private void showPopup(View r) {
        if (this.mPopupMenu == null && this.mActivity != null) {
            PopupMenu popup = new PopupMenu(this.mActivity, r);
            popup.setOnMenuItemClickListener(this);
            popup.setOnDismissListener(this);
            popup.getMenuInflater().inflate(this.mMenuRes, popup.getMenu());
            this.mActivity.onPrepareOptionsMenu(popup.getMenu());
            this.mPopupMenu = popup;
            popup.show();
        }
    }

    public void onDismiss(PopupMenu menu) {
        this.mPopupMenu = null;
    }

    public void close() {
        if (this.mPopupMenu != null) {
            this.mPopupMenu.dismiss();
        }
    }
}

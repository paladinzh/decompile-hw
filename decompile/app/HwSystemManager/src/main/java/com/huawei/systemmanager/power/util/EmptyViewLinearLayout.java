package com.huawei.systemmanager.power.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;

public class EmptyViewLinearLayout extends LinearLayout {
    private static final String TAG = "EmptyViewLinearLayout";
    private TextView mEmptyText;
    private ImageView mEmptyView;
    private LinearLayout mLinearLayout;

    public EmptyViewLinearLayout(Context context) {
        super(context);
    }

    public EmptyViewLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyViewLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addEmptyViewAndText(context, attrs);
    }

    public void setEmptyView(int drawableID) {
        if (this.mEmptyView != null) {
            this.mEmptyView.setImageResource(drawableID);
        }
    }

    public void setEmptyText(String txt) {
        if (this.mEmptyText != null) {
            this.mEmptyText.setText(txt);
        }
    }

    public void setEmptyText(int txtId) {
        if (this.mEmptyText != null) {
            this.mEmptyText.setText(txtId);
        }
    }

    private void addEmptyViewAndText(Context context, AttributeSet attrs) {
        setLayout();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.emptylinearlayout);
        if (a != null) {
            Drawable emptyDrawable = a.getDrawable(0);
            String emptyStr = a.getString(1);
            a.recycle();
            LayoutParams textLayoutParams = new LayoutParams(-2, -2);
            textLayoutParams.gravity = 1;
            LayoutParams imageLayoutParams = new LayoutParams(-2, -2);
            imageLayoutParams.gravity = 1;
            imageLayoutParams.width = dp2px(context, 70.0f);
            imageLayoutParams.height = dp2px(context, 70.0f);
            this.mEmptyView = new ImageView(context);
            if (emptyDrawable != null) {
                this.mEmptyView.setImageDrawable(emptyDrawable);
            }
            this.mEmptyText = new TextView(context);
            Resources resources = GlobalContext.getContext().getResources();
            this.mEmptyText.setTextColor(resources.getColor(resources.getIdentifier("menuitem_default_checkedcolor", "color", "androidhwext")));
            this.mEmptyText.setPadding(dp2px(context, 24.0f), 0, dp2px(context, 24.0f), 0);
            this.mEmptyText.setTextSize(2, 13.0f);
            if (emptyStr != null) {
                this.mEmptyText.setText(emptyStr);
            }
            this.mEmptyView.setLayoutParams(imageLayoutParams);
            this.mEmptyText.setLayoutParams(textLayoutParams);
            this.mLinearLayout.addView(this.mEmptyView);
            this.mLinearLayout.addView(this.mEmptyText);
        }
    }

    private int dp2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void setLayout() {
        Context context = GlobalContext.getContext();
        if (context == null) {
            HwLog.i(TAG, "Activity not found");
            return;
        }
        this.mLinearLayout = new LinearLayout(context);
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.gravity = 1;
        this.mLinearLayout.setOrientation(1);
        int screenHeight = Utility.getScreenHeight(context);
        int marginTop = (((screenHeight * 3) / 10) - getStatusBarHeight(context)) - getActionbarHeight(context);
        if (layoutParams.topMargin == 0) {
            layoutParams.topMargin = marginTop;
        }
        this.mLinearLayout.setLayoutParams(layoutParams);
        addView(this.mLinearLayout);
    }

    private int getActionbarHeight(Context context) {
        TypedArray actionBarSizeTypedArray = context.obtainStyledAttributes(new int[]{16843499});
        if (actionBarSizeTypedArray == null) {
            return 0;
        }
        int height = actionBarSizeTypedArray.getDimensionPixelSize(0, 0);
        actionBarSizeTypedArray.recycle();
        return height;
    }

    private int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            return context.getResources().getDimensionPixelSize(Integer.parseInt(c.getField("status_bar_height").get(c.newInstance()).toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return statusBarHeight;
        }
    }
}

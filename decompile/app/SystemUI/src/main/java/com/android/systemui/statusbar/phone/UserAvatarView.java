package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.systemui.R$styleable;

public class UserAvatarView extends View {
    private final UserIconDrawable mDrawable;

    public UserAvatarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDrawable = new UserIconDrawable();
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.UserAvatarView, defStyleAttr, defStyleRes);
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    setFrameColor(a.getColorStateList(attr));
                    break;
                case 1:
                    setAvatarPadding(a.getDimension(attr, 0.0f));
                    break;
                case 2:
                    setFrameWidth(a.getDimension(attr, 0.0f));
                    break;
                case 3:
                    setFramePadding(a.getDimension(attr, 0.0f));
                    break;
                case 5:
                    setBadgeDiameter(a.getDimension(attr, 0.0f));
                    break;
                case 6:
                    setBadgeMargin(a.getDimension(attr, 0.0f));
                    break;
                default:
                    break;
            }
        }
        a.recycle();
        setBackground(this.mDrawable);
    }

    public UserAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public UserAvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserAvatarView(Context context) {
        this(context, null);
    }

    public void setFrameColor(ColorStateList color) {
        this.mDrawable.setFrameColor(color);
    }

    public void setFrameWidth(float frameWidth) {
        this.mDrawable.setFrameWidth(frameWidth);
    }

    public void setFramePadding(float framePadding) {
        this.mDrawable.setFramePadding(framePadding);
    }

    public void setAvatarPadding(float avatarPadding) {
        this.mDrawable.setPadding(avatarPadding);
    }

    public void setBadgeDiameter(float diameter) {
        this.mDrawable.setBadgeRadius(0.5f * diameter);
    }

    public void setBadgeMargin(float margin) {
        this.mDrawable.setBadgeMargin(margin);
    }

    public void setAvatarWithBadge(Bitmap avatar, int userId) {
        this.mDrawable.setIcon(avatar);
        this.mDrawable.setBadgeIfManagedUser(getContext(), userId);
    }

    public void setDrawableWithBadge(Drawable d, int userId) {
        if (d instanceof UserIconDrawable) {
            throw new RuntimeException("Recursively adding UserIconDrawable");
        }
        this.mDrawable.setIconDrawable(d);
        this.mDrawable.setBadgeIfManagedUser(getContext(), userId);
    }
}

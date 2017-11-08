package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.phone.UserAvatarView;

public class UserDetailItemView extends LinearLayout {
    private Typeface mActivatedTypeface;
    private UserAvatarView mAvatar;
    private TextView mName;
    private Typeface mRegularTypeface;
    private View mRestrictedPadlock;

    public UserDetailItemView(Context context) {
        this(context, null);
    }

    public UserDetailItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserDetailItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public UserDetailItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.UserDetailItemView, defStyleAttr, defStyleRes);
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    this.mRegularTypeface = Typeface.create(a.getString(attr), 0);
                    break;
                case 1:
                    this.mActivatedTypeface = Typeface.create(a.getString(attr), 0);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
    }

    public static UserDetailItemView convertOrInflate(Context context, View convertView, ViewGroup root) {
        if (!(convertView instanceof UserDetailItemView)) {
            convertView = LayoutInflater.from(context).inflate(R.layout.qs_user_detail_item, root, false);
        }
        return (UserDetailItemView) convertView;
    }

    public void bind(String name, Bitmap picture, int userId) {
        this.mName.setText(name);
        this.mAvatar.setAvatarWithBadge(picture, userId);
    }

    public void bind(String name, Drawable picture, int userId) {
        this.mName.setText(name);
        this.mAvatar.setDrawableWithBadge(picture, userId);
    }

    public void setAvatarEnabled(boolean enabled) {
        this.mAvatar.setEnabled(enabled);
    }

    public void setDisabledByAdmin(boolean disabled) {
        boolean z;
        boolean z2 = false;
        this.mRestrictedPadlock.setVisibility(disabled ? 0 : 8);
        TextView textView = this.mName;
        if (disabled) {
            z = false;
        } else {
            z = true;
        }
        textView.setEnabled(z);
        UserAvatarView userAvatarView = this.mAvatar;
        if (!disabled) {
            z2 = true;
        }
        userAvatarView.setEnabled(z2);
    }

    public void setEnabled(boolean enabled) {
        this.mName.setEnabled(enabled);
        this.mAvatar.setEnabled(enabled);
    }

    protected void onFinishInflate() {
        this.mAvatar = (UserAvatarView) findViewById(R.id.user_picture);
        this.mName = (TextView) findViewById(R.id.user_name);
        if (this.mRegularTypeface == null) {
            this.mRegularTypeface = this.mName.getTypeface();
        }
        if (this.mActivatedTypeface == null) {
            this.mActivatedTypeface = this.mName.getTypeface();
        }
        updateTypeface();
        this.mRestrictedPadlock = findViewById(R.id.restricted_padlock);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FontSizeUtils.updateFontSize(this.mName, R.dimen.qs_detail_item_secondary_text_size);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateTypeface();
    }

    private void updateTypeface() {
        this.mName.setTypeface(ArrayUtils.contains(getDrawableState(), 16843518) ? this.mActivatedTypeface : this.mRegularTypeface);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}

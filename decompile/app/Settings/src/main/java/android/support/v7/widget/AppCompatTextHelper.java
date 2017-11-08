package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v7.appcompat.R$styleable;
import android.support.v7.text.AllCapsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.widget.TextView;

class AppCompatTextHelper {
    private static final int[] VIEW_ATTRS = new int[]{16842804, 16843119, 16843117, 16843120, 16843118};
    private TintInfo mDrawableBottomTint;
    private TintInfo mDrawableLeftTint;
    private TintInfo mDrawableRightTint;
    private TintInfo mDrawableTopTint;
    final TextView mView;

    static AppCompatTextHelper create(TextView textView) {
        if (VERSION.SDK_INT >= 17) {
            return new AppCompatTextHelperV17(textView);
        }
        return new AppCompatTextHelper(textView);
    }

    AppCompatTextHelper(TextView view) {
        this.mView = view;
    }

    void loadFromAttributes(AttributeSet attrs, int defStyleAttr) {
        Context context = this.mView.getContext();
        AppCompatDrawableManager drawableManager = AppCompatDrawableManager.get();
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, VIEW_ATTRS, defStyleAttr, 0);
        int ap = a.getResourceId(0, -1);
        if (a.hasValue(1)) {
            this.mDrawableLeftTint = createTintInfo(context, drawableManager, a.getResourceId(1, 0));
        }
        if (a.hasValue(2)) {
            this.mDrawableTopTint = createTintInfo(context, drawableManager, a.getResourceId(2, 0));
        }
        if (a.hasValue(3)) {
            this.mDrawableRightTint = createTintInfo(context, drawableManager, a.getResourceId(3, 0));
        }
        if (a.hasValue(4)) {
            this.mDrawableBottomTint = createTintInfo(context, drawableManager, a.getResourceId(4, 0));
        }
        a.recycle();
        boolean hasPwdTm = this.mView.getTransformationMethod() instanceof PasswordTransformationMethod;
        boolean z = false;
        boolean allCapsSet = false;
        ColorStateList colorStateList = null;
        if (ap != -1) {
            a = TintTypedArray.obtainStyledAttributes(context, ap, R$styleable.TextAppearance);
            if (!hasPwdTm && a.hasValue(R$styleable.TextAppearance_textAllCaps)) {
                allCapsSet = true;
                z = a.getBoolean(R$styleable.TextAppearance_textAllCaps, false);
            }
            if (VERSION.SDK_INT < 23 && a.hasValue(R$styleable.TextAppearance_android_textColor)) {
                colorStateList = a.getColorStateList(R$styleable.TextAppearance_android_textColor);
            }
            a.recycle();
        }
        a = TintTypedArray.obtainStyledAttributes(context, attrs, R$styleable.TextAppearance, defStyleAttr, 0);
        if (!hasPwdTm && a.hasValue(R$styleable.TextAppearance_textAllCaps)) {
            allCapsSet = true;
            z = a.getBoolean(R$styleable.TextAppearance_textAllCaps, false);
        }
        if (VERSION.SDK_INT < 23 && a.hasValue(R$styleable.TextAppearance_android_textColor)) {
            colorStateList = a.getColorStateList(R$styleable.TextAppearance_android_textColor);
        }
        a.recycle();
        if (colorStateList != null) {
            this.mView.setTextColor(colorStateList);
        }
        if (!hasPwdTm && allCapsSet) {
            setAllCaps(z);
        }
    }

    void onSetTextAppearance(Context context, int resId) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, resId, R$styleable.TextAppearance);
        if (a.hasValue(R$styleable.TextAppearance_textAllCaps)) {
            setAllCaps(a.getBoolean(R$styleable.TextAppearance_textAllCaps, false));
        }
        if (VERSION.SDK_INT < 23 && a.hasValue(R$styleable.TextAppearance_android_textColor)) {
            ColorStateList textColor = a.getColorStateList(R$styleable.TextAppearance_android_textColor);
            if (textColor != null) {
                this.mView.setTextColor(textColor);
            }
        }
        a.recycle();
    }

    void setAllCaps(boolean allCaps) {
        TransformationMethod allCapsTransformationMethod;
        TextView textView = this.mView;
        if (allCaps) {
            allCapsTransformationMethod = new AllCapsTransformationMethod(this.mView.getContext());
        } else {
            allCapsTransformationMethod = null;
        }
        textView.setTransformationMethod(allCapsTransformationMethod);
    }

    void applyCompoundDrawablesTints() {
        if (this.mDrawableLeftTint == null && this.mDrawableTopTint == null && this.mDrawableRightTint == null) {
            if (this.mDrawableBottomTint == null) {
                return;
            }
        }
        Drawable[] compoundDrawables = this.mView.getCompoundDrawables();
        applyCompoundDrawableTint(compoundDrawables[0], this.mDrawableLeftTint);
        applyCompoundDrawableTint(compoundDrawables[1], this.mDrawableTopTint);
        applyCompoundDrawableTint(compoundDrawables[2], this.mDrawableRightTint);
        applyCompoundDrawableTint(compoundDrawables[3], this.mDrawableBottomTint);
    }

    final void applyCompoundDrawableTint(Drawable drawable, TintInfo info) {
        if (drawable != null && info != null) {
            AppCompatDrawableManager.tintDrawable(drawable, info, this.mView.getDrawableState());
        }
    }

    protected static TintInfo createTintInfo(Context context, AppCompatDrawableManager drawableManager, int drawableId) {
        ColorStateList tintList = drawableManager.getTintList(context, drawableId);
        if (tintList == null) {
            return null;
        }
        TintInfo tintInfo = new TintInfo();
        tintInfo.mHasTintList = true;
        tintInfo.mTintList = tintList;
        return tintInfo;
    }
}

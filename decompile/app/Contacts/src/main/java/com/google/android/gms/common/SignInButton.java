package com.google.android.gms.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import com.google.android.gms.R;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.zzab;
import com.google.android.gms.common.internal.zzac;
import com.google.android.gms.dynamic.zzg.zza;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* compiled from: Unknown */
public final class SignInButton extends FrameLayout implements OnClickListener {
    public static final int COLOR_AUTO = 2;
    public static final int COLOR_DARK = 0;
    public static final int COLOR_LIGHT = 1;
    public static final int SIZE_ICON_ONLY = 2;
    public static final int SIZE_STANDARD = 0;
    public static final int SIZE_WIDE = 1;
    private int mColor;
    private int mSize;
    private Scope[] zzafT;
    private View zzafU;
    private OnClickListener zzafV;

    @Retention(RetentionPolicy.SOURCE)
    /* compiled from: Unknown */
    public @interface ButtonSize {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* compiled from: Unknown */
    public @interface ColorScheme {
    }

    public SignInButton(Context context) {
        this(context, null);
    }

    public SignInButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignInButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.zzafV = null;
        zza(context, attrs);
        setStyle(this.mSize, this.mColor, this.zzafT);
    }

    private static Button zza(Context context, int i, int i2, Scope[] scopeArr) {
        Button zzac = new zzac(context);
        zzac.zza(context.getResources(), i, i2, scopeArr);
        return zzac;
    }

    private void zza(Context context, AttributeSet attributeSet) {
        int i = 0;
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.SignInButton, 0, 0);
        try {
            this.mSize = obtainStyledAttributes.getInt(R.styleable.SignInButton_buttonSize, 0);
            this.mColor = obtainStyledAttributes.getInt(R.styleable.SignInButton_colorScheme, 2);
            String string = obtainStyledAttributes.getString(R.styleable.SignInButton_scopeUris);
            if (string != null) {
                String[] split = string.trim().split("\\s+");
                this.zzafT = new Scope[split.length];
                while (i < split.length) {
                    this.zzafT[i] = new Scope(split[i].toString());
                    i++;
                }
            } else {
                this.zzafT = null;
            }
            obtainStyledAttributes.recycle();
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
        }
    }

    private void zzar(Context context) {
        if (this.zzafU != null) {
            removeView(this.zzafU);
        }
        try {
            this.zzafU = zzab.zzb(context, this.mSize, this.mColor, this.zzafT);
        } catch (zza e) {
            Log.w("SignInButton", "Sign in button not found, using placeholder instead");
            this.zzafU = zza(context, this.mSize, this.mColor, this.zzafT);
        }
        addView(this.zzafU);
        this.zzafU.setEnabled(isEnabled());
        this.zzafU.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (this.zzafV != null && view == this.zzafU) {
            this.zzafV.onClick(this);
        }
    }

    public void setColorScheme(int colorScheme) {
        setStyle(this.mSize, colorScheme, this.zzafT);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.zzafU.setEnabled(enabled);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.zzafV = listener;
        if (this.zzafU != null) {
            this.zzafU.setOnClickListener(this);
        }
    }

    public void setScopes(Scope[] scopes) {
        setStyle(this.mSize, this.mColor, scopes);
    }

    public void setSize(int buttonSize) {
        setStyle(buttonSize, this.mColor, this.zzafT);
    }

    public void setStyle(int buttonSize, int colorScheme) {
        setStyle(buttonSize, colorScheme, this.zzafT);
    }

    public void setStyle(int buttonSize, int colorScheme, Scope[] scopes) {
        this.mSize = buttonSize;
        this.mColor = colorScheme;
        this.zzafT = scopes;
        zzar(getContext());
    }
}

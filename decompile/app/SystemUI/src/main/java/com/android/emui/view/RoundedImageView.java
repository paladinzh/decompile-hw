package com.android.emui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.emui.drawable.RoundedDrawable;
import com.android.systemui.R$styleable;

public class RoundedImageView extends ImageView {
    private static final /* synthetic */ int[] -android-widget-ImageView$ScaleTypeSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled;
    public static final TileMode DEFAULT_TILE_MODE = TileMode.CLAMP;
    private static final ScaleType[] SCALE_TYPES = new ScaleType[]{ScaleType.MATRIX, ScaleType.FIT_XY, ScaleType.FIT_START, ScaleType.FIT_CENTER, ScaleType.FIT_END, ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE};
    private ColorStateList borderColor;
    private float borderWidth;
    private float cornerRadius;
    private boolean isOval;
    private Drawable mBackgroundDrawable;
    private ColorFilter mColorFilter;
    private boolean mColorMod;
    private Drawable mDrawable;
    private boolean mHasColorFilter;
    private int mResource;
    private ScaleType mScaleType;
    private boolean mutateBackground;
    private TileMode tileModeX;
    private TileMode tileModeY;

    private static /* synthetic */ int[] -getandroid-widget-ImageView$ScaleTypeSwitchesValues() {
        if (-android-widget-ImageView$ScaleTypeSwitchesValues != null) {
            return -android-widget-ImageView$ScaleTypeSwitchesValues;
        }
        int[] iArr = new int[ScaleType.values().length];
        try {
            iArr[ScaleType.CENTER.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ScaleType.CENTER_CROP.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ScaleType.CENTER_INSIDE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ScaleType.FIT_CENTER.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ScaleType.FIT_END.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ScaleType.FIT_START.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ScaleType.FIT_XY.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ScaleType.MATRIX.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        -android-widget-ImageView$ScaleTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean z;
        if (RoundedImageView.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public RoundedImageView(Context context) {
        super(context);
        this.cornerRadius = 0.0f;
        this.borderWidth = 0.0f;
        this.borderColor = ColorStateList.valueOf(-16777216);
        this.isOval = false;
        this.mutateBackground = false;
        this.tileModeX = DEFAULT_TILE_MODE;
        this.tileModeY = DEFAULT_TILE_MODE;
        this.mColorFilter = null;
        this.mHasColorFilter = false;
        this.mColorMod = false;
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.cornerRadius = 0.0f;
        this.borderWidth = 0.0f;
        this.borderColor = ColorStateList.valueOf(-16777216);
        this.isOval = false;
        this.mutateBackground = false;
        this.tileModeX = DEFAULT_TILE_MODE;
        this.tileModeY = DEFAULT_TILE_MODE;
        this.mColorFilter = null;
        this.mHasColorFilter = false;
        this.mColorMod = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.RoundedImageView, defStyle, 0);
        int index = a.getInt(0, -1);
        if (index >= 0) {
            setScaleType(SCALE_TYPES[index]);
        } else {
            setScaleType(ScaleType.FIT_CENTER);
        }
        this.cornerRadius = (float) a.getDimensionPixelSize(1, -1);
        this.borderWidth = (float) a.getDimensionPixelSize(2, -1);
        if (this.cornerRadius < 0.0f) {
            this.cornerRadius = 0.0f;
        }
        if (this.borderWidth < 0.0f) {
            this.borderWidth = 0.0f;
        }
        this.borderColor = a.getColorStateList(3);
        if (this.borderColor == null) {
            this.borderColor = ColorStateList.valueOf(-16777216);
        }
        this.mutateBackground = a.getBoolean(4, false);
        this.isOval = a.getBoolean(5, false);
        int tileMode = a.getInt(6, -2);
        if (tileMode != -2) {
            setTileModeX(parseTileMode(tileMode));
            setTileModeY(parseTileMode(tileMode));
        }
        int tileModeX = a.getInt(7, -2);
        if (tileModeX != -2) {
            setTileModeX(parseTileMode(tileModeX));
        }
        int tileModeY = a.getInt(8, -2);
        if (tileModeY != -2) {
            setTileModeY(parseTileMode(tileModeY));
        }
        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(true);
        a.recycle();
    }

    private static TileMode parseTileMode(int tileMode) {
        switch (tileMode) {
            case 0:
                return TileMode.CLAMP;
            case 1:
                return TileMode.REPEAT;
            case 2:
                return TileMode.MIRROR;
            default:
                return null;
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    public ScaleType getScaleType() {
        return this.mScaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        if (!-assertionsDisabled) {
            if (!(scaleType != null)) {
                throw new AssertionError();
            }
        }
        if (this.mScaleType != scaleType) {
            this.mScaleType = scaleType;
            switch (-getandroid-widget-ImageView$ScaleTypeSwitchesValues()[scaleType.ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    super.setScaleType(ScaleType.FIT_XY);
                    break;
                default:
                    super.setScaleType(scaleType);
                    break;
            }
            updateDrawableAttrs();
            updateBackgroundDrawableAttrs(false);
            invalidate();
        }
    }

    public void setImageDrawable(Drawable drawable) {
        this.mResource = 0;
        this.mDrawable = RoundedDrawable.fromDrawable(drawable);
        updateDrawableAttrs();
        super.setImageDrawable(this.mDrawable);
    }

    public void setImageBitmap(Bitmap bm) {
        this.mResource = 0;
        this.mDrawable = RoundedDrawable.fromBitmap(bm);
        updateDrawableAttrs();
        super.setImageDrawable(this.mDrawable);
    }

    public void setImageResource(int resId) {
        if (this.mResource != resId) {
            this.mResource = resId;
            this.mDrawable = resolveResource();
            updateDrawableAttrs();
            super.setImageDrawable(this.mDrawable);
        }
    }

    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(getDrawable());
    }

    private Drawable resolveResource() {
        Resources rsrc = getResources();
        if (rsrc == null) {
            return null;
        }
        Drawable d = null;
        if (this.mResource != 0) {
            try {
                d = rsrc.getDrawable(this.mResource);
            } catch (Exception e) {
                Log.w("RoundedImageView", "Unable to find resource: " + this.mResource, e);
                this.mResource = 0;
            }
        }
        return RoundedDrawable.fromDrawable(d);
    }

    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    private void updateDrawableAttrs() {
        updateAttrs(this.mDrawable);
    }

    private void updateBackgroundDrawableAttrs(boolean convert) {
        if (this.mutateBackground) {
            if (convert) {
                this.mBackgroundDrawable = RoundedDrawable.fromDrawable(this.mBackgroundDrawable);
            }
            updateAttrs(this.mBackgroundDrawable);
        }
    }

    public void setColorFilter(ColorFilter cf) {
        if (this.mColorFilter != cf) {
            this.mColorFilter = cf;
            this.mHasColorFilter = true;
            this.mColorMod = true;
            applyColorMod();
            invalidate();
        }
    }

    private void applyColorMod() {
        if (this.mDrawable != null && this.mColorMod) {
            this.mDrawable = this.mDrawable.mutate();
            if (this.mHasColorFilter) {
                this.mDrawable.setColorFilter(this.mColorFilter);
            }
        }
    }

    private void updateAttrs(Drawable drawable) {
        if (drawable != null) {
            if (drawable instanceof RoundedDrawable) {
                ((RoundedDrawable) drawable).setScaleType(this.mScaleType).setCornerRadius(this.cornerRadius).setBorderWidth(this.borderWidth).setBorderColor(this.borderColor).setOval(this.isOval).setTileModeX(this.tileModeX).setTileModeY(this.tileModeY);
                applyColorMod();
            } else if (drawable instanceof LayerDrawable) {
                LayerDrawable ld = (LayerDrawable) drawable;
                int layers = ld.getNumberOfLayers();
                for (int i = 0; i < layers; i++) {
                    updateAttrs(ld.getDrawable(i));
                }
            }
        }
    }

    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        this.mBackgroundDrawable = background;
        updateBackgroundDrawableAttrs(true);
        super.setBackgroundDrawable(this.mBackgroundDrawable);
    }

    public void setTileModeX(TileMode tileModeX) {
        if (this.tileModeX != tileModeX) {
            this.tileModeX = tileModeX;
            updateDrawableAttrs();
            updateBackgroundDrawableAttrs(false);
            invalidate();
        }
    }

    public void setTileModeY(TileMode tileModeY) {
        if (this.tileModeY != tileModeY) {
            this.tileModeY = tileModeY;
            updateDrawableAttrs();
            updateBackgroundDrawableAttrs(false);
            invalidate();
        }
    }
}

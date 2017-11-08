package com.android.setupwizardlib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.setupwizardlib.view.Illustration;
import com.android.setupwizardlib.view.NavigationBar;

public class SetupWizardLayout extends TemplateLayout {
    private ColorStateList mProgressBarColor;

    protected static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean mIsProgressBarShown = false;

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public SavedState(Parcel source) {
            boolean z = false;
            super(source);
            if (source.readInt() != 0) {
                z = true;
            }
            this.mIsProgressBarShown = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mIsProgressBarShown ? 1 : 0);
        }
    }

    public SetupWizardLayout(Context context) {
        super(context, 0, 0);
        init(null, R$attr.suwLayoutTheme);
    }

    public SetupWizardLayout(Context context, int template) {
        this(context, template, 0);
    }

    public SetupWizardLayout(Context context, int template, int containerId) {
        super(context, template, containerId);
        init(null, R$attr.suwLayoutTheme);
    }

    public SetupWizardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, R$attr.suwLayoutTheme);
    }

    @TargetApi(11)
    public SetupWizardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.SuwSetupWizardLayout, defStyleAttr, 0);
        Drawable background = a.getDrawable(R$styleable.SuwSetupWizardLayout_suwBackground);
        if (background != null) {
            setLayoutBackground(background);
        } else {
            Drawable backgroundTile = a.getDrawable(R$styleable.SuwSetupWizardLayout_suwBackgroundTile);
            if (backgroundTile != null) {
                setBackgroundTile(backgroundTile);
            }
        }
        Drawable illustration = a.getDrawable(R$styleable.SuwSetupWizardLayout_suwIllustration);
        if (illustration != null) {
            setIllustration(illustration);
        } else {
            Drawable illustrationImage = a.getDrawable(R$styleable.SuwSetupWizardLayout_suwIllustrationImage);
            Drawable horizontalTile = a.getDrawable(R$styleable.SuwSetupWizardLayout_suwIllustrationHorizontalTile);
            if (!(illustrationImage == null || horizontalTile == null)) {
                setIllustration(illustrationImage, horizontalTile);
            }
        }
        int decorPaddingTop = a.getDimensionPixelSize(R$styleable.SuwSetupWizardLayout_suwDecorPaddingTop, -1);
        if (decorPaddingTop == -1) {
            decorPaddingTop = getResources().getDimensionPixelSize(R$dimen.suw_decor_padding_top);
        }
        setDecorPaddingTop(decorPaddingTop);
        float illustrationAspectRatio = a.getFloat(R$styleable.SuwSetupWizardLayout_suwIllustrationAspectRatio, -1.0f);
        if (illustrationAspectRatio == -1.0f) {
            TypedValue out = new TypedValue();
            getResources().getValue(R$dimen.suw_illustration_aspect_ratio, out, true);
            illustrationAspectRatio = out.getFloat();
        }
        setIllustrationAspectRatio(illustrationAspectRatio);
        CharSequence headerText = a.getText(R$styleable.SuwSetupWizardLayout_suwHeaderText);
        if (headerText != null) {
            setHeaderText(headerText);
        }
        a.recycle();
    }

    protected Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.mIsProgressBarShown = isProgressBarShown();
        return ss;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (ss.mIsProgressBarShown) {
                showProgressBar();
            } else {
                hideProgressBar();
            }
            return;
        }
        Log.w("SetupWizardLayout", "Ignoring restore instance state " + state);
        super.onRestoreInstanceState(state);
    }

    protected View onInflateTemplate(LayoutInflater inflater, int template) {
        if (template == 0) {
            template = R$layout.suw_template;
        }
        try {
            return super.onInflateTemplate(inflater, template);
        } catch (RuntimeException e) {
            throw new InflateException("Unable to inflate layout. Are you using @style/SuwThemeMaterial (or its descendant) as your theme?", e);
        }
    }

    protected ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = R$id.suw_layout_content;
        }
        return super.findContainer(containerId);
    }

    public NavigationBar getNavigationBar() {
        View view = findManagedViewById(R$id.suw_layout_navigation_bar);
        return view instanceof NavigationBar ? (NavigationBar) view : null;
    }

    public void setHeaderText(int title) {
        TextView titleView = getHeaderTextView();
        if (titleView != null) {
            titleView.setText(title);
        }
    }

    public void setHeaderText(CharSequence title) {
        TextView titleView = getHeaderTextView();
        if (titleView != null) {
            titleView.setText(title);
        }
    }

    public TextView getHeaderTextView() {
        return (TextView) findManagedViewById(R$id.suw_layout_title);
    }

    public void setIllustration(Drawable drawable) {
        View view = findManagedViewById(R$id.suw_layout_decor);
        if (view instanceof Illustration) {
            ((Illustration) view).setIllustration(drawable);
        }
    }

    public void setIllustration(int asset, int horizontalTile) {
        View view = findManagedViewById(R$id.suw_layout_decor);
        if (view instanceof Illustration) {
            ((Illustration) view).setIllustration(getIllustration(asset, horizontalTile));
        }
    }

    private void setIllustration(Drawable asset, Drawable horizontalTile) {
        View view = findManagedViewById(R$id.suw_layout_decor);
        if (view instanceof Illustration) {
            ((Illustration) view).setIllustration(getIllustration(asset, horizontalTile));
        }
    }

    public void setIllustrationAspectRatio(float aspectRatio) {
        View view = findManagedViewById(R$id.suw_layout_decor);
        if (view instanceof Illustration) {
            ((Illustration) view).setAspectRatio(aspectRatio);
        }
    }

    public void setDecorPaddingTop(int paddingTop) {
        View view = findManagedViewById(R$id.suw_layout_decor);
        if (view != null) {
            view.setPadding(view.getPaddingLeft(), paddingTop, view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    public void setLayoutBackground(Drawable background) {
        View view = findManagedViewById(R$id.suw_layout_decor);
        if (view != null) {
            view.setBackgroundDrawable(background);
        }
    }

    private void setBackgroundTile(Drawable backgroundTile) {
        if (backgroundTile instanceof BitmapDrawable) {
            ((BitmapDrawable) backgroundTile).setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        }
        setLayoutBackground(backgroundTile);
    }

    private Drawable getIllustration(int asset, int horizontalTile) {
        Context context = getContext();
        return getIllustration(context.getResources().getDrawable(asset), context.getResources().getDrawable(horizontalTile));
    }

    @SuppressLint({"RtlHardcoded"})
    private Drawable getIllustration(Drawable asset, Drawable horizontalTile) {
        if (getContext().getResources().getBoolean(R$bool.suwUseTabletLayout)) {
            if (horizontalTile instanceof BitmapDrawable) {
                ((BitmapDrawable) horizontalTile).setTileModeX(TileMode.REPEAT);
                ((BitmapDrawable) horizontalTile).setGravity(48);
            }
            if (asset instanceof BitmapDrawable) {
                ((BitmapDrawable) asset).setGravity(51);
            }
            LayerDrawable layers = new LayerDrawable(new Drawable[]{horizontalTile, asset});
            if (VERSION.SDK_INT >= 19) {
                layers.setAutoMirrored(true);
            }
            return layers;
        }
        if (VERSION.SDK_INT >= 19) {
            asset.setAutoMirrored(true);
        }
        return asset;
    }

    protected View findManagedViewById(int id) {
        return findViewById(id);
    }

    public boolean isProgressBarShown() {
        View progressBar = findManagedViewById(R$id.suw_layout_progress);
        if (progressBar == null || progressBar.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public void setProgressBarShown(boolean shown) {
        View progressBar = findManagedViewById(R$id.suw_layout_progress);
        if (progressBar != null) {
            progressBar.setVisibility(shown ? 0 : 8);
        } else if (shown) {
            ViewStub progressBarStub = (ViewStub) findManagedViewById(R$id.suw_layout_progress_stub);
            if (progressBarStub != null) {
                progressBarStub.inflate();
            }
            if (this.mProgressBarColor != null) {
                setProgressBarColor(this.mProgressBarColor);
            }
        }
    }

    @Deprecated
    public void showProgressBar() {
        setProgressBarShown(true);
    }

    @Deprecated
    public void hideProgressBar() {
        setProgressBarShown(false);
    }

    public void setProgressBarColor(ColorStateList color) {
        this.mProgressBarColor = color;
        if (VERSION.SDK_INT >= 21) {
            ProgressBar bar = (ProgressBar) findViewById(R$id.suw_layout_progress);
            if (bar != null) {
                bar.setIndeterminateTintList(color);
            }
        }
    }
}

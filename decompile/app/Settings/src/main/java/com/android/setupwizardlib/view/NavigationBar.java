package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.android.setupwizardlib.R$attr;
import com.android.setupwizardlib.R$id;
import com.android.setupwizardlib.R$layout;
import com.android.setupwizardlib.R$style;

public class NavigationBar extends LinearLayout implements OnClickListener {
    private Button mBackButton;
    private NavigationBarListener mListener;
    private Button mMoreButton;
    private Button mNextButton;

    public interface NavigationBarListener {
        void onNavigateBack();

        void onNavigateNext();
    }

    private static int getNavbarTheme(Context context) {
        boolean isDarkBg = true;
        TypedArray attributes = context.obtainStyledAttributes(new int[]{R$attr.suwNavBarTheme, 16842800, 16842801});
        int theme = attributes.getResourceId(0, 0);
        if (theme == 0) {
            float[] foregroundHsv = new float[3];
            float[] backgroundHsv = new float[3];
            Color.colorToHSV(attributes.getColor(1, 0), foregroundHsv);
            Color.colorToHSV(attributes.getColor(2, 0), backgroundHsv);
            if (foregroundHsv[2] <= backgroundHsv[2]) {
                isDarkBg = false;
            }
            theme = isDarkBg ? R$style.SuwNavBarThemeDark : R$style.SuwNavBarThemeLight;
        }
        attributes.recycle();
        return theme;
    }

    private static Context getThemedContext(Context context) {
        return new ContextThemeWrapper(context, getNavbarTheme(context));
    }

    public NavigationBar(Context context) {
        super(getThemedContext(context));
        init();
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(getThemedContext(context), attrs);
        init();
    }

    @TargetApi(11)
    public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getThemedContext(context), attrs, defStyleAttr);
        init();
    }

    private void init() {
        View.inflate(getContext(), R$layout.suw_navbar_view, this);
        this.mNextButton = (Button) findViewById(R$id.suw_navbar_next);
        this.mBackButton = (Button) findViewById(R$id.suw_navbar_back);
        this.mMoreButton = (Button) findViewById(R$id.suw_navbar_more);
    }

    public Button getBackButton() {
        return this.mBackButton;
    }

    public Button getNextButton() {
        return this.mNextButton;
    }

    public void setNavigationBarListener(NavigationBarListener listener) {
        this.mListener = listener;
        if (this.mListener != null) {
            getBackButton().setOnClickListener(this);
            getNextButton().setOnClickListener(this);
        }
    }

    public void onClick(View view) {
        if (this.mListener == null) {
            return;
        }
        if (view == getBackButton()) {
            this.mListener.onNavigateBack();
        } else if (view == getNextButton()) {
            this.mListener.onNavigateNext();
        }
    }
}

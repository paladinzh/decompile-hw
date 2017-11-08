package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Space;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import java.util.Objects;

public class NavigationBarInflaterView extends FrameLayout implements Tunable {
    private SparseArray<ButtonDispatcher> mButtonDispatchers;
    private String mCurrentLayout;
    private int mDensity;
    protected LayoutInflater mLandscapeInflater;
    private View mLastRot0;
    private View mLastRot90;
    protected LayoutInflater mLayoutInflater;
    protected FrameLayout mRot0;
    protected FrameLayout mRot90;

    public NavigationBarInflaterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDensity = context.getResources().getConfiguration().densityDpi;
        createInflaters();
    }

    private void createInflaters() {
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        Configuration landscape = new Configuration();
        landscape.setTo(this.mContext.getResources().getConfiguration());
        landscape.orientation = 2;
        this.mLandscapeInflater = LayoutInflater.from(this.mContext.createConfigurationContext(landscape));
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mDensity != newConfig.densityDpi) {
            this.mDensity = newConfig.densityDpi;
            createInflaters();
            inflateChildren();
            clearViews();
            inflateLayout(this.mCurrentLayout);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        inflateChildren();
        clearViews();
        inflateLayout(getDefaultLayout());
    }

    private void inflateChildren() {
        removeAllViews();
        this.mRot0 = (FrameLayout) this.mLayoutInflater.inflate(R.layout.navigation_layout, this, false);
        this.mRot0.setId(R.id.rot0);
        addView(this.mRot0);
        this.mRot90 = (FrameLayout) this.mLayoutInflater.inflate(R.layout.navigation_layout_rot90, this, false);
        this.mRot90.setId(R.id.rot90);
        addView(this.mRot90);
        if (getParent() instanceof NavigationBarView) {
            ((NavigationBarView) getParent()).updateRotatedViews();
        }
    }

    protected String getDefaultLayout() {
        return this.mContext.getString(R.string.config_navBarLayout);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(getContext()).addTunable((Tunable) this, "sysui_nav_bar");
    }

    protected void onDetachedFromWindow() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetachedFromWindow();
    }

    public void onTuningChanged(String key, String newValue) {
        if ("sysui_nav_bar".equals(key) && !Objects.equals(this.mCurrentLayout, newValue)) {
            clearViews();
            inflateLayout(newValue);
        }
    }

    protected void inflateLayout(String newLayout) {
        this.mCurrentLayout = newLayout;
        if (newLayout == null) {
            newLayout = getDefaultLayout();
        }
        String[] sets = newLayout.split(";", 3);
        String[] start = sets[0].split(",");
        String[] center = sets[1].split(",");
        String[] end = sets[2].split(",");
        inflateButtons(start, (ViewGroup) this.mRot0.findViewById(R.id.ends_group), false);
        inflateButtons(start, (ViewGroup) this.mRot90.findViewById(R.id.ends_group), true);
        inflateButtons(center, (ViewGroup) this.mRot0.findViewById(R.id.center_group), false);
        inflateButtons(center, (ViewGroup) this.mRot90.findViewById(R.id.center_group), true);
        addGravitySpacer((LinearLayout) this.mRot0.findViewById(R.id.ends_group));
        addGravitySpacer((LinearLayout) this.mRot90.findViewById(R.id.ends_group));
        inflateButtons(end, (ViewGroup) this.mRot0.findViewById(R.id.ends_group), false);
        inflateButtons(end, (ViewGroup) this.mRot90.findViewById(R.id.ends_group), true);
    }

    private void addGravitySpacer(LinearLayout layout) {
        layout.addView(new Space(this.mContext), new LayoutParams(0, 0, 1.0f));
    }

    private void inflateButtons(String[] buttons, ViewGroup parent, boolean landscape) {
        for (int i = 0; i < buttons.length; i++) {
            inflateButton(buttons[i], parent, landscape, i);
        }
    }

    protected View inflateButton(String buttonSpec, ViewGroup parent, boolean landscape, int indexInParent) {
        View v;
        LayoutInflater inflater = landscape ? this.mLandscapeInflater : this.mLayoutInflater;
        float size = extractSize(buttonSpec);
        String button = extractButton(buttonSpec);
        if ("home".equals(button)) {
            v = inflater.inflate(R.layout.home, parent, false);
            if (landscape && isSw600Dp()) {
                setupLandButton(v);
            }
        } else if ("back".equals(button)) {
            v = inflater.inflate(R.layout.back, parent, false);
            if (landscape && isSw600Dp()) {
                setupLandButton(v);
            }
        } else if ("recent".equals(button)) {
            v = inflater.inflate(R.layout.recent_apps, parent, false);
            if (landscape && isSw600Dp()) {
                setupLandButton(v);
            }
        } else if ("menu_ime".equals(button)) {
            v = inflater.inflate(R.layout.menu_ime, parent, false);
        } else if ("space".equals(button)) {
            v = inflater.inflate(R.layout.nav_key_space, parent, false);
        } else if ("clipboard".equals(button)) {
            v = inflater.inflate(R.layout.clipboard, parent, false);
        } else if (!button.startsWith("key")) {
            return null;
        } else {
            String uri = extractImage(button);
            int code = extractKeycode(button);
            v = inflater.inflate(R.layout.custom_key, parent, false);
            ((KeyButtonView) v).setCode(code);
            if (uri != null) {
                ((KeyButtonView) v).loadAsync(uri);
            }
        }
        if (size != 0.0f) {
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.width = (int) (((float) params.width) * size);
        }
        parent.addView(v);
        addToDispatchers(v);
        View lastView = landscape ? this.mLastRot90 : this.mLastRot0;
        if (lastView != null) {
            v.setAccessibilityTraversalAfter(lastView.getId());
        }
        if (landscape) {
            this.mLastRot90 = v;
        } else {
            this.mLastRot0 = v;
        }
        return v;
    }

    public static String extractImage(String buttonSpec) {
        if (buttonSpec.contains(":")) {
            return buttonSpec.substring(buttonSpec.indexOf(":") + 1, buttonSpec.indexOf(")"));
        }
        return null;
    }

    public static int extractKeycode(String buttonSpec) {
        if (buttonSpec.contains("(")) {
            return Integer.parseInt(buttonSpec.substring(buttonSpec.indexOf("(") + 1, buttonSpec.indexOf(":")));
        }
        return 1;
    }

    public static float extractSize(String buttonSpec) {
        if (buttonSpec.contains("[")) {
            return Float.parseFloat(buttonSpec.substring(buttonSpec.indexOf("[") + 1, buttonSpec.indexOf("]")));
        }
        return 1.0f;
    }

    public static String extractButton(String buttonSpec) {
        if (buttonSpec.contains("[")) {
            return buttonSpec.substring(0, buttonSpec.indexOf("["));
        }
        return buttonSpec;
    }

    private void addToDispatchers(View v) {
        if (this.mButtonDispatchers != null) {
            int indexOfKey = this.mButtonDispatchers.indexOfKey(v.getId());
            if (indexOfKey >= 0) {
                ((ButtonDispatcher) this.mButtonDispatchers.valueAt(indexOfKey)).addView(v);
            } else if (v instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) v;
                int N = viewGroup.getChildCount();
                for (int i = 0; i < N; i++) {
                    addToDispatchers(viewGroup.getChildAt(i));
                }
            }
        }
    }

    private boolean isSw600Dp() {
        return this.mContext.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    private void setupLandButton(View v) {
        Resources res = this.mContext.getResources();
        v.getLayoutParams().width = res.getDimensionPixelOffset(R.dimen.navigation_key_width_sw600dp_land);
        int padding = res.getDimensionPixelOffset(R.dimen.navigation_key_padding_sw600dp_land);
        v.setPadding(padding, v.getPaddingTop(), padding, v.getPaddingBottom());
    }

    private void clearViews() {
        if (this.mButtonDispatchers != null) {
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                ((ButtonDispatcher) this.mButtonDispatchers.valueAt(i)).clear();
            }
        }
        clearAllChildren((ViewGroup) this.mRot0.findViewById(R.id.nav_buttons));
        clearAllChildren((ViewGroup) this.mRot90.findViewById(R.id.nav_buttons));
    }

    private void clearAllChildren(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            ((ViewGroup) group.getChildAt(i)).removeAllViews();
        }
    }
}

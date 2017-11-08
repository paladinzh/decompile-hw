package com.android.settings.accessibility;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.settings.SettingsActivity;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.view.NavigationBar;
import com.android.setupwizardlib.view.NavigationBar.NavigationBarListener;

public class AccessibilitySettingsForSetupWizardActivity extends SettingsActivity {
    private boolean mSendExtraWindowStateChanged;

    protected void onCreate(Bundle savedState) {
        setMainContentId(2131886187);
        super.onCreate(savedState);
        LayoutInflater.from(this).inflate(2130968603, (FrameLayout) findViewById(2131887151));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setIsDrawerPresent(false);
        SystemBarHelper.hideSystemBars(getWindow());
        final LinearLayout parentView = (LinearLayout) findViewById(2131887012);
        parentView.setFitsSystemWindows(false);
        parentView.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                parentView.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
                return insets;
            }
        });
        NavigationBar navigationBar = (NavigationBar) findViewById(2131886188);
        navigationBar.getNextButton().setVisibility(8);
        navigationBar.setNavigationBarListener(new NavigationBarListener() {
            public void onNavigateBack() {
                AccessibilitySettingsForSetupWizardActivity.this.onNavigateUp();
            }

            public void onNavigateNext() {
            }
        });
    }

    protected void onSaveInstanceState(Bundle savedState) {
        savedState.putCharSequence("activity_title", getTitle());
        super.onSaveInstanceState(savedState);
    }

    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        setTitle(savedState.getCharSequence("activity_title"));
    }

    public void onResume() {
        super.onResume();
        this.mSendExtraWindowStateChanged = false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onNavigateUp() {
        onBackPressed();
        getWindow().getDecorView().sendAccessibilityEvent(32);
        return true;
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        if (!TextUtils.isEmpty(titleText)) {
            setTitle(titleText);
        } else if (titleRes > 0) {
            setTitle(getString(titleRes));
        }
        args.putInt("help_uri_resource", 0);
        startPreferenceFragment(Fragment.instantiate(this, fragmentClass, args), true);
        this.mSendExtraWindowStateChanged = true;
    }

    public void onAttachFragment(Fragment fragment) {
        if (this.mSendExtraWindowStateChanged) {
            getWindow().getDecorView().sendAccessibilityEvent(32);
        }
    }
}

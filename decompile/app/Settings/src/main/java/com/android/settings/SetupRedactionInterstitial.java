package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.settings.notification.RedactionInterstitial;
import com.android.settings.notification.RedactionInterstitial.RedactionInterstitialFragment;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.android.setupwizardlib.view.NavigationBar.NavigationBarListener;

public class SetupRedactionInterstitial extends RedactionInterstitial {

    public static class SetupRedactionInterstitialFragment extends RedactionInterstitialFragment implements NavigationBarListener {
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(2130969123, container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            NavigationBar navigationBar = ((SetupWizardLayout) view.findViewById(2131886616)).getNavigationBar();
            navigationBar.setNavigationBarListener(this);
            navigationBar.getBackButton().setVisibility(8);
            SetupWizardUtils.setImmersiveMode(getActivity());
        }

        public void onNavigateBack() {
            Activity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        }

        public void onNavigateNext() {
            SetupRedactionInterstitial activity = (SetupRedactionInterstitial) getActivity();
            if (activity != null) {
                activity.setResult(-1, activity.getResultIntentData());
                finish();
            }
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", SetupRedactionInterstitialFragment.class.getName());
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        return SetupRedactionInterstitialFragment.class.getName().equals(fragmentName);
    }

    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        ((LinearLayout) findViewById(2131887012)).setFitsSystemWindows(false);
    }
}

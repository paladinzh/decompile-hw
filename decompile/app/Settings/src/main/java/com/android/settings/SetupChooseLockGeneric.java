package com.android.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.fingerprint.SetupSkipDialog;

public class SetupChooseLockGeneric extends ChooseLockGeneric {
    private OnApplyWindowInsetsListener mWindowListener = new OnApplyWindowInsetsListener() {
        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            v.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
            return insets;
        }
    };

    public static class SetupChooseLockGenericFragment extends ChooseLockGenericFragment {
        private OnClickListener mBackListener = new OnClickListener() {
            public void onClick(View v) {
                Activity activity = SetupChooseLockGenericFragment.this.getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
            }
        };
        private OnClickListener mSkipListener = new OnClickListener() {
            public void onClick(View v) {
                SetupSkipDialog.newInstance(SetupChooseLockGenericFragment.this.getActivity().getIntent().getBooleanExtra(":settings:frp_supported", false)).show(SetupChooseLockGenericFragment.this.getFragmentManager());
            }
        };

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            View logoView = view.findViewById(2131886352);
            if (logoView != null) {
                logoView.setVisibility(8);
            }
            TextView backButton = (TextView) view.findViewById(2131886328);
            if (backButton != null) {
                backButton.setVisibility(0);
                backButton.setOnClickListener(this.mBackListener);
                backButton.setText(2131626591);
            }
            TextView nextButton = (TextView) view.findViewById(2131886329);
            if (nextButton != null) {
                nextButton.setVisibility(0);
                nextButton.setOnClickListener(this.mSkipListener);
                nextButton.setText(2131624550);
            }
            return view;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode != 0) {
                if (data == null) {
                    data = new Intent();
                }
                data.putExtra(":settings:password_quality", new LockPatternUtils(getActivity()).getKeyguardStoredPasswordQuality(UserHandle.myUserId()));
                getPackageManager().setComponentEnabledSetting(new ComponentName("com.android.settings", "com.android.settings.SetupRedactionInterstitial"), 1, 1);
                super.onActivityResult(requestCode, resultCode, data);
            }
            if (requestCode == 100 && resultCode != -1) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
            }
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (this.mForFingerprint) {
                getActivity().setTitle(getString(2131624718));
            } else {
                getActivity().setTitle(getString(2131624720));
            }
        }

        protected void disableUnusablePreferences(int quality, boolean hideDisabled) {
            super.disableUnusablePreferencesImpl(Math.max(quality, 65536), true);
        }

        protected void addPreferences() {
            if (this.mForFingerprint) {
                super.addPreferences();
            } else {
                addPreferencesFromResource(2131230888);
            }
        }

        public boolean onPreferenceTreeClick(Preference preference) {
            if (!"unlock_set_do_later".equals(preference.getKey())) {
                return super.onPreferenceTreeClick(preference);
            }
            SetupSkipDialog.newInstance(getActivity().getIntent().getBooleanExtra(":settings:frp_supported", false)).show(getFragmentManager());
            return true;
        }

        protected int getHelpResource() {
            return 0;
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return SetupChooseLockGenericFragment.class.getName().equals(fragmentName);
    }

    Class<? extends PreferenceFragment> getFragmentClass() {
        return SetupChooseLockGenericFragment.class;
    }

    protected boolean shouldHideActionBarInStartupGuide() {
        return false;
    }

    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Utils.hideNavigationBar(getWindow(), 5890);
        LinearLayout layout = (LinearLayout) findViewById(2131887012);
        layout.setFitsSystemWindows(true);
        layout.setOnApplyWindowInsetsListener(this.mWindowListener);
    }

    protected void onResume() {
        super.onResume();
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
}

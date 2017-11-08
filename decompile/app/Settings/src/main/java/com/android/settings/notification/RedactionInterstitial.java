package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.settings.RestrictedCheckBox;
import com.android.settings.RestrictedRadioButton;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;

public class RedactionInterstitial extends SettingsActivity {

    public static class RedactionInterstitialFragment extends SettingsPreferenceFragment implements OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
        private RadioGroup mRadioGroup;
        private RestrictedRadioButton mRedactSensitiveButton;
        private RestrictedCheckBox mRemoteInputCheckbox;
        private RestrictedRadioButton mShowAllButton;
        private int mUserId;

        protected int getMetricsCategory() {
            return 74;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(2130969047, container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            this.mRadioGroup = (RadioGroup) view.findViewById(2131887069);
            this.mShowAllButton = (RestrictedRadioButton) view.findViewById(2131887071);
            this.mRedactSensitiveButton = (RestrictedRadioButton) view.findViewById(2131887070);
            this.mRemoteInputCheckbox = (RestrictedCheckBox) view.findViewById(2131886771);
            this.mRemoteInputCheckbox.setOnCheckedChangeListener(this);
            this.mRadioGroup.setOnCheckedChangeListener(this);
            this.mUserId = Utils.getUserIdFromBundle(getContext(), getActivity().getIntent().getExtras());
            if (Utils.isManagedProfile(UserManager.get(getContext()), this.mUserId)) {
                ((TextView) view.findViewById(2131886296)).setText(2131626736);
                this.mShowAllButton.setText(2131626733);
                this.mRedactSensitiveButton.setText(2131626734);
                ((RadioButton) view.findViewById(2131887072)).setText(2131626735);
            }
        }

        public void onResume() {
            super.onResume();
            checkNotificationFeaturesAndSetDisabled(this.mShowAllButton, 12);
            checkNotificationFeaturesAndSetDisabled(this.mRedactSensitiveButton, 4);
            this.mRemoteInputCheckbox.setDisabledByAdmin(RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(getActivity(), 64, this.mUserId));
            loadFromSettings();
        }

        private void checkNotificationFeaturesAndSetDisabled(RestrictedRadioButton button, int keyguardNotifications) {
            button.setDisabledByAdmin(RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(getActivity(), keyguardNotifications, this.mUserId));
        }

        private void loadFromSettings() {
            boolean z = false;
            boolean enabled = Secure.getIntForUser(getContentResolver(), "lock_screen_show_notifications", 0, this.mUserId) != 0;
            boolean show = Secure.getIntForUser(getContentResolver(), "lock_screen_allow_private_notifications", 1, this.mUserId) != 0;
            int checkedButtonId = 2131887072;
            if (enabled) {
                if (show && !this.mShowAllButton.isDisabledByAdmin()) {
                    checkedButtonId = 2131887071;
                } else if (!this.mRedactSensitiveButton.isDisabledByAdmin()) {
                    checkedButtonId = 2131887070;
                }
            }
            this.mRadioGroup.check(checkedButtonId);
            boolean allowRemoteInput = Secure.getIntForUser(getContentResolver(), "lock_screen_allow_remote_input", 0, this.mUserId) != 0;
            RestrictedCheckBox restrictedCheckBox = this.mRemoteInputCheckbox;
            if (!allowRemoteInput) {
                z = true;
            }
            restrictedCheckBox.setChecked(z);
            updateRemoteInputCheckboxVisibility();
        }

        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int i;
            int i2 = 1;
            boolean show = checkedId == 2131887071;
            boolean enabled = checkedId != 2131887072;
            ContentResolver contentResolver = getContentResolver();
            String str = "lock_screen_allow_private_notifications";
            if (show) {
                i = 1;
            } else {
                i = 0;
            }
            Secure.putIntForUser(contentResolver, str, i, this.mUserId);
            ContentResolver contentResolver2 = getContentResolver();
            String str2 = "lock_screen_show_notifications";
            if (!enabled) {
                i2 = 0;
            }
            Secure.putIntForUser(contentResolver2, str2, i2, this.mUserId);
            updateRemoteInputCheckboxVisibility();
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
            if (buttonView == this.mRemoteInputCheckbox) {
                Secure.putIntForUser(getContentResolver(), "lock_screen_allow_remote_input", checked ? 0 : 1, this.mUserId);
            }
        }

        private void updateRemoteInputCheckboxVisibility() {
            boolean visible = this.mRadioGroup.getCheckedRadioButtonId() == 2131887071;
            boolean isManagedProfile = Utils.isManagedProfile(UserManager.get(getPrefContext()), this.mUserId);
            RestrictedCheckBox restrictedCheckBox = this.mRemoteInputCheckbox;
            int i = (!visible || isManagedProfile) ? 4 : 0;
            restrictedCheckBox.setVisibility(i);
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", RedactionInterstitialFragment.class.getName());
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        return RedactionInterstitialFragment.class.getName().equals(fragmentName);
    }
}

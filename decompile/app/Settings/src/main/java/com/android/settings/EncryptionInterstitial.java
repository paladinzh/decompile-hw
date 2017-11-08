package com.android.settings;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import java.util.List;

public class EncryptionInterstitial extends SettingsActivity {
    private static final String TAG = EncryptionInterstitial.class.getSimpleName();

    public static class EncryptionInterstitialFragment extends SettingsPreferenceFragment implements OnClickListener {
        private Preference mDontRequirePasswordToDecrypt;
        private boolean mPasswordRequired;
        private int mRequestedPasswordQuality;
        private Preference mRequirePasswordToDecrypt;
        private Intent mUnlockMethodIntent;

        protected int getMetricsCategory() {
            return 48;
        }

        public void onCreate(Bundle savedInstanceState) {
            int msgId;
            int enableId;
            int disableId;
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(2131230861);
            findPreference("encrypt_dont_require_password").setViewId(2131886094);
            this.mRequirePasswordToDecrypt = findPreference("encrypt_require_password");
            this.mDontRequirePasswordToDecrypt = findPreference("encrypt_dont_require_password");
            boolean forFingerprint = getActivity().getIntent().getBooleanExtra("for_fingerprint", false);
            Intent intent = getActivity().getIntent();
            this.mRequestedPasswordQuality = intent.getIntExtra("extra_password_quality", 0);
            this.mUnlockMethodIntent = (Intent) intent.getParcelableExtra("extra_unlock_method_intent");
            switch (this.mRequestedPasswordQuality) {
                case 65536:
                    if (forFingerprint) {
                        msgId = 2131626874;
                    } else {
                        msgId = 2131626871;
                    }
                    enableId = 2131626877;
                    disableId = 2131626880;
                    break;
                case 131072:
                case 196608:
                    if (forFingerprint) {
                        msgId = 2131626873;
                    } else {
                        msgId = 2131626870;
                    }
                    enableId = 2131626876;
                    disableId = 2131626879;
                    break;
                default:
                    if (forFingerprint) {
                        msgId = 2131626875;
                    } else {
                        msgId = 2131626872;
                    }
                    enableId = 2131626878;
                    disableId = 2131626881;
                    break;
            }
            TextView message = createHeaderView();
            message.setText(msgId);
            setHeaderView(message);
            this.mRequirePasswordToDecrypt.setTitle(enableId);
            this.mDontRequirePasswordToDecrypt.setTitle(disableId);
            setRequirePasswordState(getActivity().getIntent().getBooleanExtra("extra_require_password", true));
        }

        protected TextView createHeaderView() {
            return (TextView) LayoutInflater.from(getActivity()).inflate(2130968775, null, false);
        }

        protected void startLockIntent() {
            if (this.mUnlockMethodIntent != null) {
                this.mUnlockMethodIntent.putExtra("extra_require_password", this.mPasswordRequired);
                startActivityForResult(this.mUnlockMethodIntent, 100);
                return;
            }
            Log.wtf(EncryptionInterstitial.TAG, "no unlock intent to start");
            finish();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100 && resultCode != 0) {
                getActivity().setResult(resultCode, data);
                finish();
            }
        }

        public boolean onPreferenceTreeClick(Preference preference) {
            if (!preference.getKey().equals("encrypt_require_password")) {
                setRequirePasswordState(false);
                startLockIntent();
            } else if (!AccessibilityManager.getInstance(getActivity()).isEnabled() || this.mPasswordRequired) {
                setRequirePasswordState(true);
                startLockIntent();
            } else {
                setRequirePasswordState(false);
                showDialog(1);
            }
            return true;
        }

        public Dialog onCreateDialog(int dialogId) {
            switch (dialogId) {
                case 1:
                    int titleId;
                    int messageId;
                    CharSequence exampleAccessibility;
                    switch (this.mRequestedPasswordQuality) {
                        case 65536:
                            titleId = 2131626883;
                            messageId = 2131626886;
                            break;
                        case 131072:
                        case 196608:
                            titleId = 2131626882;
                            messageId = 2131626885;
                            break;
                        default:
                            titleId = 2131626884;
                            messageId = 2131626887;
                            break;
                    }
                    List<AccessibilityServiceInfo> list = AccessibilityManager.getInstance(getActivity()).getEnabledAccessibilityServiceList(-1);
                    if (list.isEmpty()) {
                        exampleAccessibility = "";
                    } else {
                        exampleAccessibility = ((AccessibilityServiceInfo) list.get(0)).getResolveInfo().loadLabel(getPackageManager());
                    }
                    return new Builder(getActivity()).setTitle(titleId).setMessage(getString(messageId, new Object[]{exampleAccessibility})).setCancelable(true).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
                default:
                    throw new IllegalArgumentException();
            }
        }

        private void setRequirePasswordState(boolean required) {
            this.mPasswordRequired = required;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                setRequirePasswordState(true);
                startLockIntent();
            } else if (which == -2) {
                setRequirePasswordState(false);
            }
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", EncryptionInterstitialFragment.class.getName());
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        return EncryptionInterstitialFragment.class.getName().equals(fragmentName);
    }

    public static Intent createStartIntent(Context ctx, int quality, boolean requirePasswordDefault, Intent unlockMethodIntent) {
        return new Intent(ctx, EncryptionInterstitial.class).putExtra("extra_password_quality", quality).putExtra(":settings:show_fragment_title_resid", 2131626868).putExtra("extra_require_password", requirePasswordDefault).putExtra("extra_unlock_method_intent", unlockMethodIntent);
    }
}

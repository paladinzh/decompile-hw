package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.fingerprint.utils.BiometricManager;

public class ChooseLockGenericFragmentHwBase extends SettingsPreferenceFragment {
    private static final boolean bFpTimeoutPassword = SystemProperties.getBoolean("ro.config.fp_timeout_password", false);
    private OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            new DiscardDialog(ChooseLockGenericFragmentHwBase.this.getActivity()).show();
        }
    };
    protected ChooseLockSettingsHelper mChooseLockSettingsHelper;
    protected boolean mFinishPending = false;
    protected boolean mIsFromFp;
    protected boolean mIsLastLockTypePinOrPasswd = false;

    private class DiscardDialog extends AlertDialog implements DialogInterface.OnClickListener {
        protected DiscardDialog(Context context) {
            super(context);
        }

        protected void onCreate(Bundle savedState) {
            setTitle(2131627637);
            setMessage(getContext().getString(2131627871));
            createButtons();
            super.onCreate(savedState);
        }

        protected void createButtons() {
            Context context = getContext();
            setButton(-2, context.getString(2131627632), this);
            String negativeStr = context.getResources().getString(2131625398);
            SpannableString negativeSpanText = new SpannableString(negativeStr);
            negativeSpanText.setSpan(new ForegroundColorSpan(-65536), 0, negativeStr.length(), 18);
            setButton(-1, negativeSpanText, this);
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                ChooseLockGenericFragmentHwBase.this.onPositiveClicked();
            }
            dismiss();
        }

        public void onStart() {
            super.onStart();
            Window window = getWindow();
            window.setFlags(1024, 1024);
            window.getDecorView().setSystemUiVisibility(5890);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        if (BiometricManager.isFingerprintSupported(getActivity())) {
            this.mIsLastLockTypePinOrPasswd = isLockPinOrPassword();
            if (getActivity() != null && getActivity().getIntent() != null) {
                this.mIsFromFp = getActivity().getIntent().getBooleanExtra("is_fp_screen_lock", false);
            }
        }
    }

    protected int getMetricsCategory() {
        return 27;
    }

    protected void showSwitchLockDlg(final String key, CharSequence title) {
        String dlgContent = getResources().getString(2131627630, new Object[]{title});
        Builder dialogBuilder = new Builder(getActivity());
        dialogBuilder.setTitle(2131627675);
        dialogBuilder.setMessage(dlgContent);
        dialogBuilder.setPositiveButton(2131627632, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ChooseLockGenericFragmentHwBase.this.respondPreferenceClick(key);
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton(2131624572, null);
        dialogBuilder.create().show();
    }

    protected boolean setUnlockMethod(String unlockMethod) {
        return false;
    }

    protected boolean showQuickSwitchUnavailableDialog(final String key) {
        if (key == null) {
            return false;
        }
        LockPatternUtils lockPatternUtils = this.mChooseLockSettingsHelper.utils();
        if (lockPatternUtils == null) {
            return false;
        }
        int msgResourceId = 0;
        if ("unlock_set_pattern".equals(key)) {
            msgResourceId = 2131628770;
        } else if ("unlock_set_pin".equals(key)) {
            msgResourceId = 2131628771;
        } else if ("unlock_set_password".equals(key)) {
            msgResourceId = 2131628772;
        }
        if (key.equals(getKeyForQuality(lockPatternUtils.getKeyguardStoredPasswordQuality(0)))) {
            setUnlockMethod(key);
            return false;
        }
        new Builder(getActivity()).setTitle(2131628769).setMessage(msgResourceId).setNegativeButton(2131624572, null).setPositiveButton(2131627632, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ChooseLockGenericFragmentHwBase.this.setUnlockMethod(key);
                dialog.dismiss();
            }
        }).create().show();
        return true;
    }

    private String getKeyForQuality(int quality) {
        switch (quality) {
            case 65536:
                return "unlock_set_pattern";
            case 131072:
            case 196608:
                return "unlock_set_pin";
            case 262144:
            case 327680:
            case 393216:
                return "unlock_set_password";
            default:
                return "";
        }
    }

    protected boolean respondPreferenceClick(String key) {
        MLog.w("ChooseLock", "respondPreferenceClick must be override by subclass");
        return false;
    }

    protected boolean isLockPinOrPassword() {
        LockPatternUtils lockPatternUtils = this.mChooseLockSettingsHelper.utils();
        if (lockPatternUtils == null) {
            return false;
        }
        boolean isSupported;
        if (lockPatternUtils.getKeyguardStoredPasswordQuality(UserHandle.myUserId()) >= 131072) {
            isSupported = true;
        } else {
            isSupported = false;
        }
        return isSupported;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateFingerprintEnableStatus();
    }

    protected void updateFingerprintEnableStatus() {
        this.mIsLastLockTypePinOrPasswd = isLockPinOrPassword();
        if (this.mIsLastLockTypePinOrPasswd) {
            if (!BiometricManager.isFingerprintEnabled(getActivity())) {
                BiometricManager.setFingerprintEnabled(true);
            }
        } else if (BiometricManager.isFingerprintEnabled(getActivity())) {
            BiometricManager.setFingerprintEnabled(false);
        }
    }

    protected boolean isSetupChoose() {
        return false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int i = 0;
        View view;
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver()) && !isSetupChoose()) {
            view = Utils.getCustemPreferenceContainer(inflater, 2130968670, container, super.onCreateView(inflater, container, savedInstanceState));
            initPasswordTimeoutView(view);
            setLogoViewHeight(view);
            TextView backButton = (TextView) view.findViewById(2131886328);
            if (backButton != null) {
                backButton.setVisibility(0);
                backButton.setOnClickListener(this.mBackListener);
                backButton.setText(2131626591);
            }
            TextView nextButton = (TextView) view.findViewById(2131886329);
            if (nextButton != null) {
                nextButton.setVisibility(4);
            }
            return view;
        } else if (!this.mIsFromFp) {
            return super.onCreateView(inflater, container, savedInstanceState);
        } else {
            view = Utils.getCustemPreferenceContainer(inflater, 2130968821, container, super.onCreateView(inflater, container, savedInstanceState));
            View timeoutPasswordView = view.findViewById(2131886706);
            if (timeoutPasswordView != null) {
                if (!bFpTimeoutPassword) {
                    i = 8;
                }
                timeoutPasswordView.setVisibility(i);
            }
            return view;
        }
    }

    private void initPasswordTimeoutView(View view) {
        View timeoutPasswordView = view.findViewById(2131886706);
        if (timeoutPasswordView != null) {
            timeoutPasswordView.setVisibility(bFpTimeoutPassword ? 0 : 8);
        }
    }

    private void setLogoViewHeight(View root) {
        View logoView = root.findViewById(2131886352);
        View logoContainer = root.findViewById(2131886353);
        if (logoView != null && logoContainer != null) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (!Utils.isTablet()) {
                ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (((displayMetrics.widthPixels * 4) / 5) * 16) / 100;
            } else if (2 == getResources().getConfiguration().orientation) {
                View llPrefs = root.findViewById(2131886357);
                View fpTpContainer = root.findViewById(2131886360);
                int logoViewHeight = (displayMetrics.widthPixels * 268) / 1000;
                int llPrefsWidth = (displayMetrics.widthPixels * 5) / 6;
                llPrefs.getLayoutParams().width = llPrefsWidth;
                fpTpContainer.getLayoutParams().width = llPrefsWidth;
                ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (logoViewHeight * 13) / 100;
            } else {
                ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (((displayMetrics.widthPixels * 62) / 100) * 38) / 100;
            }
        }
    }

    private void onPositiveClicked() {
        Log.e("Fingerprint", "ChooseLockGenericHwBase settings the result as CANCELED");
        getActivity().setResult(0);
        finish();
    }
}

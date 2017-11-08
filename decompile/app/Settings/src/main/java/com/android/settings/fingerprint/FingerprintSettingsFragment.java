package com.android.settings.fingerprint;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ConfirmLockPassword.InternalActivity;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.PrivacyModeManager;
import com.android.settings.PrivacySpaceSettingsHelper;
import com.android.settings.SettingsExtUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.fingerprint.FingerprintSettings.LearnMoreSpan;
import com.android.settings.fingerprint.ShortcutPaymentPreference.OnEnrollClickListener;
import com.android.settings.fingerprint.enrollment.FingerprintCalibrationIntroActivity;
import com.android.settings.fingerprint.enrollment.FingerprintEnrollActivity;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.BiometricManager.CaptureCallback;
import com.android.settings.fingerprint.utils.BiometricManager.IdentifyCallback;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.android.settings.navigation.NaviUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FingerprintSettingsFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener, IdentifyCallback, CaptureCallback, Indexable {
    private static boolean QUICK_ALIPAY_ON = SystemProperties.getBoolean("ro.config.quick_alipay_on", false);
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (!BiometricManager.isFingerprintSupported(context) || !NaviUtils.isFrontFingerNaviEnabled()) {
                return null;
            }
            List<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230790;
            result.add(sir);
            return result;
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (!BiometricManager.isFingerprintSupported(context)) {
                return null;
            }
            List<SearchIndexableRaw> result = new ArrayList();
            if (NaviUtils.isFrontFingerNaviEnabled()) {
                SearchIndexableRaw mainEntry = new SearchIndexableRaw(context);
                mainEntry.title = context.getString(2131627616);
                result.add(mainEntry);
            }
            if (BiometricManager.open(context).getEnrolledFpNum() > 0) {
                SearchIndexableRaw identifyEntry = new SearchIndexableRaw(context);
                identifyEntry.title = context.getString(2131628682);
                result.add(identifyEntry);
            }
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            boolean isHwidInstalled;
            List<String> keys = new LinkedList();
            if (NaviUtils.isFrontFingerNaviEnabled()) {
                keys.add("fingerprint_settings_root");
            }
            if (BiometricManager.open(context).getEnrolledFpNum() == 0) {
                keys.add("finger_identification");
            }
            keys.add("fp_list_cat");
            keys.add("fp_uses_cat");
            try {
                context.getPackageManager().getPackageInfo("com.huawei.hwid", 1);
                isHwidInstalled = true;
            } catch (NameNotFoundException e) {
                isHwidInstalled = false;
            } catch (Exception e2) {
                isHwidInstalled = false;
            }
            if (!(isHwidInstalled && FingerprintUtils.isHwidSupported(context))) {
                keys.add("hw_account_pref");
            }
            if (!FingerprintUtils.isQuickHwpayOn() || PrivacySpaceSettingsHelper.isPrivacyUser(context, UserHandle.myUserId())) {
                keys.add("shortcut_payment");
            }
            return keys;
        }
    };
    private boolean hasHiddenUser;
    private String hiddenUserName;
    private CustomSwitchPreference mAppLockPref;
    private BiometricManager mBiometricManager;
    private long mChallenge = 0;
    private ContentResolver mContentResolver;
    private Context mContext;
    private FingerSettingsDialogListener mDialogPrefListener = new FingerSettingsDialogListener();
    private EnrollClickListener mEnrollClickListener;
    private int mEnrollRequestCode;
    private boolean mEnrollStarted = false;
    private FingerIdentifyDialogPreference mFingerIdentifyPref;
    private final Runnable mFingerprintLockoutReset = new Runnable() {
        public void run() {
            Log.i("FingerprintSettingsFragment", "mFingerprintLockoutReset try to restore! mInFingerprintLockout = " + FingerprintSettingsFragment.this.mInFingerprintLockout + ", mEnrollStarted = " + FingerprintSettingsFragment.this.mEnrollStarted);
            FingerprintSettingsFragment.this.mInFingerprintLockout = false;
            if (FingerprintSettingsFragment.this.mEnrollStarted) {
                Log.i("FingerprintSettingsFragment", "enrollment in process, do not restart identification after lockout.");
                return;
            }
            Log.i("FingerprintSettingsFragment", "lockout finish and try to restart identify.");
            FingerprintSettingsFragment.this.setIdentifyErrCount(0);
            if (shouldRetryIdentify()) {
                Log.i("FingerprintSettingsFragment", "restart identify.");
                FingerprintSettingsFragment.this.retryIdentify();
                if (FingerprintSettingsFragment.this.mHwCustFingerprintSettingsFragment != null && FingerprintSettingsFragment.this.mHwCustFingerprintSettingsFragment.isFrontFingerPrint()) {
                    FingerprintSettingsFragment.this.mHwCustFingerprintSettingsFragment.frontFpLockoutReset();
                }
                return;
            }
            Log.i("FingerprintSettingsFragment", "identify dialog not showing, do not restart identification");
        }

        private boolean shouldRetryIdentify() {
            return !(FingerprintSettingsFragment.this.mFingerIdentifyPref != null ? FingerprintSettingsFragment.this.mFingerIdentifyPref.isDialogShowing() : false) ? FingerprintSettingsFragment.this.mShortcutPaymentPref != null ? FingerprintSettingsFragment.this.mShortcutPaymentPref.isDialogShowing() : false : true;
        }
    };
    private List<Fingerprint> mFingerprints;
    private boolean mHasClick = false;
    private boolean mHasConfirmed = false;
    private HighlightHandler mHighlightHandler;
    private CustomSwitchPreference mHwAccountPref;
    private HwCustFingerprintSettingsFragment mHwCustFingerprintSettingsFragment = null;
    private boolean mHwpayInstalled = false;
    private int mIdentifyErrCount = 0;
    private boolean mIdentifyStarted = false;
    private volatile boolean mInFingerprintLockout = false;
    private boolean mIsHiddenUser;
    private boolean mIsHighlighting = false;
    private CustomSwitchPreference mKeyGuardPref;
    private RecyclerView mList;
    private LockPatternUtils mLockPatternUtils;
    private Toast mMaxFpCountTip;
    private PackageManager mPackageManager;
    private AlertDialog mPaymentCautionDialog = null;
    private PreferenceGroup mPgUsesCat;
    private boolean mPinPasswordSetted = false;
    private PrivacyModeManager mPmm;
    private boolean mPreEnrolled = false;
    private Resources mRes;
    private Fingerprint mShortcutPayFinger = null;
    private int mShortcutPayStatus = 0;
    private ShortcutPaymentPreference mShortcutPaymentPref;
    private boolean mShowEnrollNowDialog = false;
    private int mStartMode;
    private CustomSwitchPreference mStrongBoxPref;
    private byte[] mToken;
    private int mUserId = UserHandle.myUserId();

    public class EnrollClickListener implements OnEnrollClickListener {
        private int mFingerNum;

        public void onClick(DialogInterface dialog, int which) {
            if (this.mFingerNum == 1) {
                FingerprintSettingsFragment.this.startEnrollFragment(405);
            }
            ItemUseStat.getInstance().handleClick(FingerprintSettingsFragment.this.getActivity(), 2, "shorcut_payment_start_enroll_clicked");
        }

        public void setFingerNum(int fingerNum) {
            this.mFingerNum = fingerNum;
        }
    }

    private class FingerSettingsDialogListener implements FingerprintDialogListener {
        private FingerSettingsDialogListener() {
        }

        public void onDialogClicked(DialogInterface dialog, int which, DialogPreference preference) {
            Log.d("FingerprintSettingsFragment", "identify onDialogClicked!");
            onDialogStop(dialog, preference);
        }

        public void onDialogCreated(DialogPreference preference) {
            Log.d("FingerprintSettingsFragment", "identify onDialogCreated!");
            if (preference != null) {
                if (preference == FingerprintSettingsFragment.this.mFingerIdentifyPref || preference == FingerprintSettingsFragment.this.mShortcutPaymentPref) {
                    FingerprintSettingsFragment.this.mIsHighlighting = false;
                    FingerprintSettingsFragment.this.retryIdentify();
                }
            }
        }

        private void onDialogStop(DialogInterface dialog, DialogPreference preference) {
            if (preference != null) {
                if (preference == FingerprintSettingsFragment.this.mFingerIdentifyPref || preference == FingerprintSettingsFragment.this.mShortcutPaymentPref) {
                    if (preference instanceof FingerprintDialogPreference) {
                        ((FingerprintDialogPreference) preference).clear();
                    }
                    cancelDialogIdentify();
                    FingerprintSettingsFragment.this.mIsHighlighting = false;
                }
            }
        }

        private void cancelDialogIdentify() {
            Log.i("FingerprintSettingsFragment", "identify cancelDialogIdentify ! mInFingerprintLockout = " + FingerprintSettingsFragment.this.mInFingerprintLockout + ", mIdentifyStarted = " + FingerprintSettingsFragment.this.mIdentifyStarted);
            if (!FingerprintSettingsFragment.this.mInFingerprintLockout && FingerprintSettingsFragment.this.mIdentifyStarted) {
                FingerprintSettingsFragment.this.acquireBiometricManager().cancelIdentify();
                FingerprintSettingsFragment.this.setIdentifyStarted(false);
                FingerprintSettingsFragment.this.setIdentifyErrCount(0);
            }
        }

        public void onItemHighLightOff(DialogPreference preference) {
            FingerprintSettingsFragment.this.mIsHighlighting = false;
            Log.i("FingerprintSettingsFragment", "onItemHighLightOff highlight off! mIsHighlighting = " + FingerprintSettingsFragment.this.mIsHighlighting);
        }
    }

    public class HighlightHandler extends Handler {
        public void handleMessage(Message msg) {
            Log.i("FingerprintSettingsFragment", "message arrived = " + msg.what);
            if (msg.what == 1000) {
                Log.i("FingerprintSettingsFragment", "MSG_HIGHLIGHT_OFF message arrived, fingerprint id = " + msg.arg1);
                FingerprintSettingsFragment.this.highlightPref(FingerprintUtils.generateFpPrefKey(msg.arg1), false);
                if (FingerprintSettingsFragment.this.mBiometricManager != null) {
                    FingerprintSettingsFragment.this.mFingerprints = FingerprintSettingsFragment.this.mBiometricManager.getFingerprintList(FingerprintSettingsFragment.this.mUserId);
                    FingerprintSettingsFragment.this.retryIdentify();
                }
            } else if (msg.what == 1001) {
                FingerprintSettingsFragment.this.mHasClick = false;
                return;
            }
            super.handleMessage(msg);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHwCustFingerprintSettingsFragment = (HwCustFingerprintSettingsFragment) HwCustUtils.createObj(HwCustFingerprintSettingsFragment.class, new Object[]{this});
        this.mContext = getActivity();
        this.mPackageManager = this.mContext.getPackageManager();
        this.mContentResolver = getContentResolver();
        this.mRes = getResources();
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPmm = new PrivacyModeManager(this.mContext);
        this.mHighlightHandler = new HighlightHandler();
        this.mEnrollClickListener = new EnrollClickListener();
        this.mIsHiddenUser = PrivacySpaceSettingsHelper.isPrivacyUser(this.mContext, UserHandle.myUserId());
        if (Utils.onlySupportPortrait()) {
            ((Activity) this.mContext).setRequestedOrientation(1);
        }
        if (savedInstanceState != null) {
            this.mHasConfirmed = savedInstanceState.getBoolean("has_confirmed", false);
            this.mPinPasswordSetted = savedInstanceState.getBoolean("in_confirmed", false);
            this.mToken = savedInstanceState.getByteArray("hw_auth_token");
        }
        this.mEnrollRequestCode = 400;
        if (getActivity() == null || getActivity().getIntent() == null) {
            this.mStartMode = 0;
        } else {
            this.mStartMode = getActivity().getIntent().getIntExtra("fp_settings_start_mode_key", 0);
            this.mUserId = getActivity().getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
        }
        if (this.mStartMode == 1) {
            this.mToken = ((Activity) this.mContext).getIntent().getByteArrayExtra("hw_auth_token");
            this.mBiometricManager = BiometricManager.open(this.mContext);
            if (this.mToken == null && this.mBiometricManager != null && this.mBiometricManager.getFingerprintList(this.mUserId).size() <= 0) {
                Log.e("FingerprintSettingsFragment", "Token not found, cannot start fingerprint enrollment directly.");
                finish();
            }
            this.mPinPasswordSetted = true;
        }
        if (!this.mHasConfirmed && !this.mPinPasswordSetted) {
            Log.i("FingerprintSettingsFragment", "try to unlock");
            tryToUnlock();
        }
    }

    private void tryToUnlock() {
        if (isPinOrPasswordLock()) {
            unlockPassword();
        } else {
            toScreenLockFragment();
        }
    }

    private void unlockPassword() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", InternalActivity.class.getName());
        intent.putExtra("isGuest", new PrivacyModeManager(getActivity()).isGuestModeOn());
        intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        intent.putExtra("is_from_fingerprint", true);
        this.mChallenge = acquireBiometricManager().preEnrollSafe();
        this.mPreEnrolled = true;
        intent.putExtra("has_challenge", true);
        intent.putExtra("challenge", this.mChallenge);
        intent.putExtra("return_credentials", true);
        startActivityForResult(intent, 100);
    }

    private boolean isPinOrPasswordLock() {
        boolean z = false;
        if (this.mLockPatternUtils == null) {
            return false;
        }
        if (this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId) >= 131072) {
            z = true;
        }
        return z;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mPinPasswordSetted = false;
        Log.i("FingerprintSettingsFragment", "requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (resultCode == 101) {
            finish();
        } else if (requestCode == 100 && resultCode == -1) {
            this.mHasConfirmed = true;
            if (isPinOrPasswordLock()) {
                Log.i("FingerprintSettingsFragment", "CONFIRM_EXISTING_REQUEST get Token");
                this.mToken = data.getByteArrayExtra("hw_auth_token");
                if (this.mToken == null) {
                    Log.e("FingerprintSettingsFragment", "CONFIRM_EXISTING_REQUEST token is null!");
                }
            } else {
                this.mHasConfirmed = false;
                this.mPinPasswordSetted = false;
                toScreenLockFragment();
            }
        } else if (requestCode == 100 && resultCode == 0) {
            getActivity().setResult(0);
            finish();
        } else if (requestCode == 101 && isPinOrPasswordLock() && resultCode != 0) {
            this.mPinPasswordSetted = true;
            Log.i("FingerprintSettingsFragment", "CONFIRM_EXISTING_REQUEST get Token");
            this.mToken = data.getByteArrayExtra("hw_auth_token");
            if (this.mToken == null) {
                Log.e("FingerprintSettingsFragment", "FALLBACK_REQUEST token is null!");
            }
        } else if (requestCode == 101) {
            this.mPinPasswordSetted = false;
            getActivity().setResult(0);
            finish();
        } else if (requestCode == 301 || requestCode == 302 || requestCode == 303) {
            if (resultCode != 0) {
                boolean isSuccess = false;
                if (data != null) {
                    isSuccess = data.getBooleanExtra("isSuccess", false);
                    Log.w("FingerprintSettingsFragment", "app or account bind success : " + isSuccess);
                }
                if (!isSuccess) {
                    Toast.makeText(this.mContext, this.mRes.getString(2131627657), 0).show();
                }
            }
        } else if (requestCode == 402 || requestCode == 403 || requestCode == 401 || requestCode == 404) {
            this.mEnrollRequestCode = requestCode;
            this.mEnrollStarted = false;
            if (resultCode == 100) {
                Log.i("FingerprintSettingsFragment", "Fingerprint Enrollment timeout.");
                finish();
            }
        } else if (requestCode == 200) {
            this.mEnrollStarted = false;
            if (resultCode == 100) {
                Log.i("FingerprintSettingsFragment", "Fingerprint Enrollment timeout.");
                finish();
            }
        } else if (requestCode == 405 || requestCode == 406) {
            this.mEnrollRequestCode = requestCode;
            this.mEnrollStarted = false;
            if (resultCode == 100) {
                Log.i("FingerprintSettingsFragment", "Fingerprint Enrollment timeout.");
                finish();
            }
            if (resultCode == -1) {
                int fpId = data.getIntExtra("fp_id", -1);
                Fingerprint fp = acquireBiometricManager().getFingerprint(fpId);
                if (fp == null) {
                    Log.e("FingerprintSettingsFragment", "Fingerprint enrolled not found, id = " + fpId);
                    return;
                }
                updateShortcutPayFingerprint(true, fp);
            }
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(2131230790);
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            getActivity().setTitle(2131627616);
        } else {
            getActivity().setTitle(2131627856);
        }
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        this.mList = getListView();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("has_confirmed", this.mHasConfirmed);
        outState.putBoolean("in_confirmed", this.mPinPasswordSetted);
        if (this.mToken != null) {
            outState.putByteArray("hw_auth_token", this.mToken);
        }
        super.onSaveInstanceState(outState);
    }

    public void onResume() {
        super.onResume();
        if (isPinOrPasswordLock() && !BiometricManager.isFingerprintEnabled(getActivity()) && !BiometricManager.setFingerprintEnabled(true)) {
            Log.w("FingerprintSettingsFragment", "set fingerprint enabled failed");
            ((Activity) this.mContext).finish();
        } else if (BiometricManager.isFingerprintEnabled(getActivity())) {
            this.mBiometricManager = BiometricManager.open(getActivity());
            if (this.mBiometricManager != null) {
                Log.i("FingerprintSettingsFragment", "setVibratorSwitch ");
                this.mFingerprints = this.mBiometricManager.getFingerprintList(this.mUserId);
                this.mBiometricManager.setCaptureCallback(this);
                updateUsesPrefCat();
                refreshFingerListCategory();
                handleReportNumberOfFps(this.mFingerprints);
                if (this.mHwCustFingerprintSettingsFragment == null || !this.mHwCustFingerprintSettingsFragment.isShowFingerprintVibration()) {
                    removePreference("fp_vibration_cat");
                } else {
                    this.mHwCustFingerprintSettingsFragment.updateStatus();
                }
                return;
            }
            Log.e("FingerprintSettingsFragment", "cannot open BiometriManager");
            this.mFingerprints = null;
            Toast.makeText(this.mContext, this.mRes.getString(2131627668), 0).show();
            ((Activity) this.mContext).finish();
        }
    }

    public void onPause() {
        ItemUseStat.getInstance().cacheData(this.mContext);
        super.onPause();
        if (BiometricManager.isFingerprintEnabled(getActivity())) {
            if (getsIdentifyStarted()) {
                Log.i("FingerprintSettingsFragment", "onPause cancelIdentify!");
                acquireBiometricManager().cancelIdentify();
                setIdentifyStarted(false);
                setIdentifyErrCount(0);
            }
            this.mStartMode = 0;
            this.mEnrollRequestCode = 400;
            if (this.mBiometricManager != null) {
                if (!hasNoAdminFpTempletes()) {
                    this.mBiometricManager.abort();
                }
                this.mBiometricManager.release();
                this.mBiometricManager = null;
            }
        }
    }

    public void updateUsesPrefCat() {
        this.mPgUsesCat = (PreferenceGroup) findPreference("fp_uses_cat");
        if (this.mPgUsesCat == null) {
            Log.w("FingerprintSettingsFragment", "Cannot find app bind preference category");
            return;
        }
        this.mKeyGuardPref = (CustomSwitchPreference) findPreference("key_guard_pref");
        this.mStrongBoxPref = (CustomSwitchPreference) findPreference("strong_box_pref");
        this.mAppLockPref = (CustomSwitchPreference) findPreference("app_lock_pref");
        this.mHwAccountPref = (CustomSwitchPreference) findPreference("hw_account_pref");
        this.mShortcutPaymentPref = (ShortcutPaymentPreference) findPreference("shortcut_payment");
        initKeyguardPref();
        if (this.mStartMode != 1 || !hasNoAdminFpTempletes()) {
            initStrongBoxPref();
            initAppLockPref();
            initHwidPref();
            initShortcutPaymentPref();
            if (this.mHwCustFingerprintSettingsFragment != null && this.mHwCustFingerprintSettingsFragment.isFrontFingerPrint()) {
                this.mHwCustFingerprintSettingsFragment.initRecognisePreferene();
            }
            if (this.mUserId != UserHandle.myUserId()) {
                removePreference("fp_uses_cat");
            }
        }
    }

    private boolean hasNoAdminFpTempletes() {
        if (this.mFingerprints == null || this.mFingerprints.size() == 0) {
            return true;
        }
        return false;
    }

    private boolean isShortcutPayFinger(Fingerprint fp) {
        if (this.mShortcutPayStatus != 1 || this.mShortcutPayFinger == null) {
            return false;
        }
        return fp.getFingerId() == this.mShortcutPayFinger.getFingerId();
    }

    private void updateFpListPrefCat(PreferenceGroup pgListCat) {
        String pgListCatTitle = this.mRes.getString(2131627624);
        if (this.mIsHiddenUser) {
            if (this.mFingerprints.size() > 0) {
                pgListCatTitle = pgListCatTitle + " (" + String.format(this.mRes.getString(2131628067, new Object[]{Integer.valueOf(this.mFingerprints.size())}), new Object[0]) + "/" + String.format(this.mRes.getString(2131628067, new Object[]{Integer.valueOf(1)}), new Object[0]) + ")";
            }
        } else if (this.mFingerprints.size() > 0) {
            pgListCatTitle = pgListCatTitle + " (" + String.format(this.mRes.getString(2131628067, new Object[]{Integer.valueOf(this.mFingerprints.size())}), new Object[0]) + "/" + String.format(this.mRes.getString(2131628067, new Object[]{Integer.valueOf(5)}), new Object[0]) + ")";
        }
        pgListCat.setTitle((CharSequence) pgListCatTitle);
        pgListCat.removeAll();
        if (hasNoAdminFpTempletes()) {
            if (this.mHwCustFingerprintSettingsFragment != null && this.mHwCustFingerprintSettingsFragment.isFrontFingerPrint()) {
                this.mHwCustFingerprintSettingsFragment.refreshFpPreference(pgListCat);
            }
            return;
        }
        int order = 0;
        for (Fingerprint fp : this.mBiometricManager.getFingerprintList(this.mUserId)) {
            boolean isPayment = isShortcutPayFinger(fp);
            Bundle bundle = new Bundle();
            String key = FingerprintUtils.generateFpPrefKey(fp.getFingerId());
            String desc = fp.getName().toString();
            String title = desc;
            String string = isPayment ? this.mContext.getString(2131628568) : null;
            bundle.putBoolean("fp_is_payment", isPayment);
            bundle.putInt("fp_id", fp.getFingerId());
            bundle.putString("fp_name", desc);
            bundle.putParcelable("fp_obj", fp);
            Log.i("FingerprintSettingsFragment", "id = " + fp.getFingerId() + " key = " + key);
            order++;
            pgListCat.addPreference(generatePref(key, desc, string, order, "com.android.settings.fingerprint.FingerprintManageFragment", bundle));
        }
        if (this.mHwCustFingerprintSettingsFragment != null && this.mHwCustFingerprintSettingsFragment.isFrontFingerPrint()) {
            this.mHwCustFingerprintSettingsFragment.refreshFpPreference(pgListCat);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        boolean isChecked = false;
        if (!"shortcut_payment".equals(key)) {
            isChecked = ((Boolean) newValue).booleanValue();
        }
        Log.i("FingerprintSettingsFragment", "switch preference key = " + key + ", " + "value = " + newValue);
        if (this.mBiometricManager == null) {
            return false;
        }
        if ("key_guard_pref".equals(key) || "strong_box_pref".equals(key) || "app_lock_pref".equals(key) || "hw_account_pref".equals(key)) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        }
        if ("key_guard_pref".equals(key)) {
            if (isChecked) {
                if (hasNoFpTempletes()) {
                    setPrefStatByRequest(401, false);
                    startEnrollFragment(401);
                    return false;
                }
                setKeyguardAssociation(isChecked);
                if (this.mShortcutPaymentPref != null) {
                    if (this.mShortcutPayStatus == 2) {
                        updateShortcutPayFingerprint(this.mShortcutPayFinger != null, this.mShortcutPayFinger);
                    }
                    this.mShortcutPaymentPref.setEnabled(this.mHwpayInstalled);
                }
            } else if (UserHandle.myUserId() == 0) {
                for (UserInfo user : UserManager.get(this.mContext).getUsers()) {
                    if (PrivacySpaceSettingsHelper.isPrivacyUser(user)) {
                        this.hasHiddenUser = true;
                        this.hiddenUserName = user.name;
                        break;
                    }
                }
                return privacySpaceUnlockScreen(this.hasHiddenUser);
            } else {
                return privacySpaceUnlockScreen(this.mIsHiddenUser);
            }
        } else if ("strong_box_pref".equals(key)) {
            if (!isChecked) {
                setPrefStat(this.mStrongBoxPref, !FingerprintUtils.unbindByProviderCall(303, this.mContentResolver));
                return false;
            } else if (hasNoAdminFpTempletes()) {
                setPrefStatByRequest(403, false);
                startEnrollFragment(403);
                return false;
            } else if (FingerprintUtils.getStrongBoxAssociation(this.mContentResolver) == -1) {
                setPrefStatByRequest(303, false);
                showInitAndBindDialog(303);
                return false;
            } else {
                bindAppForRequest(303, isChecked);
            }
        } else if ("app_lock_pref".equals(key)) {
            if (!isChecked) {
                setPrefStat(this.mAppLockPref, !FingerprintUtils.unbindByProviderCall(302, this.mContentResolver));
                return false;
            } else if (hasNoAdminFpTempletes()) {
                setPrefStatByRequest(402, false);
                startEnrollFragment(402);
                return false;
            } else if (FingerprintUtils.getAppLockAssociation(this.mContentResolver) == -1) {
                setPrefStatByRequest(302, false);
                showInitAndBindDialog(302);
                return false;
            } else {
                bindAppForRequest(302, isChecked);
            }
        } else if ("hw_account_pref".equals(key)) {
            if (!isChecked) {
                setPrefStat(this.mHwAccountPref, !FingerprintUtils.unbindByProviderCall(301, this.mContentResolver));
                return false;
            } else if (hasNoAdminFpTempletes()) {
                setPrefStatByRequest(404, false);
                startEnrollFragment(404);
                return false;
            } else if (FingerprintUtils.getHwidAssociation(this.mContentResolver) == 2) {
                setPrefStatByRequest(301, false);
                showInitAndBindDialog(301);
                return false;
            } else {
                bindAppForRequest(301, isChecked);
            }
        } else if ("shortcut_payment".equals(key)) {
            String value = (String) newValue;
            if (value.equals("shortcut_payment_none")) {
                updateShortcutPayFingerprint(false, null);
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "shorcut_payment_none_choosen");
            } else {
                try {
                    int specFpId = Integer.valueOf(value).intValue();
                    Fingerprint specFp = this.mBiometricManager.getFingerprint(specFpId, this.mUserId);
                    if (specFp == null) {
                        Log.e("FingerprintSettingsFragment", "Null fingerprint, id = " + specFpId);
                        return false;
                    }
                    updateShortcutPayFingerprint(true, specFp);
                    ItemUseStat.getInstance().handleClick(getActivity(), 2, "shorcut_payment_spec_fp_choosen");
                } catch (NumberFormatException e) {
                    Log.e("FingerprintSettingsFragment", "Invalid values = " + value + " exception = " + e);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean privacySpaceUnlockScreen(boolean isOrHasPrivacyUser) {
        if (isOrHasPrivacyUser) {
            showConfirmDisableUnlockScreenDialog();
            return false;
        } else if (checkShortcutPayStatus()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkShortcutPayStatus() {
        if (this.mShortcutPayStatus == 1) {
            showConfirmDisableKeyguardDialog();
            return false;
        }
        setKeyguardAssociation(false);
        if (this.mShortcutPaymentPref != null) {
            this.mShortcutPaymentPref.setEnabled(false);
        }
        return true;
    }

    private void showConfirmDisableUnlockScreenDialog() {
        Builder builder = new Builder(this.mContext);
        builder.setTitle(2131628709);
        final int currentUserId = UserHandle.myUserId();
        if (currentUserId != 0 || SettingsExtUtils.isGlobalVersion()) {
            builder.setMessage(2131628726);
        } else {
            builder.setMessage(2131628715);
        }
        builder.setPositiveButton(2131625560, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (currentUserId == 0) {
                    if (FingerprintSettingsFragment.this.mShortcutPayStatus == 1) {
                        FingerprintSettingsFragment.this.updateShortcutPayFingerprint(false, FingerprintSettingsFragment.this.mShortcutPayFinger);
                    }
                    FingerprintSettingsFragment.this.mKeyGuardPref.setChecked(false);
                    FingerprintSettingsFragment.this.setKeyguardAssociation(false);
                    if (FingerprintSettingsFragment.this.mShortcutPaymentPref != null) {
                        FingerprintSettingsFragment.this.mShortcutPaymentPref.setEnabled(false);
                        return;
                    }
                    return;
                }
                FingerprintSettingsFragment.this.mKeyGuardPref.setChecked(false);
                FingerprintSettingsFragment.this.setKeyguardAssociation(false);
            }
        });
        builder.setNegativeButton(2131624572, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                FingerprintSettingsFragment.this.mKeyGuardPref.setChecked(true);
            }
        });
        builder.create().show();
    }

    private boolean isPackageInstalled(String pkgName) {
        try {
            this.mPackageManager.getPackageInfo(pkgName, 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    private HighlightPreference generatePref(String key, String title, String summary, int order, String fragment, Bundle bundle) {
        return generatePref(key, title, summary, order, null, fragment, bundle);
    }

    private HighlightPreference generatePref(String key, String title, String summary, int order, Intent intent, String fragment, Bundle bundle) {
        HighlightPreference pref = new HighlightPreference(this.mContext);
        pref.setKey(key);
        pref.setTitle((CharSequence) title);
        pref.setSummary((CharSequence) summary);
        pref.setOrder(order);
        pref.setIntent(intent);
        pref.setFragment(fragment);
        pref.setBundle(bundle);
        if (this.mHwCustFingerprintSettingsFragment != null && this.mHwCustFingerprintSettingsFragment.fingerPrintShotcut()) {
            pref.setLayoutResource(2130968977);
        }
        pref.setWidgetLayoutResource(2130968998);
        return pref;
    }

    private void addNewFingerprint(PreferenceGroup pgListCat) {
        PreferenceScreen ps = new PreferenceScreen(this.mContext, null);
        String fpStr = this.mRes.getString(2131627636);
        Preference pref = findPreference("add_new_pref");
        ps.setFragment("com.android.settings.fingerprint.enrollment.FingerprintEnrollFragment");
        ps.setKey("add_new_pref");
        ps.setTitle((CharSequence) fpStr);
        ps.setLayoutResource(2130968977);
        ps.setWidgetLayoutResource(2130968998);
        ps.setOrder(11);
        if (pref != null) {
            pgListCat.removePreference(pref);
        }
        if (!this.mIsHiddenUser || this.mFingerprints.size() < 1) {
            pgListCat.addPreference(ps);
        }
    }

    public void onCaptureCompleted() {
        Log.i("FingerprintSettingsFragment", "onCaptureCompleted, do nothing.");
    }

    public void onInput() {
    }

    public void onWaitingForInput() {
    }

    public void onIdentified(int fpId) {
        Log.i("FingerprintSettingsFragment", "onIdentified fpId = " + fpId);
        if (this.mHwCustFingerprintSettingsFragment == null || !this.mHwCustFingerprintSettingsFragment.isFrontFingerPrint()) {
            setIdentifyStarted(false);
            retryIdentify();
            if (this.mIsHighlighting) {
                Log.w("FingerprintSettingsFragment", "do not response, mIsHighlighting = " + this.mIsHighlighting);
                return;
            } else if (this.mShortcutPaymentPref != null && this.mShortcutPaymentPref.isDialogShowing()) {
                this.mShortcutPaymentPref.highLightItem(String.valueOf(fpId));
                this.mIsHighlighting = true;
                Log.d("FingerprintSettingsFragment", "mShortcutPaymentPref highlight! mIsHighlighting = " + this.mIsHighlighting);
                return;
            } else if (this.mFingerIdentifyPref != null && this.mFingerIdentifyPref.isDialogShowing()) {
                this.mFingerIdentifyPref.highLightItem(String.valueOf(fpId));
                this.mIsHighlighting = true;
                Log.d("FingerprintSettingsFragment", "mFingerIdentifyPref highlight! mIsHighlighting = " + this.mIsHighlighting);
                return;
            } else {
                return;
            }
        }
        this.mHwCustFingerprintSettingsFragment.identifyFpID(fpId);
    }

    public void onNoMatch() {
    }

    private void highlightPref(String prefKey, boolean isHighlight) {
        if (prefKey != null && !prefKey.equals("")) {
            Log.i("FingerprintSettingsFragment", "highlight pref = " + prefKey + ", highlight = " + isHighlight);
            HighlightPreference pref = (HighlightPreference) findPreference(prefKey);
            if (pref != null) {
                if (!(!isHighlight || this.mList == null || this.mList.getAdapter() == null)) {
                    this.mList.smoothScrollToPosition(((this.mList.getAdapter().getItemCount() - this.mFingerprints.size()) + pref.getOrder()) - 1);
                }
                pref.highlightBgColor(isHighlight);
            }
        }
    }

    private void toScreenLockFragment() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
        intent.putExtra("is_fp_screen_lock", true);
        this.mChallenge = acquireBiometricManager().preEnrollSafe();
        this.mPreEnrolled = true;
        intent.putExtra("minimum_quality", 65536);
        intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        intent.putExtra("hide_disabled_prefs", true);
        intent.putExtra("has_challenge", true);
        intent.putExtra("challenge", this.mChallenge);
        Log.i("FingerprintSettingsFragment", "toScreenLock enter, challenge = " + this.mChallenge);
        startActivityForResult(intent, 101);
    }

    private String getHwidName() {
        AccountManager mgr = AccountManager.get(this.mContext);
        if (mgr == null) {
            Log.w("FingerprintSettingsFragment", "cannot get AccountManager");
            return null;
        }
        Account[] accounts = mgr.getAccounts();
        if (accounts == null) {
            Log.w("FingerprintSettingsFragment", "mgr.getAccounts is null");
            return null;
        }
        for (Account account : accounts) {
            if (account != null && "com.huawei.hwid".equals(account.type)) {
                return account.name;
            }
        }
        return null;
    }

    public boolean onPreferenceTreeClick(Preference pref) {
        if (this.mHasClick) {
            return true;
        }
        this.mHasClick = true;
        this.mHighlightHandler.sendEmptyMessageDelayed(1001, 500);
        if (pref instanceof HighlightPreference) {
            Log.i("FingerprintSettingsFragment", "highlight preference is " + pref.getKey());
            Bundle bundle = ((HighlightPreference) pref).getBundle();
            if (bundle != null) {
                Log.e("FingerprintSettingsFragment", "the fingerprint id of highlight preference is " + bundle.getInt("fp_id"));
            }
            Intent intent = new Intent();
            intent.setClass(getActivity(), FingerprintManagementActivity.class);
            intent.putExtra("fp_fragment_bundle", bundle);
            intent.putExtra("android.intent.extra.USER", this.mUserId);
            startActivityForResult(intent, 201);
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "manage_fingerprint_clicked");
            return true;
        } else if ("add_new_pref".equals(pref.getKey())) {
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "add_new_fingerprint");
            if (this.mFingerprints == null || this.mFingerprints.size() < 5) {
                int i;
                if (hasNoAdminFpTempletes()) {
                    i = 401;
                } else {
                    i = 200;
                }
                startEnrollFragment(i);
            } else {
                if (this.mMaxFpCountTip != null) {
                    this.mMaxFpCountTip.cancel();
                }
                this.mMaxFpCountTip = Toast.makeText(this.mContext, String.format(this.mRes.getString(2131627656, new Object[]{Integer.valueOf(5)}), new Object[0]), 0);
                this.mMaxFpCountTip.show();
            }
            return true;
        } else {
            if ("shortcut_payment".equals(pref.getKey())) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "shorcut_payment_pref_clicked");
            }
            return super.onPreferenceTreeClick(pref);
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        if (this.mContext instanceof FingerprintSettingsActivity) {
            ((FingerprintSettingsActivity) this.mContext).setIsToFinish(false);
        }
        super.startActivityForResult(intent, requestCode);
    }

    public void startEnrollFragment(int requestCode) {
        Log.d("FingerprintSettingsFragment", "startEnrollFragment, requestCode = " + requestCode);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        if (acquireBiometricManager().needSensorCalibration()) {
            Log.d("FingerprintSettingsFragment", "startEnrollFragment, needSensorCalibration! ");
            intent.setClass(getActivity(), FingerprintCalibrationIntroActivity.class);
        } else {
            Log.d("FingerprintSettingsFragment", "startEnrollFragment, enroll directly! ");
            intent.setClass(getActivity(), FingerprintEnrollActivity.class);
        }
        if (this.mToken != null) {
            intent.putExtra("hw_auth_token", this.mToken);
        } else {
            Log.e("FingerprintSettingsFragment", "Challenge token should always exist when Enrollment starts.");
        }
        intent.putExtra("fp_fragment_bundle", bundle);
        intent.putExtra("request_code", requestCode);
        intent.putExtra("android.intent.extra.USER", this.mUserId);
        startActivityForResult(intent, requestCode);
        this.mEnrollStarted = true;
    }

    private void initAppAndBind(int whichApp) {
        Intent intent = new Intent();
        switch (whichApp) {
            case 301:
                intent.setAction("com.huawei.hwid.ACTION_LOGINBIND_FINGERPRINT");
                intent.setPackage("com.huawei.hwid");
                intent.putExtra("requestType", 2);
                break;
            case 302:
                intent.setAction("huawei.intent.action.APPLOCK_FINGERPRINT_INIT");
                intent.setPackage("com.huawei.systemmanager");
                break;
            case 303:
                intent.setAction("huawei.intent.action.STRONGBOX_FINGERPRINT_MANAGER");
                intent.setPackage("com.huawei.hidisk");
                intent.putExtra("fingerprintAuthSwitchType", 2);
                Utils.cancelSplit(this.mContext, intent);
                break;
            default:
                return;
        }
        try {
            ((FingerprintSettingsActivity) this.mContext).setIsToFinishDelay(true);
            startActivityForResult(intent, whichApp);
        } catch (Exception e) {
            Log.w("FingerprintSettingsFragment", "initAppAndBind: start activity fail, request is " + whichApp);
            e.printStackTrace();
        }
    }

    private void setPrefStat(SwitchPreference sp, boolean isChecked) {
        if (sp != null) {
            sp.setChecked(isChecked);
        }
    }

    private void initKeyguardPref() {
        if (this.mKeyGuardPref == null) {
            Log.w("FingerprintSettingsFragment", "Cannot find key guard preference");
            return;
        }
        int i;
        Log.i("FingerprintSettingsFragment", "mStartMode = " + this.mStartMode);
        if (this.mStartMode != 1) {
            int keyguardState = FingerprintUtils.getKeyguardAssociationStatus(this.mContext, this.mUserId);
            if (keyguardState == -1) {
                if (hasNoAdminFpTempletes()) {
                    setKeyguardAssociation(false);
                    this.mKeyGuardPref.setChecked(false);
                } else {
                    setKeyguardAssociation(true);
                    this.mKeyGuardPref.setChecked(true);
                }
            } else if (keyguardState == 1) {
                this.mKeyGuardPref.setChecked(true);
                if (hasNoFpTempletes()) {
                    this.mKeyGuardPref.setChecked(false);
                    setKeyguardAssociation(false);
                } else if (hasNoAdminFpTempletes()) {
                    this.mKeyGuardPref.setChecked(true);
                    setKeyguardAssociation(true);
                }
            } else {
                this.mKeyGuardPref.setChecked(false);
                if (!hasNoAdminFpTempletes()) {
                    if (!(this.mEnrollRequestCode == 401 || this.mEnrollRequestCode == 402 || this.mEnrollRequestCode == 403 || this.mEnrollRequestCode == 404)) {
                        if (this.mEnrollRequestCode == 406) {
                        }
                    }
                    this.mKeyGuardPref.setChecked(true);
                    setKeyguardAssociation(true);
                }
            }
        } else if (hasNoFpTempletes()) {
            startEnrollFragment(401);
        } else {
            this.mKeyGuardPref.setChecked(true);
            setKeyguardAssociation(true);
            Toast.makeText(this.mContext, 2131627690, 0).show();
        }
        this.mKeyGuardPref.setOnPreferenceChangeListener(this);
        CustomSwitchPreference customSwitchPreference = this.mKeyGuardPref;
        if (hasNoFpTempletes()) {
            i = 2131627700;
        } else {
            i = 2131627701;
        }
        customSwitchPreference.setSummary(i);
    }

    private void initStrongBoxPref() {
        boolean z = true;
        if (this.mStrongBoxPref == null) {
            Log.w("FingerprintSettingsFragment", "Cannot find strong box preference");
        } else if (isPackageInstalled("com.huawei.hidisk")) {
            int summaryStr;
            int strongBoxBindStat = FingerprintUtils.getStrongBoxAssociation(this.mContentResolver);
            if (strongBoxBindStat == -1) {
                if (this.mEnrollRequestCode != 403 || hasNoAdminFpTempletes()) {
                    this.mStrongBoxPref.setChecked(false);
                } else {
                    initAppAndBind(303);
                }
            } else if (hasNoAdminFpTempletes()) {
                this.mStrongBoxPref.setChecked(false);
                if (strongBoxBindStat == 1) {
                    FingerprintUtils.unbindByProviderCall(303, this.mContentResolver);
                }
            } else if (this.mEnrollRequestCode == 403) {
                bindAppForRequest(303, true);
            } else {
                CustomSwitchPreference customSwitchPreference = this.mStrongBoxPref;
                if (strongBoxBindStat != 1) {
                    z = false;
                }
                customSwitchPreference.setChecked(z);
            }
            this.mStrongBoxPref.setOnPreferenceChangeListener(this);
            if (hasNoAdminFpTempletes()) {
                summaryStr = 2131627700;
            } else if (strongBoxBindStat == -1) {
                summaryStr = 2131627702;
            } else {
                summaryStr = 2131627703;
            }
            this.mStrongBoxPref.setSummary(summaryStr);
        } else {
            Log.i("FingerprintSettingsFragment", "strong box is not installed");
            this.mPgUsesCat.removePreference(this.mStrongBoxPref);
        }
    }

    private void initAppLockPref() {
        boolean z = true;
        if (this.mAppLockPref == null) {
            Log.w("FingerprintSettingsFragment", "Cannot find apps security preference");
        } else if (FingerprintUtils.shouldShowAppLock(this.mContext, this.mUserId)) {
            int summaryStr;
            int appLockBindStat = FingerprintUtils.getAppLockAssociation(this.mContentResolver);
            if (appLockBindStat == -1) {
                if (this.mEnrollRequestCode != 402 || hasNoAdminFpTempletes()) {
                    this.mAppLockPref.setChecked(false);
                } else {
                    initAppAndBind(302);
                }
            } else if (hasNoAdminFpTempletes()) {
                this.mAppLockPref.setChecked(false);
                if (appLockBindStat == 1) {
                    FingerprintUtils.unbindByProviderCall(302, this.mContentResolver);
                }
            } else if (this.mEnrollRequestCode == 402) {
                bindAppForRequest(302, true);
            } else {
                CustomSwitchPreference customSwitchPreference = this.mAppLockPref;
                if (appLockBindStat != 1) {
                    z = false;
                }
                customSwitchPreference.setChecked(z);
            }
            this.mAppLockPref.setOnPreferenceChangeListener(this);
            if (hasNoAdminFpTempletes()) {
                summaryStr = 2131627700;
            } else if (appLockBindStat == -1) {
                summaryStr = 2131627704;
            } else {
                summaryStr = 2131627705;
            }
            this.mAppLockPref.setSummary(summaryStr);
        } else {
            Log.i("FingerprintSettingsFragment", "do not show applock entry!");
            this.mPgUsesCat.removePreference(this.mAppLockPref);
        }
    }

    private void initHwidPref() {
        boolean z = true;
        if (this.mHwAccountPref == null) {
            Log.w("FingerprintSettingsFragment", "Cannot find huawei account preference");
        } else if (!isPackageInstalled("com.huawei.hwid")) {
            Log.i("FingerprintSettingsFragment", "huawei account is not installed");
            this.mPgUsesCat.removePreference(this.mHwAccountPref);
        } else if (FingerprintUtils.isHwidSupported(this.mContext)) {
            String summaryStr;
            int hwidBindStat = FingerprintUtils.getHwidAssociation(this.mContentResolver);
            if (hwidBindStat == -1) {
                this.mHwAccountPref.setEnabled(false);
                this.mHwAccountPref.setChecked(false);
            } else if (hwidBindStat == 2) {
                if (this.mEnrollRequestCode != 404 || hasNoAdminFpTempletes()) {
                    this.mHwAccountPref.setChecked(false);
                } else {
                    initAppAndBind(301);
                }
            } else if (hasNoAdminFpTempletes()) {
                this.mHwAccountPref.setChecked(false);
                if (hwidBindStat == 1) {
                    FingerprintUtils.unbindByProviderCall(301, this.mContentResolver);
                }
            } else if (this.mEnrollRequestCode == 404) {
                bindAppForRequest(301, true);
            } else {
                CustomSwitchPreference customSwitchPreference = this.mHwAccountPref;
                if (hwidBindStat != 1) {
                    z = false;
                }
                customSwitchPreference.setChecked(z);
            }
            this.mHwAccountPref.setOnPreferenceChangeListener(this);
            if (hwidBindStat == -1) {
                summaryStr = this.mContext.getString(2131627707);
            } else if (hwidBindStat == 2) {
                summaryStr = this.mContext.getString(2131627706);
            } else {
                summaryStr = getHwidName();
            }
            this.mHwAccountPref.setSummary((CharSequence) summaryStr);
        } else {
            this.mPgUsesCat.removePreference(this.mHwAccountPref);
        }
    }

    private void initShortcutPaymentPref() {
        int status = Secure.getIntForUser(this.mContentResolver, "fp_shortcut_enabled", 0, this.mUserId);
        Fingerprint payFinger = queryShortcutPayFinger();
        this.mHwpayInstalled = FingerprintUtils.isHwpayInstalled(this.mContext);
        int updatedStatus = handleShortcutPayChanged(status, payFinger);
        if (this.mShortcutPaymentPref != null) {
            if (!FingerprintUtils.isQuickHwpayOn()) {
                this.mPgUsesCat.removePreference(this.mShortcutPaymentPref);
            } else if (this.mIsHiddenUser) {
                this.mPgUsesCat.removePreference(this.mShortcutPaymentPref);
            } else {
                refreshShortcutPaymentPref(updatedStatus, payFinger);
            }
        }
    }

    private void setKeyguardAssociation(boolean isChecked) {
        if (this.mBiometricManager.setAssociation(this.mContext, "com.android.keyguard", isChecked, this.mUserId) < 0) {
            Log.e("FingerprintSettingsFragment", "Failed to set keyguard association = " + isChecked);
        }
    }

    private void showInitAndBindDialog(final int requestCode) {
        int title;
        int msg;
        int btnOk;
        switch (requestCode) {
            case 301:
                title = 2131627721;
                msg = 2131627722;
                btnOk = 2131627723;
                break;
            case 302:
                title = 2131627718;
                msg = 2131627719;
                btnOk = 2131627720;
                break;
            case 303:
                title = 2131627715;
                msg = 2131627716;
                btnOk = 2131627717;
                break;
            default:
                return;
        }
        Builder builder = new Builder(this.mContext);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(btnOk, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FingerprintSettingsFragment.this.initAppAndBind(requestCode);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(2131627709, null);
        builder.create().show();
    }

    private void bindAppForRequest(int requestCode, boolean isChecked) {
        int i = 0;
        Intent intent = new Intent();
        String str;
        switch (requestCode) {
            case 301:
                intent.setAction("com.huawei.hwid.FINGERPRINT_MANAGER");
                intent.setPackage("com.huawei.hwid");
                str = "fingerprintManagerType";
                if (isChecked) {
                    i = 1;
                }
                intent.putExtra(str, i);
                break;
            case 302:
                intent.setAction("huawei.intent.action.APPLOCK_FINGERPRINT_MANAGER");
                intent.setPackage("com.huawei.systemmanager");
                str = "fingerprintAuthSwitchType";
                if (isChecked) {
                    i = 1;
                }
                intent.putExtra(str, i);
                break;
            case 303:
                intent.setAction("huawei.intent.action.STRONGBOX_FINGERPRINT_MANAGER");
                intent.setPackage("com.huawei.hidisk");
                str = "fingerprintAuthSwitchType";
                if (isChecked) {
                    i = 1;
                }
                intent.putExtra(str, i);
                break;
            default:
                return;
        }
        if (this.mContext instanceof FingerprintSettingsActivity) {
            ((FingerprintSettingsActivity) this.mContext).setIsToFinishDelay(true);
        }
        try {
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Log.w("FingerprintSettingsFragment", "bindApp: start activity fail, request is " + requestCode);
            e.printStackTrace();
        }
    }

    private void setPrefStatByRequest(int requestCode, boolean isChecked) {
        switch (requestCode) {
            case 301:
            case 404:
                if (this.mHwAccountPref != null) {
                    this.mHwAccountPref.setChecked(isChecked);
                    return;
                }
                return;
            case 302:
            case 402:
                if (this.mAppLockPref != null) {
                    this.mAppLockPref.setChecked(isChecked);
                    return;
                }
                return;
            case 303:
            case 403:
                if (this.mStrongBoxPref != null) {
                    this.mStrongBoxPref.setChecked(isChecked);
                    return;
                }
                return;
            case 401:
                if (this.mKeyGuardPref != null) {
                    this.mKeyGuardPref.setChecked(isChecked);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private boolean hasNoFpTempletes() {
        return this.mBiometricManager.getEnrolledFpNum(this.mUserId) == 0;
    }

    private void handleReportNumberOfFps(List<Fingerprint> fps) {
        if (fps != null) {
            ItemUseStat.getInstance().handleClick(this.mContext, 2, String.format("user_%d_fp", new Object[]{Integer.valueOf(this.mUserId)}), fps.size());
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("FingerprintSettingsFragment", "onDestroy mPreEnrolled = " + this.mPreEnrolled + ", mIdentifyStarted = " + this.mIdentifyStarted + ", mBiometricManager = " + this.mBiometricManager);
        if (this.mPreEnrolled) {
            Log.d("FingerprintSettingsFragment", "execute postEnroll when destroy activity");
            int ret = acquireBiometricManager().postEnrollSafe(this.mChallenge);
            if (ret < 0) {
                Log.e("FingerprintSettingsFragment", "postEnroll failed, ret = " + ret);
            }
        }
    }

    public void onIdentifyError(int err) {
        if (this.mBiometricManager == null || !getsIdentifyStarted()) {
            Log.e("FingerprintSettingsFragment", "Fingerprint identification not started yet.");
            return;
        }
        setIdentifyStarted(false);
        if (err == 1) {
            Log.e("FingerprintSettingsFragment", "Fingerprint hardware unavailable, identification won't be restarted.");
        } else if (err == 7) {
            Log.w("FingerprintSettingsFragment", "Fingerprint identification is lockout!");
            this.mInFingerprintLockout = true;
            if (!this.mHighlightHandler.hasCallbacks(this.mFingerprintLockoutReset)) {
                this.mHighlightHandler.postDelayed(this.mFingerprintLockoutReset, 30000);
            }
        } else {
            this.mIdentifyErrCount++;
            if (this.mIdentifyErrCount > 10) {
                Log.e("FingerprintSettingsFragment", "Too many identification errors occured.");
                return;
            }
            Log.w("FingerprintSettingsFragment", "Identification failure count = " + this.mIdentifyErrCount);
            retryIdentify();
        }
    }

    void retryIdentify() {
        if (!hasNoAdminFpTempletes() && !this.mInFingerprintLockout) {
            Log.i("FingerprintSettingsFragment", "restart identify.");
            if (this.mBiometricManager == null) {
                this.mBiometricManager = BiometricManager.open(getActivity());
            }
            this.mBiometricManager.startIdentify(this, 16777216, this.mUserId);
            setIdentifyStarted(true);
        }
    }

    private void addFingerIdentifyPref(PreferenceGroup pgListCat) {
        Preference pref = findPreference("finger_identification");
        if (pref != null) {
            pgListCat.removePreference(pref);
        }
        if (!hasNoAdminFpTempletes()) {
            this.mFingerIdentifyPref = new FingerIdentifyDialogPreference(this.mContext, null);
            this.mFingerIdentifyPref.setKey("finger_identification");
            this.mFingerIdentifyPref.setTitle(this.mRes.getString(2131628682));
            this.mFingerIdentifyPref.setLayoutResource(2130968991);
            this.mFingerIdentifyPref.setWidgetLayoutResource(2130968998);
            this.mFingerIdentifyPref.setOrder(12);
            this.mFingerIdentifyPref.setDialogListener(this.mDialogPrefListener);
            this.mFingerIdentifyPref.setUserId(this.mUserId);
            pgListCat.addPreference(this.mFingerIdentifyPref);
        }
    }

    private void refreshFingerListCategory() {
        PreferenceGroup pgListCat = (PreferenceGroup) findPreference("fp_list_cat");
        if (pgListCat != null) {
            updateFpListPrefCat(pgListCat);
            if (!(this.mHwCustFingerprintSettingsFragment == null || !this.mHwCustFingerprintSettingsFragment.fingerPrintShotcut() || hasNoAdminFpTempletes())) {
                this.mHwCustFingerprintSettingsFragment.setFpSummary(this.mContext, this.mFingerprints);
            }
            addNewFingerprint(pgListCat);
            addFingerIdentifyPref(pgListCat);
        }
    }

    private void savePaymentStatus(int status) {
        this.mShortcutPayStatus = status;
        this.mShortcutPaymentPref.setPaymentStatus(status);
    }

    private void savePaymentFinger(Fingerprint fp) {
        this.mShortcutPayFinger = fp;
        this.mShortcutPaymentPref.setCurrentFinger(fp);
    }

    BiometricManager acquireBiometricManager() {
        if (this.mBiometricManager == null) {
            this.mBiometricManager = BiometricManager.open(this.mContext);
        }
        return this.mBiometricManager;
    }

    private Fingerprint queryShortcutPayFinger() {
        Fingerprint fp = null;
        try {
            int fpId = Secure.getIntForUser(this.mContentResolver, "fp_shortcut_payment_fp_id", this.mUserId);
            fp = getFingerLocal(fpId);
            if (fp == null) {
                Log.i("FingerprintSettingsFragment", "Shortcut payment is on but specified fingerprint not found, id = " + fpId);
            }
        } catch (Exception e) {
            Log.e("FingerprintSettingsFragment", "Failed to query shortcut payment finger.");
            e.printStackTrace();
        }
        return fp;
    }

    private Fingerprint getFingerLocal(int fpId) {
        if (this.mFingerprints == null) {
            return null;
        }
        for (Fingerprint fp : this.mFingerprints) {
            if (fpId == fp.getFingerId()) {
                return fp;
            }
        }
        return null;
    }

    private int handleShortcutPayChanged(int status, Fingerprint payFinger) {
        int retStatus = status;
        if (!(this.mHwpayInstalled || status == 0)) {
            Secure.putIntForUser(this.mContentResolver, "fp_shortcut_enabled", 0, this.mUserId);
            showConfirmHwpayRemovedDialog();
            retStatus = 0;
        }
        if (hasNoAdminFpTempletes()) {
            if (status == 0) {
                return retStatus;
            }
            Secure.putIntForUser(this.mContentResolver, "fp_shortcut_enabled", 0, this.mUserId);
            return 0;
        } else if (status != 1) {
            return retStatus;
        } else {
            if (payFinger != null && getFingerLocal(payFinger.getFingerId()) != null) {
                return retStatus;
            }
            Secure.putIntForUser(this.mContentResolver, "fp_shortcut_enabled", 0, this.mUserId);
            return 0;
        }
    }

    private void refreshShortcutPaymentPref(int status, Fingerprint payFinger) {
        boolean isChecked;
        if (status == 1) {
            if (payFinger != null) {
                this.mShortcutPaymentPref.setSummary(payFinger.getName());
                savePaymentStatus(1);
            } else {
                Log.e("FingerprintSettingsFragment", "Shortcut payment is on but specified fingerprint not found!");
                this.mShortcutPaymentPref.setSummary(2131628214);
                savePaymentStatus(0);
            }
            savePaymentFinger(payFinger);
        } else {
            if (status == 2) {
                if (payFinger == null) {
                    this.mShortcutPaymentPref.setSummary(2131628214);
                } else {
                    this.mShortcutPaymentPref.setSummary(payFinger.getName());
                }
                savePaymentFinger(payFinger);
            } else {
                if (status != 0) {
                    Log.w("FingerprintSettingsFragment", "Unexpected shortcut payment status = " + status);
                    status = 0;
                }
                this.mShortcutPaymentPref.setSummary(2131628214);
                savePaymentFinger(null);
            }
            savePaymentStatus(status);
        }
        boolean noFinger = hasNoAdminFpTempletes();
        if (this.mHwpayInstalled) {
            isChecked = this.mKeyGuardPref.isChecked();
        } else {
            isChecked = false;
        }
        int netherSummary = 2131628572;
        if (this.mHwpayInstalled) {
            netherSummary = noFinger ? 2131627700 : 2131628566;
        }
        this.mShortcutPaymentPref.setNetherSummary(this.mContext.getText(netherSummary));
        this.mShortcutPaymentPref.setOnPreferenceChangeListener(this);
        this.mShortcutPaymentPref.setEnrollListener(this.mEnrollClickListener);
        this.mShortcutPaymentPref.setEnabled(isChecked);
        this.mShortcutPaymentPref.setDialogListener(this.mDialogPrefListener);
    }

    private void updateShortcutPayFingerprint(boolean modeOn, Fingerprint fp) {
        if (this.mShortcutPaymentPref != null) {
            if (!modeOn) {
                savePaymentStatus(fp == null ? 0 : 2);
                this.mShortcutPaymentPref.setSummary(fp == null ? this.mContext.getText(2131628214) : fp.getName());
                Secure.putIntForUser(this.mContentResolver, "fp_shortcut_enabled", this.mShortcutPayStatus, this.mUserId);
            } else if (fp == null) {
                Log.e("FingerprintSettingsFragment", "Try to turn on shortcut payment but no finger specified!");
                return;
            } else {
                Log.d("FingerprintSettingsFragment", "Turn on shortcut! fpName = " + fp.getName() + ", fpId = " + fp.getFingerId());
                savePaymentStatus(1);
                Secure.putIntForUser(this.mContentResolver, "fp_shortcut_payment_fp_id", fp.getFingerId(), this.mUserId);
                Secure.putIntForUser(this.mContentResolver, "fp_shortcut_enabled", 1, this.mUserId);
                this.mShortcutPaymentPref.setSummary(fp.getName());
            }
            savePaymentFinger(fp);
            refreshFingerListCategory();
        }
    }

    private void showConfirmHwpayRemovedDialog() {
        Builder builder = new Builder(this.mContext);
        builder.setTitle(2131628569);
        builder.setMessage(2131628571);
        builder.setPositiveButton(2131624573, null);
        builder.create().show();
    }

    private void showConfirmDisableKeyguardDialog() {
        if (this.mShortcutPaymentPref != null) {
            Builder builder = new Builder(this.mContext);
            builder.setTitle(2131628219);
            builder.setMessage(2131628570);
            builder.setPositiveButton(2131628221, new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    FingerprintSettingsFragment.this.setKeyguardAssociation(false);
                    FingerprintSettingsFragment.this.updateShortcutPayFingerprint(false, FingerprintSettingsFragment.this.mShortcutPayFinger);
                    FingerprintSettingsFragment.this.mShortcutPaymentPref.setEnabled(false);
                    FingerprintSettingsFragment.this.mKeyGuardPref.setChecked(false);
                }
            });
            builder.setNegativeButton(2131624572, new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    FingerprintSettingsFragment.this.mShortcutPaymentPref.setEnabled(true);
                    FingerprintSettingsFragment.this.mKeyGuardPref.setChecked(true);
                }
            });
            builder.create().show();
        }
    }

    public void setIdentifyStarted(boolean status) {
        this.mIdentifyStarted = status;
    }

    public boolean getsIdentifyStarted() {
        return this.mIdentifyStarted;
    }

    public void setIdentifyErrCount(int number) {
        this.mIdentifyErrCount = number;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView v = (TextView) LayoutInflater.from(view.getContext()).inflate(2130968800, null);
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(getActivity(), 32, UserHandle.myUserId());
        if (admin != null) {
            v.setText(LearnMoreSpan.linkify(getText(2131624684), getString(getHelpResource()), admin));
            v.setMovementMethod(new LinkMovementMethod());
            setFooterView((View) v);
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mHighlightHandler != null) {
            this.mHighlightHandler.removeMessages(1000);
            Log.d("FingerprintSettingsFragment", "MSG_HIGHLIGHT_OFF removed.");
            this.mHighlightHandler.removeCallbacks(this.mFingerprintLockoutReset);
            Log.d("FingerprintSettingsFragment", "mFingerprintLockoutReset removed.");
        }
        if (this.mInFingerprintLockout && this.mHwCustFingerprintSettingsFragment != null && this.mHwCustFingerprintSettingsFragment.isFrontFingerPrint()) {
            this.mHwCustFingerprintSettingsFragment.sendCancelIdentifyMessage();
        }
    }
}

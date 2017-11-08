package com.android.settings.fingerprint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.ChooseLockGeneric;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SubSettings;
import com.android.settings.Utils;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.List;

public class FingerprintSettings extends SubSettings {

    public static class FingerprintPreference extends Preference {
        private Fingerprint mFingerprint;
        private View mView;

        public FingerprintPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public FingerprintPreference(Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public FingerprintPreference(Context context, AttributeSet attrs) {
            this(context, attrs, 16842894);
        }

        public FingerprintPreference(Context context) {
            this(context, null);
        }

        public View getView() {
            return this.mView;
        }

        public void setFingerprint(Fingerprint item) {
            this.mFingerprint = item;
        }

        public Fingerprint getFingerprint() {
            return this.mFingerprint;
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            this.mView = view.itemView;
        }
    }

    public static class FingerprintSettingsFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
        private AuthenticationCallback mAuthCallback = new AuthenticationCallback() {
            public void onAuthenticationSucceeded(AuthenticationResult result) {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1001, result.getFingerprint().getFingerId(), 0).sendToTarget();
            }

            public void onAuthenticationFailed() {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1002).sendToTarget();
            }

            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1003, errMsgId, 0, errString).sendToTarget();
            }

            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1004, helpMsgId, 0, helpString).sendToTarget();
            }
        };
        private CancellationSignal mFingerprintCancel;
        private final Runnable mFingerprintLockoutReset = new Runnable() {
            public void run() {
                FingerprintSettingsFragment.this.mInFingerprintLockout = false;
                FingerprintSettingsFragment.this.retryFingerprint();
            }
        };
        private FingerprintManager mFingerprintManager;
        private final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1000:
                        FingerprintSettingsFragment.this.removeFingerprintPreference(msg.arg1);
                        FingerprintSettingsFragment.this.updateAddPreference();
                        FingerprintSettingsFragment.this.retryFingerprint();
                        return;
                    case 1001:
                        FingerprintSettingsFragment.this.mFingerprintCancel = null;
                        FingerprintSettingsFragment.this.highlightFingerprintItem(msg.arg1);
                        FingerprintSettingsFragment.this.retryFingerprint();
                        return;
                    case 1003:
                        FingerprintSettingsFragment.this.handleError(msg.arg1, (CharSequence) msg.obj);
                        return;
                    default:
                        return;
                }
            }
        };
        private Drawable mHighlightDrawable;
        private boolean mInFingerprintLockout;
        private boolean mLaunchedConfirm;
        private RemovalCallback mRemoveCallback = new RemovalCallback() {
            public void onRemovalSucceeded(Fingerprint fingerprint) {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1000, fingerprint.getFingerId(), 0).sendToTarget();
            }

            public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
                Activity activity = FingerprintSettingsFragment.this.getActivity();
                if (activity != null) {
                    Toast.makeText(activity, errString, 0);
                }
            }
        };
        private byte[] mToken;
        private int mUserId;

        public static class ConfirmLastDeleteDialog extends DialogFragment {
            private Fingerprint mFp;

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                int i;
                this.mFp = (Fingerprint) getArguments().getParcelable("fingerprint");
                boolean isProfileChallengeUser = getArguments().getBoolean("isProfileChallengeUser");
                Builder title = new Builder(getActivity()).setTitle(2131624688);
                if (isProfileChallengeUser) {
                    i = 2131624690;
                } else {
                    i = 2131624689;
                }
                return title.setMessage(i).setPositiveButton(2131624691, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((FingerprintSettingsFragment) ConfirmLastDeleteDialog.this.getTargetFragment()).deleteFingerPrint(ConfirmLastDeleteDialog.this.mFp);
                        dialog.dismiss();
                    }
                }).setNegativeButton(2131624572, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
            }
        }

        public static class RenameDeleteDialog extends DialogFragment {
            private EditText mDialogTextField;
            private String mFingerName;
            private Fingerprint mFp;
            private Boolean mTextHadFocus;
            private int mTextSelectionEnd;
            private int mTextSelectionStart;

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                this.mFp = (Fingerprint) getArguments().getParcelable("fingerprint");
                if (savedInstanceState != null) {
                    this.mFingerName = savedInstanceState.getString("fingerName");
                    this.mTextHadFocus = Boolean.valueOf(savedInstanceState.getBoolean("textHadFocus"));
                    this.mTextSelectionStart = savedInstanceState.getInt("startSelection");
                    this.mTextSelectionEnd = savedInstanceState.getInt("endSelection");
                }
                final AlertDialog alertDialog = new Builder(getActivity()).setView(2130968799).setPositiveButton(2131624662, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = RenameDeleteDialog.this.mDialogTextField.getText().toString();
                        CharSequence name = RenameDeleteDialog.this.mFp.getName();
                        if (!newName.equals(name)) {
                            Log.v("FingerprintSettings", "rename " + name + " to " + newName);
                            MetricsLogger.action(RenameDeleteDialog.this.getContext(), 254, RenameDeleteDialog.this.mFp.getFingerId());
                            ((FingerprintSettingsFragment) RenameDeleteDialog.this.getTargetFragment()).renameFingerPrint(RenameDeleteDialog.this.mFp.getFingerId(), newName);
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(2131624663, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RenameDeleteDialog.this.onDeleteClick(dialog);
                    }
                }).create();
                alertDialog.setOnShowListener(new OnShowListener() {
                    public void onShow(DialogInterface dialog) {
                        RenameDeleteDialog.this.mDialogTextField = (EditText) alertDialog.findViewById(2131886637);
                        RenameDeleteDialog.this.mDialogTextField.setText(RenameDeleteDialog.this.mFingerName == null ? RenameDeleteDialog.this.mFp.getName() : RenameDeleteDialog.this.mFingerName);
                        if (RenameDeleteDialog.this.mTextHadFocus == null) {
                            RenameDeleteDialog.this.mDialogTextField.selectAll();
                        } else {
                            RenameDeleteDialog.this.mDialogTextField.setSelection(RenameDeleteDialog.this.mTextSelectionStart, RenameDeleteDialog.this.mTextSelectionEnd);
                        }
                    }
                });
                if (this.mTextHadFocus == null || this.mTextHadFocus.booleanValue()) {
                    alertDialog.getWindow().setSoftInputMode(5);
                }
                return alertDialog;
            }

            private void onDeleteClick(DialogInterface dialog) {
                Log.v("FingerprintSettings", "Removing fpId=" + this.mFp.getFingerId());
                MetricsLogger.action(getContext(), 253, this.mFp.getFingerId());
                FingerprintSettingsFragment parent = (FingerprintSettingsFragment) getTargetFragment();
                boolean isProfileChallengeUser = Utils.isManagedProfile(UserManager.get(getContext()), parent.mUserId);
                if (parent.mFingerprintManager.getEnrolledFingerprints(parent.mUserId).size() > 1) {
                    parent.deleteFingerPrint(this.mFp);
                } else {
                    ConfirmLastDeleteDialog lastDeleteDialog = new ConfirmLastDeleteDialog();
                    Bundle args = new Bundle();
                    args.putParcelable("fingerprint", this.mFp);
                    args.putBoolean("isProfileChallengeUser", isProfileChallengeUser);
                    lastDeleteDialog.setArguments(args);
                    lastDeleteDialog.setTargetFragment(getTargetFragment(), 0);
                    lastDeleteDialog.show(getFragmentManager(), ConfirmLastDeleteDialog.class.getName());
                }
                dialog.dismiss();
            }

            public void onSaveInstanceState(Bundle outState) {
                super.onSaveInstanceState(outState);
                if (this.mDialogTextField != null) {
                    outState.putString("fingerName", this.mDialogTextField.getText().toString());
                    outState.putBoolean("textHadFocus", this.mDialogTextField.hasFocus());
                    outState.putInt("startSelection", this.mDialogTextField.getSelectionStart());
                    outState.putInt("endSelection", this.mDialogTextField.getSelectionEnd());
                }
            }
        }

        private void stopFingerprint() {
            if (!(this.mFingerprintCancel == null || this.mFingerprintCancel.isCanceled())) {
                this.mFingerprintCancel.cancel();
            }
            this.mFingerprintCancel = null;
        }

        protected void handleError(int errMsgId, CharSequence msg) {
            this.mFingerprintCancel = null;
            switch (errMsgId) {
                case 5:
                    return;
                case 7:
                    this.mInFingerprintLockout = true;
                    if (!this.mHandler.hasCallbacks(this.mFingerprintLockoutReset)) {
                        this.mHandler.postDelayed(this.mFingerprintLockoutReset, 30000);
                        break;
                    }
                    break;
            }
            Activity activity = getActivity();
            if (activity != null) {
                Toast.makeText(activity, msg, 0);
            }
            retryFingerprint();
        }

        private void retryFingerprint() {
            if (!this.mInFingerprintLockout) {
                this.mFingerprintCancel = new CancellationSignal();
                this.mFingerprintManager.authenticate(null, this.mFingerprintCancel, 0, this.mAuthCallback, null, this.mUserId);
            }
        }

        protected int getMetricsCategory() {
            return 49;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                this.mToken = savedInstanceState.getByteArray("hw_auth_token");
                this.mLaunchedConfirm = savedInstanceState.getBoolean("launched_confirm", false);
            }
            this.mUserId = getActivity().getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
            this.mFingerprintManager = (FingerprintManager) getActivity().getSystemService("fingerprint");
            if (this.mToken == null && !this.mLaunchedConfirm) {
                this.mLaunchedConfirm = true;
                launchChooseOrConfirmLock();
            }
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            int i;
            super.onViewCreated(view, savedInstanceState);
            TextView v = (TextView) LayoutInflater.from(view.getContext()).inflate(2130968800, null);
            EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(getActivity(), 32, this.mUserId);
            if (admin != null) {
                i = 2131624684;
            } else {
                i = 2131624683;
            }
            v.setText(LearnMoreSpan.linkify(getText(i), getString(getHelpResource()), admin));
            v.setMovementMethod(new LinkMovementMethod());
            setFooterView((View) v);
        }

        protected void removeFingerprintPreference(int fingerprintId) {
            String name = genKey(fingerprintId);
            Preference prefToRemove = findPreference(name);
            if (prefToRemove == null) {
                Log.w("FingerprintSettings", "Can't find preference to remove: " + name);
            } else if (!getPreferenceScreen().removePreference(prefToRemove)) {
                Log.w("FingerprintSettings", "Failed to remove preference with key " + name);
            }
        }

        private PreferenceScreen createPreferenceHierarchy() {
            PreferenceScreen root = getPreferenceScreen();
            if (root != null) {
                root.removeAll();
            }
            addPreferencesFromResource(2131230862);
            root = getPreferenceScreen();
            addFingerprintItemPreferences(root);
            setPreferenceScreen(root);
            return root;
        }

        private void addFingerprintItemPreferences(PreferenceGroup root) {
            root.removeAll();
            List<Fingerprint> items = this.mFingerprintManager.getEnrolledFingerprints(this.mUserId);
            int fingerprintCount = items.size();
            for (int i = 0; i < fingerprintCount; i++) {
                Fingerprint item = (Fingerprint) items.get(i);
                FingerprintPreference pref = new FingerprintPreference(root.getContext());
                pref.setKey(genKey(item.getFingerId()));
                pref.setTitle(item.getName());
                pref.setFingerprint(item);
                pref.setPersistent(false);
                root.addPreference(pref);
                pref.setOnPreferenceChangeListener(this);
            }
            Preference addPreference = new Preference(root.getContext());
            addPreference.setKey("key_fingerprint_add");
            addPreference.setTitle(2131624645);
            addPreference.setIcon(2130838178);
            root.addPreference(addPreference);
            addPreference.setOnPreferenceChangeListener(this);
            updateAddPreference();
        }

        private void updateAddPreference() {
            boolean tooMany;
            CharSequence maxSummary;
            boolean z = false;
            if (this.mFingerprintManager.getEnrolledFingerprints(this.mUserId).size() >= getContext().getResources().getInteger(17694880)) {
                tooMany = true;
            } else {
                tooMany = false;
            }
            if (tooMany) {
                maxSummary = getContext().getString(2131624687, new Object[]{Integer.valueOf(max)});
            } else {
                maxSummary = "";
            }
            Preference addPreference = findPreference("key_fingerprint_add");
            addPreference.setSummary(maxSummary);
            if (!tooMany) {
                z = true;
            }
            addPreference.setEnabled(z);
        }

        private static String genKey(int id) {
            return "key_fingerprint_item_" + id;
        }

        public void onResume() {
            super.onResume();
            updatePreferences();
        }

        private void updatePreferences() {
            createPreferenceHierarchy();
            retryFingerprint();
        }

        public void onPause() {
            super.onPause();
            stopFingerprint();
        }

        public void onSaveInstanceState(Bundle outState) {
            outState.putByteArray("hw_auth_token", this.mToken);
            outState.putBoolean("launched_confirm", this.mLaunchedConfirm);
        }

        public boolean onPreferenceTreeClick(Preference pref) {
            if ("key_fingerprint_add".equals(pref.getKey())) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", FingerprintEnrollEnrolling.class.getName());
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                intent.putExtra("hw_auth_token", this.mToken);
                startActivityForResult(intent, 10);
            } else if (pref instanceof FingerprintPreference) {
                showRenameDeleteDialog(((FingerprintPreference) pref).getFingerprint());
                return super.onPreferenceTreeClick(pref);
            }
            return true;
        }

        private void showRenameDeleteDialog(Fingerprint fp) {
            RenameDeleteDialog renameDeleteDialog = new RenameDeleteDialog();
            Bundle args = new Bundle();
            args.putParcelable("fingerprint", fp);
            renameDeleteDialog.setArguments(args);
            renameDeleteDialog.setTargetFragment(this, 0);
            renameDeleteDialog.show(getFragmentManager(), RenameDeleteDialog.class.getName());
        }

        public boolean onPreferenceChange(Preference preference, Object value) {
            String key = preference.getKey();
            if (!"fingerprint_enable_keyguard_toggle".equals(key)) {
                Log.v("FingerprintSettings", "Unknown key:" + key);
            }
            return true;
        }

        protected int getHelpResource() {
            return 2131626554;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 102 || requestCode == 101) {
                if ((resultCode == 1 || resultCode == -1) && data != null) {
                    this.mToken = data.getByteArrayExtra("hw_auth_token");
                }
            } else if (requestCode == 10 && resultCode == 3) {
                Activity activity = getActivity();
                activity.setResult(3);
                activity.finish();
            }
            if (this.mToken == null) {
                getActivity().finish();
            }
        }

        public void onDestroy() {
            super.onDestroy();
            if (getActivity().isFinishing()) {
                Log.d("FingerprintSettings", "execute postEnroll when destroy activity");
                int result = this.mFingerprintManager.postEnroll();
                if (result < 0) {
                    Log.w("FingerprintSettings", "postEnroll failed: result = " + result);
                }
            }
        }

        private Drawable getHighlightDrawable() {
            if (this.mHighlightDrawable == null) {
                Activity activity = getActivity();
                if (activity != null) {
                    this.mHighlightDrawable = activity.getDrawable(2130838584);
                }
            }
            return this.mHighlightDrawable;
        }

        private void highlightFingerprintItem(int fpId) {
            FingerprintPreference fpref = (FingerprintPreference) findPreference(genKey(fpId));
            Drawable highlight = getHighlightDrawable();
            if (highlight != null) {
                final View view = fpref.getView();
                highlight.setHotspot((float) (view.getWidth() / 2), (float) (view.getHeight() / 2));
                view.setBackground(highlight);
                view.setPressed(true);
                view.setPressed(false);
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        view.setBackground(null);
                    }
                }, 500);
            }
        }

        private void launchChooseOrConfirmLock() {
            Intent intent = new Intent();
            long challenge = this.mFingerprintManager.preEnroll();
            if (!new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(101, getString(2131624642), null, null, challenge, this.mUserId)) {
                intent.setClassName("com.android.settings", ChooseLockGeneric.class.getName());
                intent.putExtra("minimum_quality", 65536);
                intent.putExtra("hide_disabled_prefs", true);
                intent.putExtra("has_challenge", true);
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                intent.putExtra("challenge", challenge);
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                startActivityForResult(intent, 102);
            }
        }

        private void deleteFingerPrint(Fingerprint fingerPrint) {
            this.mFingerprintManager.remove(fingerPrint, this.mUserId, this.mRemoveCallback);
        }

        private void renameFingerPrint(int fingerId, String newName) {
            this.mFingerprintManager.rename(fingerId, this.mUserId, newName);
            updatePreferences();
        }
    }

    public static class LearnMoreSpan extends URLSpan {
        private static final Typeface TYPEFACE_MEDIUM = Typeface.create("sans-serif-medium", 0);
        private EnforcedAdmin mEnforcedAdmin = null;

        private LearnMoreSpan(String url) {
            super(url);
        }

        private LearnMoreSpan(EnforcedAdmin admin) {
            super((String) null);
            this.mEnforcedAdmin = admin;
        }

        public void onClick(View widget) {
            Context ctx = widget.getContext();
            if (this.mEnforcedAdmin != null) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(ctx, this.mEnforcedAdmin);
                return;
            }
            Intent intent = HelpUtils.getHelpIntent(ctx, getURL(), ctx.getClass().getName());
            try {
                widget.startActivityForResult(intent, 0);
            } catch (ActivityNotFoundException e) {
                Log.w("FingerprintSettings", "Actvity was not found for intent, " + intent.toString());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            ds.setTypeface(TYPEFACE_MEDIUM);
        }

        public static CharSequence linkify(CharSequence rawText, String uri, EnforcedAdmin admin) {
            int i = 0;
            SpannableString msg = new SpannableString(rawText);
            Annotation[] spans = (Annotation[]) msg.getSpans(0, msg.length(), Annotation.class);
            SpannableStringBuilder builder = new SpannableStringBuilder(msg);
            int length = spans.length;
            while (i < length) {
                Annotation annotation = spans[i];
                String key = annotation.getValue();
                int start = msg.getSpanStart(annotation);
                int end = msg.getSpanEnd(annotation);
                Object obj = null;
                if ("url".equals(key)) {
                    obj = new LearnMoreSpan(uri);
                } else if ("admin_details".equals(key)) {
                    obj = new LearnMoreSpan(admin);
                }
                if (obj != null) {
                    builder.setSpan(obj, start, end, msg.getSpanFlags(obj));
                }
                i++;
            }
            return builder;
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", FingerprintSettingsFragment.class.getName());
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (FingerprintSettingsFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(2131624642));
    }

    public static Preference getFingerprintPreferenceForUser(Context context, final int userId) {
        FingerprintManager fpm = (FingerprintManager) context.getSystemService("fingerprint");
        if (fpm == null || !fpm.isHardwareDetected()) {
            Log.v("FingerprintSettings", "No fingerprint hardware detected!!");
            return null;
        }
        int fingerprintCount;
        String clazz;
        Preference fingerprintPreference = new Preference(context);
        fingerprintPreference.setKey("fingerprint_settings");
        fingerprintPreference.setTitle(2131624642);
        List<Fingerprint> items = fpm.getEnrolledFingerprints(userId);
        if (items != null) {
            fingerprintCount = items.size();
        } else {
            fingerprintCount = 0;
        }
        if (fingerprintCount > 0) {
            fingerprintPreference.setSummary(context.getResources().getQuantityString(2131689475, fingerprintCount, new Object[]{Integer.valueOf(fingerprintCount)}));
            clazz = FingerprintSettings.class.getName();
        } else {
            fingerprintPreference.setSummary(2131624647);
            clazz = FingerprintEnrollIntroduction.class.getName();
        }
        fingerprintPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Context context = preference.getContext();
                if (Utils.startQuietModeDialogIfNecessary(context, UserManager.get(context), userId)) {
                    return false;
                }
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", clazz);
                intent.putExtra("android.intent.extra.USER_ID", userId);
                context.startActivity(intent);
                return true;
            }
        });
        return fingerprintPreference;
    }
}

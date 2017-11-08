package com.android.settings.fingerprint;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.ItemUseStat;
import com.android.settings.PrivacySpaceSettingsHelper;
import com.android.settings.Utils;
import com.android.settings.fingerprint.enrollment.HwCustFingerprintEnroll;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.huawei.cust.HwCustUtils;
import java.util.List;

public class FingerprintManageFragment extends Fragment implements OnClickListener {
    private static String TAG = "FingerprintManageFragment";
    private ActionBar mActionBar;
    private Activity mContext;
    private Toast mDuplicatedNameToast;
    private TextView mFingerprintDesc;
    private TextView mFingerprintName;
    private Fingerprint mFp;
    private Button mFpDeleteButton;
    private int mFpId;
    private String mFpName;
    private Button mFpRenameButton;
    private HwCustFingerprintEnroll mHwCustFingerprintEnroll;
    private HwCustFingerprintManageFragment mHwCustFingerprintManageFragment;
    private boolean mIsPayment;
    private AlertDialog mNameAlert;
    private FingerprintRemoveHandler mRemoveHandler = new FingerprintRemoveHandler();
    private Resources mRes;
    int mUserId = UserHandle.myUserId();
    private TextWatcher nameTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            if (FingerprintManageFragment.this.mNameAlert != null) {
                Button positiveBtn = FingerprintManageFragment.this.mNameAlert.getButton(-1);
                if (s.toString().trim().length() == 0 || FingerprintManageFragment.this.isDuplicatedFpName(s.toString().trim())) {
                    positiveBtn.setEnabled(false);
                } else {
                    positiveBtn.setEnabled(true);
                }
            }
        }
    };

    private class FingerprintRemoveHandler extends RemovalCallback {
        DialogInterface innerDialog;

        private FingerprintRemoveHandler() {
        }

        public void onRemovalSucceeded(Fingerprint fingerprint) {
            Log.i(FingerprintManageFragment.TAG, String.format("Fingerprint deleted successfully, fingerprint ID : [%d], fingerprint name : [%s]", new Object[]{Integer.valueOf(fingerprint.getFingerId()), fingerprint.getName()}));
            Uri targetUri = Secure.getUriFor(String.format("fp_index_%d", new Object[]{Integer.valueOf(fingerprint.getFingerId())}));
            if (targetUri != null) {
                Log.d(FingerprintManageFragment.TAG, "delete fp ret = " + FingerprintManageFragment.this.mContext.getContentResolver().delete(targetUri, null, null) + ", id = " + fingerprint.getFingerId() + ", name = " + fingerprint.getName());
            }
            System.putIntForUser(FingerprintManageFragment.this.mContext.getContentResolver(), "fingerprint_alipay_dialog", 1, FingerprintManageFragment.this.mUserId);
            FingerprintUtils.onFingerprintNumChanged(FingerprintManageFragment.this.mContext, FingerprintManageFragment.this.mUserId);
            this.innerDialog.dismiss();
            FingerprintManageFragment.this.finish();
        }

        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
            Log.e(FingerprintManageFragment.TAG, String.format("Fingerprint deleted failed, fingerprint ID : [%d], fingerprint name : [%s], err code : [%d], err message : [%s]", new Object[]{Integer.valueOf(fp.getFingerId()), fp.getName(), Integer.valueOf(errMsgId), errString.toString()}));
            this.innerDialog.dismiss();
            FingerprintManageFragment.this.finish();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mContext = getActivity();
        Bundle bundle = getArguments();
        this.mRes = getResources();
        if (bundle != null) {
            this.mFpId = bundle.getInt("fp_id");
            this.mFpName = bundle.getString("fp_name");
            this.mFp = (Fingerprint) bundle.getParcelable("fp_obj");
            this.mIsPayment = bundle.getBoolean("fp_is_payment", false);
            Log.i(TAG, "onCreate mFpId = " + this.mFpId + ", mFpName = " + this.mFpName + ", isPayment = " + this.mIsPayment);
        }
        Intent intent = this.mContext.getIntent();
        if (intent != null) {
            this.mUserId = intent.getIntExtra("android.intent.extra.USER", UserHandle.myUserId());
        }
        if (this.mFp == null) {
            finish();
        }
        if (Utils.onlySupportPortrait()) {
            this.mContext.setRequestedOrientation(1);
        }
        super.onCreate(savedInstanceState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActionBar = this.mContext.getActionBar();
        if (this.mActionBar != null) {
            this.mActionBar.setDisplayHomeAsUpEnabled(true);
            this.mActionBar.setDisplayUseLogoEnabled(false);
            this.mActionBar.setTitle(this.mRes.getString(2131627679));
        }
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(2130968797, container, false);
        this.mFpRenameButton = (Button) fragmentView.findViewById(2131886633);
        this.mFpDeleteButton = (Button) fragmentView.findViewById(2131886634);
        this.mHwCustFingerprintManageFragment = (HwCustFingerprintManageFragment) HwCustUtils.createObj(HwCustFingerprintManageFragment.class, new Object[]{this});
        if (this.mHwCustFingerprintManageFragment != null && this.mHwCustFingerprintManageFragment.fingerPrintShotcut()) {
            this.mHwCustFingerprintManageFragment.initDiyButton(this.mContext, fragmentView);
        }
        this.mHwCustFingerprintEnroll = (HwCustFingerprintEnroll) HwCustUtils.createObj(HwCustFingerprintEnroll.class, new Object[]{this.mContext});
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHwCustFingerprintEnroll.changeImageView((ImageView) fragmentView.findViewById(2131886630));
        }
        this.mFpRenameButton.setOnClickListener(this);
        this.mFpDeleteButton.setOnClickListener(this);
        this.mFingerprintName = (TextView) fragmentView.findViewById(2131886631);
        this.mFingerprintDesc = (TextView) fragmentView.findViewById(2131886632);
        if (this.mIsPayment) {
            this.mFingerprintDesc.setVisibility(0);
        }
        return fragmentView;
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        this.mFingerprintName.setText(this.mFpName);
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                this.mContext.setResult(0);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 2131886633:
                showRenameDialog();
                return;
            case 2131886634:
                showDeleteDialog();
                return;
            default:
                return;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mHwCustFingerprintManageFragment != null && this.mHwCustFingerprintManageFragment.fingerPrintShotcut()) {
            if (resultCode == -1) {
                ((FingerprintManagementActivity) this.mContext).setResult(-1);
                finish();
            }
            if (resultCode == 101) {
                ((FingerprintManagementActivity) this.mContext).setResult(101);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showDeleteDialog() {
        final BiometricManager bm = BiometricManager.open(getActivity());
        String negativeStr = getResources().getString(2131627676);
        SpannableString negativeSpanText = new SpannableString(negativeStr);
        negativeSpanText.setSpan(new ForegroundColorSpan(-65536), 0, negativeStr.length(), 18);
        Builder builder = new Builder(this.mContext);
        if (isPaymentFingerWarning(bm)) {
            builder.setMessage(String.format(this.mRes.getString(2131628565), new Object[]{this.mFpName}));
        } else {
            UserInfo user = ((UserManager) this.mContext.getSystemService("user")).getUserInfo(UserHandle.myUserId());
            if (PrivacySpaceSettingsHelper.isPrivacyUser(user)) {
                String username = user.name;
                builder.setTitle(String.format(this.mRes.getString(2131628711), new Object[]{username}));
                builder.setMessage(2131628719);
            } else {
                builder.setMessage(String.format(this.mRes.getString(2131627654), new Object[]{this.mFpName}));
            }
        }
        builder.setPositiveButton(negativeSpanText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(FingerprintManageFragment.TAG, "mFpId = " + FingerprintManageFragment.this.mFpId);
                if (FingerprintManageFragment.this.mHwCustFingerprintManageFragment != null && FingerprintManageFragment.this.mHwCustFingerprintManageFragment.fingerPrintShotcut()) {
                    FingerprintManageFragment.this.getActivity().getContentResolver().delete(Secure.getUriFor("FP_" + FingerprintManageFragment.this.mFpId), null, null);
                }
                FingerprintManageFragment.this.mRemoveHandler.innerDialog = dialog;
                bm.removeFingerprint(FingerprintManageFragment.this.mFp, FingerprintManageFragment.this.mRemoveHandler, FingerprintManageFragment.this.mUserId);
                ItemUseStat.getInstance().handleClick(FingerprintManageFragment.this.getActivity(), 2, "delete_fingerprint");
            }
        });
        builder.setNegativeButton(2131627652, null);
        builder.show().getButton(-1).setTextColor(-65536);
    }

    private boolean isPaymentFingerWarning(BiometricManager bm) {
        try {
            int fpId = Secure.getIntForUser(getActivity().getContentResolver(), "fp_shortcut_payment_fp_id", this.mUserId);
            int paymentStatus = Secure.getIntForUser(getActivity().getContentResolver(), "fp_shortcut_enabled", this.mUserId);
            List<Fingerprint> fpList = bm.getFingerprintList(this.mUserId);
            if (fpList.size() == 2) {
                for (Fingerprint finger : fpList) {
                    if (finger.getFingerId() == fpId && fpId != this.mFpId && paymentStatus == 1) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to query shortcut payment finger.");
            e.printStackTrace();
            return false;
        }
    }

    private void showRenameDialog() {
        View renameView = LayoutInflater.from(this.mContext).inflate(2130968924, null);
        TextView tv = (TextView) renameView.findViewById(16908299);
        if (tv != null) {
            tv.setVisibility(8);
        }
        final EditText ev = (EditText) renameView.findViewById(16908291);
        ev.setText(this.mFpName);
        ev.setSelectAllOnFocus(true);
        ev.requestFocus();
        Builder builder = new Builder(this.mContext);
        builder.setTitle(2131627653);
        builder.setView(renameView);
        builder.setPositiveButton(2131627651, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FingerprintManageFragment.this.mFpName = ev.getText().toString().trim();
                FingerprintManageFragment.this.mFingerprintName.setText(FingerprintManageFragment.this.mFpName);
                Log.e(FingerprintManageFragment.TAG, "String.valueOf(mFpId) == " + String.valueOf(FingerprintManageFragment.this.mFpId));
                Log.i(FingerprintManageFragment.TAG, "mFpName == " + FingerprintManageFragment.this.mFpName);
                BiometricManager.open(FingerprintManageFragment.this.getActivity()).renameFingerprint(FingerprintManageFragment.this.mFpId, FingerprintManageFragment.this.mFpName, FingerprintManageFragment.this.mUserId);
                ItemUseStat.getInstance().handleClick(FingerprintManageFragment.this.getActivity(), 2, "rename_fingerprint");
                dialog.dismiss();
                FingerprintManageFragment.this.finish();
            }
        });
        builder.setNegativeButton(2131627652, null);
        AlertDialog alert = builder.create();
        this.mNameAlert = alert;
        ev.addTextChangedListener(this.nameTextWatcher);
        alert.getWindow().setSoftInputMode(5);
        alert.show();
        alert.getButton(-1).setEnabled(false);
    }

    private boolean isDuplicatedFpName(String fpName) {
        if (fpName == null) {
            return false;
        }
        BiometricManager bm = BiometricManager.open(getActivity());
        if (bm != null) {
            List<Fingerprint> fps = bm.getFingerprintList(this.mUserId);
            if (fps.isEmpty()) {
                Log.d(TAG, "No finger prints enrolled");
                bm.abort();
                bm.release();
                return false;
            }
            for (Fingerprint fp : fps) {
                if (fpName.equals(fp.getName())) {
                    if (this.mDuplicatedNameToast != null) {
                        this.mDuplicatedNameToast.cancel();
                    }
                    this.mDuplicatedNameToast = Toast.makeText(this.mContext, 2131627923, 0);
                    this.mDuplicatedNameToast.show();
                    bm.abort();
                    bm.release();
                    return true;
                }
            }
            bm.abort();
            bm.release();
            return false;
        }
        Log.d(TAG, "BiometricManager is null");
        return false;
    }

    private void finish() {
        this.mContext.finish();
    }
}

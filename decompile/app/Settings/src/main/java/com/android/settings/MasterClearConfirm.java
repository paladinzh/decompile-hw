package com.android.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.persistentdata.PersistentDataBlockManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.sdencryption.SdEncryptionUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.huawei.cust.HwCustUtils;

public class MasterClearConfirm extends MasterClearConfirmHwBase {
    private View mContentView;
    private HwCustMasterClear mCustMasterClear;
    private boolean mEraseSdCard;
    private OnClickListener mFinalClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (!Utils.isMonkeyRunning()) {
                if (SdCardLockUtils.isSdCardUnlocked(MasterClearConfirm.this.getActivity())) {
                    SdCardLockUtils.clearSDLockPassword(MasterClearConfirm.this.getActivity());
                }
                boolean isCheckPwdActivityExist = Utils.isPhoneFinderActivityExist(MasterClearConfirm.this.getActivity().getPackageManager(), "com.huawei.remotecontrol.intent.action.PHONEFINDER_CHECKPWD");
                if (Utils.isPhoneFinderEnabled() && Utils.isAntiTheftSupported() && isCheckPwdActivityExist) {
                    MasterClearConfirm.this.startCheckPwdActivity();
                } else {
                    MasterClearConfirm.this.phoneFactoryReset();
                }
            }
        }
    };

    protected void phoneFactoryReset() {
        final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager) getActivity().getSystemService("persistent_data_block");
        if (pdbManager == null || pdbManager.getOemUnlockEnabled() || !Utils.isDeviceProvisioned(getActivity())) {
            doMasterClear();
        } else {
            new AsyncTask<Void, Void, Void>() {
                int mOldOrientation;

                protected Void doInBackground(Void... params) {
                    pdbManager.wipe();
                    return null;
                }

                protected void onPostExecute(Void aVoid) {
                    if (MasterClearConfirm.this.getActivity() != null) {
                        MasterClearConfirm.this.getActivity().setRequestedOrientation(this.mOldOrientation);
                        MasterClearConfirm.this.doMasterClear();
                    }
                }

                protected void onPreExecute() {
                    MasterClearConfirm.this.mProgressDialog = MasterClearConfirm.this.getProgressDialog();
                    MasterClearConfirm.this.mProgressDialog.show();
                    this.mOldOrientation = MasterClearConfirm.this.getActivity().getRequestedOrientation();
                    MasterClearConfirm.this.getActivity().setRequestedOrientation(14);
                }
            }.execute(new Void[0]);
        }
    }

    protected ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getActivity().getString(2131625430));
        progressDialog.setMessage(getActivity().getString(2131625431));
        return progressDialog;
    }

    private void doMasterClear() {
        SdEncryptionUtils.backupSecretKey();
        Context context = getActivity();
        ItemUseStat.getInstance().handleClick(context, 2, "reset_phone_confirm");
        new LockPatternUtils(context).clearLock(UserHandle.myUserId());
        if (this.mEraseInternal) {
            MLog.d("MasterClearConfirmBase", "Erase Internal");
            lowlevelFormat();
            return;
        }
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(268435456);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        getActivity().sendBroadcast(intent);
        MLog.d("MasterClearConfirmBase", "send message MASTER_CLEAR");
    }

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(2131886793).setOnClickListener(this.mFinalClickListener);
        establishFinalConfirmationState(this.mContentView, this.mEraseSdCard);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_factory_reset", UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_factory_reset", UserHandle.myUserId())) {
            return inflater.inflate(2130968867, null);
        }
        if (admin != null) {
            View view = inflater.inflate(2130968617, null);
            ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), view, admin, false);
            view.setVisibility(0);
            return view;
        }
        this.mContentView = inflater.inflate(2130968866, null);
        establishFinalConfirmationState();
        setAccessibilityTitle();
        return this.mContentView;
    }

    private void setAccessibilityTitle() {
        CharSequence currentTitle = getActivity().getTitle();
        TextView confirmationMessage = (TextView) this.mContentView.findViewById(2131886791);
        if (confirmationMessage != null) {
            getActivity().setTitle(Utils.createAccessibleSequence(currentTitle, "," + confirmationMessage.getText()));
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean z;
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            z = args.getBoolean("erase_sd");
        } else {
            z = false;
        }
        this.mEraseSdCard = z;
        this.mCustMasterClear = (HwCustMasterClear) HwCustUtils.createObj(HwCustMasterClear.class, new Object[]{getActivity()});
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.remotecontrol.intent.action.CLOSE_PHONEFINDERRESULT");
        getActivity().registerReceiver(this.mReceiver, filter);
    }

    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(this.mReceiver);
    }

    protected int getMetricsCategory() {
        return 67;
    }
}

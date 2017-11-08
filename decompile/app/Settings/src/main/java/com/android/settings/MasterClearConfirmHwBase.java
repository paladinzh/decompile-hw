package com.android.settings;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;

public class MasterClearConfirmHwBase extends OptionsMenuFragment {
    protected boolean mEraseInternal;
    private boolean mIsBackup = false;
    private boolean mIsMasterClearOngoing;
    protected ProgressDialog mProgressDialog;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("com.huawei.remotecontrol.intent.action.CLOSE_PHONEFINDERRESULT".equals(intent.getAction())) {
                    int result = intent.getIntExtra("result", -1);
                    Log.d("MasterClearConfirm", "phoneFinder broadcast result is:" + String.valueOf(result));
                    if (result == 0) {
                        MasterClearConfirmHwBase.this.phoneFactoryReset();
                    }
                }
            }
        }
    };
    protected StorageManager mStorageManager;

    protected void startCheckPwdActivity() {
        Intent intent = new Intent("com.huawei.remotecontrol.intent.action.PHONEFINDER_CHECKPWD");
        intent.setPackage("com.huawei.hidisk");
        getActivity().startActivity(intent);
    }

    protected void phoneFactoryReset() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isFinal = false;
        if (savedInstanceState != null) {
            isFinal = savedInstanceState.getBoolean("key_has_backup", false);
        }
        this.mIsBackup = isFinal;
        Bundle args = getArguments();
        if (args != null) {
            this.mEraseInternal = args.getBoolean("erase_internal");
            Log.d("MasterClearConfirmBase", "mEraseInternal is: " + this.mEraseInternal);
        }
        this.mStorageManager = (StorageManager) getActivity().getSystemService("storage");
        if (savedInstanceState != null) {
            this.mIsMasterClearOngoing = savedInstanceState.getBoolean("isMasterClearOngoing");
        }
    }

    public void onStart() {
        super.onStart();
        if (this.mIsMasterClearOngoing) {
            this.mProgressDialog = getProgressDialog();
            if (this.mProgressDialog != null) {
                this.mProgressDialog.show();
            }
        }
    }

    protected ProgressDialog getProgressDialog() {
        return this.mProgressDialog;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("key_has_backup", this.mIsBackup);
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            outState.putBoolean("isMasterClearOngoing", true);
        }
        super.onSaveInstanceState(outState);
    }

    public void onStop() {
        super.onStop();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
    }

    public void establishFinalConfirmationState(View mContentView, boolean mEraseSdCard) {
    }

    protected void lowlevelFormat() {
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
        Log.d("MasterClearConfirmBase", "send message of wipe data low level");
        getActivity().sendBroadcast(intent);
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}

package com.android.contacts.hap.rcs;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;

public class RcsCLIRBroadCastHelper {
    private static volatile RcsCLIRBroadCastHelper sRcsCLIRBroadCastHelper = null;
    private int mClirMode;
    private Context mContext;
    private boolean mIsException;
    private RcsCLIRStatusReceiver mRcsCLIRStatusReceiver = null;

    public class RcsCLIRStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                RcsCLIRBroadCastHelper.this.mIsException = intent.getExtras().getBoolean("isException");
                RcsCLIRBroadCastHelper.this.mClirMode = intent.getExtras().getInt("clirMode");
                HwLog.i("RcsCLIRBroadCastHelper", "clirMode");
            }
        }
    }

    private RcsCLIRBroadCastHelper(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized RcsCLIRBroadCastHelper getInstance(Context context) {
        RcsCLIRBroadCastHelper rcsCLIRBroadCastHelper;
        synchronized (RcsCLIRBroadCastHelper.class) {
            if (sRcsCLIRBroadCastHelper == null) {
                sRcsCLIRBroadCastHelper = new RcsCLIRBroadCastHelper(context);
            }
            rcsCLIRBroadCastHelper = sRcsCLIRBroadCastHelper;
        }
        return rcsCLIRBroadCastHelper;
    }

    public void registerRcsCLIRReceiver(Context context) {
        if (this.mRcsCLIRStatusReceiver == null) {
            this.mRcsCLIRStatusReceiver = new RcsCLIRStatusReceiver();
        }
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction("com.huawei.rcs.service.clir");
        if (context != null) {
            context.registerReceiver(this.mRcsCLIRStatusReceiver, statusFilter, permission.HW_CONTACTS_ALL, null);
        }
    }

    public void unRegisterRcsCLIRReceiver(Context context) {
        if (this.mRcsCLIRStatusReceiver != null && context != null) {
            context.unregisterReceiver(this.mRcsCLIRStatusReceiver);
        }
    }

    public void getRcsCLIRStatus() {
        if (!this.mIsException) {
            sendCLIRBroadcast(0);
        }
    }

    public boolean isCLIROpen() {
        if (this.mIsException || !RcsContactsUtils.isSupportCLIR()) {
            return false;
        }
        switch (this.mClirMode) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return false;
            default:
                return false;
        }
    }

    public void showDialog(final Context context) {
        if (context != null) {
            Builder builder = new Builder(context);
            builder.setTitle(R.string.CLIR_Dialog_Title);
            builder.setPositiveButton(R.string.CLIR_set, new OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    context.startActivity(RcsCLIRBroadCastHelper.this.getGsmUmtsAdditionalCallOptionsIntent());
                }
            });
            builder.setNegativeButton(R.string.CLIR_cancel, null);
            builder.create().show();
        }
    }

    private void sendCLIRBroadcast(int operation) {
        Intent intent = new Intent();
        intent.setAction("com.huawei.rcs.contacts.clir");
        intent.putExtra("OPERATION", operation);
        HwLog.i("RcsCLIRBroadCastHelper", "query CLIR status");
        this.mContext.sendBroadcast(intent, permission.HW_CONTACTS_ALL);
    }

    private Intent getGsmUmtsAdditionalCallOptionsIntent() {
        int subId = SimFactoryManager.getUserDefaultSubscription();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName("com.android.phone", "com.android.phone.GsmUmtsAdditionalCallOptions");
        if (SimFactoryManager.isDualSim()) {
            intent.putExtra("subscription", subId);
        }
        intent.setFlags(67108864);
        return intent;
    }
}

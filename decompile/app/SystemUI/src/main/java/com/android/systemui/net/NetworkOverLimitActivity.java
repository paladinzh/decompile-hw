package com.android.systemui.net;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.INetworkPolicyManager.Stub;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.systemui.R;

public class NetworkOverLimitActivity extends Activity {
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final NetworkTemplate template = (NetworkTemplate) getIntent().getParcelableExtra("android.net.NETWORK_TEMPLATE");
        Builder builder = new Builder(this);
        builder.setTitle(getLimitedDialogTitleForTemplate(template));
        builder.setMessage(R.string.data_usage_disabled_dialog);
        builder.setPositiveButton(17039370, null);
        builder.setNegativeButton(R.string.data_usage_disabled_dialog_enable, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                NetworkOverLimitActivity.this.snoozePolicy(template);
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                NetworkOverLimitActivity.this.finish();
            }
        });
        dialog.show();
    }

    private void snoozePolicy(NetworkTemplate template) {
        try {
            Stub.asInterface(ServiceManager.getService("netpolicy")).snoozeLimit(template);
        } catch (RemoteException e) {
            Log.w("NetworkOverLimitActivity", "problem snoozing network policy", e);
        }
    }

    private static int getLimitedDialogTitleForTemplate(NetworkTemplate template) {
        switch (template.getMatchRule()) {
            case 1:
                return R.string.data_usage_disabled_dialog_mobile_title;
            case 2:
                return R.string.data_usage_disabled_dialog_3g_title;
            case 3:
                return R.string.data_usage_disabled_dialog_4g_title;
            default:
                return R.string.data_usage_disabled_dialog_title;
        }
    }
}

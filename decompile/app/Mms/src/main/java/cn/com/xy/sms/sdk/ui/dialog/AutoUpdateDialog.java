package cn.com.xy.sms.sdk.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.google.android.gms.R;

public class AutoUpdateDialog {
    public static void show(final Context ctx) {
        if (!(ctx instanceof Activity) || ((Activity) ctx).isFinishing()) {
            SmartSmsSdkUtil.smartSdkExceptionLog("AutoUpdateDialog show error:Context is not Activity or Activity is finishing", null);
            return;
        }
        AlertDialog dialog = new Builder(ctx).setCancelable(false).setTitle(R.string.duoqu_pre_version_update_2).setMessage(R.string.duoqu_update_dialog_content).setPositiveButton(R.string.duoqu_set, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                UpdateTypeDialog.showDialog(ctx, null);
            }
        }).setNegativeButton(R.string.duoqu_setting_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}

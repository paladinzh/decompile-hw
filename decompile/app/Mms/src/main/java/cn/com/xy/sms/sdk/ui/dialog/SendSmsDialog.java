package cn.com.xy.sms.sdk.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkDoAction;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.transaction.SmsMessageSender;
import com.google.android.gms.R;

public class SendSmsDialog {
    public static final String TAG = "SendSmsDialog";

    private static View createContentView(Context ctx) {
        View contentView = LayoutInflater.from(ctx).inflate(R.layout.duoqu_remind_dialog, null);
        ((TextView) contentView.findViewById(R.id.tv_dialog_content)).setText(R.string.duoqu_send_sms_remind_content_2);
        return contentView;
    }

    public static void show(final Context ctx, final String phoneNum, final String sms, final int simIndex) {
        if (!(ctx instanceof Activity) || ((Activity) ctx).isFinishing()) {
            SmartSmsSdkUtil.smartSdkExceptionLog("AutoUpdateDialog show error:Context is not Activity or Activity is finishing", null);
            return;
        }
        AlertDialog dialog = new Builder(ctx).setCancelable(false).setTitle(R.string.duoqu_send_sms_remind_title).setPositiveButton(R.string.duoqu_agree, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    new SmsMessageSender(ctx, new String[]{phoneNum}, sms, 0, simIndex).sendMessage(0);
                } catch (Exception e) {
                    Log.i(SendSmsDialog.TAG, "SmartsmsMenus send sms error:" + e.getMessage());
                }
                SmartSmsSdkUtil.setSendSmsNoRemind(ctx, ((CheckBox) ((Dialog) dialog).findViewById(R.id.ckb_remind)).isChecked());
                dialog.dismiss();
                SmartSmsSdkDoAction.forwardToComposeMessage(ctx, phoneNum);
            }
        }).setNegativeButton(R.string.duoqu_setting_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(createContentView(ctx));
        dialog.show();
    }
}

package cn.com.xy.sms.sdk.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.google.android.gms.R;

public class GotoMyPositionDialog {
    public static final int TYPE_AGREE = 1;
    public static final int TYPE_CANCEL = 0;

    private static View createContentView(Context ctx) {
        View contentView = LayoutInflater.from(ctx).inflate(R.layout.duoqu_position_dialog, null);
        ((TextView) contentView.findViewById(R.id.tv_dialog_content)).setText(R.string.duoqu_open_position_content);
        return contentView;
    }

    public static void show(Context ctx, final XyCallBack callBack) {
        if (!(ctx instanceof Activity) || ((Activity) ctx).isFinishing()) {
            SmartSmsSdkUtil.smartSdkExceptionLog("GotoMyPositionDialog show error:Context is not Activity or Activity is finishing", null);
            return;
        }
        AlertDialog dialog = new Builder(ctx).setCancelable(false).setPositiveButton(R.string.yes, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                if (callBack != null) {
                    callBack.execute(Integer.valueOf(1));
                }
            }
        }).setNegativeButton(R.string.duoqu_setting_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                if (callBack != null) {
                    callBack.execute(Integer.valueOf(0));
                }
            }
        }).create();
        dialog.setView(createContentView(ctx));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}

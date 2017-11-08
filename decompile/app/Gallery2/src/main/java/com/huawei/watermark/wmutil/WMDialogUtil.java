package com.huawei.watermark.wmutil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.gallery3d.R;

public class WMDialogUtil {
    private static AlertDialog setDailogMessage(int saveButtonResId, int cancelButtonResId, int titleSourceId, final Runnable positiveRunable, final Runnable negativeRunable, final Runnable cancelRunable, Builder alertDialog) {
        if (titleSourceId != -1) {
            alertDialog.setTitle(alertDialog.getContext().getResources().getText(titleSourceId));
        }
        if (saveButtonResId == 0) {
            saveButtonResId = 17039370;
        }
        alertDialog.setPositiveButton(saveButtonResId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (positiveRunable != null) {
                    positiveRunable.run();
                }
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (cancelRunable != null) {
                    cancelRunable.run();
                }
            }
        }).setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == 25 || keyCode == 24) {
                    return true;
                }
                return false;
            }
        });
        if (negativeRunable != null) {
            if (cancelButtonResId == 0) {
                cancelButtonResId = 17039360;
            }
            alertDialog.setNegativeButton(cancelButtonResId, new OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    if (negativeRunable != null) {
                        negativeRunable.run();
                    }
                    dialog.cancel();
                }
            });
        }
        return alertDialog.show();
    }

    public static AlertDialog showWaterMarkDialog(Activity activity, Runnable positiveRunable, Runnable negativeRunable) {
        Builder alertDialog = new Builder(activity);
        View view = LayoutInflater.from(alertDialog.getContext()).inflate(WMResourceUtil.getLayoutId(activity, "wm_jar_dialog_message_layout"), (ViewGroup) activity.findViewById(WMResourceUtil.getId(activity, "dailog_layout")));
        if (view == null) {
            return null;
        }
        alertDialog.setView(view);
        TextView content = (TextView) view.findViewById(WMResourceUtil.getId(activity, "message_content"));
        if (content != null) {
            content.setText(R.string.water_mark_network_location_dialog_message);
        }
        TextView description = (TextView) view.findViewById(WMResourceUtil.getId(activity, "message_description"));
        if (description != null) {
            description.setText(R.string.water_mark_network_dialog_description);
        }
        alertDialog.setView(view);
        alertDialog.setCancelable(false);
        return setDailogMessage(R.string.water_mark_network_attention_dialog_allow, R.string.water_mark_network_attention_dialog_cancel, -1, positiveRunable, negativeRunable, negativeRunable, alertDialog);
    }
}

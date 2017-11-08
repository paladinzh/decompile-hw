package com.huawei.permissionmanager.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;

public class DialogCommon {
    private static final String LOG_TAG = "DialogCommon";

    public static AlertDialog createRecommendConfirmDialog(Context context, final RecommendCallBack callBack) {
        RecommendCallBack callback = callBack;
        View confirmLayout = LayoutInflater.from(context).inflate(R.layout.recommend_confirm_dialog, null);
        final CheckBox checkBox = (CheckBox) confirmLayout.findViewById(R.id.remind_checkbox);
        checkBox.setChecked(true);
        Builder builder = new Builder(context);
        builder.setTitle(R.string.Recommend_Dialog_title);
        builder.setView(confirmLayout);
        builder.setPositiveButton(R.string.Recommend_Dialog_confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HsmStat.statE(Events.E_PERMISSION_RECOMMEND_CLICK);
                if (callBack != null) {
                    boolean recommendStatus = checkBox.isChecked();
                    HwLog.d(DialogCommon.LOG_TAG, "The recommendStatus is: " + recommendStatus);
                    callBack.callBackUserConfirm(recommendStatus);
                }
            }
        });
        builder.setNegativeButton(R.string.Recommend_Dialog_cancel, null);
        return builder.show();
    }

    public static AlertDialog createRecommendWaitDialog(Context context) {
        View confirmLayout = LayoutInflater.from(context).inflate(R.layout.recommend_confirm_dialog, null);
        ((CheckBox) confirmLayout.findViewById(R.id.remind_checkbox)).setChecked(true);
        Builder builder = new Builder(context);
        builder.setTitle(R.string.Recommend_Dialog_title);
        builder.setView(confirmLayout);
        builder.setPositiveButton(R.string.Recommend_Dialog_cancel, null);
        builder.setNegativeButton(R.string.Recommend_Dialog_confirm, null);
        builder.setCancelable(false);
        return builder.show();
    }
}

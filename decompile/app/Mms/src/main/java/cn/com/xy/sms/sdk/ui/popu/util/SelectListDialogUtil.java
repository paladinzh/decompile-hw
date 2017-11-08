package cn.com.xy.sms.sdk.ui.popu.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.xy.sms.sdk.ui.popu.widget.AdapterDataSource;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquDialogSelected;
import cn.com.xy.sms.sdk.ui.popu.widget.SelectDataAdapter;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.Map.Entry;

public class SelectListDialogUtil {
    private static boolean mHavePopupDialog = false;

    public static OnClickListener showSelectListDialogClickListener(Context context, String dialogTitle, String confirmText, String caneclText, AdapterDataSource adapterDataSource, DuoquDialogSelected selected, SdkCallBack callBack) {
        if (context == null || adapterDataSource == null || adapterDataSource.getDataSrouce() == null || adapterDataSource.getDataSrouce().length() == 0) {
            return null;
        }
        final Context context2 = context;
        final String str = dialogTitle;
        final String str2 = confirmText;
        final String str3 = caneclText;
        final AdapterDataSource adapterDataSource2 = adapterDataSource;
        final DuoquDialogSelected duoquDialogSelected = selected;
        final SdkCallBack sdkCallBack = callBack;
        return new OnClickListener() {
            @SuppressLint({"NewApi"})
            public void onClick(View arg0) {
                if (!SelectListDialogUtil.mHavePopupDialog) {
                    SelectListDialogUtil.mHavePopupDialog = true;
                    try {
                        SelectListDialogUtil.selectListDialog(context2, str, str2, str3, new SelectDataAdapter(context2, adapterDataSource2, duoquDialogSelected), sdkCallBack).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
    }

    @SuppressLint({"NewApi"})
    private static AlertDialog selectListDialog(Context context, String dialogTitle, String confirmText, String caneclText, final SelectDataAdapter dataAdapter, final SdkCallBack callBack) {
        return new Builder(context).setIconAttribute(16843605).setTitle(dialogTitle).setAdapter(dataAdapter, null).setPositiveButton(confirmText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SelectListDialogUtil.selectedDataCallBack(dataAdapter, callBack);
                dialog.dismiss();
            }
        }).setNegativeButton(caneclText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                SelectListDialogUtil.mHavePopupDialog = false;
            }
        }).create();
    }

    private static void selectedDataCallBack(SelectDataAdapter dataAdapter, SdkCallBack callBack) {
        for (Entry<String, Boolean> entry : dataAdapter.mCheckedStates.entrySet()) {
            if (((Boolean) entry.getValue()).equals(Boolean.valueOf(true))) {
                try {
                    int index = Integer.parseInt((String) entry.getKey());
                    ContentUtil.callBackExecute(callBack, dataAdapter.getItem(index), Integer.valueOf(index), dataAdapter.getDisplayValue(index));
                    return;
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}

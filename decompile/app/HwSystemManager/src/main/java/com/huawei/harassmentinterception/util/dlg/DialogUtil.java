package com.huawei.harassmentinterception.util.dlg;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.huawei.harassmentinterception.callback.CheckRestoreSMSCallBack;
import com.huawei.harassmentinterception.callback.ClickConfirmCallBack;
import com.huawei.systemmanager.R;

public class DialogUtil {
    public static AlertDialog createDlgWithRestoreSMS(Context context, String[] uiDesc, final CheckRestoreSMSCallBack sender, boolean isMarked) {
        CheckRestoreSMSCallBack callback = sender;
        View layout = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.interception_remove_blacklist_dialog, null);
        final CheckBox checkBox = (CheckBox) layout.findViewById(R.id.recover_alllog_checkbox);
        ((TextView) layout.findViewById(R.id.recover_alllog_msg)).setText(uiDesc[0]);
        Builder dlgBuilder = new Builder(context);
        dlgBuilder.setTitle(uiDesc[1]);
        dlgBuilder.setView(layout);
        dlgBuilder.setPositiveButton(uiDesc[2], new OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                sender.onCheckRestoreSMSButton(checkBox.isChecked());
            }
        });
        dlgBuilder.setNegativeButton(context.getResources().getString(R.string.harassmentInterception_cancel), null);
        AlertDialog dlg = dlgBuilder.show();
        if (isMarked) {
            dlg.getButton(-1).setTextColor(context.getResources().getColor(R.color.hsm_forbidden));
        }
        return dlg;
    }

    public static AlertDialog createAddSingleWhiteListDlgFromMSG(Context context, String info, CheckRestoreSMSCallBack sender) {
        String message = context.getResources().getString(R.string.harassmentAddWhiteListDlgMsg);
        String btnText = context.getResources().getString(R.string.harassmentInterception_confirm);
        return createDlgWithRestoreSMS(context, new String[]{message, info, btnText}, sender, false);
    }

    public static AlertDialog createAddWhiteListDlg(Context context, CheckRestoreSMSCallBack sender) {
        String message = context.getResources().getString(R.string.harassmentWhitelist_DlgMsg);
        String title = context.getResources().getString(R.string.harassmentWhitelist_DlgTitle);
        String btnText = context.getResources().getString(R.string.harassmentInterception_confirm);
        return createDlgWithRestoreSMS(context, new String[]{message, title, btnText}, sender, false);
    }

    public static AlertDialog createRemoveBlacklistDlg(Context context, CheckRestoreSMSCallBack sender) {
        String message = context.getResources().getString(R.string.harassmentRemoveBlacklist_DlgMsg);
        String title = context.getResources().getString(R.string.harassmentRemoveBlacklist_DlgTitle);
        String btnText = context.getResources().getString(R.string.Remove);
        return createDlgWithRestoreSMS(context, new String[]{message, title, btnText}, sender, true);
    }

    public static AlertDialog createDlg(Context context, String[] uiDesc, final ClickConfirmCallBack sender, boolean isMarked) {
        Builder alertDialog = new Builder(context);
        alertDialog.setTitle(uiDesc[1]);
        alertDialog.setMessage(uiDesc[0]);
        alertDialog.setNegativeButton(context.getResources().getString(R.string.harassment_cancle), null);
        alertDialog.setPositiveButton(uiDesc[2], new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sender.onClickConfirmButton();
            }
        });
        AlertDialog dlg = alertDialog.show();
        if (isMarked) {
            dlg.getButton(-1).setTextColor(context.getResources().getColor(R.color.hsm_forbidden));
        }
        return dlg;
    }

    public static AlertDialog createSingleWhitelistDlgFromCallLog(Context context, String info, ClickConfirmCallBack sender) {
        String message = context.getResources().getString(R.string.harassmentAddWhiteListDlgMsg);
        String btnText = context.getResources().getString(R.string.harassmentInterception_confirm);
        return createDlg(context, new String[]{message, info, btnText}, sender, false);
    }

    public static AlertDialog createRemoveWhitlistDlgIfContactExist(Context context, String info, ClickConfirmCallBack sender) {
        String message = context.getResources().getString(R.string.harassmentRemoveWhitelist_DlgMsg_InContact);
        String btnText = context.getResources().getString(R.string.Remove);
        return createDlg(context, new String[]{message, info, btnText}, sender, true);
    }

    public static AlertDialog createRemoveWhitlistDlgIfContactNotExist(Context context, String info, ClickConfirmCallBack sender) {
        String message = context.getResources().getString(R.string.harassmentRemoveWhitelist_DlgMsg_NotInContact);
        String btnText = context.getResources().getString(R.string.Remove);
        return createDlg(context, new String[]{message, info, btnText}, sender, true);
    }

    public static AlertDialog createRemoveWhitlistDlg(Context context, ClickConfirmCallBack sender) {
        String message = context.getResources().getString(R.string.harassmentRemoveWhitelist_DlgMsg);
        String title = context.getResources().getString(R.string.harassmentRemoveWhitelist_DlgTitle);
        String btnText = context.getResources().getString(R.string.Remove);
        return createDlg(context, new String[]{message, title, btnText}, sender, true);
    }
}

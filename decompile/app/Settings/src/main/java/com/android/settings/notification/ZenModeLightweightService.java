package com.android.settings.notification;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings.Global;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ZenModeLightweightService extends Service {
    private boolean isDialogShow = false;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 0;
        }
        boolean enable = intent.getIntExtra("PreventModechange", 0) == 1;
        if (!this.isDialogShow) {
            showConfirmationDialog(enable, getApplicationContext());
            this.isDialogShow = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showConfirmationDialog(boolean on, final Context context) {
        context.setTheme(getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        Builder builder = new Builder(context);
        View ringView = LayoutInflater.from(context).inflate(2130969290, null);
        builder.setView(ringView);
        builder.setTitle(getResources().getString(2131627443));
        builder.setMessage(String.format(getResources().getString(2131628889), new Object[0]));
        builder.setPositiveButton(getResources().getString(2131626103), new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                NotificationManager.from(context).setZenMode(Global.getInt(ZenModeLightweightService.this.getContentResolver(), "zen_mode_last_choosen", 3), null, "ZenModeLightweightService");
            }
        });
        builder.setNegativeButton(2131624572, null);
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                ZenModeLightweightService.this.stopSelf();
                ZenModeLightweightService.this.isDialogShow = false;
            }
        });
        ((CheckBox) ringView.findViewById(2131887603)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Global.putInt(ZenModeLightweightService.this.getContentResolver(), "zen_mode_change_do_not_ask", isChecked ? 1 : 0);
            }
        });
    }
}

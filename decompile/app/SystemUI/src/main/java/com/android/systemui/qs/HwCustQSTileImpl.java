package com.android.systemui.qs;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManager;
import com.android.systemui.R;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.badgedicon.BadgedIconHelper;
import fyusion.vislib.BuildConfig;

public class HwCustQSTileImpl extends HwCustQSTile {
    private static final boolean EE_IS_SHOW = SystemProperties.getBoolean("ro.config.hw_is_ee_show_n", false);
    private static final boolean IS_ATT;
    private static final int VOVIFI_REGISTERED = 1;
    protected final String TAG = "HwCustQSTileImpl";
    private SystemUIDialog mConfirmation = null;

    static {
        boolean equals;
        if ("07".equals(SystemProperties.get("ro.config.hw_opta"))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb"));
        } else {
            equals = false;
        }
        IS_ATT = equals;
    }

    public HwCustQSTileImpl(QSTile parent) {
        super(parent);
    }

    public boolean hasCustomForClick() {
        return IS_ATT;
    }

    public void requestStateClick(Context context, boolean isEnable) {
        showConfirmationDialog(context, isEnable);
    }

    private void showConfirmationDialog(Context context, final boolean isEnable) {
        if (this.mConfirmation == null) {
            boolean isShowDialog = false;
            CharSequence title = BuildConfig.FLAVOR;
            CharSequence message = BuildConfig.FLAVOR;
            if ((this.mParent instanceof HotspotTile) && !isEnable) {
                title = context.getString(R.string.shortcuts_open_hotspot_dialog_title);
                message = context.getString(R.string.shortcuts_open_hotspot_dialog_message);
                isShowDialog = true;
            }
            if (isShowDialog) {
                SystemUIDialog dialog = new SystemUIDialog(context);
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setShowForAllUsers(true);
                dialog.setNegativeButton(17039360, null);
                dialog.setPositiveButton(17039370, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HwCustQSTileImpl.this.mParent.setNewState(isEnable);
                    }
                });
                dialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        HwCustQSTileImpl.this.mConfirmation = null;
                    }
                });
                dialog.show();
                this.mConfirmation = dialog;
            } else {
                this.mParent.setNewState(isEnable);
            }
        }
    }

    private boolean isShowNotification() {
        int imsDomian = HwTelephonyManager.getDefault().getImsDomain();
        HwLog.i("HwCustQSTileImpl", "imsDomian " + imsDomian);
        if (EE_IS_SHOW && 1 == imsDomian) {
            return true;
        }
        return false;
    }

    public void showNotificationForVowifi(Context mContext) {
        if (isShowNotification() && mContext != null) {
            ((NotificationManager) mContext.getSystemService("notification")).notify(R.drawable.ic_notify_vowifi, new Builder(mContext).setTicker(mContext.getString(R.string.vowifi_disconnected_title)).setContentTitle(mContext.getString(R.string.vowifi_disconnected_title)).setContentText(mContext.getString(R.string.vowifi_disconnected_text)).setWhen(System.currentTimeMillis()).setVisibility(1).setSmallIcon(BadgedIconHelper.getBitampIcon(mContext, R.drawable.ic_notify_vowifi)).setAutoCancel(true).build());
        }
    }
}

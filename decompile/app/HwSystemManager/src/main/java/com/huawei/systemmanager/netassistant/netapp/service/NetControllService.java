package com.huawei.systemmanager.netassistant.netapp.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.NetWorkMgr;
import com.huawei.systemmanager.netassistant.NetAppConst;
import com.huawei.systemmanager.netassistant.NetAssistantManager;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.List;

public class NetControllService extends Service {
    public static final String LOG_TAG = "NetControllService";
    private Context mContext;
    private int mLastUid;
    private int mNetworkType = 1;
    SharedPreferences mNoteFlagsShared;
    private AlertDialog mPromptViewDialog = null;

    public void onCreate() {
        super.onCreate();
        this.mContext = GlobalContext.getContext();
        this.mNoteFlagsShared = this.mContext.getSharedPreferences("note_preferences", 4);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 1;
        }
        int uid = intent.getIntExtra("uid", -1);
        int pid = intent.getIntExtra(NetAppConst.EXT_PID, -1);
        int networkType = intent.getIntExtra(NetAppConst.EXT_NETWORKTYPE, -1);
        if (uid >= 0 && networkType >= 0) {
            if (this.mLastUid == uid && isDialogShowing()) {
                return 1;
            }
            showDialog(uid, pid, networkType);
        }
        this.mLastUid = uid;
        return 1;
    }

    private boolean isDialogShowing() {
        if (this.mPromptViewDialog == null || !this.mPromptViewDialog.isShowing()) {
            return false;
        }
        HwLog.i("NetControllService", "uid: " + this.mLastUid + "  is showing dialog ");
        return true;
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void showDialog(final int uid, int pid, final int type) {
        Builder dialogBuilder = new Builder(this.mContext);
        dialogBuilder.setPositiveButton(R.string.open_permission, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                HsmStat.statNetworkAllowDialog("ad", NetControllService.this.mNetworkType);
                NetAssistantManager.setNetworkAccessInState(uid, type, true);
                if (NetControllService.this.mNoteFlagsShared != null) {
                    String key = String.valueOf(uid);
                    Editor editor = NetControllService.this.mNoteFlagsShared.edit();
                    editor.remove(key);
                    editor.commit();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.close_permission, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                HsmStat.statNetworkAllowDialog(NetWorkMgr.VALUE_CLICK_CANCEL_IN_DIALOG, NetControllService.this.mNetworkType);
            }
        });
        this.mPromptViewDialog = dialogBuilder.create();
        View contentView = this.mPromptViewDialog.getLayoutInflater().inflate(R.layout.network_alert_dialog, null);
        this.mPromptViewDialog.setView(contentView);
        CheckBox cb = (CheckBox) contentView.findViewById(R.id.chk);
        cb.setChecked(false);
        TextView message = (TextView) contentView.findViewById(R.id.alert_message);
        String label = getAppLabel(uid, pid);
        if (type == 1) {
            message.setText(String.format(this.mContext.getString(R.string.network_mobile_message_new), new Object[]{label}));
            this.mNetworkType = 1;
        } else if (type == 2) {
            message.setText(String.format(this.mContext.getString(R.string.network_wifi_message_new), new Object[]{label}));
            this.mNetworkType = 2;
        }
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (NetControllService.this.mNoteFlagsShared != null) {
                    String key = String.valueOf(uid);
                    String mNoteFlags = NetControllService.this.mNoteFlagsShared.getString(key, "");
                    Editor editor = NetControllService.this.mNoteFlagsShared.edit();
                    if (isChecked && mNoteFlags.isEmpty()) {
                        editor.putString(key, String.valueOf(key));
                        editor.commit();
                    } else if (!isChecked && !mNoteFlags.isEmpty()) {
                        editor.remove(key);
                        editor.commit();
                    }
                }
            }
        });
        this.mPromptViewDialog.getWindow().setType(2003);
        this.mPromptViewDialog.show();
        HsmStat.statNetworkAllowDialog("d", this.mNetworkType);
    }

    private String getAppLabel(int uid, int pid) {
        String appLabel = getProcessAppLabel(pid);
        if (TextUtils.isEmpty(appLabel)) {
            appLabel = getFirstAppLabel(uid);
        }
        return trimAppLabel(appLabel);
    }

    private String getProcessAppLabel(int pid) {
        List<RunningAppProcessInfo> infos = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (HsmCollections.isEmpty(infos)) {
            return null;
        }
        Object pkgName = null;
        for (RunningAppProcessInfo info : infos) {
            if (info.pid == pid) {
                pkgName = info.processName;
                break;
            }
        }
        String appLabel = null;
        if (!TextUtils.isEmpty(pkgName)) {
            appLabel = HsmPackageManager.getInstance().getLabel(pkgName);
        }
        return appLabel;
    }

    private String getFirstAppLabel(int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        String[] pkgNames = pm.getPackagesForUid(uid);
        if (pkgNames == null || pkgNames.length <= 0) {
            return null;
        }
        String appLabel = null;
        try {
            ApplicationInfo mApp = pm.getApplicationInfo(pkgNames[0], 0);
            if (mApp != null) {
                appLabel = mApp.loadLabel(pm).toString();
            }
        } catch (NameNotFoundException e) {
            HwLog.e("NetControllService", "exception in get first app appName");
        }
        return appLabel;
    }

    private String trimAppLabel(String appLabel) {
        if (TextUtils.isEmpty(appLabel)) {
            return appLabel;
        }
        return appLabel.replaceAll("\\s", "");
    }
}

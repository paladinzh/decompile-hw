package com.huawei.systemmanager.spacecleanner.uninstall;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.widget.OneKeyCleanActivity;

public class UninstalledAppService extends Service {
    private static final String TAG = "UninstalledAppService";

    private class CreateMsgTask extends AsyncTask<Void, Void, Void> {
        private Intent intent;
        private String mMsgBody;
        private String[] mPathes;

        public CreateMsgTask(Intent intent) {
            this.intent = intent;
        }

        protected Void doInBackground(Void... params) {
            this.mPathes = this.intent.getStringArrayExtra("pathes");
            String name = this.intent.getStringExtra("name");
            String appTrashMsg = TrashPubApi.getAppTrashInfo(GlobalContext.getContext(), this.mPathes);
            this.mMsgBody = UninstalledAppService.this.getString(R.string.trash_uninstall_phone_files, new Object[]{name, appTrashMsg});
            return null;
        }

        protected void onPostExecute(Void params) {
            if (this.mPathes == null || TextUtils.isEmpty(this.mMsgBody)) {
                HwLog.i(UninstalledAppService.TAG, "onPostExecute , arg is wrong");
            } else {
                UninstalledAppService.this.showUninstalledAppDialog(this.mMsgBody, this.mPathes);
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.i(TAG, "onStartCommand");
        if (intent == null) {
            return 0;
        }
        new CreateMsgTask(intent).execute(new Void[0]);
        return super.onStartCommand(intent, flags, startId);
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    private void showUninstalledAppDialog(String body, final String[] pathes) {
        HwLog.i(TAG, "showUninstalledAppDialog start");
        setTheme(getResources().getIdentifier(OneKeyCleanActivity.EMUI_THEME, null, null));
        Builder build = new Builder(this);
        build.setTitle(R.string.common_dialog_title_tip);
        build.setMessage(body);
        build.setPositiveButton(R.string.delete_rightnow, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HwLog.i(UninstalledAppService.TAG, "showUninstalledAppDialog on positive button click");
                final String[] strArr = pathes;
                new Thread("Uninstalled_app_delete_file_thread") {
                    public void run() {
                        for (String appTrashRootDir : strArr) {
                            FileUtil.deleteFile(appTrashRootDir);
                        }
                    }
                }.start();
            }
        });
        build.setNegativeButton(R.string.temporary_notdelete, null);
        AlertDialog dialog = build.create();
        dialog.getWindow().setType(2003);
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                HwLog.i(UninstalledAppService.TAG, "showUninstalledAppDialog on dismiss");
                UninstalledAppService.this.stopSelf();
            }
        });
        dialog.show();
    }
}

package com.huawei.systemmanager.spacecleanner.uninstall;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class UninstalledAppTrashDialog extends HsmActivity {
    private AlertDialog mDialog;

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String name = intent.getStringExtra("name");
        final String[] pathes = intent.getStringArrayExtra("pathes");
        String appTrashMsg = TrashPubApi.getAppTrashInfo(this, pathes);
        String body = getString(R.string.trash_uninstall_sdcard_files, new Object[]{name, appTrashMsg});
        Builder build = new Builder(this);
        build.setTitle(R.string.common_dialog_title_tip);
        build.setMessage(body);
        build.setPositiveButton(R.string.delete_rightnow, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
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
        this.mDialog = build.create();
        this.mDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                UninstalledAppTrashDialog.this.finish();
            }
        });
        this.mDialog.show();
    }
}

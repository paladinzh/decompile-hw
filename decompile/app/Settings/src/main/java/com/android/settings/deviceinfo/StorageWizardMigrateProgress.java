package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.MoveCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class StorageWizardMigrateProgress extends StorageWizardBase {
    private final MoveCallback mCallback = new MoveCallback() {
        public void onStatusChanged(int moveId, int status, long estMillis) {
            if (StorageWizardMigrateProgress.this.mMoveId == moveId) {
                Context context = StorageWizardMigrateProgress.this;
                if (PackageManager.isMoveStatusFinished(status)) {
                    Log.d("StorageSettings", "Finished with status " + status);
                    if (status != -100) {
                        Toast.makeText(context, StorageWizardMigrateProgress.this.getString(2131625681), 1).show();
                    } else if (StorageWizardMigrateProgress.this.mDisk != null) {
                        Intent finishIntent = new Intent("com.android.systemui.action.FINISH_WIZARD");
                        finishIntent.addFlags(1073741824);
                        StorageWizardMigrateProgress.this.sendBroadcast(finishIntent);
                        if (!StorageWizardMigrateProgress.this.isFinishing()) {
                            Intent intent = new Intent(context, StorageWizardReady.class);
                            intent.putExtra("android.os.storage.extra.DISK_ID", StorageWizardMigrateProgress.this.mDisk.getId());
                            StorageWizardMigrateProgress.this.startActivity(intent);
                        }
                    }
                    StorageWizardMigrateProgress.this.finishAffinity();
                } else {
                    StorageWizardMigrateProgress.this.setCurrentProgress(status);
                }
            }
        }
    };
    private int mMoveId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mVolume == null) {
            finish();
            return;
        }
        setContentView(2130969159);
        this.mMoveId = getIntent().getIntExtra("android.content.pm.extra.MOVE_ID", -1);
        String descrip = this.mStorage.getBestVolumeDescription(this.mVolume);
        setIllustrationType(1);
        setHeaderText(2131625353, descrip);
        setBodyText(2131625354, descrip);
        getNextButton().setVisibility(8);
        getPackageManager().registerMoveCallback(this.mCallback, new Handler());
        this.mCallback.onStatusChanged(this.mMoveId, getPackageManager().getMoveStatus(this.mMoveId), -1);
    }
}

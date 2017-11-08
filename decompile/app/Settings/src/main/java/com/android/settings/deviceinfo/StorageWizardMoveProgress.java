package com.android.settings.deviceinfo;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.MoveCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class StorageWizardMoveProgress extends StorageWizardBase {
    private final MoveCallback mCallback = new MoveCallback() {
        public void onStatusChanged(int moveId, int status, long estMillis) {
            if (StorageWizardMoveProgress.this.mMoveId == moveId) {
                if (PackageManager.isMoveStatusFinished(status)) {
                    Log.d("StorageSettings", "Finished with status " + status);
                    if (status != -100) {
                        Toast.makeText(StorageWizardMoveProgress.this, StorageWizardMoveProgress.this.moveStatusToMessage(status), 1).show();
                    }
                    StorageWizardMoveProgress.this.finishAffinity();
                } else {
                    StorageWizardMoveProgress.this.setCurrentProgress(status);
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
        String appName = getIntent().getStringExtra("android.intent.extra.TITLE");
        String volumeName = this.mStorage.getBestVolumeDescription(this.mVolume);
        setIllustrationType(1);
        setHeaderText(2131625360, appName);
        setBodyText(2131625361, volumeName, appName);
        getNextButton().setVisibility(8);
        getPackageManager().registerMoveCallback(this.mCallback, new Handler());
        this.mCallback.onStatusChanged(this.mMoveId, getPackageManager().getMoveStatus(this.mMoveId), -1);
    }

    protected void onDestroy() {
        super.onDestroy();
        getPackageManager().unregisterMoveCallback(this.mCallback);
    }

    private CharSequence moveStatusToMessage(int returnCode) {
        switch (returnCode) {
            case -8:
                return getString(2131625686);
            case -5:
                return getString(2131625684);
            case -4:
                return getString(2131625683);
            case -3:
                return getString(2131625685);
            case -2:
                return getString(2131625682);
            case -1:
                return getString(2131625681);
            default:
                return getString(2131625681);
        }
    }
}

package com.android.settings.deviceinfo;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import com.android.internal.util.Preconditions;

public class StorageWizardMoveConfirm extends StorageWizardBase {
    private ApplicationInfo mApp;
    private String mPackageName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mVolume == null) {
            finish();
            return;
        }
        setContentView(2130969155);
        try {
            this.mPackageName = getIntent().getStringExtra("android.intent.extra.PACKAGE_NAME");
            this.mApp = getPackageManager().getApplicationInfo(this.mPackageName, 0);
            Preconditions.checkState(getPackageManager().getPackageCandidateVolumes(this.mApp).contains(this.mVolume));
            String appName = getPackageManager().getApplicationLabel(this.mApp).toString();
            String volumeName = this.mStorage.getBestVolumeDescription(this.mVolume);
            setIllustrationType(1);
            setHeaderText(2131625358, appName);
            setBodyText(2131625359, appName, volumeName);
            getNextButton().setText(2131625676);
        } catch (NameNotFoundException e) {
            finish();
        }
    }

    public void onNavigateNext() {
        String appName = getPackageManager().getApplicationLabel(this.mApp).toString();
        int moveId = getPackageManager().movePackage(this.mPackageName, this.mVolume);
        Intent intent = new Intent(this, StorageWizardMoveProgress.class);
        intent.putExtra("android.content.pm.extra.MOVE_ID", moveId);
        intent.putExtra("android.intent.extra.TITLE", appName);
        intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
        startActivity(intent);
        finishAffinity();
    }
}

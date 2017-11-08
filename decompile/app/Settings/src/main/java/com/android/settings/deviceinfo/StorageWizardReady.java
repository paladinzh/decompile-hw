package com.android.settings.deviceinfo;

import android.os.Bundle;
import android.os.storage.VolumeInfo;

public class StorageWizardReady extends StorageFormatBase {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        VolumeInfo publicVol = findFirstVolume(0);
        VolumeInfo privateVol = findFirstVolume(1);
        if (publicVol != null) {
            setBodyText(2131625356, this.mDisk.getDescription());
        } else if (privateVol != null) {
            setBodyText(2131625357, this.mDisk.getDescription());
        }
        this.mNavigationNext.setText(2131624576);
    }

    public void onNavigateNext() {
        finishAffinity();
    }
}

package com.android.settings.deviceinfo;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SdCardLockUtils;
import com.android.settings.Utils;

public class StorageWizardFormatConfirm extends StorageFormatBase {
    private boolean mFormatPrivate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        this.mFormatPrivate = getIntent().getBooleanExtra("format_private", false);
        if (this.mFormatPrivate) {
            setBodyText(2131625340, this.mDisk.getDescription());
        } else if (ActivityManager.getCurrentUser() == 0 && SdCardLockUtils.isPasswordProtected(this)) {
            setBodyText(2131628152, this.mDisk.getDescription());
        } else {
            setBodyText(2131625342, this.mDisk.getDescription());
        }
        this.mNavigationNext.setText(2131625343);
        this.mNavigationNext.setTextColor(-65536);
    }

    public void onNavigateNext() {
        if (!Utils.isMonkeyRunning()) {
            if (ActivityManager.getCurrentUser() == 0 && SdCardLockUtils.isSdCardUnlocked(this)) {
                SdCardLockUtils.clearSDLockPassword(this);
            }
            Intent intent = new Intent(this, StorageWizardFormatProgress.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            intent.putExtra("format_private", this.mFormatPrivate);
            intent.putExtra("forget_uuid", getIntent().getStringExtra("forget_uuid"));
            startActivity(intent);
            finishAffinity();
        }
    }
}

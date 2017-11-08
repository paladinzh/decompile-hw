package com.android.settings.deviceinfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Objects;

public abstract class StorageFormatBase extends Activity {
    protected TextView mBodyText;
    protected DiskInfo mDisk;
    protected Button mNavigationNext;
    protected ProgressBar mProgressBar;
    protected TextView mProgressSummaryText;
    protected TextView mSecondaryBodyText;
    protected StorageManager mStorage;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onDiskDestroyed(DiskInfo disk) {
            if (StorageFormatBase.this.mDisk.id.equals(disk.id)) {
                StorageFormatBase.this.finish();
            }
        }
    };
    protected VolumeInfo mVolume;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mStorage = (StorageManager) getSystemService(StorageManager.class);
        String volumeId = getIntent().getStringExtra("android.os.storage.extra.VOLUME_ID");
        if (!TextUtils.isEmpty(volumeId)) {
            this.mVolume = this.mStorage.findVolumeById(volumeId);
        }
        String diskId = getIntent().getStringExtra("android.os.storage.extra.DISK_ID");
        if (!TextUtils.isEmpty(diskId)) {
            this.mDisk = this.mStorage.findDiskById(diskId);
        } else if (this.mVolume != null) {
            this.mDisk = this.mVolume.getDisk();
        }
        if (this.mDisk != null) {
            this.mStorage.registerListener(this.mStorageListener);
        }
        setupViews();
    }

    private void setupViews() {
        setContentView(2130969148);
        this.mBodyText = (TextView) findViewById(2131887213);
        this.mSecondaryBodyText = (TextView) findViewById(2131887214);
        this.mProgressSummaryText = (TextView) findViewById(2131887216);
        this.mProgressBar = (ProgressBar) findViewById(2131887215);
        OnClickListener navigateListener = new OnClickListener() {
            public void onClick(View arg0) {
                StorageFormatBase.this.onNavigateNext();
            }
        };
        this.mNavigationNext = (Button) findViewById(2131887217);
        this.mNavigationNext.setOnClickListener(navigateListener);
        setTitle(getText(2131625288));
    }

    protected void onDestroy() {
        this.mStorage.unregisterListener(this.mStorageListener);
        super.onDestroy();
    }

    protected void setCurrentProgress(int progress) {
        this.mProgressBar.setProgress(progress);
        this.mProgressSummaryText.setText(NumberFormat.getPercentInstance().format(((double) progress) / 100.0d));
    }

    protected void setBodyText(int resId, String... args) {
        this.mBodyText.setText(TextUtils.expandTemplate(getText(resId), args));
    }

    protected void setSecondaryBodyText(int resId, String... args) {
        this.mSecondaryBodyText.setText(TextUtils.expandTemplate(getText(resId), args));
    }

    protected void setKeepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().setFlags(128, 128);
        }
    }

    public void onNavigateNext() {
        throw new UnsupportedOperationException();
    }

    protected VolumeInfo findFirstVolume(int type) {
        for (VolumeInfo vol : this.mStorage.getVolumes()) {
            if (this.mDisk != null && Objects.equals(this.mDisk.getId(), vol.getDiskId()) && vol.getType() == type) {
                return vol;
            }
        }
        return null;
    }
}

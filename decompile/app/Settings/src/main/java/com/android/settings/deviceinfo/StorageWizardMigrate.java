package com.android.settings.deviceinfo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

public class StorageWizardMigrate extends StorageWizardBase {
    private MigrateEstimateTask mEstimate;
    private RadioButton mRadioLater;
    private final OnCheckedChangeListener mRadioListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView == StorageWizardMigrate.this.mRadioNow) {
                    StorageWizardMigrate.this.mRadioLater.setChecked(false);
                } else if (buttonView == StorageWizardMigrate.this.mRadioLater) {
                    StorageWizardMigrate.this.mRadioNow.setChecked(false);
                }
                StorageWizardMigrate.this.getNextButton().setEnabled(true);
            }
        }
    };
    private RadioButton mRadioNow;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(2130969157);
        setIllustrationType(1);
        setHeaderText(2131625346, this.mDisk.getDescription());
        setBodyText(2131625258, new String[0]);
        this.mRadioNow = (RadioButton) findViewById(2131887224);
        this.mRadioLater = (RadioButton) findViewById(2131887225);
        this.mRadioNow.setOnCheckedChangeListener(this.mRadioListener);
        this.mRadioLater.setOnCheckedChangeListener(this.mRadioListener);
        getNextButton().setEnabled(false);
        this.mEstimate = new MigrateEstimateTask(this) {
            public void onPostExecute(String size, String time) {
                StorageWizardMigrate.this.setBodyText(2131625347, StorageWizardMigrate.this.mDisk.getDescription(), time, size);
            }
        };
        this.mEstimate.copyFrom(getIntent());
        this.mEstimate.execute(new Void[0]);
    }

    public void onNavigateNext() {
        Intent intent;
        if (this.mRadioNow.isChecked()) {
            intent = new Intent(this, StorageWizardMigrateConfirm.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            this.mEstimate.copyTo(intent);
            startActivity(intent);
        } else if (this.mRadioLater.isChecked()) {
            intent = new Intent(this, StorageWizardReady.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            startActivity(intent);
        }
    }
}

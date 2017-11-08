package com.android.settings.deviceinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import android.widget.Toast;
import java.util.Objects;

public class StorageWizardMigrateConfirm extends StorageWizardBase {
    private MigrateEstimateTask mEstimate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130969155);
        if (this.mVolume == null) {
            this.mVolume = findFirstVolume(1);
        }
        VolumeInfo sourceVol = getPackageManager().getPrimaryStorageCurrentVolume();
        if (sourceVol == null || this.mVolume == null) {
            Log.d("StorageSettings", "Missing either source or target volume");
            finish();
            return;
        }
        final String sourceDescrip = this.mStorage.getBestVolumeDescription(sourceVol);
        String targetDescrip = this.mStorage.getBestVolumeDescription(this.mVolume);
        setIllustrationType(1);
        setHeaderText(2131625350, targetDescrip);
        setBodyText(2131625258, new String[0]);
        setSecondaryBodyText(2131625354, targetDescrip);
        this.mEstimate = new MigrateEstimateTask(this) {
            public void onPostExecute(String size, String time) {
                StorageWizardMigrateConfirm.this.setBodyText(2131625351, time, size, sourceDescrip);
            }
        };
        this.mEstimate.copyFrom(getIntent());
        this.mEstimate.execute(new Void[0]);
        getNextButton().setText(2131625352);
    }

    public void onNavigateNext() {
        Intent intent;
        try {
            int moveId = getPackageManager().movePrimaryStorage(this.mVolume);
            intent = new Intent(this, StorageWizardMigrateProgress.class);
            intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
            intent.putExtra("android.content.pm.extra.MOVE_ID", moveId);
            startActivity(intent);
            finishAffinity();
        } catch (IllegalArgumentException e) {
            if (Objects.equals(this.mVolume.getFsUuid(), ((StorageManager) getSystemService("storage")).getPrimaryStorageVolume().getUuid())) {
                intent = new Intent(this, StorageWizardReady.class);
                intent.putExtra("android.os.storage.extra.DISK_ID", getIntent().getStringExtra("android.os.storage.extra.DISK_ID"));
                startActivity(intent);
                finishAffinity();
                return;
            }
            throw e;
        } catch (IllegalStateException e2) {
            Toast.makeText(this, getString(2131625680), 1).show();
            finishAffinity();
        }
    }
}

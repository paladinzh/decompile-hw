package com.android.settings.deviceinfo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.support.v7.preference.Preference;
import android.text.format.Formatter;
import com.android.settings.Utils;
import java.io.File;

public class StorageVolumePreference extends Preference {
    private int mColor;
    private final StorageManager mStorageManager;
    private int mUsedPercent = -1;
    private final VolumeInfo mVolume;

    public StorageVolumePreference(Context context, VolumeInfo volume, int color) {
        Drawable icon;
        super(context);
        this.mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
        this.mVolume = volume;
        this.mColor = color;
        setLayoutResource(2130969154);
        setKey(volume.getId());
        setTitle(this.mStorageManager.getBestVolumeDescription(volume));
        if ("private".equals(volume.getId())) {
            icon = context.getDrawable(2130838425);
        } else {
            icon = context.getDrawable(2130838439);
        }
        if (volume.isMountedReadable()) {
            long freeBytes;
            File path = volume.getPath();
            if (volume.getType() == 0) {
                freeBytes = path.getFreeSpace();
            } else {
                freeBytes = Utils.getStorageAvailableSize(this.mStorageManager);
            }
            long totalBytes = path.getTotalSpace();
            long usedBytes = totalBytes - freeBytes;
            String total = Formatter.formatFileSize(context, totalBytes);
            String free = Formatter.formatFileSize(context, totalBytes - usedBytes);
            setSummary(context.getString(2131627576, new Object[]{total, free}));
            if (freeBytes < this.mStorageManager.getStorageLowBytes(path)) {
                this.mColor = StorageSettings.COLOR_WARNING;
                icon = context.getDrawable(2130838462);
            }
        } else {
            setSummary(volume.getStateDescription());
            this.mUsedPercent = -1;
        }
        icon.mutate();
        icon.setTint(this.mColor);
        setIcon(icon);
        setWidgetLayoutResource(2130968998);
    }
}

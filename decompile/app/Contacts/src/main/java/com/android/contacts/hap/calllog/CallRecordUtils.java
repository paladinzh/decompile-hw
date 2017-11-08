package com.android.contacts.hap.calllog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.android.contacts.util.HwLog;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CallRecordUtils {
    public static String[] getRecordStoragePaths(Context context) {
        ArrayList<String> paths = new ArrayList();
        StorageVolume[] storageVolumes = ((StorageManager) context.getSystemService("storage")).getVolumeList();
        if (storageVolumes != null) {
            for (StorageVolume sv : storageVolumes) {
                File f = new File(sv.getPath() + "/record");
                if (f.exists() && f.canRead()) {
                    paths.add(f.getAbsolutePath());
                }
            }
        }
        return (String[]) paths.toArray(new String[paths.size()]);
    }

    @SuppressLint({"HwHardCodeDateFormat"})
    public static long getCallRecordCreatedDate(String recordName) {
        if (recordName == null || recordName.length() == 0) {
            return -1;
        }
        try {
            String[] splitResult = recordName.split("\\.")[0].split("_");
            return new SimpleDateFormat("yyyyMMddHHmmss").parse(splitResult[splitResult.length - 1]).getTime();
        } catch (Exception e) {
            HwLog.e("CallRecordUtils", "parse record file create date failed");
            e.printStackTrace();
            return -1;
        }
    }

    public static String getRecordPhoneNumber(String recordName) {
        if (recordName == null || recordName.length() == 0) {
            return null;
        }
        try {
            String[] splitResult = recordName.split("\\.")[0].split("_");
            if (splitResult.length != 2 && splitResult.length != 4) {
                return "common-number";
            }
            if (splitResult.length == 4 && splitResult[2] != null && splitResult[2].length() >= 3) {
                return splitResult[2].replaceAll("[^0-9|+]", "");
            }
            if (splitResult.length == 2 && splitResult[0] != null) {
                String[] numParse = splitResult[0].split("@");
                if (numParse[numParse.length - 1].length() >= 3) {
                    return numParse[numParse.length - 1].replaceAll("[^0-9|+]", "");
                }
            }
            return "common-number";
        } catch (Exception e) {
            HwLog.e("CallRecordUtils", "parse record file name failed");
            e.printStackTrace();
        }
    }
}

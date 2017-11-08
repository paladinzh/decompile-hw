package com.android.settings;

import android.content.res.AssetManager;

public class HwCustSettingUtilsImpl extends HwCustSettingUtils {
    private String usaFolderName = "en_us-optb840";

    public String changeToUsaFolder(AssetManager am, String optb, String fileName, String folderName) {
        if (optb.contains("840") && "huawei_copyright.html".equals(fileName) && !folderName.contains("-optb840") && Utils.isFileExist(am, this.usaFolderName, fileName)) {
            return this.usaFolderName;
        }
        return folderName;
    }
}

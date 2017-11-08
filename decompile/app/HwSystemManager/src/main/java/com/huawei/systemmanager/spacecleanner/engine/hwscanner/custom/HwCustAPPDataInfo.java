package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import java.util.ArrayList;

public class HwCustAPPDataInfo {
    private String mPkgName;
    private ArrayList<String> mProtectPaths;
    private ArrayList<HwCustTrashInfo> mTrash;

    public HwCustAPPDataInfo(String pkg, ArrayList<String> paths, ArrayList<HwCustTrashInfo> trash) {
        this.mPkgName = pkg;
        this.mProtectPaths = paths;
        this.mTrash = trash;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public ArrayList<String> getProtectPath() {
        return this.mProtectPaths;
    }

    public ArrayList<HwCustTrashInfo> getTrash() {
        return this.mTrash;
    }
}

package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import java.util.List;

public class HwCustTrashInfo {
    private List<String> mFilterPaths;
    private int mKeepTime;
    private int mKeeplatest;
    private String mMatchRule;
    private String mPkgName;
    private boolean mRecommended;
    private String mTrashPath;
    private int mTrashType;

    public List<String> getFilterPaths() {
        return this.mFilterPaths;
    }

    public void setFilterPaths(List<String> mFilterPaths) {
        this.mFilterPaths = mFilterPaths;
    }

    public HwCustTrashInfo(String pkg, String path, int type, boolean recommended, String rule, int time, int latest) {
        this.mPkgName = pkg;
        this.mTrashPath = path;
        this.mTrashType = type;
        this.mRecommended = recommended;
        this.mMatchRule = rule;
        this.mKeepTime = time;
        this.mKeeplatest = latest;
    }

    public HwCustTrashInfo(HwCustTrashInfo trash) {
        if (trash != null) {
            this.mPkgName = trash.mPkgName;
            this.mTrashPath = trash.mTrashPath;
            this.mTrashType = trash.mTrashType;
            this.mRecommended = trash.mRecommended;
            this.mMatchRule = trash.mMatchRule;
            this.mKeepTime = trash.mKeepTime;
            this.mKeeplatest = trash.mKeeplatest;
        }
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public String getTrashPath() {
        return this.mTrashPath;
    }

    public int getTrashType() {
        return this.mTrashType;
    }

    public boolean getRecommend() {
        return this.mRecommended;
    }

    public String getMatchRule() {
        return this.mMatchRule;
    }

    public int getKeepTime() {
        return this.mKeepTime;
    }

    public int getKeeplatest() {
        return this.mKeeplatest;
    }
}

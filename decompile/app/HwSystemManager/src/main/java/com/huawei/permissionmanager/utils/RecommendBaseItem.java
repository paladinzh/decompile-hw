package com.huawei.permissionmanager.utils;

public class RecommendBaseItem {
    private boolean mIsRecommend = false;
    private int mPercent;
    private int mPermissionRecommendStatus;

    public RecommendBaseItem(boolean recommend, int permissionRecommendStatus, int percent) {
        this.mIsRecommend = recommend;
        this.mPermissionRecommendStatus = permissionRecommendStatus;
        this.mPercent = percent;
    }

    public boolean isCurrentPermissionHasRecommendStatus() {
        return this.mIsRecommend;
    }

    public int getCurrentPermissionRecommendStatus() {
        return this.mPermissionRecommendStatus;
    }

    public int getRecommendPercent() {
        return this.mPercent;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("IsRecommend[").append(this.mIsRecommend).append("] ");
        buf.append("recommendStatus[").append(this.mPermissionRecommendStatus).append("] ");
        buf.append("percent[").append(this.mPercent).append("] ");
        buf.append("} ");
        return buf.toString();
    }
}

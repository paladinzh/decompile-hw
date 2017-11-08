package com.huawei.openalliance.ad.inter;

/* compiled from: Unknown */
public class MagLockAdContent {
    private String clickMonitorUrl;
    private String contentId;
    private long endTime;
    private long fileSize;
    private String impMonitorUrl;
    private String md5;
    private String metaData;
    private String paramFromServer;
    private String previewMd5;
    private String previewSha256;
    private String previewUrl;
    private String sha256;
    private String url;

    public String getClickMonitorUrl() {
        return this.clickMonitorUrl;
    }

    public String getContentId() {
        return this.contentId;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public String getImpMonitorUrl() {
        return this.impMonitorUrl;
    }

    public String getMetaData() {
        return this.metaData;
    }

    public String getParamFromServer() {
        return this.paramFromServer;
    }

    public void setClickMonitorUrl(String str) {
        this.clickMonitorUrl = str;
    }

    public void setContentId(String str) {
        this.contentId = str;
    }

    public void setEndTime(long j) {
        this.endTime = j;
    }

    public void setFileSize(long j) {
        this.fileSize = j;
    }

    public void setImpMonitorUrl(String str) {
        this.impMonitorUrl = str;
    }

    public void setMd5(String str) {
        this.md5 = str;
    }

    public void setMetaData(String str) {
        this.metaData = str;
    }

    public void setParamFromServer(String str) {
        this.paramFromServer = str;
    }

    public void setPreviewMd5(String str) {
        this.previewMd5 = str;
    }

    public void setPreviewSha256(String str) {
        this.previewSha256 = str;
    }

    public void setPreviewUrl(String str) {
        this.previewUrl = str;
    }

    public void setSha256(String str) {
        this.sha256 = str;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public String toString() {
        return "MagLockAdContent [contentId=" + this.contentId + ", endTime=" + this.endTime + ", url=" + this.url + ", md5=" + this.md5 + ", sha256=" + this.sha256 + ", previewSha256=" + this.previewSha256 + ", fileSize=" + this.fileSize + ", previewUrl=" + this.previewUrl + ", previewMd5=" + this.previewMd5 + "]";
    }
}

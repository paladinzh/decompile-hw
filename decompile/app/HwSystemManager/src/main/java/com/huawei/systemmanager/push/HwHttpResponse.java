package com.huawei.systemmanager.push;

public class HwHttpResponse {
    private String mDownloadUrl;
    private String mInfoString;
    private String mResultCode;
    private String mVersion;
    private String mVersionInfo;

    public HwHttpResponse(String mResultCode, String mInfoString, String mVersion, String mVersionInfo, String mDownloadUrl) {
        this.mResultCode = mResultCode;
        this.mInfoString = mInfoString;
        this.mVersion = mVersion;
        this.mVersionInfo = mVersionInfo;
        this.mDownloadUrl = mDownloadUrl;
    }

    public String getmResultCode() {
        return this.mResultCode;
    }

    public String getmInfoString() {
        return this.mInfoString;
    }

    public String getmVersion() {
        return this.mVersion;
    }

    public String getmVersionInfo() {
        return this.mVersionInfo;
    }

    public String getmDownloadUrl() {
        return this.mDownloadUrl;
    }

    public void setmResultCode(String mResultCode) {
        this.mResultCode = mResultCode;
    }

    public void setmInfoString(String mInfoString) {
        this.mInfoString = mInfoString;
    }

    public void setmVersion(String mVersion) {
        this.mVersion = mVersion;
    }

    public void setmVersionInfo(String mVersionInfo) {
        this.mVersionInfo = mVersionInfo;
    }

    public void setmDownloadUrl(String mDownloadUrl) {
        this.mDownloadUrl = mDownloadUrl;
    }

    public String toString() {
        return "HttpResponse [mResultCode=" + this.mResultCode + ", mInfoString=" + this.mInfoString + ", mVersion=" + this.mVersion + ", mVersionInfo=" + this.mVersionInfo + ", mDownloadUrl=" + this.mDownloadUrl + "]";
    }
}

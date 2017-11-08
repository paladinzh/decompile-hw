package com.huawei.systemmanager.antivirus.statistics;

public class VirusInfoBuilder {
    private long mCloudScanCount = 0;
    private long mUnKnownCount = 0;
    private String mVendor;
    private long mVirusScanCount = 0;

    public VirusInfoBuilder(String vendor) {
        this.mVendor = vendor;
    }

    VirusInfoBuilder setVendor(String vendor) {
        this.mVendor = vendor;
        return this;
    }

    public String getVendor() {
        return this.mVendor;
    }

    public long getCloudScanCount() {
        return this.mCloudScanCount;
    }

    public long getVirusScanCount() {
        return this.mVirusScanCount;
    }

    public long getUnKnownCount() {
        return this.mUnKnownCount;
    }

    public VirusInfoBuilder setCloudScanCount(long cloudScanCount) {
        this.mCloudScanCount = cloudScanCount;
        return this;
    }

    public VirusInfoBuilder setVirusScanCount(long virusScanCount) {
        this.mVirusScanCount = virusScanCount;
        return this;
    }

    public VirusInfoBuilder setUnKnownCount(long unKnownCount) {
        this.mUnKnownCount = unKnownCount;
        return this;
    }

    public void increaseCloudScanCount() {
        this.mCloudScanCount++;
    }

    public void increaseVirusScanCount() {
        this.mVirusScanCount++;
    }

    public void increaseUnKnownCount() {
        this.mUnKnownCount++;
    }

    public String toString() {
        return String.format("VENDOR=%s,TOTAL_COUNT=%d,VIRUS_COUNT=%d,UNKNOWN_COUNT=%d", new Object[]{this.mVendor, Long.valueOf(this.mCloudScanCount), Long.valueOf(this.mVirusScanCount), Long.valueOf(this.mUnKnownCount)});
    }
}

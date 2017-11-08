package com.trustlook.sdk.cloudscan;

import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.Result;
import java.util.List;

public class ScanResult extends Result {
    List<AppInfo> a;

    public List<AppInfo> getList() {
        return this.a;
    }

    public void setList(List<AppInfo> list) {
        this.a = list;
    }
}

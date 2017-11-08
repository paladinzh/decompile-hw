package com.huawei.systemmanager.comm.module;

import com.huawei.systemmanager.customize.CustConifgChecker;

public class HwCustModuleCustomizeImpl extends HwCustModuleCustomize {
    private int removeAdFilter = -1;
    private int removeNetwork = -1;
    private int removeVirus = -1;

    public HwCustModuleCustomizeImpl() {
        CustConifgChecker mCustChecker = new CustConifgChecker();
        this.removeAdFilter = mCustChecker.getFeatureIntConfig("remove_ad_filter");
        this.removeVirus = mCustChecker.getFeatureIntConfig("remove_virus");
        this.removeNetwork = mCustChecker.getFeatureIntConfig("remove_net_assistant");
    }

    public boolean hasAdFinderCustConfig() {
        return -1 != this.removeAdFilter;
    }

    public boolean adFinderEntryEnabled() {
        return 1 == this.removeAdFilter;
    }

    public boolean hasVirusCustConfig() {
        return -1 != this.removeVirus;
    }

    public boolean virusEntryEnabled() {
        return 1 == this.removeVirus;
    }

    public boolean hasNetworkCustConfig() {
        return -1 != this.removeNetwork;
    }

    public boolean networkEntryEnabled() {
        return 1 == this.removeNetwork;
    }
}

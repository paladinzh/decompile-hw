package com.huawei.systemmanager.antivirus.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.AdvertiseEntity;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.BaseEntity;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.RiskPermEntity;
import com.huawei.systemmanager.antivirus.ui.view.GlobalScanProgressWrapper;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import java.util.List;

public class AntiVirusGlobalScanActivity extends AntiVirusActivity {
    protected void initScanModeData(View actionbarLayout) {
        ((TextView) actionbarLayout.findViewById(R.id.title)).setText(getString(R.string.virus_global_scan));
    }

    protected int getScanMode() {
        return 1;
    }

    protected List<BaseEntity> getMoreItems(int totalAdCount, int checkedAdCount) {
        List<BaseEntity> list = Lists.newArrayList();
        if (!AntiVirusTools.isAbroad()) {
            list.add(new AdvertiseEntity(totalAdCount, checkedAdCount));
        }
        list.add(new RiskPermEntity(this.mRiskPerm.size()));
        return list;
    }

    protected void setVirusScanProgressWrapper(ViewGroup viewGroup) {
        this.mScanProgressShow = new GlobalScanProgressWrapper(viewGroup);
        this.mScanProgressShow.initView();
    }
}

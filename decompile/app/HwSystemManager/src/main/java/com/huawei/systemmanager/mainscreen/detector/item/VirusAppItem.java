package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.ui.ScanResultListActivity;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import java.util.ArrayList;
import java.util.List;

public class VirusAppItem extends PkgDetectItem<ScanResultEntity> {
    private static final String TAG = "VirusAppItem";
    private final List<ScanResultEntity> mRiskAndNotOfficalApps = Lists.newArrayList();

    private VirusAppItem() {
    }

    public String getName() {
        return getContext().getString(R.string.software_type_virus);
    }

    public String getTitle(Context ctx) {
        if (isOptimized()) {
            return getContext().getString(R.string.no_have_virus);
        }
        int count = getAppCount();
        return getContext().getResources().getQuantityString(R.plurals.have_virus, count, new Object[]{Integer.valueOf(count)});
    }

    public int getItemType() {
        return 1;
    }

    public int score() {
        int count = getAppCount();
        if (count == 0) {
            return 0;
        }
        return Math.min(count + 24, 30);
    }

    public boolean isManulOptimize() {
        return true;
    }

    public Intent getOptimizeIntent(Context ctx) {
        ArrayList<ScanResultEntity> virusList = converToCustomItem();
        Intent intent = new Intent();
        intent.setClass(ctx, ScanResultListActivity.class);
        intent.putExtra(AntiVirusTools.RESULT_TYPE, AntiVirusTools.TYPE_VIRUS);
        intent.putExtra(AntiVirusTools.RESULT_LIST, virusList);
        return intent;
    }

    public int getOptimizeActionType() {
        return 3;
    }

    public String getOptimizeActionName() {
        int score = score();
        return getContext().getString(R.string.main_screen_detect_operation_uninstall, new Object[]{Utility.getLocaleNumber(score)});
    }

    protected String getPkgFromCustomItem(ScanResultEntity item) {
        return item.packageName;
    }

    public String getTag() {
        return TAG;
    }

    public DetectItem copy() {
        return this;
    }

    public void receiveVirusscanApps(Intent intent) {
        if (intent != null) {
            ScanResultEntity app = (ScanResultEntity) intent.getSerializableExtra("key_result");
            if (app != null) {
                if (app.type == AntiVirusTools.TYPE_VIRUS) {
                    addPkgsToApps(Lists.newArrayList(app));
                } else if (app.type == 303 || app.type == 304) {
                    addRisAndNotOfficalApps(Lists.newArrayList(app));
                }
            }
        }
    }

    private void addRisAndNotOfficalApps(List<ScanResultEntity> apps) {
        synchronized (this.mRiskAndNotOfficalApps) {
            this.mRiskAndNotOfficalApps.addAll(apps);
        }
    }

    private int getRiskAndNotOfficalNum() {
        int size;
        synchronized (this.mRiskAndNotOfficalApps) {
            removeAppNotInstalled(this.mRiskAndNotOfficalApps);
            size = this.mRiskAndNotOfficalApps.size();
        }
        return size;
    }

    public int getAllAppNums() {
        return getRiskAndNotOfficalNum() + getAppCount();
    }

    public static VirusAppItem create(List<ScanResultEntity> allApps) {
        VirusAppItem item = new VirusAppItem();
        List<ScanResultEntity> riskAndNotOfficalApps = Lists.newArrayList();
        List<ScanResultEntity> virusApps = Lists.newArrayList();
        for (ScanResultEntity app : allApps) {
            if (app.type == AntiVirusTools.TYPE_VIRUS) {
                virusApps.add(app);
            } else if (app.type == 303 || app.type == 304) {
                riskAndNotOfficalApps.add(app);
            }
        }
        item.addRisAndNotOfficalApps(riskAndNotOfficalApps);
        item.init(virusApps);
        return item;
    }

    public void statOptimizeEvent() {
        HsmStat.statE(Events.E_OPTMIZE_VIRUS_VIEW);
    }
}

package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.utils.AntivirusTipUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.detector.DetectTaskManager;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;
import com.huawei.systemmanager.mainscreen.detector.item.VirusAppItem;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;

public class EntryVirusScan extends SimpleEntrace {
    public static final String NAME = "EntryVirusScan";
    private Runnable mCaculateTask = new Runnable() {
        public void run() {
            EntryVirusScan.this.mTipView.postSetNumber(EntryVirusScan.this.getCompetitorCount(GlobalContext.getContext()) + EntryVirusScan.this.getDetectVirusCount());
        }
    };
    private DetectTaskManager mDetectMgr;
    private final TipView mTipView = new TipView();

    public void onResume() {
        refreshData();
    }

    protected int getIconResId() {
        return R.drawable.ic_virus_scan_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.systemmanager_module_title_virus;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_VIRUSSCANNER;
    }

    public String getEntryName() {
        return NAME;
    }

    protected void onCreateView(View container) {
        this.mTipView.setTipTextView((TextView) container.findViewById(R.id.tip));
        Utility.setViewEnabled(container, Utility.isOwnerUser(false));
    }

    public void setDetectMgr(DetectTaskManager detector) {
        this.mDetectMgr = detector;
        refreshData();
    }

    public void refreshData() {
        if (this.mDetectMgr != null) {
            SERIAL_EXECUTOR.execute(this.mCaculateTask);
        }
    }

    private int getCompetitorCount(Context ctx) {
        return AntivirusTipUtil.getCompetitorsNotviewd(ctx).size();
    }

    private int getDetectVirusCount() {
        if (this.mDetectMgr == null) {
            return 0;
        }
        for (DetectItem item : this.mDetectMgr.getResult()) {
            if (item instanceof VirusAppItem) {
                return ((VirusAppItem) item).getAllAppNums();
            }
        }
        return 0;
    }
}

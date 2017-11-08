package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.text.format.Formatter;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.optimize.ProcessManager;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.ProcessFilterPolicy;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessItem extends DetectItem {
    private static final String TAG = "ProcessItem";
    private int appNum = 0;
    private final List<String> mProcessList = Collections.synchronizedList(new ArrayList());
    private long mTotalSize = 0;

    public String getTitle(Context ctx) {
        String title = "";
        int state = getState();
        int count;
        long size;
        switch (state) {
            case 1:
                return ctx.getString(R.string.mainscreen_no_suggest_clean_app_running_background);
            case 2:
                count = this.appNum;
                size = this.mTotalSize;
                return ctx.getResources().getQuantityString(R.plurals.scan_memory_before_clear, count, new Object[]{Integer.valueOf(count), Formatter.formatFileSize(ctx, size)});
            case 3:
                count = this.appNum;
                size = this.mTotalSize;
                return ctx.getResources().getQuantityString(R.plurals.scan_memory_after_clear, count, new Object[]{Integer.valueOf(count), Formatter.formatFileSize(ctx, size)});
            default:
                HwLog.e(TAG, "getTitle unknow state:" + state);
                return title;
        }
    }

    public void doScan() {
        List<ProcessAppItem> processList = ProcessFilterPolicy.getRunningApps(getContext());
        List<String> pkgList = Lists.newArrayList();
        int totalMemory = 0;
        for (ProcessAppItem item : processList) {
            if (!(item.isKeyProcess() || item.isProtect())) {
                pkgList.add(item.getPackageName());
                totalMemory = (int) (((long) totalMemory) + item.getMemoryCost());
            }
        }
        this.mProcessList.clear();
        if (pkgList.isEmpty()) {
            setState(1);
            this.mTotalSize = 0;
            this.appNum = 0;
        } else {
            setState(2);
            this.mProcessList.addAll(pkgList);
            this.mTotalSize = (long) totalMemory;
            this.appNum = pkgList.size();
        }
        HwLog.i(TAG, "do scan called, appNum:" + this.appNum + ", totalMemory:" + this.mTotalSize + ", appList:" + pkgList);
    }

    protected int score() {
        return Math.min(this.appNum, 10);
    }

    public void refresh() {
        HwLog.i(TAG, "refresh called");
        doScan();
    }

    public int getOptimizeActionType() {
        return 2;
    }

    public void doOptimize(Context ctx) {
        ProcessManager.clearPackages(Lists.newArrayList(this.mProcessList));
        setState(3);
    }

    public boolean isManulOptimize() {
        return false;
    }

    public int getItemType() {
        return 10;
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.look);
    }

    public String getName() {
        return getContext().getString(R.string.back_app);
    }

    public String getTag() {
        return TAG;
    }

    public void printf(StringBuilder appendable) {
        super.printf(appendable);
        appendable.append(",process:").append(this.mProcessList.toString());
    }

    public DetectItem copy() {
        ProcessItem item = new ProcessItem();
        item.mProcessList.addAll(this.mProcessList);
        item.mTotalSize = this.mTotalSize;
        item.appNum = this.appNum;
        item.setState(getState());
        return item;
    }
}

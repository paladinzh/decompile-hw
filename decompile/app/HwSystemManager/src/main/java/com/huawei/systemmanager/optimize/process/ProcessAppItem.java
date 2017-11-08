package com.huawei.systemmanager.optimize.process;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comparator.BooleanComparator;
import com.huawei.systemmanager.comparator.SizeComparator;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProcessAppItem extends ProtectAppItem {
    public static final ProcessAppItem EMPTY_NORMAL_ITEM = new EmptyNormalProcessAppItem();
    public static final Comparator<ProcessAppItem> MEMORY_COPARATOR = new SizeComparator<ProcessAppItem>() {
        public long getKey(ProcessAppItem processAppItem) {
            return processAppItem.getMemoryCost();
        }
    };
    public static final Comparator<ProcessAppItem> PROTECT_COMPARATOR = new BooleanComparator<ProcessAppItem>() {
        public boolean getKey(ProcessAppItem processAppItem) {
            return processAppItem.isProtect();
        }
    };
    private int mADJ = SpaceConst.SCANNER_TYPE_ALL;
    private boolean mKeyTask;
    private long mMemoryCost;
    private List<Integer> mPids = Lists.newArrayList();

    public static class EmptyNormalProcessAppItem extends ProcessAppItem {
        private void dummyFunc() {
        }

        public void setKeyTask(boolean key) {
            dummyFunc();
        }

        public void setMemoryCost(long memory) {
            dummyFunc();
        }

        public void setProtect(boolean isProtect) {
            dummyFunc();
        }

        public void setChecked(boolean checked) {
            dummyFunc();
        }
    }

    protected ProcessAppItem() {
    }

    public ProcessAppItem(HsmPkgInfo info) {
        super(info);
    }

    public long getMemoryCost() {
        return this.mMemoryCost;
    }

    public boolean isKeyProcess() {
        return this.mKeyTask;
    }

    public boolean isChecked() {
        if (this.mKeyTask) {
            return false;
        }
        return super.isChecked();
    }

    public boolean isCheckable() {
        if (this.mKeyTask) {
            return false;
        }
        return true;
    }

    public void setKeyTask(boolean key) {
        this.mKeyTask = key;
    }

    public void setMemoryCost(long memory) {
        this.mMemoryCost = memory;
    }

    void addPid(int pid) {
        this.mPids.add(Integer.valueOf(pid));
    }

    public List<Integer> getPids() {
        return Collections.unmodifiableList(this.mPids);
    }

    public void setADJ(int adj) {
        this.mADJ = adj;
    }

    public int getADJ() {
        return this.mADJ;
    }
}

package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DetectItem {
    public static final int ACTION_EXECUTE_COST_TIME = 2;
    public static final int ACTION_EXECUTE_QUICK = 1;
    public static final int ACTION_JUMP = 3;
    public static final int STATE_NEED_OPTIMIZED = 2;
    public static final int STATE_OPTIMIZED = 3;
    public static final int STATE_SECURITY = 1;
    private static final String TAG = "DetectItem";
    public static final int TYPE_AD_APP = 5;
    public static final int TYPE_BLUETOOT = 9;
    public static final int TYPE_HARASMENT = 14;
    public static final int TYPE_HARASSMENT_BLOCK_INTELL_CLOCK = 15;
    public static final int TYPE_NUMBER_MARK = 16;
    public static final int TYPE_POWER_MODE = 13;
    public static final int TYPE_PROCESS_MEMORY = 10;
    public static final int TYPE_SECURITY_PATCH = 4;
    public static final int TYPE_STARTUP_APP = 6;
    public static final int TYPE_STORAGE_SPACE = 2;
    public static final int TYPE_TRAFFIC_DATA = 3;
    public static final int TYPE_TRASH_FILE = 11;
    public static final int TYPE_VIRUS_APP = 1;
    public static final int TYPE_VIRUS_UPDATE = 12;
    public static final int TYPE_WHITE_LIST = 7;
    public static final int TYPE_WIFI = 8;
    public static final int TYPE_WIFISEC = 17;
    public static final Comparator<DetectItem> sComparator = new Comparator<DetectItem>() {
        public int compare(DetectItem lhs, DetectItem rhs) {
            return lhs.getItemType() - rhs.getItemType();
        }
    };
    private AtomicInteger mState = new AtomicInteger(1);

    public abstract DetectItem copy();

    public abstract int getItemType();

    public abstract String getName();

    public abstract int getOptimizeActionType();

    public abstract String getTag();

    public abstract String getTitle(Context context);

    public abstract boolean isManulOptimize();

    public abstract void refresh();

    protected abstract int score();

    public String getDescription(Context ctx) {
        return "";
    }

    public void doScan() {
        HwLog.w(TAG, "donot call doScan in this item, item type:" + getItemType());
    }

    public boolean isOptimized() {
        int state = this.mState.get();
        if (state == 1 || state == 3) {
            return true;
        }
        return false;
    }

    public int getScore() {
        if (isOptimized()) {
            return 0;
        }
        return score();
    }

    public Intent getOptimizeIntent(Context ctx) {
        return null;
    }

    public void doOptimize(Context ctx) {
        HwLog.w(TAG, "donot call do optimize in this item, item type:" + getItemType());
    }

    public String getOptimizeActionName() {
        return "";
    }

    public boolean isVisiable() {
        return true;
    }

    public boolean isEnable() {
        return true;
    }

    protected int setState(int state) {
        return this.mState.getAndSet(state);
    }

    protected int getState() {
        return this.mState.get();
    }

    public void printf(StringBuilder appendable) {
        appendable.append(getTag()).append(ConstValues.SEPARATOR_KEYWORDS_EN).append(getScore());
    }

    protected final Context getContext() {
        return GlobalContext.getContext();
    }

    public void statOptimizeEvent() {
        HwLog.w(getTag(), "statOptimizeEvent called do nothing!");
    }
}

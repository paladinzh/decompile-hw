package com.huawei.systemmanager.mainscreen.detector.task;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.mainscreen.detector.item.BluetoothItem;
import com.huawei.systemmanager.mainscreen.detector.item.BootupItem;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;
import com.huawei.systemmanager.mainscreen.detector.item.HrassCallIntellItem;
import com.huawei.systemmanager.mainscreen.detector.item.NumberMarkItem;
import com.huawei.systemmanager.mainscreen.detector.item.ProcessItem;
import com.huawei.systemmanager.mainscreen.detector.item.TrafficDataItem;
import com.huawei.systemmanager.mainscreen.detector.item.VirusUpdateItem;
import com.huawei.systemmanager.mainscreen.detector.item.WhiteListItem;
import com.huawei.systemmanager.mainscreen.detector.item.WifiItem;
import com.huawei.systemmanager.mainscreen.detector.item.WifiSecDetectItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

public class MixDetectTask extends DetectTask {
    private static final String TAG = "MixDetectTask";
    private Executor mExecutor;

    public MixDetectTask(Context context, Executor executor) {
        super(context);
        this.mExecutor = executor;
    }

    public String getTaskName() {
        return TAG;
    }

    protected Executor getExecutor() {
        return this.mExecutor;
    }

    protected void doTask() {
        publishTaskStart();
        doDetectItems();
        publishTaskFinish();
    }

    private void doDetectItems() {
        List<DetectItem> items = prepareItems();
        int size = items.size();
        for (int i = 0; i < size; i++) {
            if (isCanceled()) {
                HwLog.i(TAG, "task is already canceled!");
                return;
            }
            DetectItem item = (DetectItem) items.get(i);
            publishProgressChange(item.getClass().getSimpleName(), ((float) (i * 100)) / ((float) size));
            item.doScan();
            publishItemFount(item);
        }
    }

    private List<DetectItem> prepareItems() {
        List<DetectItem> items = Lists.newArrayList();
        items.add(new ProcessItem());
        items.add(new TrafficDataItem());
        items.add(new WhiteListItem());
        items.add(new BootupItem());
        items.add(new VirusUpdateItem());
        items.add(new HrassCallIntellItem());
        items.add(new BluetoothItem());
        items.add(new WifiItem());
        items.add(new NumberMarkItem());
        items.add(new WifiSecDetectItem());
        Iterator<DetectItem> it = items.iterator();
        while (it.hasNext()) {
            DetectItem item = (DetectItem) it.next();
            if (!item.isEnable()) {
                HwLog.i(TAG, "item is not enabled, remove it, itemType:" + item.getItemType());
                it.remove();
            }
        }
        return items;
    }

    public int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_main_screen_mix_scan);
    }
}

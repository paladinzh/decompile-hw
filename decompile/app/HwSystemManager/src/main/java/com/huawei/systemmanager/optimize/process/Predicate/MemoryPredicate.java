package com.huawei.systemmanager.optimize.process.Predicate;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug.MemoryInfo;
import android.support.v4.util.ArrayMap;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MemoryPredicate extends FutureTaskPredicate<Map<Integer, Long>, ProcessAppItem> {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final String TAG = "MemoryPredicate";
    private final Context mContext;
    private final List<Integer> mPids;

    private static class MemoryTaskCallable implements Callable<Map<Integer, Long>> {
        private ActivityManager am;
        private List<Integer> pidList;

        private MemoryTaskCallable(ActivityManager activityManager, List<Integer> list) {
            this.am = activityManager;
            this.pidList = list;
        }

        public Map<Integer, Long> call() throws Exception {
            Map<Integer, Long> res = new ArrayMap();
            int[] pids = MemoryPredicate.changeListToInt(this.pidList);
            MemoryInfo[] memoryInfos = this.am.getProcessMemoryInfo(pids);
            if (memoryInfos == null) {
                HwLog.e(MemoryPredicate.TAG, "get getProcessMemoryInfo result is null!!");
                return res;
            }
            int pidSize = pids.length;
            if (pidSize != memoryInfos.length) {
                HwLog.e(MemoryPredicate.TAG, "get getProcessMemoryInfo result length not correct!!");
                return res;
            }
            for (int i = 0; i < pidSize; i++) {
                res.put(Integer.valueOf(pids[i]), Long.valueOf(((long) memoryInfos[i].getTotalPss()) * 1024));
            }
            return res;
        }
    }

    public MemoryPredicate(Context ctx, List<Integer> pids) {
        this.mContext = ctx;
        this.mPids = pids;
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        Map<Integer, Long> pidMap = (Map) getResult();
        if (pidMap == null) {
            pidMap = Collections.emptyMap();
        }
        long memory = 0;
        for (Integer pid : input.getPids()) {
            Long temp = (Long) pidMap.get(pid);
            if (temp != null) {
                memory += temp.longValue();
            }
        }
        input.setMemoryCost(memory);
        if (memory > 0) {
            return true;
        }
        HwLog.i(TAG, input.getPackageName() + " memory is " + memory + ", did not show!");
        return false;
    }

    protected Map<Integer, Long> doInbackground() throws Exception {
        return computePidMemoryIntoThread(this.mContext, this.mPids, getThreadNum());
    }

    private int getThreadNum() {
        int threadNum = 2;
        if (CPU_COUNT <= 4) {
            threadNum = CPU_COUNT - 1;
        }
        return Math.max(1, threadNum);
    }

    private static Map<Integer, Long> computePidMemoryIntoThread(Context ctx, List<Integer> pidsList, int threadNum) throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        ActivityManager am = (ActivityManager) ctx.getSystemService("activity");
        int size = pidsList.size();
        int part = size / threadNum;
        List<FutureTask<Map<Integer, Long>>> futureTasks = Lists.newArrayListWithCapacity(threadNum);
        for (int i = 0; i < threadNum; i++) {
            int startIndex = part * i;
            int endIndex = part * (i + 1);
            if (i == threadNum - 1) {
                endIndex = size;
            }
            FutureTask<Map<Integer, Long>> futureTask = new FutureTask(new MemoryTaskCallable(am, pidsList.subList(startIndex, endIndex)));
            futureTasks.add(futureTask);
            HsmExecutor.executeTask(futureTask, "MemoryThreadPart#" + i);
        }
        Map<Integer, Long> result = new ArrayMap();
        for (FutureTask<Map<Integer, Long>> task : futureTasks) {
            result.putAll((Map) task.get());
        }
        HwLog.i(TAG, "computePidMemoryIntoThread threadNum:" + threadNum + ",cost:" + (System.currentTimeMillis() - start) + ", pid num:" + pidsList.size());
        return result;
    }

    private static int[] changeListToInt(List<Integer> list) {
        int size = list.size();
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = ((Integer) list.get(i)).intValue();
        }
        return result;
    }
}

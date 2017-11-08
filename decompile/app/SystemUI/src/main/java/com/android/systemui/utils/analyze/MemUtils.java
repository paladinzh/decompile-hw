package com.android.systemui.utils.analyze;

import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.Process;
import com.android.systemui.utils.HwLog;
import java.lang.reflect.InvocationTargetException;

public class MemUtils {
    private static Runnable gcRunnable = new Runnable() {
        public void run() {
            MemUtils.printCurrentMemoryInfo();
            try {
                Class<?> systemClass = Class.forName(System.class.getName());
                systemClass.getMethod("gc", new Class[0]).invoke(null, null);
                systemClass.getMethod("runFinalization", new Class[0]).invoke(null, null);
                systemClass.getMethod("gc", new Class[0]).invoke(null, null);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (NoSuchMethodException ex2) {
                ex2.printStackTrace();
            } catch (IllegalAccessException ex3) {
                ex3.printStackTrace();
            } catch (InvocationTargetException ex4) {
                ex4.printStackTrace();
            } catch (RuntimeException ex5) {
                ex5.printStackTrace();
            }
            MemUtils.printCurrentMemoryInfo();
        }
    };

    public static void triggerAsyncGC() {
        HwLog.i("MemUtils", "triggerAsyncGC called");
        new Thread(gcRunnable).start();
    }

    private static void printCurrentMemoryInfo() {
        MemoryInfo mem = new MemoryInfo();
        Debug.getMemoryInfo(Process.myPid(), mem);
        HwLog.i("MemUtils", "SystemUI current memory info{Pid[" + Process.myPid() + "],Total Pss[" + mem.getTotalPss() + "],Total Uss[" + mem.getTotalUss() + "],Total Private Dirty[" + mem.getTotalPrivateDirty() + "],Total Private Clean[" + mem.getTotalPrivateClean() + "],Summary Code[" + mem.getSummaryCode() + "],Summary Graphics[" + mem.getSummaryGraphics() + "],Summary JavaHeap[" + mem.getSummaryJavaHeap() + "],Summary NativeHeap[" + mem.getSummaryNativeHeap() + "],Summary PrivateOther[" + mem.getSummaryPrivateOther() + "],Summary Stack[" + mem.getSummaryStack() + "],Summary System[" + mem.getSummarySystem() + "]}");
    }

    public static void logCurrentMemoryInfo() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    MemoryInfo mem = new MemoryInfo();
                    Debug.getMemoryInfo(Process.myPid(), mem);
                    HwLog.i("MemUtils", "logCurrentMemoryInfo::current memory info{Pid[" + Process.myPid() + "],Total Pss[" + mem.getTotalPss() + "],Total Uss[" + mem.getTotalUss() + "],Total Private Dirty[" + mem.getTotalPrivateDirty() + "],Total Private Clean[" + mem.getTotalPrivateClean() + "],Summary Code[" + mem.getSummaryCode() + "],Summary Graphics[" + mem.getSummaryGraphics() + "],Summary JavaHeap[" + mem.getSummaryJavaHeap() + "],Summary NativeHeap[" + mem.getSummaryNativeHeap() + "],Summary PrivateOther[" + mem.getSummaryPrivateOther() + "],Summary Stack[" + mem.getSummaryStack() + "],Summary System[" + mem.getSummarySystem() + "]}");
                } catch (Exception e) {
                    HwLog.e("MemUtils", "logCurrentMemoryInfo:: occur exception:" + e);
                }
            }
        }).start();
    }
}

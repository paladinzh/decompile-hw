package com.android.systemui.utils.analyze;

import android.util.SparseArray;
import com.android.systemui.utils.HwLog;

public class PerfDebugUtils {
    private static SparseArray<Duration> mRecentsLaunchElapsedTime = new SparseArray();
    private static SparseArray<Duration> mRecentsRemoveAllElapsedTime = new SparseArray();

    static class Duration {
        long end = -1;
        boolean isValid = false;
        int sceneStage;
        long start = -1;

        Duration() {
        }

        public int computeDuration() {
            if (this.start < 0) {
                return -1;
            }
            if (this.end < 0) {
                return -2;
            }
            if (this.end < this.start) {
                return -3;
            }
            this.isValid = true;
            return (int) (this.end - this.start);
        }

        public String toString() {
            switch (computeDuration()) {
                case -3:
                    return String.format("stage%d=end < start invalid", new Object[]{Integer.valueOf(this.sceneStage)});
                case -2:
                    return String.format("stage%d=end invalid", new Object[]{Integer.valueOf(this.sceneStage)});
                case -1:
                    return String.format("stage%d=start invalid", new Object[]{Integer.valueOf(this.sceneStage)});
                default:
                    return String.format("stage%d=%dms", new Object[]{Integer.valueOf(this.sceneStage), Integer.valueOf(computeDuration())});
            }
        }
    }

    public static void beginSystraceSection(String sectionName) {
    }

    public static void endSystraceSection() {
    }

    public static void setThreadPolicy() {
    }

    public static void keyOperationTimeConsumed(String scenario, long startTime) {
        keyOperationTimeConsumed(scenario, startTime, 0);
    }

    public static void keyOperationTimeConsumed(String scenario, long startTime, long threshold) {
        long consumed = System.currentTimeMillis() - startTime;
        if (threshold < consumed) {
            HwLog.i("PerfDebugUtils", "keyOperationTimeConsumed of [" + scenario + "]" + " is " + consumed + "ms");
        }
    }

    public static void perfRecentsLaunchElapsedTimeBegin(int sceneStage) {
    }

    public static void perfRecentsLaunchElapsedTimeEnd(int sceneStage) {
    }

    public static void dumpRecentsLaunchTime() {
    }

    public static void perfRecentsRemoveAllElapsedTimeBegin(int sceneStage) {
    }

    public static void perfRecentsRemoveAllElapsedTimeEnd(int sceneStage) {
    }

    public static void dumpRecentsRemoveAllTime() {
    }

    private static void perfElapsedTimeBegin(String perfScene, int sceneStage, long time) {
        Duration duration = new Duration();
        duration.start = time;
        duration.sceneStage = sceneStage;
        SparseArray<Duration> spDurations = getElaspsedTimeArrayByScene(perfScene);
        if (spDurations != null) {
            spDurations.put(sceneStage, duration);
        }
    }

    private static void perfElapsedTimeEnd(String perfScene, int sceneStage, long time) {
        SparseArray<Duration> elaspsedTimeArray = getElaspsedTimeArrayByScene(perfScene);
        if (elaspsedTimeArray != null) {
            Duration duration = (Duration) elaspsedTimeArray.get(sceneStage);
            if (duration != null) {
                duration.end = time;
            }
            elaspsedTimeArray.put(sceneStage, duration);
        }
    }

    private static void dumpElaspsedTimeArray(String perfScene) {
        SparseArray<Duration> elaspsedTimeArray = getElaspsedTimeArrayByScene(perfScene);
        if (elaspsedTimeArray != null) {
            StringBuffer stringBuffer = new StringBuffer();
            int size = elaspsedTimeArray.size();
            long totalTime = 0;
            stringBuffer.append(String.format("%s:", new Object[]{perfScene}));
            stringBuffer.append("[");
            for (int i = 0; i < size; i++) {
                Duration duration = (Duration) elaspsedTimeArray.get(elaspsedTimeArray.keyAt(i));
                if (duration != null) {
                    stringBuffer.append(duration.toString());
                    stringBuffer.append(",");
                    if (duration.isValid) {
                        totalTime += (long) duration.computeDuration();
                    }
                }
            }
            stringBuffer.append(String.format("totalTime=%dms", new Object[]{Long.valueOf(totalTime)}));
            stringBuffer.append("]");
            elaspsedTimeArray.clear();
            HwLog.i("PerfDebugUtils", stringBuffer.toString());
        }
    }

    private static SparseArray<Duration> getElaspsedTimeArrayByScene(String perfScene) {
        if (perfScene.equals("RECENTS_LAUNCH")) {
            return mRecentsLaunchElapsedTime;
        }
        if (perfScene.equals("RECENTS_REMOVE_ALL")) {
            return mRecentsRemoveAllElapsedTime;
        }
        return null;
    }

    private static boolean checkRecentsLaunchTimeValid() {
        if (mRecentsLaunchElapsedTime.size() < 5) {
            return false;
        }
        Duration duration = (Duration) mRecentsLaunchElapsedTime.get(1);
        return duration != null && duration.computeDuration() >= 0;
    }

    private static boolean checkRecentsRemoveALlTimeValid() {
        if (mRecentsRemoveAllElapsedTime.size() < 5) {
            return false;
        }
        return true;
    }
}

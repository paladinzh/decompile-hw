package android.media;

public class HwMediaMonitorDummy implements IHwMediaMonitor {
    private static final String TAG = "HwMediaMonitorDummy";
    private static IHwMediaMonitor mHwMediaMonitor = null;

    public static IHwMediaMonitor getDefault() {
        IHwMediaMonitor iHwMediaMonitor;
        synchronized (HwMediaMonitorDummy.class) {
            if (mHwMediaMonitor == null) {
                mHwMediaMonitor = new HwMediaMonitorDummy();
            }
            iHwMediaMonitor = mHwMediaMonitor;
        }
        return iHwMediaMonitor;
    }

    public int writeLogMsg(int priority, int type, String msg) {
        return 0;
    }

    public int writeMediaBigData(int pid, int type, String msg) {
        return 0;
    }

    public void writeMediaBigDataByReportInf(int pid, int type, String msg) {
    }

    public int forceLogSend(int level) {
        return 0;
    }
}

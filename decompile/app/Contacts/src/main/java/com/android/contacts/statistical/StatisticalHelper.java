package com.android.contacts.statistical;

import android.content.Context;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.huawei.bd.Reporter;

public class StatisticalHelper {
    private static StatisticalHelper instance = new StatisticalHelper();
    Context mContext;

    public static class Report implements Runnable {
        private Context lContext;
        private int lId;
        private String msg;

        public Report(Context context, int eventID, String eventMsg) {
            this.lContext = context;
            this.lId = eventID;
            this.msg = eventMsg;
        }

        public void run() {
            StatisticalHelper.report(this.lContext, this.lId, this.msg);
        }
    }

    public static class ReportSimState implements Runnable {
        private Context lContext;

        ReportSimState(Context context) {
            this.lContext = context;
        }

        public void run() {
            int simPresence;
            boolean mIsSim1Present = SimFactoryManager.isSIM1CardPresent();
            boolean mIsSim2Present = SimFactoryManager.isSIM2CardPresent();
            boolean isSimEnabled = mIsSim1Present ? SimFactoryManager.isSimEnabled(0) : false;
            boolean isSimEnabled2 = mIsSim2Present ? SimFactoryManager.isSimEnabled(1) : false;
            if (mIsSim1Present) {
                simPresence = 1;
            } else {
                simPresence = 0;
            }
            if (mIsSim2Present) {
                simPresence |= 2;
            }
            boolean isSim1Ready = isSimEnabled;
            boolean isSim2Ready = isSimEnabled2;
            if (!(isSimEnabled || isSimEnabled2)) {
                simPresence = 0;
            }
            switch (simPresence) {
                case 1:
                case 2:
                    StatisticalHelper.report(this.lContext, 1006, String.format("{SIM:%d}", new Object[]{Integer.valueOf(1)}));
                    return;
                case 3:
                    StatisticalHelper.report(this.lContext, 1006, String.format("{SIM:%d}", new Object[]{Integer.valueOf(2)}));
                    return;
                default:
                    return;
            }
        }
    }

    public static StatisticalHelper getInstance() {
        return instance;
    }

    public void init(Context context) {
        if (context != null) {
            this.mContext = context;
        }
    }

    public static boolean report(Context context, int eventID, String eventMsg) {
        boolean ret = Reporter.e(context, eventID, eventMsg);
        if (HwLog.HWFLOW) {
            HwLog.i("StatisticalHelper", "eventId:" + eventID + " ret:" + ret);
        }
        return ret;
    }

    public static boolean report(int eventID) {
        boolean ret = Reporter.c(getInstance().mContext, eventID);
        if (HwLog.HWFLOW) {
            HwLog.i("StatisticalHelper", "eventId:" + eventID + " ret:" + ret);
        }
        return ret;
    }

    public static void reportYellowPageTimes(Context context) {
        if (HwLog.HWFLOW) {
            HwLog.i("StatisticalHelper", "reportYellowPageTimes");
        }
        report(1002);
    }

    public static void reportVoiceSearchTimes(Context context) {
        if (HwLog.HWFLOW) {
            HwLog.i("StatisticalHelper", "reportVoiceSearchTimes");
        }
        report(1004);
    }

    public static void reportDialPortal(Context context, int type) {
        ContactsThreadPool.getInstance().execute(new Report(context, 1005, String.format("{DIAL:%d}", new Object[]{Integer.valueOf(type)})));
    }

    private static void sendReports(int id, String record) {
        Report report = getInstance().getReport(id, record);
        if (report == null) {
            HwLog.e("Reporter", "StatisticalHelper.Report is null");
        } else {
            ContactsThreadPool.getInstance().execute(report);
        }
    }

    public static void sendReport(int id, String count) {
        sendReports(id, String.format(MapHelper.getValue(id), new Object[]{count}));
    }

    public static void sendReport(int id, int count) {
        sendReports(id, String.format(MapHelper.getValue(id), new Object[]{Integer.valueOf(count)}));
    }

    protected Report getReport(int eventID, String eventMsg) {
        if (this.mContext == null) {
            return null;
        }
        return new Report(this.mContext, eventID, eventMsg);
    }
}

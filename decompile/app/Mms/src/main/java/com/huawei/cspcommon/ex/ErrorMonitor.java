package com.huawei.cspcommon.ex;

import android.content.ComponentName;
import android.content.Intent;
import android.text.format.Time;
import com.android.internal.telephony.SmsApplication;
import com.android.messaging.util.BugleActivityUtil;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.BaseApp;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwSpecialUtils;
import com.huawei.mms.util.HwTelephony;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.MmsRadarInfoManager;
import com.huawei.mms.util.MonitorMms;
import com.huawei.mms.util.MonitorMms.EventStreamMms;

public class ErrorMonitor {

    public static class Radar {
        public static final int reportChr(int sub, int error, String msg) {
            Intent intent = new Intent("com.huawei.android.chr.action.ACTION_REPORT_CHR");
            intent.putExtra("module_id", 5000);
            intent.putExtra("event_id", error);
            intent.putExtra("sub", sub);
            intent.putExtra("app_data", msg);
            BaseApp.getApplication().sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
            MLog.d("ErrorMonitor", "report chr radar msg:" + msg);
            return 0;
        }
    }

    public static boolean procUnhandledError(int errType, String msg, Throwable tr) {
        StringBuilder info = new StringBuilder("!! ErrorMonitor checked an unhandled Error ").append(errType).append(" msg:").append(msg);
        if (tr != null) {
            info.append("  ").append(tr.getMessage());
            ExceptionMonitor.checkExcption(tr);
        }
        MLog.e("CSP_RADAR", info.toString());
        return false;
    }

    public static boolean reportErrorInfo(int errType, String msg) {
        return reportErrorInfo(errType, msg, null);
    }

    public static boolean reportErrorInfo(final int errType, final String msg, final Throwable object) {
        if (!BaseApp.isInMainThread() || !(object instanceof OutOfMemoryError)) {
            return procUnhandledError(errType, msg, object);
        }
        ThreadEx.execute(new Runnable() {
            public void run() {
                ErrorMonitor.procUnhandledError(errType, msg, object);
            }
        });
        return true;
    }

    public static String getTimeMark() {
        new Time().setToNow();
        return String.format("%2d_%02d_%02d_%02d_%02d", new Object[]{Integer.valueOf(now.month), Integer.valueOf(now.monthDay), Integer.valueOf(now.hour), Integer.valueOf(now.minute), Integer.valueOf(now.second)});
    }

    public static void reportRadar(int eventId, String reason) {
        reportRadar(eventId, 0, reason, null);
    }

    public static void reportRadar(int eventId, int subId, String reason) {
        reportRadar(eventId, subId, reason, null);
    }

    public static void reportRadar(int eventId, int subId, String reason, Throwable tr) {
        reportRadar(eventId, 0, subId, reason, null, null);
    }

    public static void reportRadar(int eventId, int errorType, int subId, String reason, String supplementInfo) {
        reportRadar(eventId, errorType, subId, reason, supplementInfo, null);
    }

    public static void reportRadar(int eventId, int errorType, int subId, String reason, String supplementInfo, Throwable tr) {
        reportRadar(eventId, errorType, subId, reason, null, supplementInfo, tr);
    }

    public static void reportRadar(int eventId, String reason, String operationType, Throwable tr) {
        reportRadar(eventId, 0, 0, reason, operationType, null, tr);
    }

    public static void reportRadar(int eventId, int errorType, int subId, String reason, String operationType, String supplementInfo, Throwable tr) {
        MLog.e("ErrorMonitor", "Radar info { eventId:" + eventId + ", errorType:" + errorType + ", subId:" + subId + ", reason:" + reason + ", operationType:" + operationType + ", supplementInfo:" + supplementInfo + " }");
        if (isNeedSendRadar(eventId, tr)) {
            EventStreamMms eventStreamMms = null;
            try {
                eventStreamMms = MonitorMms.openEventStream(eventId);
                StringBuilder infoBuilder = new StringBuilder(reason).append("  ");
                StringBuilder reasonInfo = new StringBuilder(reason).append("  ");
                switch (eventId) {
                    case 907000002:
                        getPhoneStatus(infoBuilder);
                        getDefaultSmsInfo(infoBuilder);
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, reasonInfo.toString());
                        eventStreamMms.setParam((short) 2, supplementInfo);
                        break;
                    case 907000003:
                        getPhoneStatus(infoBuilder);
                        getDefaultSmsInfo(infoBuilder);
                        reasonInfo.append(MmsRadarInfoManager.getInstance().getRadarInfo(1311));
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, subId);
                        eventStreamMms.setParam((short) 2, reasonInfo.toString());
                        eventStreamMms.setParam((short) 3, supplementInfo);
                        break;
                    case 907000004:
                        MLog.d("ErrorMonitor", "EVENTID.SMS_RECEIVE_FAILED");
                        getPhoneStatus(infoBuilder);
                        getDefaultSmsInfo(infoBuilder);
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, subId);
                        eventStreamMms.setParam((short) 2, reasonInfo.toString());
                        eventStreamMms.setParam((short) 3, supplementInfo);
                        break;
                    case 907000011:
                        getPhoneStatus(infoBuilder);
                        getMmsInfo(infoBuilder);
                        reasonInfo.append(MmsRadarInfoManager.getInstance().getRadarInfo(1331));
                        reasonInfo.append(MmsRadarInfoManager.getInstance().getRadarInfo(1330));
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, subId);
                        eventStreamMms.setParam((short) 2, reasonInfo.toString());
                        eventStreamMms.setParam((short) 3, supplementInfo);
                        break;
                    case 907000012:
                        getPhoneStatus(infoBuilder);
                        getMmsInfo(infoBuilder);
                        reasonInfo.append(MmsRadarInfoManager.getInstance().getRadarInfo(1332));
                        reasonInfo.append(MmsRadarInfoManager.getInstance().getRadarInfo(1330));
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, subId);
                        eventStreamMms.setParam((short) 2, reasonInfo.toString());
                        eventStreamMms.setParam((short) 3, supplementInfo);
                        break;
                    case 907000015:
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, subId);
                        eventStreamMms.setParam((short) 2, reasonInfo.toString());
                        eventStreamMms.setParam((short) 3, operationType);
                        eventStreamMms.setParam((short) 4, supplementInfo);
                        break;
                    case 907000016:
                        MLog.d("ErrorMonitor", "EVENTID.SMS_SETTING_FAILED");
                        getPhoneStatus(infoBuilder);
                        getDefaultSmsInfo(infoBuilder);
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, subId);
                        eventStreamMms.setParam((short) 2, reasonInfo.toString());
                        eventStreamMms.setParam((short) 3, supplementInfo);
                        break;
                    case 907000021:
                        MLog.d("ErrorMonitor", "EVENTID.SMS_APN_ACTIVITY_ERROR");
                        getPhoneStatus(infoBuilder);
                        getDefaultSmsInfo(infoBuilder);
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, subId);
                        eventStreamMms.setParam((short) 2, reasonInfo.toString());
                        eventStreamMms.setParam((short) 3, supplementInfo);
                        break;
                    case 907000022:
                        getPhoneStatus(infoBuilder);
                        getDefaultSmsInfo(infoBuilder);
                        reasonInfo.append(MmsRadarInfoManager.getInstance().getRadarInfo(1330));
                        eventStreamMms.setParam((short) 0, errorType);
                        eventStreamMms.setParam((short) 1, subId);
                        eventStreamMms.setParam((short) 2, reasonInfo.toString());
                        eventStreamMms.setParam((short) 3, supplementInfo);
                        break;
                }
                MLog.e("ErrorMonitor", "Radar send eventId[" + eventId + "] " + infoBuilder.toString());
                MLog.d("ErrorMonitor", "sendEvent return " + MonitorMms.sendEvent(eventStreamMms));
                if (isNeedToTriggerChr(eventId)) {
                    Radar.reportChr(subId, eventId, reasonInfo.toString());
                }
                if (eventStreamMms != null) {
                    MonitorMms.closeEventStream(eventStreamMms);
                }
            } catch (Throwable th) {
                if (eventStreamMms != null) {
                    MonitorMms.closeEventStream(eventStreamMms);
                }
            }
        } else {
            MLog.w("ErrorMonitor", "it's needn't report Radar!");
        }
    }

    private static boolean isNeedSendRadar(int eventId, Throwable tr) {
        if (tr instanceof SecurityException) {
            if (BugleActivityUtil.checkPermissionIfNeeded(MmsApp.getApplication(), null)) {
                MLog.w("ErrorMonitor", "Missing operation we do check, not trigger radar");
                return false;
            }
            MLog.w("ErrorMonitor", "Missing operation ", tr);
        }
        if (eventId < 907000000 || eventId > 907002999) {
            MLog.d("ErrorMonitor", "!!! RadarException unproced error " + eventId);
            return false;
        } else if (!MccMncConfig.getDefault().hasInnerOperator()) {
            return true;
        } else {
            MLog.d("ErrorMonitor", "one of the mccmnc is 46060, not trigger radar");
            return false;
        }
    }

    private static boolean isNeedToTriggerChr(int eventId) {
        switch (eventId) {
            case 907000003:
            case 907000011:
            case 907000012:
                MLog.d("CSP_RADAR", "Need To TriggerChr for error");
                return true;
            default:
                MLog.d("CSP_RADAR", "Not need to trigger Chr");
                return false;
        }
    }

    private static void getMmsInfo(StringBuilder sb) {
        HwSpecialUtils.dumpDataServiceSettings(sb);
        sb.append("  ");
    }

    private static void getPhoneStatus(StringBuilder sb) {
        sb.append(getDualModeName()).append(";  ");
        if (MmsApp.getDefaultTelephonyManager().isNetworkRoaming()) {
            sb.append("In roaming");
        }
        getCardInfo(sb);
    }

    private static String getDualModeName() {
        switch (MessageUtils.getDsdsMode()) {
            case 0:
                return "DSDS_MODE_SINGLE";
            case 1:
                return "DSDS_MODE_CDMA_GSM";
            case 2:
                return "DSDS_MODE_UMTS_GSM";
            case 3:
                return "DSDS_MODE_TDSCDMA_GSM";
            default:
                return "DSDS_MODE_UNKNOW";
        }
    }

    private static String getCardStatusName(int state) {
        switch (state) {
            case 0:
                return "CARD_INVALID";
            case 1:
                return "CARD_VALID";
            case 2:
                return "CARD_NOT_INSERT";
            default:
                return "Unknow status " + state;
        }
    }

    private static void getCardInfo(StringBuilder sb) {
        if (MessageUtils.isMultiSimEnabled()) {
            int state = MessageUtils.getIccCardStatus(0);
            sb.append("Card-1 state <").append(getCardStatusName(state)).append(">  ");
            if (state == 1) {
                sb.append(MmsApp.getDefaultMSimTelephonyManager().getSimOperator(0));
            }
            state = MessageUtils.getIccCardStatus(1);
            sb.append(";  Card-2 state <").append(getCardStatusName(state)).append(">  ");
            if (state == 1) {
                sb.append(MmsApp.getDefaultMSimTelephonyManager().getSimOperator(1));
                return;
            }
            return;
        }
        sb.append("Card state <").append(getCardStatusName(MessageUtils.getIccCardStatus())).append(">  ");
        sb.append(HwTelephony.getDefault().getSimOperator());
    }

    private static void getDefaultSmsInfo(StringBuilder strBuilder) {
        String defaultSmsPakage = "";
        ComponentName componentName = SmsApplication.getDefaultMmsApplication(BaseApp.getApplication(), false);
        if (componentName != null) {
            defaultSmsPakage = componentName.getPackageName();
        }
        strBuilder.append("default sms:").append(defaultSmsPakage);
        strBuilder.append("  ");
    }
}

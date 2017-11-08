package com.huawei.mms.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.popu.util.ViewPartId;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor.Radar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MmsRadarInfoManager {
    private static final MmsRadarInfoManager mmsRadarInfoManager = new MmsRadarInfoManager();
    private final HashMap<Integer, ArrayList<String>> errorInfoMap = new HashMap();
    private final ServiceHandler mHandler;
    private boolean mIsMms = false;
    private MmsRadarInfoData mMmsRadarInfoData = null;
    private final AtomicInteger mSendingSms = new AtomicInteger(0);
    private int mSendingSmsSub = 0;
    private final Looper mServiceLooper;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            MLog.d("MmsRadarInfoManager", "handle message:" + MmsRadarInfoManager.this.getEventDescription(msg.what));
            switch (msg.what) {
                case 100:
                    MmsRadarInfoManager.this.mSendingSmsSub = msg.arg1;
                    sendEmptyMessageDelayed(VTMCDataCache.MAXSIZE, Constant.MINUTE);
                    return;
                case 101:
                    removeMessages(VTMCDataCache.MAXSIZE);
                    return;
                case 102:
                    MmsRadarInfoManager.this.mSendingSmsSub = msg.arg1;
                    MmsRadarInfoManager.this.mSendingSms.getAndIncrement();
                    sendEmptyMessageDelayed(ViewPartId.PART_BODY_HORIZ_TABLE, 180000);
                    return;
                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    MmsRadarInfoManager.this.mSendingSms.getAndDecrement();
                    removeMessages(ViewPartId.PART_BODY_HORIZ_TABLE);
                    return;
                case VTMCDataCache.MAXSIZE /*500*/:
                    MmsRadarInfoManager.this.handleSaveMessageTimeout();
                    return;
                case ViewPartId.PART_BODY_HORIZ_TABLE /*501*/:
                    MmsRadarInfoManager.this.handleSendSmsTimeout();
                    return;
                default:
                    return;
            }
        }
    }

    private MmsRadarInfoManager() {
        HandlerThread thread = new HandlerThread("MmsRadarInfoManager", 10);
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mHandler = new ServiceHandler(this.mServiceLooper);
        this.mMmsRadarInfoData = new MmsRadarInfoData();
    }

    public static synchronized MmsRadarInfoManager getInstance() {
        MmsRadarInfoManager mmsRadarInfoManager;
        synchronized (MmsRadarInfoManager.class) {
            mmsRadarInfoManager = mmsRadarInfoManager;
        }
        return mmsRadarInfoManager;
    }

    public void writeLogMsg(int sceneDef, String msg) {
        Integer sceneDefInteger = Integer.valueOf(sceneDef);
        ArrayList<String> radarInfo = (ArrayList) this.errorInfoMap.get(sceneDefInteger);
        if (radarInfo == null) {
            radarInfo = new ArrayList();
        }
        if (radarInfo.size() > 20) {
            radarInfo.remove(0);
            radarInfo.add(msg);
        } else {
            radarInfo.add(msg);
        }
        this.errorInfoMap.put(sceneDefInteger, radarInfo);
    }

    public String getRadarInfo(int sceneDef) {
        String ret = "";
        ArrayList<String> radarInfo = (ArrayList) this.errorInfoMap.get(Integer.valueOf(sceneDef));
        if (radarInfo == null) {
            return ret;
        }
        ret = radarInfo.toString();
        radarInfo.clear();
        return ret;
    }

    public void reportReceiveOrSendResult(boolean isSuccess, int type, int sub, int errorId, String des) {
        MLog.d("MmsRadarInfoManager", "reportReceiveOrSendResult: isSuccess: " + isSuccess + " type:" + type + " sub:" + sub + " errorId:" + errorId + " des:" + des);
        this.mMmsRadarInfoData.updateSelfRepairPara(isSuccess, type, sub, errorId, des);
    }

    private String getEventDescription(int event) {
        switch (event) {
            case 100:
                return "EVENT_SEND_MESSAGE_USER_CLICK_SEND_BUTTON";
            case 101:
                return "EVENT_SEND_MESSAGE_SAVED_TO_DB_DONE";
            case 102:
                return "EVENT_SEND_SMS_BEGIN";
            case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                return "EVENT_SEND_SMS_DONE";
            case VTMCDataCache.MAXSIZE /*500*/:
                return "EVENT_SAVE_MESSAGE_TO_DB_TIMEOUT";
            case ViewPartId.PART_BODY_HORIZ_TABLE /*501*/:
                return "EVENT_SEND_SMS_TIMEOUT";
            default:
                return "unknown event";
        }
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public void setIsMms(boolean isMms) {
        this.mIsMms = isMms;
    }

    private void handleSaveMessageTimeout() {
        if (this.mIsMms) {
            Radar.reportChr(this.mSendingSmsSub, 1331, "mms save fail");
        } else {
            Radar.reportChr(this.mSendingSmsSub, 1311, "sms save fail");
        }
    }

    private void handleSendSmsTimeout() {
        if (this.mSendingSms.get() > 0) {
            MLog.d("MmsRadarInfoManager", "sms send fail time out");
            Radar.reportChr(this.mSendingSmsSub, 1311, "sms send timeout");
        }
        this.mSendingSms.set(0);
    }

    public boolean isSmscCorrectNow(int sub) {
        return this.mMmsRadarInfoData.isSmscCorrect(sub);
    }

    public boolean isSmsCanSendNow(int sub) {
        return this.mMmsRadarInfoData.isSmsCanSend(sub);
    }

    public void initSelfRepairPara() {
        this.mMmsRadarInfoData.initSelfRepairPara();
    }

    public void initSelfRepairPara(int sub) {
        this.mMmsRadarInfoData.initSelfRepairPara(sub);
    }

    public void syncSmscFromCard(int sub) {
        this.mMmsRadarInfoData.updateSmscAddrToSharedPref(sub);
    }

    public void tryToRepairSmscInCard(int sub) {
        this.mMmsRadarInfoData.updateSmscAddrToSimCard(sub);
    }
}

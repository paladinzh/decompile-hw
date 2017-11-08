package com.huawei.systemmanager.netassistant.traffic.trafficcorrection;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.v4.view.InputDeviceCompat;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.SparseArray;
import com.google.android.collect.Lists;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.ext.SimCardManagerExt;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficStatisticsInfo;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.TrafficCorrectionWrapper;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.TrafficCorrectionWrapper.IHwTrafficCorrectionListener;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import tmsdk.bg.module.network.ProfileInfo;

public class NATAutoAdjustService extends Service implements IHwTrafficCorrectionListener, MessageHandler {
    private static final int MSG_ANALYSIS_SMS_TIMEOUT = 200;
    private static final int MSG_CORRECTION_TIMEOUT = 201;
    private static final int MSG_REQUEST_PROFILE = 202;
    private static final int MSG_REQUEST_REFRESHVIEW = 203;
    private static final String TAG = NATAutoAdjustService.class.getSimpleName();
    private static final long TRAFFIC_CORRECTION_TIME_LIMIT = 180000;
    private static final long TRAFFIC_SMS_CORRECTION_TIME_LIMIT = 10000;
    private GenericHandler mHandler;
    private boolean mIsNetAssistantEnable;
    private SparseArray<TrafficInfo> mSparseTrafficInfo = new SparseArray();

    public void onCreate() {
        super.onCreate();
        this.mHandler = new GenericHandler(this);
        this.mIsNetAssistantEnable = CustomizeManager.getInstance().isFeatureEnabled(30);
        HwLog.i(TAG, "isNetAssistantEnable = " + this.mIsNetAssistantEnable);
        if (this.mIsNetAssistantEnable) {
            TrafficCorrectionWrapper.getInstance().setTrafficCorrectionListener(this);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null || !this.mIsNetAssistantEnable) {
            return 2;
        }
        if (TextUtils.equals(intent.getAction(), ShareCfg.AUTO_ADJUST_SMS_ACTION)) {
            if (intent.getBundleExtra(ShareCfg.EXTRA_ADJUST_SERVICE_DATA) != null) {
                HwLog.i(TAG, "onStartCommand , AUTO_ADJUST_SMS_ACTION");
                HsmStat.statE(Events.E_NETASSISTANT_CALIBRATE_RECEIVER_SMS);
                startSmsAnalysis(intent.getBundleExtra(ShareCfg.EXTRA_ADJUST_SERVICE_DATA));
            }
        } else if (TextUtils.equals(intent.getAction(), ShareCfg.SEND_ADJUST_SMS_ACTION)) {
            HwLog.i(TAG, "onStartCommand , SEND_ADJUST_SMS_ACTION");
            if (intent.getStringExtra(ShareCfg.EXTRA_SEND_SMS_IMSI) != null) {
                startAdjust(intent.getStringExtra(ShareCfg.EXTRA_SEND_SMS_IMSI));
            }
        } else if (TextUtils.equals(intent.getAction(), ShareCfg.REQUEST_PROFIL_ACTION) && intent.getStringExtra(ShareCfg.EXTRA_SEND_SMS_IMSI) != null) {
            HwLog.i(TAG, "onStartCommand , REQUEST_PROFIL_ACTION");
            startRequestProfile(intent.getStringExtra(ShareCfg.EXTRA_SEND_SMS_IMSI));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startAdjust(String imsi) {
        int simIndex = SimCardManager.getInstance().getSimcardIndex(imsi);
        TrafficCorrectionWrapper.getInstance().setConfig(simIndex, NetAssistantDBManager.getInstance().getAdjustProvince(imsi), NetAssistantDBManager.getInstance().getAdjustCity(imsi), NetAssistantDBManager.getInstance().getAdjustProvider(imsi), NetAssistantDBManager.getInstance().getAdjustBrand(imsi), NetAssistantDBManager.getInstance().getSettingBeginDate(imsi));
        TrafficCorrectionWrapper.getInstance().startCorrection(simIndex);
        this.mHandler.sendEmptyMessageDelayed(201, TRAFFIC_CORRECTION_TIME_LIMIT);
    }

    private void startSmsAnalysis(Bundle bundle) {
        SmsMessage msg = null;
        StringBuffer sb = new StringBuffer();
        if (bundle != null) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            if (smsObj != null) {
                int slotid = bundle.getInt("slot");
                TrafficInfo info = (TrafficInfo) this.mSparseTrafficInfo.get(slotid);
                if (info == null) {
                    HwLog.i(TAG, "get info is null slotid = " + slotid);
                    return;
                }
                for (Object object : smsObj) {
                    msg = SmsMessage.createFromPdu((byte[]) object);
                    if (!(msg == null || msg.mWrappedSmsMessage == null)) {
                        sb.append(msg.getDisplayMessageBody());
                    }
                }
                HwLog.i(TAG, "startSmsAnalysis slotid = " + slotid);
                if (!(msg == null || msg.mWrappedSmsMessage == null || !TextUtils.equals(msg.getOriginatingAddress(), info.getQuerryPort()))) {
                    TrafficCorrectionWrapper.getInstance().analysisSMS(slotid, info.getQuerryCode(), info.getQuerryPort(), sb.toString());
                    Message message = this.mHandler.obtainMessage();
                    message.what = 200;
                    message.arg1 = slotid;
                    this.mHandler.sendMessageDelayed(message, 10000);
                }
            }
        }
    }

    private void startRequestProfile(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            HwLog.i(TAG, "startRequestProfile, arg is wrong");
            return;
        }
        TrafficCorrectionWrapper.getInstance().requestProfile(SimCardManager.getInstance().getSimcardIndex(imsi));
        this.mHandler.sendEmptyMessageDelayed(202, TRAFFIC_CORRECTION_TIME_LIMIT);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mIsNetAssistantEnable) {
            TrafficCorrectionWrapper.getInstance().setTrafficCorrectionListener(null);
        }
        this.mHandler.quiteLooper();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onNeedSmsCorrection(int simIndex, String queryCode, String queryPort) {
        ArrayList<String> sms = Lists.newArrayList();
        sms.add(queryCode);
        HwLog.i(TAG, "onNeedSmsCorrection simIndex =" + simIndex + " queryCode = " + queryCode + " queryPort = " + queryPort);
        this.mSparseTrafficInfo.put(simIndex, new TrafficInfo(new TrafficItem(), new TrafficItem(), new TrafficItem(), simIndex, queryCode, queryPort));
        if (this.mSparseTrafficInfo.get(simIndex) == null) {
            HwLog.e(TAG, "this slot should not to adjust");
            HsmStat.statE(Events.E_NETASSISTANT_CALIBRATE_NOT_RESPONCE_WTHERE_SEND_SMS);
            return;
        }
        HsmStat.statE(Events.E_NETASSISTANT_CALIBRATE_RESPONCE_SEND_SMS);
        SimCardManagerExt.sendMessageFromPlatform(queryPort, sms, simIndex, GlobalContext.getContext());
    }

    public void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes) {
        HwLog.i(TAG, "onTrafficInfoNotify simIndex = " + simIndex + " trafficClass = " + trafficClass + " subClass = " + subClass + " kBytes = " + kBytes);
        TrafficInfo info = (TrafficInfo) this.mSparseTrafficInfo.get(simIndex);
        if (info != null) {
            long resByte = ((long) kBytes) * 1024;
            if (trafficClass == 1) {
                setNormalTrafficSubClass(info, subClass, resByte);
            }
            if (trafficClass == 3) {
                set4GTrafficSubClass(info, subClass, resByte);
            }
            if (trafficClass == 2) {
                setLeisureTrafficSubClass(info, subClass, resByte);
            }
        }
    }

    public void onProfileNotify(int simIndex, ProfileInfo info) {
        HwLog.i(TAG, "onProfileNotify");
        if (info == null) {
            HwLog.i(TAG, "onProfileNotify,arg is wrong");
            return;
        }
        try {
            Stub.asInterface(ServiceManager.getService(NetAssistantService.NET_ASSISTANT)).putSimProfileDes(new SimProfileDes(info));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void set4GTrafficSubClass(TrafficInfo info, int subClass, long resByte) {
        if (subClass == 258) {
            info.m4GItem.setUsedTraffic(resByte);
        } else if (subClass == InputDeviceCompat.SOURCE_KEYBOARD) {
            info.m4GItem.setLeftTraffic(resByte);
        } else if (subClass == 259) {
            info.m4GItem.setTotalTraffic(resByte);
        }
    }

    private void setNormalTrafficSubClass(TrafficInfo info, int subClass, long resByte) {
        if (subClass == 258) {
            info.mNormalItem.setUsedTraffic(resByte);
        } else if (subClass == InputDeviceCompat.SOURCE_KEYBOARD) {
            info.mNormalItem.setLeftTraffic(resByte);
        } else if (subClass == 259) {
            info.mNormalItem.setTotalTraffic(resByte);
        }
    }

    private void setLeisureTrafficSubClass(TrafficInfo info, int subClass, long resByte) {
        if (subClass == 258) {
            info.mLeisureItem.setUsedTraffic(resByte);
        } else if (subClass == InputDeviceCompat.SOURCE_KEYBOARD) {
            info.mLeisureItem.setLeftTraffic(resByte);
        } else if (subClass == 259) {
            info.mLeisureItem.setTotalTraffic(resByte);
        }
    }

    public void onError(int simIndex, int errorCode) {
        HwLog.e(TAG, "Traffic Auto Adjust error, simIndex = " + simIndex + " errorCode = " + errorCode);
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, String.valueOf(errorCode));
        HsmStat.statE((int) Events.E_NETASSISTANT_CALIBRATE_ONERROR, statParam);
    }

    private void setTrafficInfoInner(String imsi, long realByte) {
        long curTime = DateUtil.getCurrentTimeMills();
        int month = DateUtil.getYearMonth(imsi);
        TrafficStatisticsInfo tsInfo = new TrafficStatisticsInfo(imsi, month, 301);
        tsInfo.get();
        tsInfo.setTraffic(realByte);
        tsInfo.setRecordTime(curTime);
        tsInfo.save(null);
        HwLog.i(TAG, "setTrafficInfoInner month = " + month + " adjust bytes = " + realByte);
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 200:
                startCorrection(msg.arg1);
                HwLog.i(TAG, "receiver msg MSG_ANALYSIS_SMS_TIMEOUT");
                sendBroadcastToRefreshView();
                return;
            case 201:
                HwLog.i(TAG, "receiver msg MSG_CORRECTION_TIMEOUT, stop service");
                if (!this.mHandler.hasMessages(201)) {
                    this.mHandler.removeCallbacksAndMessages(null);
                    this.mSparseTrafficInfo.clear();
                    stopSelf();
                    return;
                }
                return;
            case 202:
                HwLog.i(TAG, "receiver msg MSG_REQUEST_PROFILE, stop service");
                if (!this.mHandler.hasMessages(201) && !this.mHandler.hasMessages(200)) {
                    this.mHandler.removeCallbacksAndMessages(null);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void startCorrection(int simIndex) {
        TrafficInfo info = (TrafficInfo) this.mSparseTrafficInfo.get(simIndex);
        if (info == null) {
            HwLog.i(TAG, "info is null");
            return;
        }
        HsmStat.statE(Events.E_NETASSISTANT_CALIBRATE_RECEIVER_TENCENT_ANALYSIS);
        String imsi = SimCardManager.getInstance().getSimcardByIndex(info.getSimIndex());
        if (info.isReadyToAnalysis()) {
            HwLog.i(TAG, "ready to normal analysis");
            setTrafficInfoInner(imsi, info.getTrafficResult());
            setTrafficMonthTotal(imsi, info);
            HsmStat.statE(Events.E_NETASSISTANT_TRAFFIC_CORRECTION_SUCCESS);
        } else if (info.isReadyToLocalAnalysis()) {
            long totalPackage = NatSettingManager.getLimitNotifyByte(imsi);
            long adjustValue = (totalPackage - info.mNormalItem.mLeftTraffic) - info.m4GItem.mLeftTraffic;
            HwLog.i(TAG, "ready to local package analysis total = " + totalPackage + " adjust value = " + adjustValue);
            if (adjustValue < 0) {
                adjustValue = 0;
            }
            setTrafficInfoInner(imsi, adjustValue);
            HsmStat.statE(Events.E_NETASSISTANT_TRAFFIC_CORRECTION_SUCCESS);
        } else {
            HsmStat.statE(Events.E_NETASSISTANT_TRAFFIC_CORRECTION_FAILED);
        }
        setLeisureTrafficInner(imsi, info.mLeisureItem);
    }

    private void setTrafficMonthTotal(String imsi, TrafficInfo info) {
        HwLog.i(TAG, "setTrafficMonthTotal normal = " + info.mNormalItem.mTotalTraffic + " 4G = " + info.m4GItem.mTotalTraffic);
        long totalByte = info.mNormalItem.mTotalTraffic + info.m4GItem.mTotalTraffic;
        if (totalByte > 0) {
            NatSettingManager.setMonthLimitNotifyByte(imsi, totalByte);
        }
    }

    public void setLeisureTrafficInner(String imsi, TrafficItem leisureTraffic) {
        if (leisureTraffic.isTrafficValid()) {
            TrafficStatisticsInfo trafficInfo = new TrafficStatisticsInfo(imsi, DateUtil.getYearMonth(imsi), 302);
            trafficInfo.setTraffic(leisureTraffic.getTrafficUsed());
            trafficInfo.save(null);
        }
    }

    private void sendBroadcastToRefreshView() {
        HwLog.i(TAG, "sendBroadcastToRefreshView");
        Intent intent = new Intent(ShareCfg.RECEIVE_REFRESH_TRAFFIC_ACTION);
        intent.setPackage(getPackageName());
        sendBroadcastAsUser(intent, UserHandle.OWNER, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }
}

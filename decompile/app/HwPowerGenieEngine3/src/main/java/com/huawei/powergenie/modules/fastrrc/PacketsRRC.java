package com.huawei.powergenie.modules.fastrrc;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.debugtest.LogUtils;
import java.util.ArrayList;

public final class PacketsRRC {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.pg_fd_debug", false);
    private static final ArrayList<String> WAKEUP_ALARM = new ArrayList<String>() {
        {
            add("com.tencent.mobileqq");
            add("com.sina.weibo");
            add("com.sina.weibog3");
            add("com.tencent.mm");
            add("com.google.android.gsf");
            add("com.tencent.news");
            add("com.Qunar");
            add("com.huawei.appmarket");
            add("com.baidu.netdisk");
            add("vStudio.Android.Camera360");
            add("com.youdao.dict");
            add("com.netease.newsreader.activity");
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    PacketsRRC.this.removePendingMessage(100);
                    if (PacketsRRC.this.mICoreContext.isScreenOff() || !PacketsRRC.this.mPacketsControl.isDataTrans()) {
                        if (PacketsRRC.this.mICoreContext.isScreenOff() && PacketsRRC.this.mPacketsControl.updateDataTransmitting() > 0) {
                            PacketsRRC.this.listenerDataScreenOff(1000);
                            break;
                        } else {
                            PacketsRRC.this.disableRrc();
                            break;
                        }
                    }
                    if (PacketsRRC.DEBUG) {
                        Log.d("PacketsRRC", "we shouldn't disable RRC when transmitting!!!");
                    }
                    if (!PacketsRRC.this.mListenerData) {
                        sendEmptyMessageDelayed(101, 2000);
                        break;
                    }
                    break;
                case 101:
                    if (PacketsRRC.DEBUG) {
                        Log.d("PacketsRRC", "try to release rrc!!! dataTrans=" + PacketsRRC.this.mPacketsControl.isDataTrans());
                    }
                    if (PacketsRRC.this.mPacketsControl.isDataTrans()) {
                        if (PacketsRRC.this.mNeedListenerData) {
                            PacketsRRC.this.mListenerData = true;
                            PacketsRRC.this.mPacketsControl.updateDataTransmitting();
                            if (PacketsRRC.DEBUG) {
                                Log.e("PacketsRRC", "handler listener data start!!!");
                                break;
                            }
                        }
                    }
                    PacketsRRC.this.removePendingMessage(100);
                    PacketsRRC.this.disableRrc();
                    break;
                    break;
                case 102:
                    if (SystemClock.elapsedRealtime() - PacketsRRC.this.mScreenOffRRCSendTime < ((long) (PacketsRRC.this.mScreenOffDelay + 2000))) {
                        if (PacketsRRC.this.mPacketsControl.updateDataTransmitting() <= 0) {
                            PacketsRRC.this.disableRrc();
                            break;
                        } else {
                            PacketsRRC.this.listenerDataScreenOff(2000);
                            break;
                        }
                    }
                    Log.d("PacketsRRC", "time out , drop this message");
                    return;
            }
        }
    };
    private ICoreContext mICoreContext;
    protected int mListenerCount = 0;
    private boolean mListenerData = false;
    private boolean mNeedListenerData = false;
    private PacketsControl mPacketsControl;
    private int mScreenOffDelay = -1;
    private long mScreenOffRRCSendTime = -1;
    private int mScreenOffTryTimes = -1;

    protected PacketsRRC(PacketsControl packets, ICoreContext pgContext) {
        this.mPacketsControl = packets;
        this.mICoreContext = pgContext;
    }

    protected boolean handleActionInner(PowerAction action) {
        if (DEBUG) {
            Log.d("PacketsRRC", "handleAction:" + action);
        }
        long expireMs = System.currentTimeMillis() - action.getTimeStamp();
        if (expireMs >= 2000) {
            Log.w("PacketsRRC", "drop action because of timeout:" + expireMs + "ms");
            return true;
        } else if (!this.mPacketsControl.isPermit(true)) {
            return true;
        } else {
            this.mListenerData = false;
            this.mNeedListenerData = false;
            switch (action.getActionId()) {
                case 201:
                    this.mNeedListenerData = true;
                    long revPacketsNum = this.mPacketsControl.updateDataTransmitting();
                    if (revPacketsNum <= 0) {
                        if (DEBUG) {
                            Log.d("PacketsRRC", "no data transmitting.");
                            break;
                        }
                    }
                    postDisableRrc(2000);
                    LogUtils.c("WEBPAGE_FINISH", revPacketsNum);
                    break;
                    break;
                case 202:
                    postRrcScreenOffOrOn(500, 2000);
                    LogUtils.c("DOWNLOAD_FINISH", "TYPE_APP_DOWNLOAD");
                    break;
                case 216:
                    postRrcScreenOffOrOn(500, 2000);
                    LogUtils.c("HW_PUSH_RRC", "hw push");
                    break;
                case 224:
                    int alarmType = action.getExtraInt();
                    String pkg = action.getPkgName();
                    if ((alarmType == 0 || alarmType == 2) && pkg != null && WAKEUP_ALARM.contains(pkg)) {
                        if (this.mPacketsControl.updateDataTransmitting() <= 0) {
                            if (DEBUG) {
                                Log.d("PacketsRRC", "no data transmitting. only " + pkg + " alarm");
                                break;
                            }
                        }
                        postRrcScreenOffOrOn(1000, -1);
                        if (this.mICoreContext.isScreenOff()) {
                            LogUtils.c("ALARM_RRC", pkg);
                            break;
                        }
                    }
                    break;
                case 249:
                    if (this.mPacketsControl.updateDataTransmitting() <= 0) {
                        if (DEBUG) {
                            Log.d("PacketsRRC", "no data transmitting. only ims notification");
                            break;
                        }
                    }
                    postRrcScreenOffOrOn(500, 2000);
                    LogUtils.c("IMS_RRC", action.getPkgName());
                    break;
                    break;
                default:
                    Log.w("PacketsRRC", "action unknown!!!");
                    break;
            }
            return true;
        }
    }

    protected void postRrcScreenOffOrOn(long offDealyMs, long onDelayMs) {
        if (this.mICoreContext.isScreenOff() && offDealyMs >= 0) {
            if (offDealyMs > 0) {
                this.mScreenOffTryTimes = 3;
            }
            postDisableRrc(offDealyMs);
        } else if (!this.mICoreContext.isScreenOff() && onDelayMs >= 0) {
            postDisableRrc(onDelayMs);
        }
    }

    protected void postDisableRrc(long delayMillis) {
        if (DEBUG) {
            Log.d("PacketsRRC", "delay: " + delayMillis + "ms to disable rrc");
        }
        cancelPendingMsg();
        if (delayMillis > 0) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), delayMillis);
            return;
        }
        disableRrc();
    }

    protected void setListenerData(boolean enable) {
        this.mListenerData = enable;
    }

    protected void listenerDataScreenOff(int interval) {
        if (DEBUG) {
            Log.d("PacketsRRC", "listener data when screen off, delay " + interval + "ms to listen");
        }
        if (this.mICoreContext.isScreenOff()) {
            removePendingMessage(102);
            if (this.mScreenOffTryTimes >= 0 && interval > 0) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(102), (long) interval);
                this.mScreenOffRRCSendTime = SystemClock.elapsedRealtime();
                this.mScreenOffDelay = interval;
                this.mScreenOffTryTimes--;
            }
            return;
        }
        if (DEBUG) {
            Log.d("PacketsRRC", "screen on, exit listener");
        }
    }

    protected void listenerData() {
        if (!this.mListenerData) {
            this.mListenerCount = 0;
        } else if (this.mPacketsControl.updateDataTransmitting() > 10) {
            this.mListenerData = false;
            this.mListenerCount = 0;
            removePendingMessage(100);
            if (DEBUG) {
                Log.e("PacketsRRC", "large trans data to stop listener. ");
            }
        } else if (this.mListenerCount == 0) {
            postDisableRrc(10000);
            this.mListenerCount++;
        } else if (this.mListenerCount == 1) {
            if (!this.mHandler.hasMessages(100)) {
                postDisableRrc(5000);
            }
            this.mListenerCount++;
        } else {
            removePendingMessage(100);
            this.mListenerData = false;
            this.mListenerCount = 0;
            if (DEBUG) {
                Log.e("PacketsRRC", "data activity count too more to stop listener. ");
            }
        }
    }

    public void removePendingMessage(int eventId) {
        if (this.mHandler.hasMessages(eventId)) {
            this.mHandler.removeMessages(eventId);
        }
    }

    public void cancelPendingMsg() {
        removePendingMessage(100);
        removePendingMessage(101);
        removePendingMessage(102);
    }

    private void disableRrc() {
        this.mPacketsControl.offRRC(1);
        if (!this.mListenerData && this.mNeedListenerData) {
            this.mListenerData = true;
            this.mPacketsControl.updateDataTransmitting();
            if (DEBUG) {
                Log.i("PacketsRRC", "disableRrc listener data start!!!");
            }
        }
    }
}

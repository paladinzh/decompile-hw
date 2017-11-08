package com.huawei.powergenie.modules.fastrrc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class PacketsFD {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.pg_fd_debug", false);
    private Context mContext;
    protected int mDelaytimeScreenOff = SystemProperties.getInt("ro.config.hw_fdtimer_screenOff", 4000);
    protected int mDelaytimeScreenOn = SystemProperties.getInt("ro.config.hw_fdtimer_screenOn", 10000);
    private long mFDMsgSendTime = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (PacketsFD.DEBUG) {
                        Log.d("PacketsFD", "msg into fast dormancy");
                    }
                    long interval = SystemClock.elapsedRealtime() - PacketsFD.this.mFDMsgSendTime;
                    if (interval <= 60000) {
                        PacketsFD.this.enableFastDormancy();
                        PacketsFD.this.releaseWakeLock();
                        break;
                    }
                    Log.w("PacketsFD", "timeout to trigger fastdormancy, maybe system in sleep, interval:" + interval);
                    PacketsFD.this.releaseWakeLock();
                    return;
                case 101:
                    PacketsFD.this.checkDataActivity();
                    PacketsFD.this.startCheckDataActivity();
                    break;
            }
        }
    };
    private boolean mHoldWakeLock = false;
    private ICoreContext mICoreContext;
    private boolean mIsUsbOnEnableFD = SystemProperties.getBoolean("ro.config.hw_USB_on_enable_FD", false);
    private String mOperator = null;
    private HashMap<String, FastDormancyPara> mOperatorFDPara = new HashMap();
    private PacketsControl mPacketsControl;
    private boolean mScreenOn = true;
    private WakeLock mWakeLock = null;

    private static class FastDormancyPara {
        public boolean fdEnable = false;
        public int fdTimerScreenoff = -1;
        public int fdTimerScreenon = -1;
        public boolean usbFDEnable = false;

        public FastDormancyPara(boolean enable, boolean usbEnable, int timerScreenOn, int timerScreenOff) {
            this.fdEnable = enable;
            this.usbFDEnable = usbEnable;
            this.fdTimerScreenon = timerScreenOn;
            this.fdTimerScreenoff = timerScreenOff;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(" FD Enable=").append(this.fdEnable);
            builder.append(" USB Enable=").append(this.usbFDEnable);
            builder.append(" FD Timer On=").append(this.fdTimerScreenon);
            builder.append(" FD Timer Off=").append(this.fdTimerScreenoff);
            return builder.toString();
        }
    }

    public PacketsFD(PacketsControl packets, ICoreContext pgContext) {
        boolean z = false;
        this.mPacketsControl = packets;
        this.mICoreContext = pgContext;
        this.mContext = this.mICoreContext.getContext();
        if (!this.mICoreContext.isScreenOff()) {
            z = true;
        }
        this.mScreenOn = z;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "FD");
    }

    protected boolean initializeFDFeature() {
        boolean fd;
        if (loadFastDormancyConf(this.mContext) && this.mOperatorFDPara.size() > 0) {
            this.mOperator = getSimOperator();
            Log.i("PacketsFD", "FastDormancy-hw operator:" + this.mOperator);
        }
        if (this.mOperator == null || !this.mOperatorFDPara.containsKey(this.mOperator)) {
            fd = SystemProperties.getBoolean("ro.config.hw_fast_dormancy", false);
        } else {
            fd = ((FastDormancyPara) this.mOperatorFDPara.get(this.mOperator)).fdEnable;
        }
        if (fd) {
            updateFDPara();
        }
        return fd;
    }

    protected boolean isAdjustFDFeatureByOpt() {
        return this.mOperatorFDPara.size() > 0;
    }

    private String getSimOperator() {
        if (TelephonyManager.getDefault().getSimState() != 5) {
            return null;
        }
        String numeric = TelephonyManager.getDefault().getSimOperator();
        if (numeric == null || numeric.length() <= 4) {
            return null;
        }
        return numeric.substring(0, 3) + numeric.substring(3);
    }

    protected boolean handleSimStateChg() {
        boolean fdEnable = this.mPacketsControl.isSupportFDFeature();
        String operator = getSimOperator();
        if (operator == null || operator.equals(this.mOperator)) {
            return fdEnable;
        }
        this.mOperator = operator;
        Log.i("PacketsFD", "sim state chg, operator:" + this.mOperator);
        if (this.mOperatorFDPara.containsKey(operator)) {
            return ((FastDormancyPara) this.mOperatorFDPara.get(operator)).fdEnable;
        }
        return SystemProperties.getBoolean("ro.config.hw_fast_dormancy", false);
    }

    protected boolean handleActionInner(PowerAction action) {
        boolean z = false;
        if (DEBUG) {
            Log.d("PacketsFD", "handleActionInner:" + action);
        }
        switch (action.getActionId()) {
            case 224:
                if (!(this.mScreenOn || -1 == this.mDelaytimeScreenOff)) {
                    triggerFastDormancyNow();
                    break;
                }
            case 300:
                this.mScreenOn = true;
                stopCheckDataActivity();
                cancelFastDormancy();
                break;
            case 301:
                this.mScreenOn = false;
                cancelFastDormancy();
                startFDScnOff();
                break;
            case 330:
                if (!this.mICoreContext.isScreenOff()) {
                    z = true;
                }
                this.mScreenOn = z;
                if (updateFDPara() && !this.mScreenOn) {
                    startFDScnOff();
                    break;
                }
            default:
                Log.w("PacketsFD", "action unknown!!!");
                break;
        }
        return true;
    }

    private void triggerFastDormancyNow() {
        if (this.mHandler.hasMessages(100) && SystemClock.elapsedRealtime() - this.mFDMsgSendTime >= ((long) this.mDelaytimeScreenOff)) {
            Log.i("PacketsFD", "fastdormancy timeout when screen off, trigger now");
            this.mHandler.removeMessages(100);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(100));
        }
    }

    private boolean enableFastDormancy() {
        boolean ret = false;
        if (!isPermitDisableFD(true)) {
            Log.i("PacketsFD", "not permit to do FD");
        } else if (this.mScreenOn || this.mPacketsControl.updateDataTransmitting() <= 0) {
            ret = this.mPacketsControl.offRRC(2);
        } else {
            if (DEBUG) {
                Log.i("PacketsFD", "There still has data transmition, do nothing!");
            }
            return false;
        }
        return ret;
    }

    private void startFDScnOff() {
        if (-1 == this.mDelaytimeScreenOff) {
            stopCheckDataActivity();
            cancelFastDormancy();
            return;
        }
        startCheckDataActivity(0);
    }

    private void startCheckDataActivity(long delay) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), delay);
    }

    private void startCheckDataActivity() {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), 500);
    }

    protected void stopCheckDataActivity() {
        if (this.mHandler.hasMessages(101)) {
            this.mHandler.removeMessages(101);
        }
    }

    private void checkDataActivity() {
        if (isPermitDisableFD(DEBUG)) {
            if (this.mPacketsControl.updateDataTransmitting() > 0) {
                startFastDormancy();
            } else {
                triggerFastDormancyNow();
            }
        }
    }

    protected void startFastDormancy() {
        if (isPermitDisableFD(DEBUG)) {
            int delaytime;
            cancelFastDormancy();
            if (this.mScreenOn) {
                delaytime = this.mDelaytimeScreenOn;
            } else {
                delaytime = this.mDelaytimeScreenOff;
                if (delaytime <= 5000) {
                    acquireWakeLock();
                }
            }
            this.mFDMsgSendTime = SystemClock.elapsedRealtime();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), (long) delaytime);
            Log.i("PacketsFD", "Delay " + delaytime + "ms to send FastDormancy Cmd.");
        }
    }

    private void acquireWakeLock() {
        if (!this.mHoldWakeLock) {
            this.mWakeLock.acquire();
            this.mHoldWakeLock = true;
        }
    }

    private void releaseWakeLock() {
        if (this.mHoldWakeLock) {
            this.mHoldWakeLock = false;
            this.mWakeLock.release();
        }
    }

    protected void cancelFastDormancy() {
        releaseWakeLock();
        if (this.mHandler.hasMessages(100)) {
            this.mHandler.removeMessages(100);
        }
        if (DEBUG) {
            Log.d("PacketsFD", "cancel FD msg");
        }
    }

    private void fixFdDelayTime() {
        if (this.mDelaytimeScreenOff > 60000) {
            this.mDelaytimeScreenOff = 60000;
        } else if (this.mDelaytimeScreenOff < 1000 && this.mDelaytimeScreenOff != -1) {
            this.mDelaytimeScreenOff = 1000;
        }
        if (this.mDelaytimeScreenOn > 60000) {
            this.mDelaytimeScreenOn = 60000;
        } else if (this.mDelaytimeScreenOn < 1000 && this.mDelaytimeScreenOn != -1) {
            this.mDelaytimeScreenOn = 1000;
        }
    }

    private boolean updateFDPara() {
        int fdTimerScreenoff;
        boolean isDirty = false;
        if (this.mOperator == null || !this.mOperatorFDPara.containsKey(this.mOperator)) {
            this.mIsUsbOnEnableFD = SystemProperties.getBoolean("ro.config.hw_USB_on_enable_FD", false);
            this.mDelaytimeScreenOn = SystemProperties.getInt("ro.config.hw_fdtimer_screenOn", 10000);
            fdTimerScreenoff = SystemProperties.getInt("ro.config.hw_fdtimer_screenOff", 4000);
        } else {
            this.mIsUsbOnEnableFD = ((FastDormancyPara) this.mOperatorFDPara.get(this.mOperator)).usbFDEnable;
            this.mDelaytimeScreenOn = ((FastDormancyPara) this.mOperatorFDPara.get(this.mOperator)).fdTimerScreenon;
            fdTimerScreenoff = ((FastDormancyPara) this.mOperatorFDPara.get(this.mOperator)).fdTimerScreenoff;
        }
        if (fdTimerScreenoff != this.mDelaytimeScreenOff) {
            isDirty = true;
        }
        this.mDelaytimeScreenOff = fdTimerScreenoff;
        fixFdDelayTime();
        return isDirty;
    }

    private boolean isPermitDisableFD(boolean enableLog) {
        boolean z;
        PacketsControl packetsControl = this.mPacketsControl;
        if (DEBUG || this.mScreenOn) {
            z = true;
        } else {
            z = false;
        }
        if (!packetsControl.isPermit(z)) {
            return false;
        }
        if (this.mPacketsControl.isCDMA3GNetType()) {
            if (enableLog) {
                Log.i("PacketsFD", "CDMA 3G Network Type! do nothing");
            }
            return false;
        } else if (-1 == this.mDelaytimeScreenOn && this.mScreenOn) {
            if (enableLog) {
                Log.i("PacketsFD", "Screen on. do nothing.");
            }
            return false;
        } else if (-1 == this.mDelaytimeScreenOff && !this.mScreenOn) {
            if (enableLog) {
                Log.i("PacketsFD", "Screen off. do nothing.");
            }
            return false;
        } else if (this.mIsUsbOnEnableFD || !((IDeviceState) this.mICoreContext.getService("device")).isCharging()) {
            return true;
        } else {
            if (enableLog) {
                Log.i("PacketsFD", "is connecting power now.do nothing.");
            }
            return false;
        }
    }

    private InputStream getConfStream(String file) {
        InputStream inStream;
        try {
            inStream = new FileInputStream("/data/cust/xml/" + file);
        } catch (FileNotFoundException e) {
            Log.w("PacketsFD", "config not found:/data/cust/xml/" + file);
            try {
                inStream = new FileInputStream("/product/etc/hwpg/" + file);
            } catch (FileNotFoundException e2) {
                Log.w("PacketsFD", "config not found:/product/etc/hwpg/" + file);
                return null;
            }
        }
        return inStream;
    }

    private boolean loadFastDormancyConf(Context context) {
        boolean ret = false;
        InputStream inputStream = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            inputStream = getConfStream("fastdormancy_parameter.xml");
            if (inputStream == null) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
            parser.setInput(inputStream, "UTF-8");
            String mccmnc = null;
            boolean fdEable = false;
            boolean usbFDEnable = false;
            int fdTimerScreenon = -1;
            int fdTimerScreenoff = -1;
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String nodeName = parser.getName();
                switch (eventType) {
                    case NativeAdapter.PLATFORM_HI /*2*/:
                        if (!"operator".equals(nodeName)) {
                            if (!"fd_enable".equals(nodeName)) {
                                if (!"usb_fd_enable".equals(nodeName)) {
                                    if (!"fd_timer_screenon".equals(nodeName)) {
                                        if (!"fd_timer_screenoff".equals(nodeName)) {
                                            break;
                                        }
                                        fdTimerScreenoff = Integer.parseInt(parser.nextText());
                                        break;
                                    }
                                    fdTimerScreenon = Integer.parseInt(parser.nextText());
                                    break;
                                }
                                usbFDEnable = "true".equals(parser.nextText());
                                break;
                            }
                            fdEable = "true".equals(parser.nextText());
                            break;
                        }
                        mccmnc = parser.getAttributeValue(0) + parser.getAttributeValue(1);
                        break;
                    case NativeAdapter.PLATFORM_K3V3 /*3*/:
                        if (!"operator".equals(nodeName)) {
                            break;
                        }
                        FastDormancyPara fdPara = new FastDormancyPara(fdEable, usbFDEnable, fdTimerScreenon, fdTimerScreenoff);
                        if (mccmnc != null) {
                            this.mOperatorFDPara.put(mccmnc, fdPara);
                            if (DEBUG) {
                                Log.i("PacketsFD", "FD operator para config:" + mccmnc + " ---> " + fdPara.toString());
                            }
                        }
                        mccmnc = null;
                        fdEable = false;
                        usbFDEnable = false;
                        fdTimerScreenon = -1;
                        fdTimerScreenoff = -1;
                        break;
                    default:
                        break;
                }
            }
            ret = true;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (!ret) {
                this.mOperatorFDPara.clear();
            }
            return ret;
        } catch (FileNotFoundException e3) {
            Log.i("PacketsFD", "fastdormancy parameter not found");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
        } catch (XmlPullParserException e5) {
            e5.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e2222) {
                    e2222.printStackTrace();
                }
            }
        } catch (IOException e6) {
            e6.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e22222) {
                    e22222.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e222222) {
                    e222222.printStackTrace();
                }
            }
        }
    }
}

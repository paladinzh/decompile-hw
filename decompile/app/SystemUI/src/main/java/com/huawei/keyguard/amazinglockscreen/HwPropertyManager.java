package com.huawei.keyguard.amazinglockscreen;

import android.text.format.DateFormat;
import com.huawei.keyguard.HwUnlockConstants$ListenerType;
import com.huawei.keyguard.amazinglockscreen.data.Charge;
import com.huawei.keyguard.amazinglockscreen.data.ExpressParser;
import com.huawei.keyguard.amazinglockscreen.data.Missed;
import com.huawei.keyguard.amazinglockscreen.data.Move;
import com.huawei.keyguard.amazinglockscreen.data.OwnerInfo;
import com.huawei.keyguard.amazinglockscreen.data.Position;
import com.huawei.keyguard.amazinglockscreen.data.Time;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class HwPropertyManager {
    private static HwPropertyManager sInstance;
    private boolean isTriggered;
    private HashMap<HwUnlockConstants$ListenerType, ArrayList<PropertyListener>> mCallbacks = new HashMap();
    private ExpressParser mExpressParser = ExpressParser.getInstance();

    public interface PropertyListener {
        void onChange();
    }

    public static synchronized void setsInstance(HwPropertyManager sInstance) {
        synchronized (HwPropertyManager.class) {
            sInstance = sInstance;
        }
    }

    public static synchronized HwPropertyManager getInstance() {
        HwPropertyManager hwPropertyManager;
        synchronized (HwPropertyManager.class) {
            if (sInstance == null) {
                sInstance = new HwPropertyManager();
            }
            hwPropertyManager = sInstance;
        }
        return hwPropertyManager;
    }

    public static synchronized void clean() {
        synchronized (HwPropertyManager.class) {
            if (sInstance != null) {
                sInstance.unregisterCallbaks();
            }
        }
    }

    private HwPropertyManager() {
    }

    public void registerCallback(HwUnlockConstants$ListenerType type, PropertyListener callback) {
        if (this.mCallbacks.get(type) == null) {
            this.mCallbacks.put(type, new ArrayList());
        }
        ((ArrayList) this.mCallbacks.get(type)).add(callback);
    }

    public void unregisterCallbaks() {
        this.mCallbacks.clear();
        this.mExpressParser.clean();
        setsInstance(null);
    }

    public void setTriggerFlag(boolean value) {
        this.isTriggered = value;
    }

    public boolean getTriggerFlag() {
        return this.isTriggered;
    }

    public void setLanguage(String language) {
        this.mExpressParser.setSystemValue("language", language);
    }

    public String getLanguage() {
        return (String) this.mExpressParser.getSystemValue("language");
    }

    public void updatePosition(int x, int y) {
        if (x >= 0 && y >= 0) {
            Position p = (Position) this.mExpressParser.getSystemValue("point");
            float scalePara = AmazingUtils.getScalePara();
            if (scalePara == 1.0f || scalePara == 0.0f) {
                p.setX(x);
                p.setY(y);
            } else {
                p.setX((int) (((float) x) / scalePara));
                p.setY((int) (((float) y) / scalePara));
            }
            notify(HwUnlockConstants$ListenerType.POSITION);
        }
    }

    public void updateMove(int x, int y) {
        Move p = (Move) this.mExpressParser.getSystemValue("move");
        float scalePara = AmazingUtils.getScalePara();
        if (scalePara == 1.0f || scalePara == 0.0f) {
            p.setX(x);
            p.setY(y);
        } else {
            p.setX((int) (((float) x) * scalePara));
            p.setY((int) (((float) y) * scalePara));
        }
        notify(HwUnlockConstants$ListenerType.MOVE);
    }

    public void updateStart(boolean start) {
        this.mExpressParser.setSystemValue("start", Boolean.valueOf(start));
        notify(HwUnlockConstants$ListenerType.START);
    }

    public void updateTime(String hour1, String hour2, String min1, String min2, String ampm, boolean showampm) {
        HwLog.d("HwPropertyManager", "updateTime:" + showampm);
        HwLog.d("HwPropertyManager", "updateTime: hour1" + hour1);
        HwLog.d("HwPropertyManager", "updateTime: hour2" + hour2);
        HwLog.d("HwPropertyManager", "updateTime: min1" + min1);
        HwLog.d("HwPropertyManager", "updateTime: min2" + min2);
        this.mExpressParser.setSystemValue("time", new Time(hour1, hour2, min1, min2, ampm, showampm));
        updateTimeValue(Integer.parseInt(DateFormat.format("HH", System.currentTimeMillis()).toString()));
        notify(HwUnlockConstants$ListenerType.TIME);
    }

    public void updateDate(String date) {
        updateDayOfWeek();
        this.mExpressParser.setSystemValue("date", date);
        notify(HwUnlockConstants$ListenerType.DATE);
    }

    private void updateDayOfWeek() {
        int week = 1;
        try {
            week = Calendar.getInstance().get(7);
        } catch (IllegalArgumentException e) {
            HwLog.w("HwPropertyManager", "time is not set!");
        } catch (ArrayIndexOutOfBoundsException e2) {
            HwLog.w("HwPropertyManager", "ArrayIndexOutOfBoundsException!");
        }
        this.mExpressParser.setSystemValue("week", Integer.valueOf(week));
        notify(HwUnlockConstants$ListenerType.WEEK);
    }

    public void updateDefaultTime(String hour1, String hour2, String min1, String min2, String ampm, boolean showampm) {
        this.mExpressParser.setSystemValue("time_default", new Time(hour1, hour2, min1, min2, ampm, showampm));
        notify(HwUnlockConstants$ListenerType.TIME_DEFAULT);
    }

    public void updateRoamingTime(String hour1, String hour2, String min1, String min2, String ampm, boolean showampm) {
        this.mExpressParser.setSystemValue("time_roaming", new Time(hour1, hour2, min1, min2, ampm, showampm));
        notify(HwUnlockConstants$ListenerType.TIME_ROAMING);
    }

    public void updateDefaultDate(String date) {
        this.mExpressParser.setSystemValue("date_default", date);
        notify(HwUnlockConstants$ListenerType.DATE_DEFAULT);
    }

    public void updateRoamingDate(String date) {
        this.mExpressParser.setSystemValue("date_roaming", date);
        notify(HwUnlockConstants$ListenerType.DATE_ROAMING);
    }

    public void updateDualClockVisibility(boolean visible) {
        this.mExpressParser.setSystemValue("dualclock", Boolean.valueOf(visible));
        notify(HwUnlockConstants$ListenerType.DUALCLOCK);
        if (visible) {
            notify(HwUnlockConstants$ListenerType.CLOCKDESC_DEFAULT);
            notify(HwUnlockConstants$ListenerType.CLOCKDESC_ROAMING);
        }
    }

    public void updateBatteryInfo(String info, boolean showBattery, int level, String chargePercent) {
        this.mExpressParser.setSystemValue("charge", new Charge(info, showBattery, level, chargePercent));
        notify(HwUnlockConstants$ListenerType.CHARGE);
    }

    public void updatePressState(boolean isPressed) {
        this.mExpressParser.setSystemValue("press", Boolean.valueOf(isPressed));
        notify(HwUnlockConstants$ListenerType.PRESS_STATE);
    }

    public void updateCallCount(String msg, int count) {
        this.mExpressParser.setSystemValue("call", new Missed(msg, count));
        notify(HwUnlockConstants$ListenerType.CALL_COUNT);
    }

    public void updateMessageCount(String msg, int count) {
        this.mExpressParser.setSystemValue("message", new Missed(msg, count));
        notify(HwUnlockConstants$ListenerType.MESSAGE_COUNT);
    }

    public void updateOwnerInfo(String info, boolean isShow) {
        if (info != null) {
            info = info.replace("\r", BuildConfig.FLAVOR).replace("\n", " ");
        }
        this.mExpressParser.setSystemValue("ownerinfo", new OwnerInfo(info, isShow));
        notify(HwUnlockConstants$ListenerType.OWNER_INFO);
    }

    public void updateUnlockTip(String tip) {
        this.mExpressParser.setSystemValue("unlocktip", tip);
        notify(HwUnlockConstants$ListenerType.UNLOCK_TIP);
    }

    public void updateUnlockerState(String unlockerName, int state) {
        HwLog.i("HwPropertyManager", "mytest onTouchEvent updateUnlockerState unlockerName=" + unlockerName + " state" + state);
        this.mExpressParser.setSystemValue(unlockerName, Integer.valueOf(state));
        notify(HwUnlockConstants$ListenerType.UNLOCK_STATE);
    }

    public void updateUnlockerMoveX(String unlockerName, int moveX) {
        HwLog.i("HwPropertyManager", "onTouchEvent updateUnlockerMoveX unlockerName=" + unlockerName + " moveX" + moveX);
        this.mExpressParser.setSystemValue(unlockerName, Integer.valueOf(moveX));
        notify(HwUnlockConstants$ListenerType.UNLOCK_MOVEX);
    }

    public void updateUnlockerMoveY(String unlockerName, int moveY) {
        HwLog.i("HwPropertyManager", "onTouchEvent updateUnlockerMoveY unlockerName=" + unlockerName + " moveY" + moveY);
        this.mExpressParser.setSystemValue(unlockerName, Integer.valueOf(moveY));
        notify(HwUnlockConstants$ListenerType.UNLOCK_MOVEY);
    }

    public void updateMusicVisible(int value) {
        HwLog.i("HwPropertyManager", "mytest updateMusicVisible value=" + value);
        this.mExpressParser.setSystemValue("music_visible", Integer.valueOf(value));
        notify(HwUnlockConstants$ListenerType.MUSIC_VISIBLE);
    }

    public void updateMusicState(int state) {
        HwLog.i("HwPropertyManager", "mytest  updateMusicState state" + state);
        this.mExpressParser.setSystemValue("music_state", Integer.valueOf(state));
        notify(HwUnlockConstants$ListenerType.MUSIC_STATE);
    }

    public void updateMusicText(String musicText) {
        HwLog.i("HwPropertyManager", "mytest  updateMusicText musicText" + musicText);
        this.mExpressParser.setSystemValue("music_text", musicText);
        notify(HwUnlockConstants$ListenerType.MUSIC_TEXT);
    }

    public void updateMusicPrevState(int state) {
        this.mExpressParser.setSystemValue("music_prev", Integer.valueOf(state));
        notify(HwUnlockConstants$ListenerType.MUSIC_PREV);
    }

    public void updateMusicNextState(int state) {
        this.mExpressParser.setSystemValue("music_next", Integer.valueOf(state));
        notify(HwUnlockConstants$ListenerType.MUSIC_NEXT);
    }

    public void updateMusicPauseState(int state) {
        this.mExpressParser.setSystemValue("music_pause", Integer.valueOf(state));
        notify(HwUnlockConstants$ListenerType.MUSIC_PAUSE);
    }

    public void updateMusicPlayState(int state) {
        this.mExpressParser.setSystemValue("music_play", Integer.valueOf(state));
        notify(HwUnlockConstants$ListenerType.MUSIC_PLAY);
    }

    public void updateTimeValue(int value) {
        this.mExpressParser.setSystemValue("time_value", Integer.valueOf(value));
        notify(HwUnlockConstants$ListenerType.TIME_VALUE);
    }

    private void notify(HwUnlockConstants$ListenerType type) {
        ArrayList<PropertyListener> propertyListeners = (ArrayList) this.mCallbacks.get(type);
        if (propertyListeners != null) {
            for (int i = 0; i < propertyListeners.size(); i++) {
                ((PropertyListener) propertyListeners.get(i)).onChange();
            }
        }
    }
}

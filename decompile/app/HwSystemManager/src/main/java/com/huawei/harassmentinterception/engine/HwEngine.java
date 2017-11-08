package com.huawei.harassmentinterception.engine;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.engine.HwEngineManager.EngineId;
import com.huawei.harassmentinterception.update.IHwUpdateListener;
import java.util.List;

public abstract class HwEngine {
    protected Context mContext = null;
    protected EngineMode mMode = EngineMode.OFF;

    public enum EngineMode {
        OFF,
        BLOCK_BLACKLIST,
        BLOCK_STRANGER,
        BLOCK_UNKNOW,
        INTELLIGENT,
        PASS_WHITELIST,
        PASS_CONTACT,
        USER_DEFINE,
        BLOCK_ALL
    }

    public abstract void destroyEngine(int i);

    public abstract EngineId getEngineId();

    public abstract boolean handleSms(SmsIntentWrapper smsIntentWrapper);

    public abstract void initEngine(EngineMode engineMode, int i);

    public HwEngine(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void setMode(EngineMode mode) {
        this.mMode = mode;
    }

    public EngineMode getMode() {
        return this.mMode;
    }

    public boolean handleIncomingCall(String number) {
        return false;
    }

    public int doUpdate(IHwUpdateListener hwUpdateListener) {
        return 0;
    }

    public int cancelUpdate() {
        return 0;
    }

    public void setAutoUpdateInterval(int nInterval) {
    }

    public int getAutoUpdateInterval() {
        return 0;
    }

    public void triggerAutoUpdate() {
    }

    public void addBlackList(String number) {
    }

    public void setBlackList(List<String> list) {
    }

    public List<String> getBlackList() {
        return null;
    }

    public void addWhiteList(String number) {
    }

    public void setWhiteList(List<String> list) {
    }

    public List<String> getWhiteList() {
        return null;
    }
}

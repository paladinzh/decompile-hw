package com.huawei.systemmanager.hsmstat;

import android.content.Context;
import com.huawei.systemmanager.hsmstat.IHsmStat.SimpleHsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.theme.v1.HiStat;

public class HiHsmStat extends SimpleHsmStat {
    private static final String TAG = "HsmStat_info";
    private final Context mContext;
    private Boolean mStateSucess = null;

    public HiHsmStat(Context ctx) {
        this.mContext = ctx;
        setMaxMsg(Long.valueOf(HsmStatConst.DATE_MAX_LENTH));
        setStatTimeOut(Long.valueOf(72));
        setRFull(false);
    }

    public boolean eStat(String key, String value) {
        return onStatE(this.mContext, key, value);
    }

    public boolean rStat() {
        return onStatR(this.mContext);
    }

    public boolean isEnable() {
        return checkCanStat();
    }

    private boolean checkCanStat() {
        if (this.mStateSucess == null) {
            return true;
        }
        return this.mStateSucess.booleanValue();
    }

    private void setCanState(boolean can) {
        this.mStateSucess = Boolean.valueOf(can);
    }

    private void setMaxMsg(Long l) {
        if (checkCanStat()) {
            try {
                HiStat.setMaxMsg(l);
                setCanState(true);
            } catch (Exception e) {
                HwLog.e("HsmStat_info", "setMaxMsg " + e);
                setCanState(false);
            } catch (Error e2) {
                HwLog.e("HsmStat_info", "setMaxMsg " + e2);
                setCanState(false);
            }
        }
    }

    private void setStatTimeOut(Long l) {
        if (checkCanStat()) {
            try {
                HiStat.setRecordExpireTimeOut(l);
                setCanState(true);
            } catch (Exception e) {
                HwLog.e("HsmStat_info", "setStatTimeOut " + e);
                setCanState(false);
            } catch (Error e2) {
                HwLog.e("HsmStat_info", "setStatTimeOut " + e2);
                setCanState(false);
            }
        }
    }

    private void setRFull(boolean r) {
        if (checkCanStat()) {
            try {
                HiStat.setRFull(r);
                setCanState(true);
            } catch (Exception e) {
                HwLog.e("HsmStat_info", "setRFull " + e);
                setCanState(false);
            } catch (Error e2) {
                HwLog.e("HsmStat_info", "setRFull " + e2);
                setCanState(false);
            }
        }
    }

    private boolean onStatR(Context context) {
        if (!checkCanStat()) {
            return false;
        }
        try {
            HiStat.r(context);
            return true;
        } catch (Exception e) {
            HwLog.e("HsmStat_info", "onStatR Exception " + e);
            setCanState(false);
            return false;
        } catch (Error e2) {
            HwLog.e("HsmStat_info", "onStatR Error " + e2);
            setCanState(false);
            return false;
        }
    }

    private boolean onStatE(Context context, String key, String value) {
        if (!checkCanStat()) {
            return false;
        }
        try {
            HiStat.e(context, key, value);
            return true;
        } catch (Exception e) {
            HwLog.e("HsmStat_info", "onStatE Exception " + e);
            setCanState(false);
            return false;
        } catch (Error e2) {
            HwLog.e("HsmStat_info", "onStatE Error " + e2);
            setCanState(false);
            return false;
        }
    }
}

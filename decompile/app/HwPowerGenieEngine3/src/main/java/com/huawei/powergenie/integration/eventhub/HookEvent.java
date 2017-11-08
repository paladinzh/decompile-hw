package com.huawei.powergenie.integration.eventhub;

import android.text.TextUtils;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.adapter.RawEvent;

public final class HookEvent extends Event {
    private int mPid;
    private String mPkgName;
    private long mTimestamp;
    private String mValue1;
    private String mValue2;
    private String mValue3;
    private String mValue4;

    public HookEvent(int evtID) {
        super(evtID);
    }

    protected void fill(RawEvent rawEvt) {
        super.setEventId(rawEvt.getEventId());
        this.mPid = rawEvt.getPid();
        this.mTimestamp = rawEvt.getTimestamp();
        this.mPkgName = null;
        this.mValue1 = null;
        this.mValue2 = null;
        this.mValue3 = null;
        this.mValue4 = null;
        parsePayload(rawEvt.getPayload());
    }

    public void resetAs(HookEvent evt) {
        super.setEventId(evt.getEventId());
        this.mPid = evt.getPid();
        this.mPkgName = evt.getPkgName();
        this.mValue1 = evt.getValue1();
        this.mValue2 = evt.getValue2();
        this.mValue3 = evt.getValue3();
        this.mValue4 = evt.getValue4();
    }

    protected void parsePayload(String payload) {
        if (!TextUtils.isEmpty(payload)) {
            String[] msgSeg = payload.split("\\|");
            for (int seg = 0; seg < msgSeg.length; seg++) {
                switch (seg) {
                    case NativeAdapter.PLATFORM_QCOM /*0*/:
                        this.mPkgName = msgSeg[seg];
                        break;
                    case NativeAdapter.PLATFORM_MTK /*1*/:
                        this.mValue1 = msgSeg[seg];
                        break;
                    case NativeAdapter.PLATFORM_HI /*2*/:
                        this.mValue2 = msgSeg[seg];
                        break;
                    case NativeAdapter.PLATFORM_K3V3 /*3*/:
                        this.mValue3 = msgSeg[seg];
                        break;
                    case 4:
                        this.mValue4 = msgSeg[seg];
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void updatePkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public void setValue1(String val) {
        this.mValue1 = val;
    }

    public void setValue2(String val) {
        this.mValue2 = val;
    }

    public void setValue3(String val) {
        this.mValue3 = val;
    }

    public void setValue4(String val) {
        this.mValue4 = val;
    }

    public int getType() {
        return 2;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public String getValue1() {
        return this.mValue1;
    }

    public String getValue2() {
        return this.mValue2;
    }

    public String getValue3() {
        return this.mValue3;
    }

    public String getValue4() {
        return this.mValue4;
    }

    public int getPid() {
        return this.mPid;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" EventID=").append(getEventId());
        builder.append(" Pkg=").append(this.mPkgName);
        if (this.mValue1 != null) {
            builder.append(" Value1=").append(this.mValue1);
        }
        if (this.mValue2 != null) {
            builder.append(" Value2=").append(this.mValue2);
        }
        if (this.mValue3 != null) {
            builder.append(" Value3=").append(this.mValue3);
        }
        if (this.mValue4 != null) {
            builder.append(" Value4=").append(this.mValue4);
        }
        builder.append(" Pid=").append(this.mPid);
        return builder.toString();
    }
}

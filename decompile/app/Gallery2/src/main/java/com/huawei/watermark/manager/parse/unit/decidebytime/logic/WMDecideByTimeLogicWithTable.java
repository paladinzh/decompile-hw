package com.huawei.watermark.manager.parse.unit.decidebytime.logic;

import com.huawei.watermark.wmutil.WMCollectionUtil;
import java.util.Vector;

public class WMDecideByTimeLogicWithTable implements WMDecideByTimeBaseLogic {
    public Vector<TimeToValue> mTimeToValues = new Vector();

    public static class TimeToValue {
        int end_hour;
        int start_hour;
        String value;

        public TimeToValue(int startH, int endH, String v) {
            this.start_hour = startH;
            this.end_hour = endH;
            this.value = v;
        }
    }

    public void addTimeToValueElement(TimeToValue value) {
        this.mTimeToValues.add(value);
    }

    public String getValueWithHour(int hour) {
        return getValueByTime(new long[]{(long) hour});
    }

    public String getValueByTime(long[] time) {
        if (WMCollectionUtil.isEmptyCollection(this.mTimeToValues)) {
            return null;
        }
        String res = null;
        for (int i = 0; i < this.mTimeToValues.size(); i++) {
            TimeToValue temp = (TimeToValue) this.mTimeToValues.elementAt(i);
            if (((long) temp.start_hour) <= time[0] && ((long) temp.end_hour) > time[0]) {
                res = temp.value;
                break;
            }
        }
        return res;
    }
}

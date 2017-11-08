package com.huawei.watermark.manager.parse.unit.decidebytime.view;

import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithTable;
import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithTable.TimeToValue;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.baseview.WMDecideByTimeElementImage;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WM24HourImage extends WMDecideByTimeElementImage {
    private static String[] mTimePngs = new String[]{"", "one.png", "two.png", "three.png", "four.png", "five.png", "six.png", "seven.png", "eight.png", "nine.png", "ten.png"};
    private int bit;
    private int[] index = new int[]{4, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3};

    public WM24HourImage(XmlPullParser parser) {
        super(parser);
        this.bit = getIntByAttributeName(parser, "bit");
    }

    public void consWMDecideByTimeLogic() {
        this.mWMDecideByTimeLogic = new WMDecideByTimeLogicWithTable();
        setTimeToValues();
    }

    public void setTimeToValues() {
        if (this.bit == 0) {
            for (int i = 0; i < 24; i++) {
                addTimeToValueElement(new TimeToValue(i, i + 1, mTimePngs[this.index[i]]));
            }
        } else if (this.bit == 1) {
            addTimeToValueElement(new TimeToValue(0, 1, mTimePngs[10]));
            addTimeToValueElement(new TimeToValue(1, 10, mTimePngs[0]));
            addTimeToValueElement(new TimeToValue(10, 24, mTimePngs[10]));
        } else if (this.bit == 2) {
            addTimeToValueElement(new TimeToValue(0, 1, mTimePngs[2]));
            addTimeToValueElement(new TimeToValue(1, 20, mTimePngs[0]));
            addTimeToValueElement(new TimeToValue(20, 24, mTimePngs[2]));
        }
    }

    public void addTimeToValueElement(TimeToValue value) {
        ((WMDecideByTimeLogicWithTable) this.mWMDecideByTimeLogic).addTimeToValueElement(value);
    }

    public String getValueWithTime() {
        return ((WMDecideByTimeLogicWithTable) this.mWMDecideByTimeLogic).getValueWithHour(Calendar.getInstance().get(11));
    }
}

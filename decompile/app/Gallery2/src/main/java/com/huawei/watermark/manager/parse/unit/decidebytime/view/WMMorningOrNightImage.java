package com.huawei.watermark.manager.parse.unit.decidebytime.view;

import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithTable;
import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithTable.TimeToValue;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.baseview.WMDecideByTimeElementImage;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMMorningOrNightImage extends WMDecideByTimeElementImage {
    public WMMorningOrNightImage(XmlPullParser parser) {
        super(parser);
    }

    public void consWMDecideByTimeLogic() {
        this.mWMDecideByTimeLogic = new WMDecideByTimeLogicWithTable();
        setTimeToValues();
    }

    public void setTimeToValues() {
        addTimeToValueElement(new TimeToValue(0, 5, "wan an.png"));
        addTimeToValueElement(new TimeToValue(5, 12, "zao an.png"));
        addTimeToValueElement(new TimeToValue(12, 18, "wu an.png"));
        addTimeToValueElement(new TimeToValue(18, 24, "wan an.png"));
    }

    public void addTimeToValueElement(TimeToValue value) {
        ((WMDecideByTimeLogicWithTable) this.mWMDecideByTimeLogic).addTimeToValueElement(value);
    }

    public String getValueWithTime() {
        return ((WMDecideByTimeLogicWithTable) this.mWMDecideByTimeLogic).getValueWithHour(Calendar.getInstance().get(11));
    }
}

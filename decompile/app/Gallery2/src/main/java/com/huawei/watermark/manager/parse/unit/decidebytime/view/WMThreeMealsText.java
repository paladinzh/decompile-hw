package com.huawei.watermark.manager.parse.unit.decidebytime.view;

import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithTable;
import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithTable.TimeToValue;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.baseview.WMDecideByTimeElementText;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMThreeMealsText extends WMDecideByTimeElementText {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMThreeMealsText.class.getSimpleName());

    public WMThreeMealsText(XmlPullParser parser) {
        super(parser);
    }

    public void consWMDecideByTimeLogic() {
        this.mWMDecideByTimeLogic = new WMDecideByTimeLogicWithTable();
        setTimeToValues();
    }

    public void setTimeToValues() {
        addTimeToValueElement(new TimeToValue(0, 5, "water_mark_my_supper"));
        addTimeToValueElement(new TimeToValue(5, 10, "water_mark_my_breakfast"));
        addTimeToValueElement(new TimeToValue(10, 15, "water_mark_my_Lunch"));
        addTimeToValueElement(new TimeToValue(15, 21, "water_mark_my_dinner"));
        addTimeToValueElement(new TimeToValue(21, 24, "water_mark_my_supper"));
    }

    public void addTimeToValueElement(TimeToValue value) {
        ((WMDecideByTimeLogicWithTable) this.mWMDecideByTimeLogic).addTimeToValueElement(value);
    }

    public String getValueWithTime() {
        return ((WMDecideByTimeLogicWithTable) this.mWMDecideByTimeLogic).getValueWithHour(Calendar.getInstance().get(11));
    }
}

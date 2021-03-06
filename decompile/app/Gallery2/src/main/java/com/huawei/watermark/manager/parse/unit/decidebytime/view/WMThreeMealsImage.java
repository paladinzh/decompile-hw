package com.huawei.watermark.manager.parse.unit.decidebytime.view;

import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithTable;
import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithTable.TimeToValue;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.baseview.WMDecideByTimeElementImage;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMThreeMealsImage extends WMDecideByTimeElementImage {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMThreeMealsImage.class.getSimpleName());

    public WMThreeMealsImage(XmlPullParser parser) {
        super(parser);
    }

    public void consWMDecideByTimeLogic() {
        this.mWMDecideByTimeLogic = new WMDecideByTimeLogicWithTable();
        setTimeToValues();
    }

    public void setTimeToValues() {
        addTimeToValueElement(new TimeToValue(0, 5, "dinner.png"));
        addTimeToValueElement(new TimeToValue(5, 10, "breakfast.png"));
        addTimeToValueElement(new TimeToValue(10, 15, "lunch.png"));
        addTimeToValueElement(new TimeToValue(15, 24, "dinner.png"));
    }

    public void addTimeToValueElement(TimeToValue value) {
        ((WMDecideByTimeLogicWithTable) this.mWMDecideByTimeLogic).addTimeToValueElement(value);
    }

    public String getValueWithTime() {
        return ((WMDecideByTimeLogicWithTable) this.mWMDecideByTimeLogic).getValueWithHour(Calendar.getInstance().get(11));
    }
}

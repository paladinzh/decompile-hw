package com.huawei.watermark.manager.parse.unit.decidebytime.view;

import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeLogicWithAlgorithm;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.baseview.WMDecideByTimeElementImage;
import com.huawei.watermark.manager.parse.util.WMLunarSearch;
import com.huawei.watermark.wmutil.WMStringUtil;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WM24JieQiImage extends WMDecideByTimeElementImage {

    private static class WMDecideByTimeLogicWith24JieQi extends WMDecideByTimeLogicWithAlgorithm {
        WMLunarSearch l;

        private WMDecideByTimeLogicWith24JieQi() {
            this.l = new WMLunarSearch();
        }

        public String getValueByTime(long[] time) {
            if (time == null || time.length < 1) {
                return "";
            }
            this.l.setTime(time[0]);
            String TermIdValue = this.l.getTermIdString();
            if (WMStringUtil.isEmptyString(TermIdValue)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(time[0]);
                int[] jieqibeforeafter = this.l.getnearsolarTerm(cal.get(1), cal.getTime());
                String jieqibefore = null;
                String jieqiafter = null;
                if (jieqibeforeafter[0] >= 0 && jieqibeforeafter[0] < this.l.getSolarTermIdStrCount()) {
                    jieqibefore = this.l.getSolarTermIdStrByIndex(jieqibeforeafter[0]);
                }
                if (jieqibeforeafter[1] >= 0 && jieqibeforeafter[1] < this.l.getSolarTermIdStrCount()) {
                    jieqiafter = this.l.getSolarTermIdStrByIndex(jieqibeforeafter[1]);
                }
                TermIdValue = jieqibefore + "_" + jieqiafter;
            }
            return TermIdValue + ".png";
        }
    }

    public WM24JieQiImage(XmlPullParser parser) {
        super(parser);
    }

    public void consWMDecideByTimeLogic() {
        this.mWMDecideByTimeLogic = new WMDecideByTimeLogicWith24JieQi();
    }

    public String getValueWithTime() {
        return ((WMDecideByTimeLogicWith24JieQi) this.mWMDecideByTimeLogic).getValueByTime(new long[]{System.currentTimeMillis()});
    }
}

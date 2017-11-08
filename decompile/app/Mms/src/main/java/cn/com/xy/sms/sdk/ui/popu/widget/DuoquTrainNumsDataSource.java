package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONArray;

public class DuoquTrainNumsDataSource implements DuoquSource {
    private JSONArray mDataSource = null;

    public DuoquTrainNumsDataSource(JSONArray dataSource) {
        this.mDataSource = dataSource;
    }

    public int getLength() {
        return this.mDataSource == null ? 0 : this.mDataSource.length();
    }

    public Object getValue(int index) {
        if (this.mDataSource == null || this.mDataSource.length() <= index) {
            return null;
        }
        return this.mDataSource.opt(index);
    }
}

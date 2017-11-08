package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONArray;
import org.json.JSONException;

public abstract class AdapterDataSource {
    private static final String DEFAULT_DISPLAY_KEY = "name";
    protected JSONArray mDataSource = null;

    public JSONArray getDataSrouce() {
        return this.mDataSource;
    }

    public String getDisplayValue(int index) {
        try {
            if (this.mDataSource == null || this.mDataSource.length() <= index) {
                return "";
            }
            return this.mDataSource.getJSONObject(index).optString("name");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}

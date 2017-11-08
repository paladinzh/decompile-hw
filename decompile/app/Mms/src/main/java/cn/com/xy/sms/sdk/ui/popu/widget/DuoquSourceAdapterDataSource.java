package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONArray;
import org.json.JSONObject;

public class DuoquSourceAdapterDataSource extends AdapterDataSource {
    public static final String DISPLAY_KEY = "name";
    public static final String INDEX_KEY = "index";

    public DuoquSourceAdapterDataSource(DuoquSource source) {
        this.mDataSource = createDataSource(source);
    }

    private static JSONArray createDataSource(DuoquSource source) {
        if (source.getLength() == 0) {
            return null;
        }
        JSONArray sourceJson = new JSONArray();
        try {
            int length = source.getLength();
            for (int i = 0; i < length; i++) {
                JSONObject data = new JSONObject();
                data.put(INDEX_KEY, String.valueOf(i));
                data.put("name", source.getValue(i));
                sourceJson.put(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sourceJson;
    }
}

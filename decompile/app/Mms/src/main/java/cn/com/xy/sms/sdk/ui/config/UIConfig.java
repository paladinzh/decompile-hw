package cn.com.xy.sms.sdk.ui.config;

import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class UIConfig {
    private static final String TAG = "UIConfig";
    public static final String UIVERSION = "201606071515";

    public static String getUIVersion() {
        return UIVERSION;
    }

    public static JSONArray getDefaultSuportMenuData() {
        try {
            JSONArray array = new JSONArray();
            JSONObject jobj = new JSONObject();
            jobj.put("name", ContentUtil.web_statement);
            jobj.put("action_data", "7B2274797065223A225745425F41424F5554227D");
            array.put(jobj);
            jobj = new JSONObject();
            jobj.put("name", ContentUtil.refresh);
            jobj.put("web_menu_type", "WM_RELOAD");
            jobj.put("action_data", "7B7D");
            array.put(jobj);
            return array;
        } catch (Throwable t) {
            LogManager.e(TAG, "getDefaultSuportMenuData：" + t.getMessage(), t);
            return null;
        }
    }
}

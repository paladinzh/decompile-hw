package cn.com.xy.sms.sdk.provider;

import android.content.ContentValues;
import android.net.Uri;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.dex.DexUtil;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
final class a implements Runnable {
    private final /* synthetic */ Map a;

    a(Map map) {
        this.a = map;
    }

    public final void run() {
        try {
            JSONObject handerContactValueMap = DexUtil.handerContactValueMap(this.a);
            if (handerContactValueMap != null) {
                try {
                    JSONArray jSONArray = handerContactValueMap.getJSONArray(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH);
                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject jSONObject = jSONArray.getJSONObject(i);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("phone", jSONObject.getString("phone").trim().replace("-", ""));
                        contentValues.put("name", jSONObject.getString("name"));
                        contentValues.put(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, jSONObject.toString());
                        Constant.getContext().getContentResolver().insert(Uri.parse(ContactsProvider.URI), contentValues);
                    }
                } catch (Throwable th) {
                }
            }
        } catch (Throwable th2) {
            th2.getMessage();
        }
    }
}

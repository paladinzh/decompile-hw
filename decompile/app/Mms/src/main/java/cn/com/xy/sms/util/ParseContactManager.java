package cn.com.xy.sms.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.provider.ContactsProvider;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class ParseContactManager {
    private static final String a = "ParseContactManager";

    private static Cursor a(Context context, String str, String[] strArr) {
        if (context == null || TextUtils.isEmpty(str)) {
            return null;
        }
        String replace = str.replace("-", "");
        ContentResolver contentResolver = context.getContentResolver();
        Uri parse = Uri.parse(ContactsProvider.URI);
        StringBuffer stringBuffer = new StringBuffer("phone");
        stringBuffer.append(" = '").append(replace).append("'");
        Cursor query = contentResolver.query(parse, strArr, stringBuffer.toString(), null, null);
        if (query != null && query.moveToFirst()) {
            if ((System.currentTimeMillis() - query.getLong(query.getColumnIndex("update_time")) <= DexUtil.getUpdateCycleByType(25, 604800000) ? 1 : null) == null) {
                contentResolver.delete(parse, "phone = ?", new String[]{replace});
                query.close();
                return null;
            }
        }
        return query;
    }

    public static JSONArray getExtendForContact(Context context, String str, Map<String, String> map) {
        JSONArray jSONArray = new JSONArray();
        Cursor a = a(context, str, new String[]{MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, "update_time"});
        if (a != null) {
            try {
                if (a.moveToFirst()) {
                    while (true) {
                        String string = a.getString(a.getColumnIndex(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH));
                        if (!StringUtils.isNull(string)) {
                            jSONArray.put(new JSONObject(string));
                        }
                        if (!a.moveToNext()) {
                            break;
                        }
                    }
                }
            } catch (Throwable th) {
                if (a != null) {
                    a.close();
                }
            }
        }
        if (a != null) {
            a.close();
        }
        return jSONArray;
    }

    public static String getMsgForContact(Context context, String str, Map<String, String> map) {
        Cursor a = a(context, str, new String[]{"name", "update_time"});
        Object string = (a != null && a.moveToFirst()) ? a.getString(a.getColumnIndex("name")) : null;
        if (a != null) {
            a.close();
        }
        return !TextUtils.isEmpty(string) ? string : null;
    }
}

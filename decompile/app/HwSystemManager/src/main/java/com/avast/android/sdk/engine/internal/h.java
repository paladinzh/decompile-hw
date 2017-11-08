package com.avast.android.sdk.engine.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Patterns;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.MessageScanResultContainer.MessageScanResultStructure;
import com.avast.android.sdk.engine.MessageType;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.avast.android.sdk.engine.UrlCheckResultStructure;
import com.avast.android.sdk.engine.UrlSource;
import com.avast.android.sdk.engine.internal.q.c;
import com.avast.android.sdk.engine.internal.vps.a.b;
import com.avast.android.sdk.engine.internal.vps.a.g;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

/* compiled from: Unknown */
public class h {
    public static List<MessageScanResultStructure> a(Context context, Integer num, String str, MessageType messageType, String str2, Map<String, File> map) {
        Map hashMap = new HashMap();
        hashMap.put(Short.valueOf(b.STRUCTURE_VERSION_INT_ID.a()), MessageScanResultStructure.getVersionCode());
        hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
        hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
        hashMap.put(Short.valueOf(g.SENDER_STRING_ID.a()), str);
        if (messageType == null) {
            hashMap.put(Short.valueOf(g.MESSAGE_TYPE_SHORT_ID.a()), null);
        } else {
            hashMap.put(Short.valueOf(g.MESSAGE_TYPE_SHORT_ID.a()), Short.valueOf(messageType.getId()));
        }
        hashMap.put(Short.valueOf(g.MESSAGE_CONTENT_STRING_ID.a()), str2);
        hashMap.put(Short.valueOf(g.ADDITIONAL_FILES_MAP_ID.a()), map);
        return MessageScanResultStructure.parseResultList((byte[]) q.a(context, c.SCAN_MESSAGE, hashMap));
    }

    @SuppressLint({"NewApi"})
    public static Map<String, List<UrlCheckResultStructure>> a(Context context, Integer num, String str) {
        Map hashMap = new HashMap();
        if (str != null) {
            Matcher matcher = Patterns.WEB_URL.matcher(str);
            while (matcher.find()) {
                String group = matcher.group();
                String str2 = (group.startsWith("http://") || group.startsWith("https://")) ? group : "http://" + group;
                if (hashMap.get(group) == null) {
                    hashMap.put(group, EngineInterface.checkUrl(context, num, str2, UrlSource.MESSAGE));
                }
            }
        }
        return hashMap;
    }

    public static Map<String, List<ScanResultStructure>> a(Context context, Integer num, Map<String, File> map) {
        Map<String, List<ScanResultStructure>> hashMap = new HashMap();
        for (Entry entry : map.entrySet()) {
            hashMap.put((String) entry.getKey(), EngineInterface.scan(context, num, (File) entry.getValue(), null, 48));
        }
        return hashMap;
    }
}

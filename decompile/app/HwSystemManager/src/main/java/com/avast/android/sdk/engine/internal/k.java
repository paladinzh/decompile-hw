package com.avast.android.sdk.engine.internal;

import android.content.Context;
import android.net.Uri;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ServerInformation;
import com.avast.android.sdk.engine.internal.vps.a.b;
import com.avast.android.sdk.engine.internal.vps.a.i;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class k {
    public static ServerInformation a(Context context, Integer num) {
        ServerInformation serverInformation = new ServerInformation("http", "ui.ff.avast.com", Integer.valueOf(80), "urlinfo/v3/_MD/");
        Uri customUrlInfoServerUri = EngineInterface.getEngineConfig().getCustomUrlInfoServerUri();
        if (customUrlInfoServerUri != null) {
            serverInformation = new ServerInformation(customUrlInfoServerUri.getScheme(), customUrlInfoServerUri.getHost(), Integer.valueOf(customUrlInfoServerUri.getPort()), customUrlInfoServerUri.getPath());
        }
        return a(context, num, serverInformation, i.WEBSHIELD_SERVER_ID);
    }

    private static ServerInformation a(Context context, Integer num, ServerInformation serverInformation, i iVar) {
        Object obj;
        if (num != null && num.intValue() >= 0) {
            obj = null;
        } else {
            num = EngineInterface.acquireVpsContextId(context);
            obj = 1;
        }
        if (num != null && num.intValue() >= 0) {
            try {
                Map hashMap = new HashMap();
                hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
                hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), num);
                hashMap.put(Short.valueOf(b.STRUCTURE_VERSION_INT_ID.a()), ServerInformation.getVersionCode());
                hashMap.put(Short.valueOf(b.OPTION_OVERRIDE_SHORT_ID.a()), Short.valueOf(iVar.a()));
                hashMap.put(Short.valueOf(i.SERVER_ADDRESS_STRING_ID.a()), serverInformation.serverAddress);
                hashMap.put(Short.valueOf(i.SERVER_PATH_STRING_ID.a()), serverInformation.serverPath);
                hashMap.put(Short.valueOf(i.SERVER_PORT_INT_ID.a()), serverInformation.serverPort);
                hashMap.put(Short.valueOf(i.SERVER_PROTOCOL_STRING_ID.a()), serverInformation.serverProtocol);
                ServerInformation parse = ServerInformation.parse(null);
                if (parse != null) {
                    serverInformation = parse;
                }
                hashMap.clear();
                if (obj != null) {
                    EngineInterface.releaseVpsContextId(context, num.intValue());
                }
            } catch (Throwable th) {
                if (obj != null) {
                    EngineInterface.releaseVpsContextId(context, num.intValue());
                }
            }
        }
        return serverInformation;
    }

    public static ServerInformation b(Context context, Integer num) {
        return a(context, num, new ServerInformation("http", "ab.ff.avast.com", Integer.valueOf(80), "cgi-bin/submit50.cgi"), i.FALSE_POSITIVE_SERVER_ID);
    }

    public static ServerInformation c(Context context, Integer num) {
        ServerInformation serverInformation = new ServerInformation("http", "au.ff.avast.com", Integer.valueOf(80), "android/");
        Uri customVpsUpdateServerUri = EngineInterface.getEngineConfig().getCustomVpsUpdateServerUri();
        if (customVpsUpdateServerUri != null) {
            serverInformation = new ServerInformation(customVpsUpdateServerUri.getScheme(), customVpsUpdateServerUri.getHost(), Integer.valueOf(customVpsUpdateServerUri.getPort()), customVpsUpdateServerUri.getPath());
        }
        return a(context, num, serverInformation, i.UPDATE_SERVER_ID);
    }

    public static ServerInformation d(Context context, Integer num) {
        return a(context, num, new ServerInformation("http", "ta.ff.avast.com", Integer.valueOf(80), "F/"), i.TYPOSQUATTING_CONFIRMATION_SERVER_ID);
    }
}

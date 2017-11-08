package com.huawei.systemmanager.rainbow.client.connect.result;

import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackgroundValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.MessageSafeConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.StartupConfigFile;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonColMap {
    public String mColumnField;
    public String mJsonField;

    public JsonColMap(String jsonField, String columnField) {
        this.mJsonField = jsonField;
        this.mColumnField = columnField;
    }

    public static HashMap<Integer, ArrayList<JsonColMap>> getJsonColMaps() {
        HashMap<Integer, ArrayList<JsonColMap>> jsonColMaps = new HashMap();
        ArrayList<JsonColMap> maps = new ArrayList();
        maps.add(new JsonColMap("fbd", NotificationConfigFile.COL_CAN_FORBIDDEN));
        maps.add(new JsonColMap("nCfg", "notificationCfg"));
        maps.add(new JsonColMap("sCfg", NotificationConfigFile.COL_STATUSBAR));
        maps.add(new JsonColMap("lCfg", NotificationConfigFile.COL_LOCKSCREEN));
        maps.add(new JsonColMap("hCfg", NotificationConfigFile.COL_HEADSUB));
        maps.add(new JsonColMap("isC", "isControlled"));
        jsonColMaps.put(Integer.valueOf(33), maps);
        maps = new ArrayList();
        maps.add(new JsonColMap("isS", "isShow"));
        maps.add(new JsonColMap("isP", "isProtected"));
        jsonColMaps.put(Integer.valueOf(31), maps);
        maps = new ArrayList();
        maps.add(new JsonColMap("isR", StartupConfigFile.COL_RECEIVER));
        maps.add(new JsonColMap("isP", StartupConfigFile.COL_SERVICE_PROVIDER));
        maps.add(new JsonColMap("isC", "isControlled"));
        jsonColMaps.put(Integer.valueOf(32), maps);
        maps = new ArrayList();
        maps.add(new JsonColMap("isC", "isControlled"));
        maps.add(new JsonColMap("isK", BackgroundValues.COL_IS_KEY_TASK));
        maps.add(new JsonColMap("isP", "isProtected"));
        jsonColMaps.put(Integer.valueOf(35), maps);
        maps = new ArrayList();
        maps.add(new JsonColMap(MessageSafeConfigFile.COL_PARTNER, MessageSafeConfigFile.COL_PARTNER));
        maps.add(new JsonColMap(MessageSafeConfigFile.COL_MESSAGE_NUMBER, MessageSafeConfigFile.COL_MESSAGE_NUMBER));
        maps.add(new JsonColMap(MessageSafeConfigFile.COL_SECURE_LINK, MessageSafeConfigFile.COL_SECURE_LINK));
        maps.add(new JsonColMap("status", "status"));
        jsonColMaps.put(Integer.valueOf(36), maps);
        return jsonColMaps;
    }
}

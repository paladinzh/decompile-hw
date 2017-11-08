package com.android.server.location.gnsschrlog;

public class CSubApk_Name extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogString strApkName = new LogString(50);
    public LogString strApkVersion = new LogString(12);

    public CSubApk_Name() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strApkName", Integer.valueOf(50));
        this.fieldMap.put("strApkName", this.strApkName);
        this.lengthMap.put("strApkVersion", Integer.valueOf(12));
        this.fieldMap.put("strApkVersion", this.strApkVersion);
        this.enSubEventId.setValue("Apk_Name");
    }
}

package com.android.server.location.gnsschrlog;

public class CSubHiGeo_Scan_Status extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lFiveSecToTenSec = new LogLong();
    public LogLong lLessThanFiveSec = new LogLong();
    public LogLong lOverTenSec = new LogLong();

    public CSubHiGeo_Scan_Status() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lLessThanFiveSec", Integer.valueOf(8));
        this.fieldMap.put("lLessThanFiveSec", this.lLessThanFiveSec);
        this.lengthMap.put("lFiveSecToTenSec", Integer.valueOf(8));
        this.fieldMap.put("lFiveSecToTenSec", this.lFiveSecToTenSec);
        this.lengthMap.put("lOverTenSec", Integer.valueOf(8));
        this.fieldMap.put("lOverTenSec", this.lOverTenSec);
        this.enSubEventId.setValue("HiGeo_Scan_Status");
    }
}

package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_ABS_STATISTICS_EVENT extends ChrLogBaseEventModel {
    public ENCEventId enEventId = new ENCEventId();
    public LogLong lmimo_screen_on_time = new LogLong();
    public LogLong lmimo_time = new LogLong();
    public LogLong lsiso_screen_on_time = new LogLong();
    public LogLong lsiso_time = new LogLong();
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogShort usLen = new LogShort();
    public LogShort usantenna_preempted_screen_off_event = new LogShort();
    public LogShort usantenna_preempted_screen_on_event = new LogShort();
    public LogShort uslong_connect_event = new LogShort();
    public LogShort usmax_ping_pong_times = new LogShort();
    public LogShort usmo_mt_call_event = new LogShort();
    public LogShort usping_pong_times = new LogShort();
    public LogShort ussearch_event = new LogShort();
    public LogShort usshort_connect_event = new LogShort();
    public LogShort ussiso_to_mimo_event = new LogShort();

    public CSegEVENT_WIFI_ABS_STATISTICS_EVENT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("uslong_connect_event", Integer.valueOf(2));
        this.fieldMap.put("uslong_connect_event", this.uslong_connect_event);
        this.lengthMap.put("usshort_connect_event", Integer.valueOf(2));
        this.fieldMap.put("usshort_connect_event", this.usshort_connect_event);
        this.lengthMap.put("ussearch_event", Integer.valueOf(2));
        this.fieldMap.put("ussearch_event", this.ussearch_event);
        this.lengthMap.put("usantenna_preempted_screen_on_event", Integer.valueOf(2));
        this.fieldMap.put("usantenna_preempted_screen_on_event", this.usantenna_preempted_screen_on_event);
        this.lengthMap.put("usantenna_preempted_screen_off_event", Integer.valueOf(2));
        this.fieldMap.put("usantenna_preempted_screen_off_event", this.usantenna_preempted_screen_off_event);
        this.lengthMap.put("usmo_mt_call_event", Integer.valueOf(2));
        this.fieldMap.put("usmo_mt_call_event", this.usmo_mt_call_event);
        this.lengthMap.put("ussiso_to_mimo_event", Integer.valueOf(2));
        this.fieldMap.put("ussiso_to_mimo_event", this.ussiso_to_mimo_event);
        this.lengthMap.put("usping_pong_times", Integer.valueOf(2));
        this.fieldMap.put("usping_pong_times", this.usping_pong_times);
        this.lengthMap.put("usmax_ping_pong_times", Integer.valueOf(2));
        this.fieldMap.put("usmax_ping_pong_times", this.usmax_ping_pong_times);
        this.lengthMap.put("lmimo_time", Integer.valueOf(8));
        this.fieldMap.put("lmimo_time", this.lmimo_time);
        this.lengthMap.put("lsiso_time", Integer.valueOf(8));
        this.fieldMap.put("lsiso_time", this.lsiso_time);
        this.lengthMap.put("lmimo_screen_on_time", Integer.valueOf(8));
        this.fieldMap.put("lmimo_screen_on_time", this.lmimo_screen_on_time);
        this.lengthMap.put("lsiso_screen_on_time", Integer.valueOf(8));
        this.fieldMap.put("lsiso_screen_on_time", this.lsiso_screen_on_time);
        this.enEventId.setValue("WIFI_ABS_STATISTICS_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}

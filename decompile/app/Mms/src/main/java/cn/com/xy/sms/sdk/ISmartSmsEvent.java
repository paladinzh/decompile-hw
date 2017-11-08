package cn.com.xy.sms.sdk;

import java.util.Map;

public interface ISmartSmsEvent {
    public static final int SMARTSMS_EVENT_REFRESH = 1;
    public static final int SMARTSMS_EVENT_SET_SPAND_TOUCH_MONITOR = 2;

    int onSmartSmsEvent(int i, Map map);
}

package com.huawei.thermal.eventhub;

import com.huawei.thermal.event.Event;

public interface EventListener {
    void handleEvent(Event event);
}

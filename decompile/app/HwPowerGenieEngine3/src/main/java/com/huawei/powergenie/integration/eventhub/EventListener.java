package com.huawei.powergenie.integration.eventhub;

public interface EventListener {
    void handleEvent(Event event);
}

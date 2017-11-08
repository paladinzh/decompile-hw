package org.w3c.dom.events;

public interface Event {
    String getType();

    void initEvent(String str, boolean z, boolean z2);
}

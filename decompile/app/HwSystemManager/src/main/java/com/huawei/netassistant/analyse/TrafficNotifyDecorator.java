package com.huawei.netassistant.analyse;

public class TrafficNotifyDecorator extends TrafficNotifyComponent {
    protected TrafficNotifyComponent mComponent;

    public void decorateNotify(TrafficNotifyComponent component) {
        this.mComponent = component;
    }

    public void notifyTraffic() {
        if (this.mComponent != null) {
            this.mComponent.notifyTraffic();
        }
    }
}

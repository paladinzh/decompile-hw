package com.huawei.watermark.controller.callback;

public interface SensorProcessorListener {
    void registerSensor();

    void setPressureValueChangeListener(PressureValueChangeListener pressureValueChangeListener);

    void unRegisterSensor();
}

package com.huawei.anim.visualeffect;

public interface SensorEffect {

    public interface Listener {
        void onChanged(int i, float[] fArr);
    }

    public static class RequestSensorInfo {
        public int rate;
        public int type;
    }
}

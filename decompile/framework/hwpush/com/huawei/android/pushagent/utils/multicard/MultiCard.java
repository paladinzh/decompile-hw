package com.huawei.android.pushagent.utils.multicard;

public interface MultiCard {

    public enum SupportMode {
        MODE_SUPPORT_UNKNOWN,
        MODE_NOT_SUPPORT_GEMINI,
        MODE_SUPPORT_HW_GEMINI,
        MODE_SUPPORT_MTK_GEMINI
    }

    String getDeviceId(int i);
}

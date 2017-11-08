package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

public class WeChatTypeCons {
    public static final int WECHAT_AUDIO = 2;
    public static final int WECHAT_PHOTO = 0;
    public static final int WECHAT_VIDEO = 1;

    public static final boolean isWeChatAudio(int index) {
        return index == 2;
    }

    public static final boolean isWeChatPhoto(int index) {
        return index == 0;
    }

    public static final boolean isWeChatVideo(int index) {
        return index == 1;
    }

    public static final boolean isWeChatVideoPhoto(int index) {
        return !isWeChatPhoto(index) ? isWeChatVideo(index) : true;
    }

    public static final boolean isWeChatMediaType(int index) {
        return !isWeChatVideoPhoto(index) ? isWeChatAudio(index) : true;
    }
}

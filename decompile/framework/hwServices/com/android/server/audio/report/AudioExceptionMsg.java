package com.android.server.audio.report;

public class AudioExceptionMsg {
    public String msgPackagename = null;
    public int msgType = 0;

    public AudioExceptionMsg(int type, String packagename) {
        this.msgType = type;
        this.msgPackagename = packagename;
    }
}

package com.android.server.audio;

public class AudioExceptionRecord {
    public String mLastMutePackageName = null;

    public void updateMuteMsg(String packagename) {
        this.mLastMutePackageName = packagename;
    }
}

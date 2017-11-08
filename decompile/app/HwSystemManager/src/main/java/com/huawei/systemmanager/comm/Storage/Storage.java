package com.huawei.systemmanager.comm.Storage;

public class Storage {
    public static final int POSITION_INTERNAL = 0;
    public static final int POSITION_SDCARD = 1;
    public static final int POSITION_USB = 2;
    private final boolean mAvaliable;
    private final String mPath;
    private final int mPosition;

    public Storage(int position, String path, boolean avaliable) {
        this.mPosition = position;
        this.mPath = path;
        this.mAvaliable = avaliable;
    }

    int getPosition() {
        return this.mPosition;
    }

    String getPath() {
        return this.mPath;
    }

    boolean isAvaliable() {
        return this.mAvaliable;
    }
}

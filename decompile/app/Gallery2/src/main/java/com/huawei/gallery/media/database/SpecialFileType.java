package com.huawei.gallery.media.database;

public enum SpecialFileType {
    DEFAULT(0),
    _3D_FYUSE(11);
    
    public final int fileType;

    private SpecialFileType(int typ) {
        this.fileType = typ;
    }
}

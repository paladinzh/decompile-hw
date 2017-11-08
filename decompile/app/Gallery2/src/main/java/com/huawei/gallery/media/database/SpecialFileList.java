package com.huawei.gallery.media.database;

public enum SpecialFileList {
    DEFAULT(0),
    BLACK_LIST(1),
    WHITE_LIST(2);
    
    public final int listType;

    private SpecialFileList(int typ) {
        this.listType = typ;
    }
}

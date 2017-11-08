package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.spacecleanner.engine.trash.FileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.Comparator;

public class TecentWeChatTrashFile extends FileTrash {
    public static final Comparator<Trash> WECHAT_COMPARATOR = new Comparator<Trash>() {
        public int compare(Trash lhs, Trash rhs) {
            if (lhs.getYear() > rhs.getYear()) {
                return 1;
            }
            if (lhs.getYear() == rhs.getYear() && lhs.getMonth() > rhs.getMonth()) {
                return 1;
            }
            if (lhs.getYear() == rhs.getYear() && lhs.getMonth() == rhs.getMonth()) {
                return 0;
            }
            return -1;
        }
    };
    private byte mMonth;
    private int mTrashType;
    private short mYear;

    public TecentWeChatTrashFile(String path, PathEntry pathEntry) {
        super(path, pathEntry);
    }

    public void setType(int type) {
        this.mTrashType = type;
    }

    public void setDate(short year, byte month) {
        this.mYear = year;
        this.mMonth = month;
    }

    public int getType() {
        return this.mTrashType;
    }

    public short getYear() {
        return this.mYear;
    }

    public byte getMonth() {
        return this.mMonth;
    }

    public static final TecentWeChatTrashFile creator(String path, int type, short year, byte month) {
        TecentWeChatTrashFile trash = new TecentWeChatTrashFile(path, null);
        trash.setType(type);
        trash.setDate(year, month);
        return trash;
    }
}

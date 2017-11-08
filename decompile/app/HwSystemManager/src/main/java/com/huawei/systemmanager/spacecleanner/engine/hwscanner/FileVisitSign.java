package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import com.huawei.systemmanager.comm.Storage.PathEntry;

public class FileVisitSign {
    public static final FileVisitSign TERMINATE = new FileVisitSign();
    private PathEntry mPathEntry;
    private int mTypeInclude;

    private FileVisitSign() {
    }

    private FileVisitSign(PathEntry pathEntry) {
        this.mPathEntry = pathEntry;
    }

    public int getIncludeType() {
        return this.mTypeInclude;
    }

    public int getPosition() {
        return this.mPathEntry.mPosition;
    }

    public PathEntry getPathEntry() {
        return this.mPathEntry;
    }

    public boolean checkInclude(int type) {
        return (this.mTypeInclude & type) != 0;
    }

    public FileVisitSign addInclude(int include) {
        if (checkInclude(include)) {
            return this;
        }
        FileVisitSign sign = new FileVisitSign();
        sign.mPathEntry = this.mPathEntry;
        sign.mTypeInclude = this.mTypeInclude | include;
        return sign;
    }

    public static final FileVisitSign create(PathEntry entry) {
        return new FileVisitSign(entry);
    }
}

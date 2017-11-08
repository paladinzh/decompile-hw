package com.huawei.harassmentinterception.common;

public class BlockReason {
    private int markCount;
    private int reason;
    private int type;

    public BlockReason(int reason, int type) {
        this.reason = reason;
        this.type = type;
        this.markCount = 0;
    }

    public BlockReason(int reason, int type, int markCount) {
        this.reason = reason;
        this.type = type;
        this.markCount = markCount;
    }

    public int getReason() {
        return this.reason;
    }

    public int getType() {
        return this.type;
    }

    public int getMarkCount() {
        return this.markCount;
    }
}

package com.android.server.location;

public class HwGnssDftGnssSessionParam {
    public long catchSvTime = 0;
    public boolean isGpsdResart = false;
    public int lostPosCnt = 0;
    public long startTime = 0;
    public long stopTime = 0;
    public int ttff = 0;

    public void resetParam() {
        this.startTime = 0;
        this.ttff = 0;
        this.stopTime = 0;
        this.catchSvTime = 0;
        this.lostPosCnt = 0;
        this.isGpsdResart = false;
    }
}

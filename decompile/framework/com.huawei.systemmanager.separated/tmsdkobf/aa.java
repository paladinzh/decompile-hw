package tmsdkobf;

/* compiled from: Unknown */
public final class aa extends fs {
    public int am = 0;
    public int ar = -1;
    public int as = -1;
    public int at = 0;
    public String au = "";
    public String av = "";
    public int aw = -1;
    public int ax = 0;
    public String checkSum = "";
    public String downNetName = "";
    public int downSize = -1;
    public byte downType = (byte) 0;
    public int downnetType = 0;
    public int errorCode = 0;
    public String errorMsg = "";
    public int fileSize = 0;
    public int rssi = -1;
    public int sdcardStatus = -1;
    public byte success = (byte) 1;
    public int timestamp = -1;
    public String url = "";

    public fs newInit() {
        return new aa();
    }

    public void readFrom(fq fqVar) {
        this.am = fqVar.a(this.am, 0, true);
        this.checkSum = fqVar.a(1, false);
        this.timestamp = fqVar.a(this.timestamp, 2, false);
        this.url = fqVar.a(3, false);
        this.success = (byte) fqVar.a(this.success, 4, false);
        this.downSize = fqVar.a(this.downSize, 5, false);
        this.ar = fqVar.a(this.ar, 6, false);
        this.as = fqVar.a(this.as, 7, false);
        this.downType = (byte) fqVar.a(this.downType, 8, false);
        this.errorCode = fqVar.a(this.errorCode, 9, false);
        this.downnetType = fqVar.a(this.downnetType, 10, false);
        this.downNetName = fqVar.a(11, false);
        this.at = fqVar.a(this.at, 12, false);
        this.au = fqVar.a(13, false);
        this.errorMsg = fqVar.a(14, false);
        this.rssi = fqVar.a(this.rssi, 15, false);
        this.sdcardStatus = fqVar.a(this.sdcardStatus, 16, false);
        this.fileSize = fqVar.a(this.fileSize, 17, false);
        this.av = fqVar.a(18, false);
        this.aw = fqVar.a(this.aw, 19, false);
        this.ax = fqVar.a(this.ax, 20, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.am, 0);
        if (this.checkSum != null) {
            frVar.a(this.checkSum, 1);
        }
        if (this.timestamp != -1) {
            frVar.write(this.timestamp, 2);
        }
        if (this.url != null) {
            frVar.a(this.url, 3);
        }
        if (this.success != (byte) 1) {
            frVar.b(this.success, 4);
        }
        if (this.downSize != -1) {
            frVar.write(this.downSize, 5);
        }
        if (this.ar != -1) {
            frVar.write(this.ar, 6);
        }
        if (this.as != -1) {
            frVar.write(this.as, 7);
        }
        if (this.downType != (byte) 0) {
            frVar.b(this.downType, 8);
        }
        if (this.errorCode != 0) {
            frVar.write(this.errorCode, 9);
        }
        if (this.downnetType != 0) {
            frVar.write(this.downnetType, 10);
        }
        if (this.downNetName != null) {
            frVar.a(this.downNetName, 11);
        }
        if (this.at != 0) {
            frVar.write(this.at, 12);
        }
        if (this.au != null) {
            frVar.a(this.au, 13);
        }
        if (this.errorMsg != null) {
            frVar.a(this.errorMsg, 14);
        }
        if (this.rssi != -1) {
            frVar.write(this.rssi, 15);
        }
        if (this.sdcardStatus != -1) {
            frVar.write(this.sdcardStatus, 16);
        }
        if (this.fileSize != 0) {
            frVar.write(this.fileSize, 17);
        }
        if (this.av != null) {
            frVar.a(this.av, 18);
        }
        if (this.aw != -1) {
            frVar.write(this.aw, 19);
        }
        if (this.ax != 0) {
            frVar.write(this.ax, 20);
        }
    }
}

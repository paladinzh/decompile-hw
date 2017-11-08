package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cf extends fs {
    static ArrayList<String> fb = new ArrayList();
    static ArrayList<String> fc = new ArrayList();
    public String apkName = "";
    public String apkPackage = "";
    public String certMD5 = "";
    public String description = "";
    public String developer = "";
    public int downloadCount = 0;
    public String iconUrl = "";
    public ArrayList<String> imageUrls = null;
    public boolean isInSoftwareDB = true;
    public int official = 0;
    public ArrayList<String> sensitivePermissions = null;
    public long size = 0;
    public String source = "";
    public int versionCode = 0;
    public String versionName = "";
    public String virsusDescription = "";
    public String virsusName = "";

    static {
        fb.add("");
        fc.add("");
    }

    public fs newInit() {
        return new cf();
    }

    public void readFrom(fq fqVar) {
        this.apkPackage = fqVar.a(0, false);
        this.apkName = fqVar.a(1, false);
        this.iconUrl = fqVar.a(2, false);
        this.versionCode = fqVar.a(this.versionCode, 3, false);
        this.versionName = fqVar.a(4, false);
        this.size = fqVar.a(this.size, 5, false);
        this.official = fqVar.a(this.official, 6, false);
        this.developer = fqVar.a(7, false);
        this.certMD5 = fqVar.a(8, false);
        this.isInSoftwareDB = fqVar.a(this.isInSoftwareDB, 9, false);
        this.description = fqVar.a(10, false);
        this.imageUrls = (ArrayList) fqVar.b(fb, 11, false);
        this.downloadCount = fqVar.a(this.downloadCount, 12, false);
        this.source = fqVar.a(13, false);
        this.sensitivePermissions = (ArrayList) fqVar.b(fc, 14, false);
        this.virsusName = fqVar.a(15, false);
        this.virsusDescription = fqVar.a(16, false);
    }

    public void writeTo(fr frVar) {
        if (this.apkPackage != null) {
            frVar.a(this.apkPackage, 0);
        }
        if (this.apkName != null) {
            frVar.a(this.apkName, 1);
        }
        if (this.iconUrl != null) {
            frVar.a(this.iconUrl, 2);
        }
        if (this.versionCode != 0) {
            frVar.write(this.versionCode, 3);
        }
        if (this.versionName != null) {
            frVar.a(this.versionName, 4);
        }
        if (this.size != 0) {
            frVar.b(this.size, 5);
        }
        if (this.official != 0) {
            frVar.write(this.official, 6);
        }
        if (this.developer != null) {
            frVar.a(this.developer, 7);
        }
        if (this.certMD5 != null) {
            frVar.a(this.certMD5, 8);
        }
        if (!this.isInSoftwareDB) {
            frVar.a(this.isInSoftwareDB, 9);
        }
        if (this.description != null) {
            frVar.a(this.description, 10);
        }
        if (this.imageUrls != null) {
            frVar.a(this.imageUrls, 11);
        }
        if (this.downloadCount != 0) {
            frVar.write(this.downloadCount, 12);
        }
        if (this.source != null) {
            frVar.a(this.source, 13);
        }
        if (this.sensitivePermissions != null) {
            frVar.a(this.sensitivePermissions, 14);
        }
        if (this.virsusName != null) {
            frVar.a(this.virsusName, 15);
        }
        if (this.virsusDescription != null) {
            frVar.a(this.virsusDescription, 16);
        }
    }
}

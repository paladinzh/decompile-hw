package com.amap.api.mapcore.util;

@fv(a = "update_item_download_info")
/* compiled from: DTDownloadInfo */
class bi {
    @fw(a = "mAdcode", b = 6)
    private String a = "";
    @fw(a = "fileLength", b = 5)
    private long b = 0;
    @fw(a = "splitter", b = 2)
    private int c = 0;
    @fw(a = "startPos", b = 5)
    private long d = 0;
    @fw(a = "endPos", b = 5)
    private long e = 0;

    public bi(String str, long j, int i, long j2, long j3) {
        this.a = str;
        this.b = j;
        this.c = i;
        this.d = j2;
        this.e = j3;
    }

    public static String a(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mAdcode");
        stringBuilder.append("='");
        stringBuilder.append(str);
        stringBuilder.append("'");
        return stringBuilder.toString();
    }
}

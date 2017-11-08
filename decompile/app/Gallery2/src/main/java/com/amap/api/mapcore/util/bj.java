package com.amap.api.mapcore.util;

@fv(a = "update_item_file")
/* compiled from: DTFileInfo */
class bj {
    @fw(a = "mAdcode", b = 6)
    private String a = "";
    @fw(a = "file", b = 6)
    private String b = "";

    public bj(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public String a() {
        return this.b;
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

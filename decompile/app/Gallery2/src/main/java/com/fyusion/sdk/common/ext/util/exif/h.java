package com.fyusion.sdk.common.ext.util.exif;

import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
class h {
    private static final int[] d = new int[]{0, 1, 2, 3, 4};
    private final int a;
    private final Map<Short, ExifTag> b = new HashMap();
    private int c = 0;

    h(int i) {
        this.a = i;
    }

    protected static int[] a() {
        return d;
    }

    protected ExifTag a(ExifTag exifTag) {
        exifTag.a(this.a);
        return (ExifTag) this.b.put(Short.valueOf(exifTag.getTagId()), exifTag);
    }

    protected ExifTag a(short s) {
        return (ExifTag) this.b.get(Short.valueOf(s));
    }

    protected void a(int i) {
        this.c = i;
    }

    protected void b(short s) {
        this.b.remove(Short.valueOf(s));
    }

    protected ExifTag[] b() {
        return (ExifTag[]) this.b.values().toArray(new ExifTag[this.b.size()]);
    }

    protected int c() {
        return this.a;
    }

    protected int d() {
        return this.b.size();
    }

    protected int e() {
        return this.c;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof h)) {
            h hVar = (h) obj;
            if (hVar.c() == this.a && hVar.d() == d()) {
                for (ExifTag exifTag : hVar.b()) {
                    if (!ExifInterface.a(exifTag.getTagId()) && !exifTag.equals((ExifTag) this.b.get(Short.valueOf(exifTag.getTagId())))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}

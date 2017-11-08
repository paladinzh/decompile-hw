package com.fyusion.sdk.core.util.a;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/* compiled from: Unknown */
public class b {
    public static final b a = new b(103669760);
    protected static final Comparator<byte[]> b = new Comparator<byte[]>() {
        public int a(byte[] bArr, byte[] bArr2) {
            return bArr.length - bArr2.length;
        }

        public /* synthetic */ int compare(Object obj, Object obj2) {
            return a((byte[]) obj, (byte[]) obj2);
        }
    };
    private List<byte[]> c = new LinkedList();
    private List<byte[]> d = new ArrayList(64);
    private int e = 0;
    private final int f;

    public b(int i) {
        this.f = i;
    }

    private synchronized void b() {
        while (this.e > this.f) {
            byte[] bArr = (byte[]) this.c.remove(0);
            this.d.remove(bArr);
            this.e -= bArr.length;
        }
    }

    public synchronized void a() {
        this.c.clear();
        this.d.clear();
        this.e = 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(byte[] bArr) {
        if (bArr != null) {
            if (bArr.length <= this.f) {
                this.c.add(bArr);
                int binarySearch = Collections.binarySearch(this.d, bArr, b);
                if (binarySearch < 0) {
                    binarySearch = (-binarySearch) - 1;
                }
                this.d.add(binarySearch, bArr);
                this.e += bArr.length;
                b();
            }
        }
    }

    public synchronized byte[] a(int i) {
        int i2 = 0;
        while (i2 < this.d.size()) {
            byte[] bArr = (byte[]) this.d.get(i2);
            if (bArr.length < i) {
                i2++;
            } else {
                this.e -= bArr.length;
                this.d.remove(i2);
                this.c.remove(bArr);
                return bArr;
            }
        }
        return new byte[i];
    }
}

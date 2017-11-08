package com.fyusion.sdk.core.a;

import com.fyusion.sdk.common.i;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/* compiled from: Unknown */
public class h {
    private i a;
    private String b;
    private final int c;
    private final int d;
    private int e;
    private final int f;
    private int g = -1;

    public h(i iVar, int i, int i2, int i3, int i4) {
        this.c = i2;
        this.d = i3;
        this.e = i4;
        this.f = i;
        this.a = iVar;
        this.b = iVar.b() + File.separator + i;
    }

    public String a() {
        return this.b;
    }

    public void a(int i) {
        this.e = i;
    }

    public void a(FileInputStream fileInputStream) {
        if (fileInputStream != null) {
            this.a.a(fileInputStream);
        }
    }

    public void a(FileOutputStream fileOutputStream) {
        this.a.a(fileOutputStream);
    }

    public void b(int i) {
        this.g = i;
    }

    public boolean b() {
        return true;
    }

    public FileOutputStream c() throws IOException {
        return this.a.a(this.f);
    }

    public FileInputStream d() throws IOException {
        try {
            return this.a.b(this.f);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Index out of bounds");
        }
    }

    public int e() {
        return this.c;
    }

    public int f() {
        return this.d;
    }

    public int g() {
        return this.e;
    }

    public int h() {
        return this.g;
    }
}

package com.loc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: Unknown */
public class dh {
    protected File a;
    protected int[] b;
    private ArrayList c;
    private boolean d = false;

    protected dh(File file, ArrayList arrayList, int[] iArr) {
        this.a = file;
        this.c = arrayList;
        this.b = iArr;
    }

    public void a(boolean z) {
        this.d = z;
    }

    public byte[] a() {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            byte[] bArr = (byte[]) it.next();
            try {
                dataOutputStream.writeInt(bArr.length);
                dataOutputStream.write(bArr);
            } catch (IOException e) {
            }
        }
        try {
            byteArrayOutputStream.close();
            dataOutputStream.close();
        } catch (IOException e2) {
        }
        return byteArrayOutputStream.toByteArray();
    }

    protected final boolean b() {
        return this.d;
    }

    protected final int c() {
        if (this.c == null) {
            return 0;
        }
        int i = 0;
        for (int i2 = 0; i2 < this.c.size(); i2++) {
            i += this.c.get(i2) == null ? 0 : ((byte[]) this.c.get(i2)).length;
        }
        return i;
    }
}

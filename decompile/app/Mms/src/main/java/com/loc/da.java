package com.loc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/* compiled from: Unknown */
final class da implements Serializable {
    protected byte[] a = new byte[16];
    protected byte[] b = new byte[16];
    protected byte[] c = new byte[16];
    protected short d = (short) 0;
    protected short e = (short) 0;
    protected byte f = (byte) 0;
    protected byte[] g = new byte[16];
    protected byte[] h = new byte[32];
    protected short i = (short) 0;
    protected ArrayList j = new ArrayList();
    private byte k = (byte) 41;
    private short l = (short) 0;

    da() {
    }

    private Boolean a(DataOutputStream dataOutputStream) {
        Boolean.valueOf(true);
        try {
            OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream2 = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream2.flush();
            dataOutputStream2.write(this.a);
            dataOutputStream2.write(this.b);
            dataOutputStream2.write(this.c);
            dataOutputStream2.writeShort(this.d);
            dataOutputStream2.writeShort(this.e);
            dataOutputStream2.writeByte(this.f);
            this.g[15] = (byte) 0;
            dataOutputStream2.write(di.a(this.g, this.g.length));
            this.h[31] = (byte) 0;
            dataOutputStream2.write(di.a(this.h, this.h.length));
            dataOutputStream2.writeShort(this.i);
            for (short s = (short) 0; s < this.i; s = (short) (s + 1)) {
                OutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream3 = new DataOutputStream(byteArrayOutputStream2);
                dataOutputStream3.flush();
                cy cyVar = (cy) this.j.get(s);
                if (!(cyVar.c == null || cyVar.c.a(dataOutputStream3).booleanValue())) {
                    Boolean.valueOf(false);
                }
                if (cyVar.d != null) {
                    if (!cyVar.d.a(dataOutputStream3).booleanValue()) {
                        Boolean.valueOf(false);
                    }
                }
                if (!(cyVar.e == null || cyVar.e.a(dataOutputStream3).booleanValue())) {
                    Boolean.valueOf(false);
                }
                if (!(cyVar.f == null || cyVar.f.a(dataOutputStream3).booleanValue())) {
                    Boolean.valueOf(false);
                }
                if (!(cyVar.g == null || cyVar.g.a(dataOutputStream3).booleanValue())) {
                    Boolean.valueOf(false);
                }
                cyVar.a = (short) Integer.valueOf(byteArrayOutputStream2.size() + 4).shortValue();
                dataOutputStream2.writeShort(cyVar.a);
                dataOutputStream2.writeInt(cyVar.b);
                dataOutputStream2.write(byteArrayOutputStream2.toByteArray());
            }
            this.l = (short) Integer.valueOf(byteArrayOutputStream.size()).shortValue();
            dataOutputStream.writeByte(this.k);
            dataOutputStream.writeShort(this.l);
            dataOutputStream.write(byteArrayOutputStream.toByteArray());
            return Boolean.valueOf(true);
        } catch (IOException e) {
            return Boolean.valueOf(false);
        }
    }

    protected final byte[] a() {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        a(new DataOutputStream(byteArrayOutputStream));
        return byteArrayOutputStream.toByteArray();
    }
}

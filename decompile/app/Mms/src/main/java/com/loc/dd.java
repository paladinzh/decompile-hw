package com.loc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/* compiled from: Unknown */
final class dd implements Serializable {
    protected byte a = (byte) 0;
    protected ArrayList b = new ArrayList();
    private byte c = (byte) 3;

    dd() {
    }

    protected final Boolean a(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeByte(this.c);
            dataOutputStream.writeByte(this.a);
            for (int i = 0; i < this.b.size(); i++) {
                de deVar = (de) this.b.get(i);
                dataOutputStream.writeByte(deVar.a);
                Object obj = new byte[deVar.a];
                System.arraycopy(deVar.b, 0, obj, 0, deVar.a >= deVar.b.length ? deVar.b.length : deVar.a);
                dataOutputStream.write(obj);
                dataOutputStream.writeDouble(deVar.c);
                dataOutputStream.writeInt(deVar.d);
                dataOutputStream.writeInt(deVar.e);
                dataOutputStream.writeDouble(deVar.f);
                dataOutputStream.writeByte(deVar.g);
                dataOutputStream.writeByte(deVar.h);
                obj = new byte[deVar.h];
                System.arraycopy(deVar.i, 0, obj, 0, deVar.h >= deVar.i.length ? deVar.i.length : deVar.h);
                dataOutputStream.write(obj);
                dataOutputStream.writeByte(deVar.j);
            }
            return Boolean.valueOf(true);
        } catch (IOException e) {
            return Boolean.valueOf(false);
        }
    }
}

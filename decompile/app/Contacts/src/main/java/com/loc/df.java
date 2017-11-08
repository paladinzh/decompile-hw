package com.loc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/* compiled from: Unknown */
final class df implements Serializable {
    protected byte a = (byte) 0;
    protected ArrayList b = new ArrayList();
    private byte c = (byte) 8;

    df() {
    }

    protected final Boolean a(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeByte(this.c);
            dataOutputStream.writeByte(this.a);
            for (byte b = (byte) 0; b < this.a; b++) {
                dg dgVar = (dg) this.b.get(b);
                dataOutputStream.write(dgVar.a);
                dataOutputStream.writeShort(dgVar.b);
                dataOutputStream.write(di.a(dgVar.c, dgVar.c.length));
            }
            return Boolean.valueOf(true);
        } catch (IOException e) {
            return Boolean.valueOf(false);
        }
    }
}

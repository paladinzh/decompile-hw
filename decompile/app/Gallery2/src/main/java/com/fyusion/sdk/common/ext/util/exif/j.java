package com.fyusion.sdk.common.ext.util.exif;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* compiled from: Unknown */
class j extends FilterOutputStream {
    private final ByteBuffer a = ByteBuffer.allocate(4);

    public j(OutputStream outputStream) {
        super(outputStream);
    }

    public j a(int i) throws IOException {
        this.a.rewind();
        this.a.putInt(i);
        this.out.write(this.a.array());
        return this;
    }

    public j a(Rational rational) throws IOException {
        a((int) rational.getNumerator());
        a((int) rational.getDenominator());
        return this;
    }

    public j a(ByteOrder byteOrder) {
        this.a.order(byteOrder);
        return this;
    }

    public j a(short s) throws IOException {
        this.a.rewind();
        this.a.putShort(s);
        this.out.write(this.a.array(), 0, 2);
        return this;
    }
}

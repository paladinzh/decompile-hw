package com.fyusion.sdk.common.ext.util.exif;

/* compiled from: Unknown */
public class Rational {
    private final long a;
    private final long b;

    public Rational(long j, long j2) {
        this.a = j;
        this.b = j2;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Rational)) {
            return false;
        }
        Rational rational = (Rational) obj;
        if (!(this.a == rational.a && this.b == rational.b)) {
            z = false;
        }
        return z;
    }

    public long getDenominator() {
        return this.b;
    }

    public long getNumerator() {
        return this.a;
    }

    public String toString() {
        return this.a + "/" + this.b;
    }
}

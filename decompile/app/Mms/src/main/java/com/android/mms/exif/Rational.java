package com.android.mms.exif;

public class Rational {
    private final long mDenominator;
    private final long mNumerator;

    public Rational(long nominator, long denominator) {
        this.mNumerator = nominator;
        this.mDenominator = denominator;
    }

    public long getNumerator() {
        return this.mNumerator;
    }

    public long getDenominator() {
        return this.mDenominator;
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
        Rational data = (Rational) obj;
        if (!(this.mNumerator == data.mNumerator && this.mDenominator == data.mDenominator)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Long.valueOf(((4294967295L & this.mNumerator) << 32) | (this.mDenominator & 4294967295L)).hashCode();
    }

    public String toString() {
        return this.mNumerator + "/" + this.mDenominator;
    }
}

package java.lang;

import java.util.regex.Pattern;
import sun.misc.DoubleConsts;
import sun.misc.FDBigInt;

public class FloatingDecimal {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static final ThreadLocal<FloatingDecimal> TL_INSTANCE = new ThreadLocal<FloatingDecimal>() {
        protected FloatingDecimal initialValue() {
            return new FloatingDecimal();
        }
    };
    private static FDBigInt[] b5p = null;
    private static final double[] big10pow = new double[]{1.0E16d, 1.0E32d, 1.0E64d, 1.0E128d, 1.0E256d};
    static final int bigDecimalExponent = 324;
    static final int expBias = 1023;
    static final long expMask = 9218868437227405312L;
    static final long expOne = 4607182418800017408L;
    static final int expShift = 52;
    static final long fractHOB = 4503599627370496L;
    static final long fractMask = 4503599627370495L;
    private static Pattern hexFloatPattern = null;
    static final long highbit = Long.MIN_VALUE;
    static final long highbyte = -72057594037927936L;
    private static final char[] infinity = new char[]{'I', 'n', 'f', 'i', 'n', 'i', 't', 'y'};
    static final int intDecimalDigits = 9;
    private static final long[] long5pow = new long[]{1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125, 6103515625L, 30517578125L, 152587890625L, 762939453125L, 3814697265625L, 19073486328125L, 95367431640625L, 476837158203125L, 2384185791015625L, 11920928955078125L, 59604644775390625L, 298023223876953125L, 1490116119384765625L};
    static final long lowbytes = 72057594037927935L;
    static final int maxDecimalDigits = 15;
    static final int maxDecimalExponent = 308;
    static final int maxSmallBinExp = 62;
    private static final int maxSmallTen = (small10pow.length - 1);
    static final int minDecimalExponent = -324;
    static final int minSmallBinExp = -21;
    private static final int[] n5bits = new int[]{0, 3, 5, 7, 10, 12, 14, 17, 19, 21, 24, 26, 28, 31, 33, 35, 38, 40, 42, 45, 47, 49, expShift, 54, 56, 59, 61};
    private static final char[] notANumber = new char[]{'N', 'a', 'N'};
    private static ThreadLocal perThreadBuffer = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new char[26];
        }
    };
    static final long signMask = Long.MIN_VALUE;
    static final int singleExpBias = 127;
    static final int singleExpMask = 2139095040;
    static final int singleExpShift = 23;
    static final int singleFractHOB = 8388608;
    static final int singleFractMask = 8388607;
    static final int singleMaxDecimalDigits = 7;
    static final int singleMaxDecimalExponent = 38;
    private static final int singleMaxSmallTen = (singleSmall10pow.length - 1);
    static final int singleMinDecimalExponent = -45;
    static final int singleSignMask = Integer.MIN_VALUE;
    private static final float[] singleSmall10pow = new float[]{1.0f, 10.0f, 100.0f, 1000.0f, 10000.0f, 100000.0f, 1000000.0f, 1.0E7f, 1.0E8f, 1.0E9f, 1.0E10f};
    private static final double[] small10pow = new double[]{1.0d, 10.0d, 100.0d, 1000.0d, 10000.0d, 100000.0d, 1000000.0d, 1.0E7d, 1.0E8d, 1.0E9d, 1.0E10d, 1.0E11d, 1.0E12d, 1.0E13d, 1.0E14d, 1.0E15d, 1.0E16d, 1.0E17d, 1.0E18d, 1.0E19d, 1.0E20d, 1.0E21d, 1.0E22d};
    private static final int[] small5pow = new int[]{1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125};
    private static final double[] tiny10pow = new double[]{1.0E-16d, 1.0E-32d, 1.0E-64d, 1.0E-128d, 1.0E-256d};
    private static final char[] zero = new char[]{'0', '0', '0', '0', '0', '0', '0', '0'};
    int bigIntExp;
    int bigIntNBits;
    int decExponent;
    char[] digits;
    boolean fromHex;
    boolean isExceptional;
    boolean isNegative;
    boolean mustSetRoundDir;
    int nDigits;
    int roundDir;

    java.lang.FloatingDecimal parseHexString(java.lang.String r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.FloatingDecimal.parseHexString(java.lang.String):java.lang.FloatingDecimal
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.FloatingDecimal.parseHexString(java.lang.String):java.lang.FloatingDecimal");
    }

    private static int countBits(long v) {
        if (v == 0) {
            return 0;
        }
        while ((highbyte & v) == 0) {
            v <<= 8;
        }
        while (v > 0) {
            v <<= 1;
        }
        int n = 0;
        while ((lowbytes & v) != 0) {
            v <<= 8;
            n += 8;
        }
        while (v != 0) {
            v <<= 1;
            n++;
        }
        return n;
    }

    private static synchronized FDBigInt big5pow(int p) {
        Object obj = null;
        synchronized (FloatingDecimal.class) {
            if (!-assertionsDisabled) {
                if (p >= 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError(Integer.valueOf(p));
                }
            }
            if (b5p == null) {
                b5p = new FDBigInt[(p + 1)];
            } else if (b5p.length <= p) {
                Object t = new FDBigInt[(p + 1)];
                System.arraycopy(b5p, 0, t, 0, b5p.length);
                b5p = t;
            }
            FDBigInt fDBigInt;
            if (b5p[p] != null) {
                fDBigInt = b5p[p];
                return fDBigInt;
            } else if (p < small5pow.length) {
                fDBigInt = new FDBigInt(small5pow[p]);
                b5p[p] = fDBigInt;
                return fDBigInt;
            } else if (p < long5pow.length) {
                fDBigInt = new FDBigInt(long5pow[p]);
                b5p[p] = fDBigInt;
                return fDBigInt;
            } else {
                int q = p >> 1;
                int r = p - q;
                FDBigInt bigq = b5p[q];
                if (bigq == null) {
                    bigq = big5pow(q);
                }
                if (r < small5pow.length) {
                    fDBigInt = bigq.mult(small5pow[r]);
                    b5p[p] = fDBigInt;
                    return fDBigInt;
                }
                FDBigInt bigr = b5p[r];
                if (bigr == null) {
                    bigr = big5pow(r);
                }
                fDBigInt = bigq.mult(bigr);
                b5p[p] = fDBigInt;
                return fDBigInt;
            }
        }
    }

    private static FDBigInt multPow52(FDBigInt v, int p5, int p2) {
        if (p5 != 0) {
            if (p5 < small5pow.length) {
                v = v.mult(small5pow[p5]);
            } else {
                v = v.mult(big5pow(p5));
            }
        }
        if (p2 != 0) {
            v.lshiftMe(p2);
        }
        return v;
    }

    private static FDBigInt constructPow52(int p5, int p2) {
        FDBigInt v = new FDBigInt(big5pow(p5));
        if (p2 != 0) {
            v.lshiftMe(p2);
        }
        return v;
    }

    private FDBigInt doubleToBigInt(double dval) {
        long lbits = Double.doubleToLongBits(dval) & Long.MAX_VALUE;
        int binexp = (int) (lbits >>> expShift);
        lbits &= 4503599627370495L;
        if (binexp > 0) {
            lbits |= fractHOB;
        } else {
            if (!-assertionsDisabled) {
                if ((lbits != 0 ? 1 : null) == null) {
                    throw new AssertionError(Long.valueOf(lbits));
                }
            }
            binexp++;
            while ((fractHOB & lbits) == 0) {
                lbits <<= 1;
                binexp--;
            }
        }
        binexp -= 1023;
        int nbits = countBits(lbits);
        lbits >>>= 53 - nbits;
        this.bigIntExp = (binexp + 1) - nbits;
        this.bigIntNBits = nbits;
        return new FDBigInt(lbits);
    }

    private static double ulp(double dval, boolean subtracting) {
        double ulpval;
        long lbits = Double.doubleToLongBits(dval) & Long.MAX_VALUE;
        int binexp = (int) (lbits >>> 52);
        if (subtracting && binexp >= expShift && (4503599627370495L & lbits) == 0) {
            binexp--;
        }
        if (binexp > expShift) {
            ulpval = Double.longBitsToDouble(((long) (binexp - 52)) << 52);
        } else if (binexp == 0) {
            ulpval = Double.MIN_VALUE;
        } else {
            ulpval = Double.longBitsToDouble(1 << (binexp - 1));
        }
        if (subtracting) {
            return -ulpval;
        }
        return ulpval;
    }

    float stickyRound(double dval) {
        long lbits = Double.doubleToLongBits(dval);
        long binexp = lbits & 9218868437227405312L;
        if (binexp == 0 || binexp == 9218868437227405312L) {
            return (float) dval;
        }
        return (float) Double.longBitsToDouble(lbits + ((long) this.roundDir));
    }

    private void developLongDigits(int decExponent, long lvalue, long insignificant) {
        int ndigits;
        char[] digits;
        int digitno;
        int i = 0;
        while (insignificant >= 10) {
            insignificant /= 10;
            i++;
        }
        if (i != 0) {
            long pow10 = long5pow[i] << i;
            long residue = lvalue % pow10;
            lvalue /= pow10;
            decExponent += i;
            if (residue >= (pow10 >> 1)) {
                lvalue++;
            }
        }
        int c;
        int digitno2;
        if (lvalue <= 2147483647L) {
            if (!-assertionsDisabled) {
                if ((lvalue > 0 ? 1 : null) == null) {
                    throw new AssertionError(Long.valueOf(lvalue));
                }
            }
            int ivalue = (int) lvalue;
            ndigits = 10;
            digits = (char[]) perThreadBuffer.get();
            digitno = 9;
            c = ivalue % 10;
            ivalue /= 10;
            while (c == 0) {
                decExponent++;
                c = ivalue % 10;
                ivalue /= 10;
            }
            while (true) {
                digitno2 = digitno;
                if (ivalue == 0) {
                    break;
                }
                digitno = digitno2 - 1;
                digits[digitno2] = (char) (c + 48);
                decExponent++;
                c = ivalue % 10;
                ivalue /= 10;
            }
            digits[digitno2] = (char) (c + 48);
            digitno = digitno2;
        } else {
            ndigits = 20;
            digits = (char[]) perThreadBuffer.get();
            digitno = 19;
            c = (int) (lvalue % 10);
            lvalue /= 10;
            while (c == 0) {
                decExponent++;
                c = (int) (lvalue % 10);
                lvalue /= 10;
            }
            while (true) {
                digitno2 = digitno;
                if (lvalue == 0) {
                    break;
                }
                digitno = digitno2 - 1;
                digits[digitno2] = (char) (c + 48);
                decExponent++;
                c = (int) (lvalue % 10);
                lvalue /= 10;
            }
            digits[digitno2] = (char) (c + 48);
            digitno = digitno2;
        }
        ndigits -= digitno;
        char[] result = new char[ndigits];
        System.arraycopy(digits, digitno, result, 0, ndigits);
        this.digits = result;
        this.decExponent = decExponent + 1;
        this.nDigits = ndigits;
    }

    private void roundup() {
        int i = this.nDigits - 1;
        int q = this.digits[i];
        if (q == 57) {
            while (q == 57 && i > 0) {
                this.digits[i] = '0';
                i--;
                q = this.digits[i];
            }
            if (q == 57) {
                this.decExponent++;
                this.digits[0] = '1';
                return;
            }
        }
        this.digits[i] = (char) (q + 1);
    }

    private FloatingDecimal() {
        this.mustSetRoundDir = false;
        this.fromHex = false;
        this.roundDir = 0;
    }

    static {
        boolean z;
        if (FloatingDecimal.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public static FloatingDecimal getThreadLocalInstance() {
        return (FloatingDecimal) TL_INSTANCE.get();
    }

    public FloatingDecimal loadDouble(double d) {
        long dBits = Double.doubleToLongBits(d);
        this.mustSetRoundDir = false;
        this.fromHex = false;
        this.roundDir = 0;
        if ((Long.MIN_VALUE & dBits) != 0) {
            this.isNegative = true;
            dBits ^= Long.MIN_VALUE;
        } else {
            this.isNegative = false;
        }
        int binExp = (int) ((9218868437227405312L & dBits) >> expShift);
        long fractBits = dBits & 4503599627370495L;
        if (binExp == 2047) {
            this.isExceptional = true;
            if (fractBits == 0) {
                this.digits = infinity;
            } else {
                this.digits = notANumber;
                this.isNegative = false;
            }
            this.nDigits = this.digits.length;
            return this;
        }
        int nSignificantBits;
        this.isExceptional = false;
        if (binExp != 0) {
            fractBits |= fractHOB;
            nSignificantBits = 53;
        } else if (fractBits == 0) {
            this.decExponent = 0;
            this.digits = zero;
            this.nDigits = 1;
            return this;
        } else {
            while ((fractHOB & fractBits) == 0) {
                fractBits <<= 1;
                binExp--;
            }
            nSignificantBits = (binExp + expShift) + 1;
            binExp++;
        }
        dtoa(binExp - 1023, fractBits, nSignificantBits);
        return this;
    }

    public FloatingDecimal loadFloat(float f) {
        int fBits = Float.floatToIntBits(f);
        this.mustSetRoundDir = false;
        this.fromHex = false;
        this.roundDir = 0;
        if ((fBits & Integer.MIN_VALUE) != 0) {
            this.isNegative = true;
            fBits ^= Integer.MIN_VALUE;
        } else {
            this.isNegative = false;
        }
        int binExp = (2139095040 & fBits) >> 23;
        int fractBits = fBits & 8388607;
        if (binExp == 255) {
            this.isExceptional = true;
            if (((long) fractBits) == 0) {
                this.digits = infinity;
            } else {
                this.digits = notANumber;
                this.isNegative = false;
            }
            this.nDigits = this.digits.length;
            return this;
        }
        int nSignificantBits;
        this.isExceptional = false;
        if (binExp != 0) {
            fractBits |= singleFractHOB;
            nSignificantBits = 24;
        } else if (fractBits == 0) {
            this.decExponent = 0;
            this.digits = zero;
            this.nDigits = 1;
            return this;
        } else {
            while ((fractBits & singleFractHOB) == 0) {
                fractBits <<= 1;
                binExp--;
            }
            nSignificantBits = (binExp + 23) + 1;
            binExp++;
        }
        dtoa(binExp - 127, ((long) fractBits) << 29, nSignificantBits);
        return this;
    }

    private void dtoa(int binExp, long fractBits, int nSignificantBits) {
        int nFractBits = countBits(fractBits);
        int nTinyBits = Math.max(0, (nFractBits - binExp) - 1);
        if (binExp > maxSmallBinExp || binExp < minSmallBinExp || nTinyBits >= long5pow.length || n5bits[nTinyBits] + nFractBits >= 64 || nTinyBits != 0) {
            int i;
            int ndigit;
            boolean low;
            boolean high;
            long lowDigitDifference;
            int decExp = (int) Math.floor((((Double.longBitsToDouble((-4503599627370497L & fractBits) | expOne) - 1.5d) * 0.289529654d) + 0.176091259d) + (((double) binExp) * 0.301029995663981d));
            int B5 = Math.max(0, -decExp);
            int B2 = (B5 + nTinyBits) + binExp;
            int S5 = Math.max(0, decExp);
            int S2 = S5 + nTinyBits;
            int M5 = B5;
            int M2 = B2 - nSignificantBits;
            fractBits >>>= 53 - nFractBits;
            B2 -= nFractBits - 1;
            int common2factor = Math.min(B2, S2);
            B2 -= common2factor;
            S2 -= common2factor;
            M2 -= common2factor;
            if (nFractBits == 1) {
                M2--;
            }
            if (M2 < 0) {
                B2 -= M2;
                S2 -= M2;
                M2 = 0;
            }
            char[] digits = new char[18];
            this.digits = digits;
            int Bbits = (nFractBits + B2) + (B5 < n5bits.length ? n5bits[B5] : B5 * 3);
            int i2 = S2 + 1;
            if (S5 + 1 < n5bits.length) {
                i = n5bits[S5 + 1];
            } else {
                i = (S5 + 1) * 3;
            }
            int tenSbits = i2 + i;
            int q;
            int ndigit2;
            if (Bbits >= 64 || tenSbits >= 64) {
                FDBigInt Bval = multPow52(new FDBigInt(fractBits), B5, B2);
                FDBigInt Sval = constructPow52(S5, S2);
                FDBigInt Mval = constructPow52(B5, M2);
                int shiftBias = Sval.normalizeMe();
                Bval.lshiftMe(shiftBias);
                Mval.lshiftMe(shiftBias);
                FDBigInt tenSval = Sval.mult(10);
                ndigit = 0;
                q = Bval.quoRemIteration(Sval);
                Mval = Mval.mult(10);
                low = Bval.cmp(Mval) < 0;
                high = Bval.add(Mval).cmp(tenSval) > 0;
                if (!-assertionsDisabled) {
                    if ((q < 10 ? 1 : null) == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[0] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = false;
                    high = false;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = Bval.quoRemIteration(Sval);
                    Mval = Mval.mult(10);
                    if (!-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    low = Bval.cmp(Mval) < 0;
                    high = Bval.add(Mval).cmp(tenSval) > 0;
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                if (high && low) {
                    Bval.lshiftMe(1);
                    lowDigitDifference = (long) Bval.cmp(tenSval);
                    ndigit = ndigit2;
                } else {
                    lowDigitDifference = 0;
                    ndigit = ndigit2;
                }
            } else if (Bbits >= 32 || tenSbits >= 32) {
                long b = (long5pow[B5] * fractBits) << B2;
                long s = long5pow[S5] << S2;
                long tens = s * 10;
                ndigit = 0;
                q = (int) (b / s);
                b = 10 * (b % s);
                long m = (long5pow[B5] << M2) * 10;
                low = b < m;
                high = b + m > tens;
                if (!-assertionsDisabled) {
                    if ((q < 10 ? 1 : null) == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[0] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = false;
                    high = false;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = (int) (b / s);
                    b = 10 * (b % s);
                    m *= 10;
                    if (!-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    if (m > 0) {
                        low = b < m;
                        high = b + m > tens;
                    } else {
                        low = true;
                        high = true;
                    }
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                lowDigitDifference = (b << 1) - tens;
                ndigit = ndigit2;
            } else {
                int b2 = (((int) fractBits) * small5pow[B5]) << B2;
                int s2 = small5pow[S5] << S2;
                int tens2 = s2 * 10;
                ndigit = 0;
                q = b2 / s2;
                b2 = (b2 % s2) * 10;
                int m2 = (small5pow[B5] << M2) * 10;
                low = b2 < m2;
                high = b2 + m2 > tens2;
                if (!-assertionsDisabled) {
                    Object obj;
                    if (q < 10) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[0] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = false;
                    high = false;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = b2 / s2;
                    b2 = (b2 % s2) * 10;
                    m2 *= 10;
                    if (!-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    if (((long) m2) > 0) {
                        low = b2 < m2;
                        high = b2 + m2 > tens2;
                    } else {
                        low = true;
                        high = true;
                    }
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                lowDigitDifference = (long) ((b2 << 1) - tens2);
                ndigit = ndigit2;
            }
            this.decExponent = decExp + 1;
            this.digits = digits;
            this.nDigits = ndigit;
            if (high) {
                if (!low) {
                    roundup();
                } else if (lowDigitDifference == 0) {
                    if ((digits[this.nDigits - 1] & 1) != 0) {
                        roundup();
                    }
                } else if (lowDigitDifference > 0) {
                    roundup();
                }
            }
            return;
        }
        long halfULP;
        if (binExp > nSignificantBits) {
            halfULP = 1 << ((binExp - nSignificantBits) - 1);
        } else {
            halfULP = 0;
        }
        if (binExp >= expShift) {
            fractBits <<= binExp - 52;
        } else {
            fractBits >>>= 52 - binExp;
        }
        developLongDigits(0, fractBits, halfULP);
    }

    public String toString() {
        StringBuffer result = new StringBuffer(this.nDigits + 8);
        if (this.isNegative) {
            result.append('-');
        }
        if (this.isExceptional) {
            result.append(this.digits, 0, this.nDigits);
        } else {
            result.append("0.");
            result.append(this.digits, 0, this.nDigits);
            result.append('e');
            result.append(this.decExponent);
        }
        return new String(result);
    }

    public String toJavaFormatString() {
        char[] result = (char[]) perThreadBuffer.get();
        return new String(result, 0, getChars(result));
    }

    private int getChars(char[] result) {
        if (!-assertionsDisabled) {
            if ((this.nDigits <= 19 ? 1 : 0) == 0) {
                throw new AssertionError(Integer.valueOf(this.nDigits));
            }
        }
        int i = 0;
        if (this.isNegative) {
            result[0] = '-';
            i = 1;
        }
        if (this.isExceptional) {
            System.arraycopy(this.digits, 0, result, i, this.nDigits);
            return i + this.nDigits;
        } else if (this.decExponent > 0 && this.decExponent < 8) {
            int charLength = Math.min(this.nDigits, this.decExponent);
            System.arraycopy(this.digits, 0, result, i, charLength);
            i += charLength;
            if (charLength < this.decExponent) {
                charLength = this.decExponent - charLength;
                System.arraycopy(zero, 0, result, i, charLength);
                i += charLength;
                i = i + 1;
                result[i] = '.';
                i = i + 1;
                result[i] = '0';
                return i;
            }
            i = i + 1;
            result[i] = '.';
            if (charLength < this.nDigits) {
                int t = this.nDigits - charLength;
                System.arraycopy(this.digits, charLength, result, i, t);
                return i + t;
            }
            i = i + 1;
            result[i] = '0';
            return i;
        } else if (this.decExponent > 0 || this.decExponent <= -3) {
            int e;
            i = i + 1;
            result[i] = this.digits[0];
            i = i + 1;
            result[i] = '.';
            if (this.nDigits > 1) {
                System.arraycopy(this.digits, 1, result, i, this.nDigits - 1);
                i += this.nDigits - 1;
            } else {
                i = i + 1;
                result[i] = '0';
                i = i;
            }
            i = i + 1;
            result[i] = 'E';
            if (this.decExponent <= 0) {
                i = i + 1;
                result[i] = '-';
                e = (-this.decExponent) + 1;
                i = i;
            } else {
                e = this.decExponent - 1;
            }
            if (e <= 9) {
                i = i + 1;
                result[i] = (char) (e + 48);
                return i;
            } else if (e <= 99) {
                i = i + 1;
                result[i] = (char) ((e / 10) + 48);
                i = i + 1;
                result[i] = (char) ((e % 10) + 48);
                return i;
            } else {
                i = i + 1;
                result[i] = (char) ((e / 100) + 48);
                e %= 100;
                i = i + 1;
                result[i] = (char) ((e / 10) + 48);
                i = i + 1;
                result[i] = (char) ((e % 10) + 48);
                return i;
            }
        } else {
            i = i + 1;
            result[i] = '0';
            i = i + 1;
            result[i] = '.';
            if (this.decExponent != 0) {
                System.arraycopy(zero, 0, result, i, -this.decExponent);
                i -= this.decExponent;
            }
            System.arraycopy(this.digits, 0, result, i, this.nDigits);
            return i + this.nDigits;
        }
    }

    public void appendTo(AbstractStringBuilder buf) {
        if (this.isNegative) {
            buf.append('-');
        }
        if (this.isExceptional) {
            buf.append(this.digits, 0, this.nDigits);
            return;
        }
        if (this.decExponent > 0 && this.decExponent < 8) {
            int charLength = Math.min(this.nDigits, this.decExponent);
            buf.append(this.digits, 0, charLength);
            if (charLength < this.decExponent) {
                buf.append(zero, 0, this.decExponent - charLength);
                buf.append(".0");
            } else {
                buf.append('.');
                if (charLength < this.nDigits) {
                    buf.append(this.digits, charLength, this.nDigits - charLength);
                } else {
                    buf.append('0');
                }
            }
        } else if (this.decExponent > 0 || this.decExponent <= -3) {
            int e;
            buf.append(this.digits[0]);
            buf.append('.');
            if (this.nDigits > 1) {
                buf.append(this.digits, 1, this.nDigits - 1);
            } else {
                buf.append('0');
            }
            buf.append('E');
            if (this.decExponent <= 0) {
                buf.append('-');
                e = (-this.decExponent) + 1;
            } else {
                e = this.decExponent - 1;
            }
            if (e <= 9) {
                buf.append((char) (e + 48));
            } else if (e <= 99) {
                buf.append((char) ((e / 10) + 48));
                buf.append((char) ((e % 10) + 48));
            } else {
                buf.append((char) ((e / 100) + 48));
                e %= 100;
                buf.append((char) ((e / 10) + 48));
                buf.append((char) ((e % 10) + 48));
            }
        } else {
            buf.append("0.");
            if (this.decExponent != 0) {
                buf.append(zero, 0, -this.decExponent);
            }
            buf.append(this.digits, 0, this.nDigits);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public FloatingDecimal readJavaFormatString(String in) throws NumberFormatException {
        boolean isNegative = false;
        boolean signSeen = false;
        try {
            in = in.trim();
            int l = in.length();
            if (l == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = 0;
            switch (in.charAt(0)) {
                case '+':
                    break;
                case '-':
                    isNegative = true;
                    break;
            }
            i = 1;
            signSeen = true;
            char c = in.charAt(i);
            if (c == 'N' || c == 'I') {
                char[] targetChars;
                boolean potentialNaN = false;
                if (c == 'N') {
                    targetChars = notANumber;
                    potentialNaN = true;
                } else {
                    targetChars = infinity;
                }
                int j = 0;
                while (i < l && j < targetChars.length) {
                    if (in.charAt(i) == targetChars[j]) {
                        i++;
                        j++;
                    } else {
                        throw new NumberFormatException("For input string: \"" + in + "\"");
                    }
                }
                if (j == targetChars.length && i == l) {
                    FloatingDecimal loadDouble;
                    if (potentialNaN) {
                        loadDouble = loadDouble(Double.NaN);
                    } else {
                        double d;
                        if (isNegative) {
                            d = Double.NEGATIVE_INFINITY;
                        } else {
                            d = Double.POSITIVE_INFINITY;
                        }
                        loadDouble = loadDouble(d);
                    }
                    return loadDouble;
                }
                throw new NumberFormatException("For input string: \"" + in + "\"");
            }
            int decExp;
            int expSign;
            int expVal;
            boolean expOverflow;
            int expAt;
            int i2;
            int expLimit;
            if (c == '0' && l > i + 1) {
                char ch = in.charAt(i + 1);
                if (ch == 'x' || ch == 'X') {
                    return parseHexString(in);
                }
            }
            char[] digits = new char[l];
            int nDigits = 0;
            boolean decSeen = false;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;
            while (i < l) {
                c = in.charAt(i);
                switch (c) {
                    case ZipConstants.CENHDR /*46*/:
                        if (decSeen) {
                            throw new NumberFormatException("multiple points");
                        }
                        decPt = i;
                        if (signSeen) {
                            decPt--;
                        }
                        decSeen = true;
                        continue;
                    case '0':
                        if (nDigits <= 0) {
                            nLeadZero++;
                            break;
                        }
                        nTrailZero++;
                        continue;
                    case '1':
                    case '2':
                    case '3':
                    case expShift /*52*/:
                    case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        int nDigits2 = nDigits;
                        while (nTrailZero > 0) {
                            nDigits = nDigits2 + 1;
                            digits[nDigits2] = '0';
                            nTrailZero--;
                            nDigits2 = nDigits;
                        }
                        nDigits = nDigits2 + 1;
                        digits[nDigits2] = c;
                        continue;
                    default:
                        break;
                }
                if (nDigits == 0) {
                    digits = zero;
                    nDigits = 1;
                }
                if (decSeen) {
                    decExp = nDigits + nTrailZero;
                } else {
                    decExp = decPt - nLeadZero;
                }
                if (i < l) {
                    c = in.charAt(i);
                    if (c == 'e' || c == 'E') {
                        expSign = 1;
                        expVal = 0;
                        expOverflow = false;
                        i++;
                        switch (in.charAt(i)) {
                            case '+':
                                break;
                            case '-':
                                expSign = -1;
                                break;
                        }
                        i++;
                        expAt = i;
                        i2 = i;
                        while (i2 < l) {
                            if (expVal >= 214748364) {
                                expOverflow = true;
                            }
                            i = i2 + 1;
                            c = in.charAt(i2);
                            switch (c) {
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case expShift /*52*/:
                                case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                    expVal = (expVal * 10) + (c - 48);
                                    i2 = i;
                                default:
                                    i--;
                                    break;
                            }
                            expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                            if (!expOverflow || expVal > expLimit) {
                                decExp = expSign * expLimit;
                            } else {
                                decExp += expSign * expVal;
                            }
                        }
                        i = i2;
                        expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                        if (expOverflow) {
                        }
                        decExp = expSign * expLimit;
                    }
                }
                if (i >= l || (i == l - 1 && (in.charAt(i) == 'f' || in.charAt(i) == 'F' || in.charAt(i) == 'd' || in.charAt(i) == 'D'))) {
                    this.isNegative = isNegative;
                    this.decExponent = decExp;
                    this.digits = digits;
                    this.nDigits = nDigits;
                    this.isExceptional = false;
                    return this;
                }
                throw new NumberFormatException("For input string: \"" + in + "\"");
            }
            if (nDigits == 0) {
                digits = zero;
                nDigits = 1;
            }
            if (decSeen) {
                decExp = nDigits + nTrailZero;
            } else {
                decExp = decPt - nLeadZero;
            }
            if (i < l) {
                c = in.charAt(i);
                expSign = 1;
                expVal = 0;
                expOverflow = false;
                i++;
                switch (in.charAt(i)) {
                    case '+':
                        break;
                    case '-':
                        expSign = -1;
                        break;
                }
                i++;
                expAt = i;
                i2 = i;
                while (i2 < l) {
                    if (expVal >= 214748364) {
                        expOverflow = true;
                    }
                    i = i2 + 1;
                    c = in.charAt(i2);
                    switch (c) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case expShift /*52*/:
                        case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            expVal = (expVal * 10) + (c - 48);
                            i2 = i;
                        default:
                            i--;
                            break;
                    }
                    expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                    if (expOverflow) {
                    }
                    decExp = expSign * expLimit;
                }
                i = i2;
                expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                if (expOverflow) {
                }
                decExp = expSign * expLimit;
            }
            this.isNegative = isNegative;
            this.decExponent = decExp;
            this.digits = digits;
            this.nDigits = nDigits;
            this.isExceptional = false;
            return this;
        } catch (StringIndexOutOfBoundsException e) {
        }
    }

    public double doubleValue() {
        int kDigits = Math.min(this.nDigits, 16);
        if (this.digits != infinity && this.digits != notANumber) {
            int i;
            int i2;
            if (this.mustSetRoundDir) {
                this.roundDir = 0;
            }
            int iValue = this.digits[0] - 48;
            int iDigits = Math.min(kDigits, 9);
            for (i = 1; i < iDigits; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            long lValue = (long) iValue;
            for (i = iDigits; i < kDigits; i++) {
                lValue = (10 * lValue) + ((long) (this.digits[i] - 48));
            }
            double dValue = (double) lValue;
            int exp = this.decExponent - kDigits;
            if (this.nDigits <= 15) {
                if (exp == 0 || dValue == 0.0d) {
                    if (this.isNegative) {
                        dValue = -dValue;
                    }
                    return dValue;
                } else if (exp >= 0) {
                    if (exp <= maxSmallTen) {
                        rValue = dValue * small10pow[exp];
                        if (this.mustSetRoundDir) {
                            tValue = rValue / small10pow[exp];
                            if (tValue == dValue) {
                                i2 = 0;
                            } else if (tValue < dValue) {
                                i2 = 1;
                            } else {
                                i2 = -1;
                            }
                            this.roundDir = i2;
                        }
                        if (this.isNegative) {
                            rValue = -rValue;
                        }
                        return rValue;
                    }
                    int slop = 15 - kDigits;
                    if (exp <= maxSmallTen + slop) {
                        dValue *= small10pow[slop];
                        rValue = dValue * small10pow[exp - slop];
                        if (this.mustSetRoundDir) {
                            tValue = rValue / small10pow[exp - slop];
                            if (tValue == dValue) {
                                i2 = 0;
                            } else if (tValue < dValue) {
                                i2 = 1;
                            } else {
                                i2 = -1;
                            }
                            this.roundDir = i2;
                        }
                        if (this.isNegative) {
                            rValue = -rValue;
                        }
                        return rValue;
                    }
                } else if (exp >= (-maxSmallTen)) {
                    rValue = dValue / small10pow[-exp];
                    tValue = rValue * small10pow[-exp];
                    if (this.mustSetRoundDir) {
                        if (tValue == dValue) {
                            i2 = 0;
                        } else if (tValue < dValue) {
                            i2 = 1;
                        } else {
                            i2 = -1;
                        }
                        this.roundDir = i2;
                    }
                    if (this.isNegative) {
                        rValue = -rValue;
                    }
                    return rValue;
                }
            }
            int j;
            double t;
            if (exp > 0) {
                if (this.decExponent > 309) {
                    return this.isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }
                if ((exp & 15) != 0) {
                    dValue *= small10pow[exp & 15];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = 0;
                    while (exp > 1) {
                        if ((exp & 1) != 0) {
                            dValue *= big10pow[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                    t = dValue * big10pow[j];
                    if (Double.isInfinite(t)) {
                        if (Double.isInfinite((dValue / 2.0d) * big10pow[j])) {
                            return this.isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        }
                        t = Double.MAX_VALUE;
                    }
                    dValue = t;
                }
            } else if (exp < 0) {
                exp = -exp;
                if (this.decExponent < -325) {
                    return this.isNegative ? -0.0d : 0.0d;
                }
                if ((exp & 15) != 0) {
                    dValue /= small10pow[exp & 15];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = 0;
                    while (exp > 1) {
                        if ((exp & 1) != 0) {
                            dValue *= tiny10pow[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                    t = dValue * tiny10pow[j];
                    if (t == 0.0d) {
                        if ((dValue * 2.0d) * tiny10pow[j] == 0.0d) {
                            return this.isNegative ? -0.0d : 0.0d;
                        }
                        t = Double.MIN_VALUE;
                    }
                    dValue = t;
                }
            }
            FDBigInt bigD0 = new FDBigInt(lValue, this.digits, kDigits, this.nDigits);
            exp = this.decExponent - this.nDigits;
            do {
                int B5;
                int B2;
                int D5;
                int D2;
                int hulpbias;
                boolean overvalue;
                FDBigInt diff;
                FDBigInt bigB = doubleToBigInt(dValue);
                if (exp >= 0) {
                    B5 = 0;
                    B2 = 0;
                    D5 = exp;
                    D2 = exp;
                } else {
                    B5 = -exp;
                    B2 = B5;
                    D5 = 0;
                    D2 = 0;
                }
                if (this.bigIntExp >= 0) {
                    B2 += this.bigIntExp;
                } else {
                    D2 -= this.bigIntExp;
                }
                int Ulp2 = B2;
                if (this.bigIntExp + this.bigIntNBits <= -1022) {
                    hulpbias = (this.bigIntExp + 1023) + expShift;
                } else {
                    hulpbias = 54 - this.bigIntNBits;
                }
                B2 += hulpbias;
                D2 += hulpbias;
                int common2 = Math.min(B2, Math.min(D2, Ulp2));
                D2 -= common2;
                Ulp2 -= common2;
                bigB = multPow52(bigB, B5, B2 - common2);
                FDBigInt bigD = multPow52(new FDBigInt(bigD0), D5, D2);
                int cmpResult = bigB.cmp(bigD);
                if (cmpResult <= 0) {
                    if (cmpResult >= 0) {
                        break;
                    }
                    overvalue = false;
                    diff = bigD.sub(bigB);
                } else {
                    overvalue = true;
                    diff = bigB.sub(bigD);
                    if (this.bigIntNBits == 1 && this.bigIntExp > -1022) {
                        Ulp2--;
                        if (Ulp2 < 0) {
                            Ulp2 = 0;
                            diff.lshiftMe(1);
                        }
                    }
                }
                cmpResult = diff.cmp(constructPow52(B5, Ulp2));
                if (cmpResult >= 0) {
                    if (cmpResult != 0) {
                        dValue += ulp(dValue, overvalue);
                        if (dValue == 0.0d) {
                            break;
                        }
                    } else {
                        dValue += ulp(dValue, overvalue) * 0.5d;
                        if (this.mustSetRoundDir) {
                            this.roundDir = overvalue ? -1 : 1;
                        }
                    }
                } else if (this.mustSetRoundDir) {
                    if (overvalue) {
                        i2 = -1;
                    } else {
                        i2 = 1;
                    }
                    this.roundDir = i2;
                }
            } while (dValue != Double.POSITIVE_INFINITY);
            if (this.isNegative) {
                dValue = -dValue;
            }
            return dValue;
        } else if (this.digits == notANumber) {
            return Double.NaN;
        } else {
            return this.isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
    }

    public float floatValue() {
        int kDigits = Math.min(this.nDigits, 8);
        if (this.digits != infinity && this.digits != notANumber) {
            int i;
            int iValue = this.digits[0] - 48;
            for (i = 1; i < kDigits; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            float fValue = (float) iValue;
            int exp = this.decExponent - kDigits;
            if (this.nDigits <= 7) {
                if (exp == 0 || fValue == 0.0f) {
                    if (this.isNegative) {
                        fValue = -fValue;
                    }
                    return fValue;
                } else if (exp >= 0) {
                    if (exp <= singleMaxSmallTen) {
                        fValue *= singleSmall10pow[exp];
                        if (this.isNegative) {
                            fValue = -fValue;
                        }
                        return fValue;
                    }
                    int slop = 7 - kDigits;
                    if (exp <= singleMaxSmallTen + slop) {
                        fValue = (fValue * singleSmall10pow[slop]) * singleSmall10pow[exp - slop];
                        if (this.isNegative) {
                            fValue = -fValue;
                        }
                        return fValue;
                    }
                } else if (exp >= (-singleMaxSmallTen)) {
                    fValue /= singleSmall10pow[-exp];
                    if (this.isNegative) {
                        fValue = -fValue;
                    }
                    return fValue;
                }
            } else if (this.decExponent >= this.nDigits && this.nDigits + this.decExponent <= 15) {
                long lValue = (long) iValue;
                for (i = kDigits; i < this.nDigits; i++) {
                    lValue = (10 * lValue) + ((long) (this.digits[i] - 48));
                }
                fValue = (float) (((double) lValue) * small10pow[this.decExponent - this.nDigits]);
                if (this.isNegative) {
                    fValue = -fValue;
                }
                return fValue;
            }
            if (this.decExponent > 39) {
                return this.isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            } else if (this.decExponent < -46) {
                return this.isNegative ? -0.0f : 0.0f;
            } else {
                this.mustSetRoundDir = !this.fromHex;
                return stickyRound(doubleValue());
            }
        } else if (this.digits == notANumber) {
            return Float.NaN;
        } else {
            return this.isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        }
    }

    private static synchronized Pattern getHexFloatPattern() {
        Pattern pattern;
        synchronized (FloatingDecimal.class) {
            if (hexFloatPattern == null) {
                hexFloatPattern = Pattern.compile("([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?");
            }
            pattern = hexFloatPattern;
        }
        return pattern;
    }

    static String stripLeadingZeros(String s) {
        return s.replaceFirst("^0+", "");
    }

    static int getHexDigit(String s, int position) {
        int value = Character.digit(s.charAt(position), 16);
        if (value > -1 && value < 16) {
            return value;
        }
        throw new AssertionError("Unexpected failure of digit conversion of " + s.charAt(position));
    }
}

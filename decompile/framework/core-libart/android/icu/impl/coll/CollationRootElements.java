package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;

public final class CollationRootElements {
    static final /* synthetic */ boolean -assertionsDisabled = (!CollationRootElements.class.desiredAssertionStatus());
    static final int IX_COMMON_SEC_AND_TER_CE = 3;
    static final int IX_COUNT = 5;
    static final int IX_FIRST_PRIMARY_INDEX = 2;
    static final int IX_FIRST_SECONDARY_INDEX = 1;
    public static final int IX_FIRST_TERTIARY_INDEX = 0;
    static final int IX_SEC_TER_BOUNDARIES = 4;
    public static final long PRIMARY_SENTINEL = 4294967040L;
    public static final int PRIMARY_STEP_MASK = 127;
    public static final int SEC_TER_DELTA_FLAG = 128;
    private long[] elements;

    public CollationRootElements(long[] rootElements) {
        this.elements = rootElements;
    }

    public int getTertiaryBoundary() {
        return (((int) this.elements[4]) << 8) & Normalizer2Impl.JAMO_VT;
    }

    long getFirstTertiaryCE() {
        return this.elements[(int) this.elements[0]] & -129;
    }

    long getLastTertiaryCE() {
        return this.elements[((int) this.elements[1]) - 1] & -129;
    }

    public int getLastCommonSecondary() {
        return (((int) this.elements[4]) >> 16) & Normalizer2Impl.JAMO_VT;
    }

    public int getSecondaryBoundary() {
        return (((int) this.elements[4]) >> 8) & Normalizer2Impl.JAMO_VT;
    }

    long getFirstSecondaryCE() {
        return this.elements[(int) this.elements[1]] & -129;
    }

    long getLastSecondaryCE() {
        return this.elements[((int) this.elements[2]) - 1] & -129;
    }

    long getFirstPrimary() {
        return this.elements[(int) this.elements[2]];
    }

    long getFirstPrimaryCE() {
        return Collation.makeCE(getFirstPrimary());
    }

    long lastCEWithPrimaryBefore(long p) {
        if (p == 0) {
            return 0;
        }
        long secTer;
        if (!-assertionsDisabled) {
            if ((p > this.elements[(int) this.elements[2]] ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int index = findP(p);
        long q = this.elements[index];
        if (p == (PRIMARY_SENTINEL & q)) {
            if (!-assertionsDisabled) {
                if (((127 & q) == 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            secTer = this.elements[index - 1];
            if ((128 & secTer) == 0) {
                p = secTer & PRIMARY_SENTINEL;
                secTer = 83887360;
            } else {
                index -= 2;
                while (true) {
                    p = this.elements[index];
                    if ((128 & p) == 0) {
                        break;
                    }
                    index--;
                }
                p &= PRIMARY_SENTINEL;
            }
        } else {
            p = q & PRIMARY_SENTINEL;
            secTer = 83887360;
            while (true) {
                index++;
                q = this.elements[index];
                if ((128 & q) == 0) {
                    break;
                }
                secTer = q;
            }
            if (!-assertionsDisabled) {
                if (((127 & q) == 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
        }
        return (p << 32) | (-129 & secTer);
    }

    long firstCEWithPrimaryAtLeast(long p) {
        if (p == 0) {
            return 0;
        }
        int index = findP(p);
        if (p != (this.elements[index] & PRIMARY_SENTINEL)) {
            do {
                index++;
                p = this.elements[index];
            } while ((128 & p) != 0);
            if (!-assertionsDisabled) {
                if (((127 & p) == 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
        }
        return (p << 32) | 83887360;
    }

    long getPrimaryBefore(long p, boolean isCompressible) {
        int step;
        int index = findPrimary(p);
        long q = this.elements[index];
        if (p == (q & PRIMARY_SENTINEL)) {
            step = ((int) q) & 127;
            if (step == 0) {
                do {
                    index--;
                    p = this.elements[index];
                } while ((128 & p) != 0);
                return p & PRIMARY_SENTINEL;
            }
        }
        long nextElement = this.elements[index + 1];
        if (-assertionsDisabled || isEndOfPrimaryRange(nextElement)) {
            step = ((int) nextElement) & 127;
        } else {
            throw new AssertionError();
        }
        if ((65535 & p) == 0) {
            return Collation.decTwoBytePrimaryByOneStep(p, isCompressible, step);
        }
        return Collation.decThreeBytePrimaryByOneStep(p, isCompressible, step);
    }

    int getSecondaryBefore(long p, int s) {
        int index;
        int previousSec;
        int sec;
        if (p == 0) {
            index = (int) this.elements[1];
            previousSec = 0;
            sec = (int) (this.elements[index] >> 16);
        } else {
            index = findPrimary(p) + 1;
            previousSec = 256;
            sec = ((int) getFirstSecTerForPrimary(index)) >>> 16;
        }
        if (!-assertionsDisabled) {
            if ((s >= sec ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int index2 = index;
        while (s > sec) {
            previousSec = sec;
            if (!-assertionsDisabled) {
                if (((this.elements[index2] & 128) != 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            sec = (int) (this.elements[index2] >> 16);
            index2++;
        }
        if (!-assertionsDisabled) {
            if ((sec == s ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        return previousSec;
    }

    int getTertiaryBefore(long p, int s, int t) {
        int index;
        int previousTer;
        long secTer;
        if (!-assertionsDisabled) {
            if (((t & -16192) == 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (p == 0) {
            if (s == 0) {
                index = (int) this.elements[0];
                previousTer = 0;
            } else {
                index = (int) this.elements[1];
                previousTer = 256;
            }
            secTer = this.elements[index] & -129;
        } else {
            index = findPrimary(p) + 1;
            previousTer = 256;
            secTer = getFirstSecTerForPrimary(index);
        }
        long st = (((long) s) << 16) | ((long) t);
        int index2 = index;
        while (st > secTer) {
            if (((int) (secTer >> 16)) == s) {
                previousTer = (int) secTer;
            }
            if (!-assertionsDisabled) {
                Object obj;
                if ((this.elements[index2] & 128) != 0) {
                    obj = 1;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            secTer = this.elements[index2] & -129;
            index2++;
        }
        if (!-assertionsDisabled) {
            if ((secTer == st ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        return 65535 & previousTer;
    }

    int findPrimary(long p) {
        Object obj = 1;
        if (!-assertionsDisabled) {
            if (((255 & p) == 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int index = findP(p);
        if (!-assertionsDisabled) {
            if (!(isEndOfPrimaryRange(this.elements[index + 1]) || p == (this.elements[index] & PRIMARY_SENTINEL))) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return index;
    }

    long getPrimaryAfter(long p, int index, boolean isCompressible) {
        if (!-assertionsDisabled) {
            if (!(p != (this.elements[index] & PRIMARY_SENTINEL) ? isEndOfPrimaryRange(this.elements[index + 1]) : true)) {
                throw new AssertionError();
            }
        }
        index++;
        long q = this.elements[index];
        if ((128 & q) == 0) {
            int step = ((int) q) & 127;
            if (step != 0) {
                if ((65535 & p) == 0) {
                    return Collation.incTwoBytePrimaryByOffset(p, isCompressible, step);
                }
                return Collation.incThreeBytePrimaryByOffset(p, isCompressible, step);
            }
        }
        while ((128 & q) != 0) {
            index++;
            q = this.elements[index];
        }
        if (!-assertionsDisabled) {
            if (((127 & q) == 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        return q;
    }

    int getSecondaryAfter(int index, int s) {
        long secTer;
        int secLimit;
        int i = 1;
        int i2 = 0;
        if (index == 0) {
            if (!-assertionsDisabled) {
                if (s != 0) {
                    i2 = 1;
                }
                if (i2 == 0) {
                    throw new AssertionError();
                }
            }
            index = (int) this.elements[1];
            secTer = this.elements[index];
            secLimit = 65536;
        } else {
            if (!-assertionsDisabled) {
                if (index < ((int) this.elements[2])) {
                    i = 0;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            secTer = getFirstSecTerForPrimary(index + 1);
            secLimit = getSecondaryBoundary();
        }
        do {
            int sec = (int) (secTer >> 16);
            if (sec > s) {
                return sec;
            }
            index++;
            secTer = this.elements[index];
        } while ((128 & secTer) != 0);
        return secLimit;
    }

    int getTertiaryAfter(int index, int s, int t) {
        int terLimit;
        long secTer;
        if (index == 0) {
            if (s == 0) {
                if (!-assertionsDisabled) {
                    if ((t != 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                index = (int) this.elements[0];
                terLimit = 16384;
            } else {
                index = (int) this.elements[1];
                terLimit = getTertiaryBoundary();
            }
            secTer = this.elements[index] & -129;
        } else {
            if (!-assertionsDisabled) {
                if ((index >= ((int) this.elements[2]) ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            secTer = getFirstSecTerForPrimary(index + 1);
            terLimit = getTertiaryBoundary();
        }
        long st = ((((long) s) & 4294967295L) << 16) | ((long) t);
        while (secTer <= st) {
            index++;
            secTer = this.elements[index];
            if ((128 & secTer) == 0 || (secTer >> 16) > ((long) s)) {
                return terLimit;
            }
            secTer &= -129;
        }
        if (!-assertionsDisabled) {
            Object obj;
            if ((secTer >> 16) == ((long) s)) {
                obj = 1;
            } else {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return ((int) secTer) & 65535;
    }

    private long getFirstSecTerForPrimary(int index) {
        long secTer = this.elements[index];
        if ((128 & secTer) == 0) {
            return 83887360;
        }
        secTer &= -129;
        if (secTer > 83887360) {
            return 83887360;
        }
        return secTer;
    }

    private int findP(long p) {
        if (!-assertionsDisabled) {
            if (((p >> 24) != 254 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int start = (int) this.elements[2];
        if (!-assertionsDisabled) {
            if ((p >= this.elements[start] ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int limit = this.elements.length - 1;
        if (!-assertionsDisabled) {
            if ((this.elements[limit] >= PRIMARY_SENTINEL ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if ((p < this.elements[limit] ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        while (start + 1 < limit) {
            int i = (start + limit) / 2;
            long q = this.elements[i];
            if ((128 & q) != 0) {
                int j;
                for (j = i + 1; j != limit; j++) {
                    q = this.elements[j];
                    if ((128 & q) == 0) {
                        i = j;
                        break;
                    }
                }
                if ((128 & q) != 0) {
                    for (j = i - 1; j != start; j--) {
                        q = this.elements[j];
                        if ((128 & q) == 0) {
                            i = j;
                            break;
                        }
                    }
                    if ((128 & q) != 0) {
                        break;
                    }
                }
            }
            if (p < (PRIMARY_SENTINEL & q)) {
                limit = i;
            } else {
                start = i;
            }
        }
        return start;
    }

    private static boolean isEndOfPrimaryRange(long q) {
        return (128 & q) == 0 && (127 & q) != 0;
    }
}

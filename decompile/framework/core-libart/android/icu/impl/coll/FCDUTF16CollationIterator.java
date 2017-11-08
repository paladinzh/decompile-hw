package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;

public final class FCDUTF16CollationIterator extends UTF16CollationIterator {
    static final /* synthetic */ boolean -assertionsDisabled = (!FCDUTF16CollationIterator.class.desiredAssertionStatus());
    private static final int rawStart = 0;
    private int checkDir;
    private final Normalizer2Impl nfcImpl;
    private StringBuilder normalized;
    private int rawLimit;
    private CharSequence rawSeq;
    private int segmentLimit;
    private int segmentStart;

    public FCDUTF16CollationIterator(CollationData d) {
        super(d);
        this.nfcImpl = d.nfcImpl;
    }

    public FCDUTF16CollationIterator(CollationData data, boolean numeric, CharSequence s, int p) {
        super(data, numeric, s, p);
        this.rawSeq = s;
        this.segmentStart = p;
        this.rawLimit = s.length();
        this.nfcImpl = data.nfcImpl;
        this.checkDir = 1;
    }

    public boolean equals(Object other) {
        boolean z = true;
        boolean z2 = false;
        if (!equals(other)) {
            return false;
        }
        FCDUTF16CollationIterator o = (FCDUTF16CollationIterator) other;
        if (this.checkDir != o.checkDir) {
            return false;
        }
        if (this.checkDir == 0) {
            if ((this.seq == this.rawSeq) != (o.seq == o.rawSeq)) {
                return false;
            }
        }
        if (this.checkDir != 0 || this.seq == this.rawSeq) {
            if (this.pos + 0 != o.pos + 0) {
                z = false;
            }
            return z;
        }
        if (this.segmentStart + 0 == o.segmentStart + 0 && this.pos - this.start == o.pos - o.start) {
            z2 = true;
        }
        return z2;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public void resetToOffset(int newOffset) {
        reset();
        this.seq = this.rawSeq;
        int i = newOffset + 0;
        this.pos = i;
        this.segmentStart = i;
        this.start = i;
        this.limit = this.rawLimit;
        this.checkDir = 1;
    }

    public int getOffset() {
        if (this.checkDir != 0 || this.seq == this.rawSeq) {
            return this.pos + 0;
        }
        if (this.pos == this.start) {
            return this.segmentStart + 0;
        }
        return this.segmentLimit + 0;
    }

    public void setText(boolean numeric, CharSequence s, int p) {
        super.setText(numeric, s, p);
        this.rawSeq = s;
        this.segmentStart = p;
        int length = s.length();
        this.limit = length;
        this.rawLimit = length;
        this.checkDir = 1;
    }

    public int nextCodePoint() {
        while (this.checkDir <= 0) {
            if (this.checkDir == 0 && this.pos != this.limit) {
                CharSequence charSequence = this.seq;
                int i = this.pos;
                this.pos = i + 1;
                char c = charSequence.charAt(i);
                break;
            }
            switchToForward();
        }
        if (this.pos == this.limit) {
            return -1;
        }
        charSequence = this.seq;
        i = this.pos;
        this.pos = i + 1;
        c = charSequence.charAt(i);
        if (CollationFCD.hasTccc(c) && (CollationFCD.maybeTibetanCompositeVowel(c) || (this.pos != this.limit && CollationFCD.hasLccc(this.seq.charAt(this.pos))))) {
            this.pos--;
            nextSegment();
            charSequence = this.seq;
            i = this.pos;
            this.pos = i + 1;
            c = charSequence.charAt(i);
        }
        if (Character.isHighSurrogate(c) && this.pos != this.limit) {
            char trail = this.seq.charAt(this.pos);
            if (Character.isLowSurrogate(trail)) {
                this.pos++;
                return Character.toCodePoint(c, trail);
            }
        }
        return c;
    }

    public int previousCodePoint() {
        while (this.checkDir >= 0) {
            if (this.checkDir == 0 && this.pos != this.start) {
                CharSequence charSequence = this.seq;
                int i = this.pos - 1;
                this.pos = i;
                char c = charSequence.charAt(i);
                break;
            }
            switchToBackward();
        }
        if (this.pos == this.start) {
            return -1;
        }
        charSequence = this.seq;
        i = this.pos - 1;
        this.pos = i;
        c = charSequence.charAt(i);
        if (CollationFCD.hasLccc(c) && (CollationFCD.maybeTibetanCompositeVowel(c) || (this.pos != this.start && CollationFCD.hasTccc(this.seq.charAt(this.pos - 1))))) {
            this.pos++;
            previousSegment();
            charSequence = this.seq;
            i = this.pos - 1;
            this.pos = i;
            c = charSequence.charAt(i);
        }
        if (Character.isLowSurrogate(c) && this.pos != this.start) {
            char lead = this.seq.charAt(this.pos - 1);
            if (Character.isHighSurrogate(lead)) {
                this.pos--;
                return Character.toCodePoint(lead, c);
            }
        }
        return c;
    }

    protected long handleNextCE32() {
        while (this.checkDir <= 0) {
            if (this.checkDir == 0 && this.pos != this.limit) {
                CharSequence charSequence = this.seq;
                int i = this.pos;
                this.pos = i + 1;
                char c = charSequence.charAt(i);
                break;
            }
            switchToForward();
        }
        if (this.pos == this.limit) {
            return -4294967104L;
        }
        charSequence = this.seq;
        i = this.pos;
        this.pos = i + 1;
        c = charSequence.charAt(i);
        if (CollationFCD.hasTccc(c) && (CollationFCD.maybeTibetanCompositeVowel(c) || (this.pos != this.limit && CollationFCD.hasLccc(this.seq.charAt(this.pos))))) {
            this.pos--;
            nextSegment();
            charSequence = this.seq;
            i = this.pos;
            this.pos = i + 1;
            c = charSequence.charAt(i);
        }
        return makeCodePointAndCE32Pair(c, this.trie.getFromU16SingleLead(c));
    }

    protected void forwardNumCodePoints(int num) {
        while (num > 0 && nextCodePoint() >= 0) {
            num--;
        }
    }

    protected void backwardNumCodePoints(int num) {
        while (num > 0 && previousCodePoint() >= 0) {
            num--;
        }
    }

    private void switchToForward() {
        int i;
        if (!-assertionsDisabled) {
            i = ((this.checkDir >= 0 || this.seq != this.rawSeq) && !(this.checkDir == 0 && this.pos == this.limit)) ? 0 : 1;
            if (i == 0) {
                throw new AssertionError();
            }
        }
        if (this.checkDir < 0) {
            i = this.pos;
            this.segmentStart = i;
            this.start = i;
            if (this.pos == this.segmentLimit) {
                this.limit = this.rawLimit;
                this.checkDir = 1;
                return;
            }
            this.checkDir = 0;
            return;
        }
        if (this.seq != this.rawSeq) {
            this.seq = this.rawSeq;
            i = this.segmentLimit;
            this.segmentStart = i;
            this.start = i;
            this.pos = i;
        }
        this.limit = this.rawLimit;
        this.checkDir = 1;
    }

    private void nextSegment() {
        int i = 1;
        if (!-assertionsDisabled) {
            int i2 = (this.checkDir <= 0 || this.seq != this.rawSeq || this.pos == this.limit) ? 0 : 1;
            if (i2 == 0) {
                throw new AssertionError();
            }
        }
        int p = this.pos;
        int prevCC = 0;
        do {
            int q = p;
            int c = Character.codePointAt(this.seq, p);
            p += Character.charCount(c);
            int fcd16 = this.nfcImpl.getFCD16(c);
            int leadCC = fcd16 >> 8;
            if (leadCC == 0 && q != this.pos) {
                this.segmentLimit = q;
                this.limit = q;
                break;
            } else if (leadCC == 0 || (prevCC <= leadCC && !CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                prevCC = fcd16 & 255;
                if (p == this.rawLimit) {
                    break;
                }
            } else {
                while (true) {
                    q = p;
                    if (p != this.rawLimit) {
                        c = Character.codePointAt(this.seq, p);
                        p += Character.charCount(c);
                        if (this.nfcImpl.getFCD16(c) <= 255) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                normalize(this.pos, q);
                this.pos = this.start;
            }
        } while (prevCC != 0);
        this.segmentLimit = p;
        this.limit = p;
        if (!-assertionsDisabled) {
            if (this.pos == this.limit) {
                i = 0;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        this.checkDir = 0;
    }

    private void switchToBackward() {
        int i = 1;
        if (!-assertionsDisabled) {
            if ((this.checkDir <= 0 || this.seq != this.rawSeq) && !(this.checkDir == 0 && this.pos == this.start)) {
                i = 0;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        if (this.checkDir > 0) {
            i = this.pos;
            this.segmentLimit = i;
            this.limit = i;
            if (this.pos == this.segmentStart) {
                this.start = 0;
                this.checkDir = -1;
                return;
            }
            this.checkDir = 0;
            return;
        }
        if (this.seq != this.rawSeq) {
            this.seq = this.rawSeq;
            i = this.segmentStart;
            this.segmentLimit = i;
            this.limit = i;
            this.pos = i;
        }
        this.start = 0;
        this.checkDir = -1;
    }

    private void previousSegment() {
        int i = 1;
        if (!-assertionsDisabled) {
            int i2 = (this.checkDir >= 0 || this.seq != this.rawSeq || this.pos == this.start) ? 0 : 1;
            if (i2 == 0) {
                throw new AssertionError();
            }
        }
        int p = this.pos;
        int nextCC = 0;
        do {
            int q = p;
            int c = Character.codePointBefore(this.seq, p);
            p -= Character.charCount(c);
            int fcd16 = this.nfcImpl.getFCD16(c);
            int trailCC = fcd16 & 255;
            if (trailCC == 0 && q != this.pos) {
                this.segmentStart = q;
                this.start = q;
                break;
            } else if (trailCC == 0 || ((nextCC == 0 || trailCC <= nextCC) && !CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                nextCC = fcd16 >> 8;
                if (p == 0) {
                    break;
                }
            } else {
                while (true) {
                    q = p;
                    if (fcd16 <= 255 || p == 0) {
                        break;
                    }
                    c = Character.codePointBefore(this.seq, p);
                    p -= Character.charCount(c);
                    fcd16 = this.nfcImpl.getFCD16(c);
                    if (fcd16 == 0) {
                        break;
                    }
                }
                normalize(q, this.pos);
                this.pos = this.limit;
            }
        } while (nextCC != 0);
        this.segmentStart = p;
        this.start = p;
        if (!-assertionsDisabled) {
            if (this.pos == this.start) {
                i = 0;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        this.checkDir = 0;
    }

    private void normalize(int from, int to) {
        if (this.normalized == null) {
            this.normalized = new StringBuilder();
        }
        this.nfcImpl.decompose(this.rawSeq, from, to, this.normalized, to - from);
        this.segmentStart = from;
        this.segmentLimit = to;
        this.seq = this.normalized;
        this.start = 0;
        this.limit = this.start + this.normalized.length();
    }
}

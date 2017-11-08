package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import java.io.Serializable;
import javax.annotation.Nullable;

@GwtCompatible
public final class Range<C extends Comparable> implements Predicate<C>, Serializable {
    private static final /* synthetic */ int[] -com-google-common-collect-BoundTypeSwitchesValues = null;
    private static final Range<Comparable> ALL = new Range(Cut.belowAll(), Cut.aboveAll());
    private static final Function<Range, Cut> LOWER_BOUND_FN = new Function<Range, Cut>() {
        public Cut apply(Range range) {
            return range.lowerBound;
        }
    };
    static final Ordering<Range<?>> RANGE_LEX_ORDERING = new Ordering<Range<?>>() {
        public int compare(Range<?> left, Range<?> right) {
            return ComparisonChain.start().compare(left.lowerBound, right.lowerBound).compare(left.upperBound, right.upperBound).result();
        }
    };
    private static final Function<Range, Cut> UPPER_BOUND_FN = new Function<Range, Cut>() {
        public Cut apply(Range range) {
            return range.upperBound;
        }
    };
    private static final long serialVersionUID = 0;
    final Cut<C> lowerBound;
    final Cut<C> upperBound;

    private static /* synthetic */ int[] -getcom-google-common-collect-BoundTypeSwitchesValues() {
        if (-com-google-common-collect-BoundTypeSwitchesValues != null) {
            return -com-google-common-collect-BoundTypeSwitchesValues;
        }
        int[] iArr = new int[BoundType.values().length];
        try {
            iArr[BoundType.CLOSED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[BoundType.OPEN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -com-google-common-collect-BoundTypeSwitchesValues = iArr;
        return iArr;
    }

    static <C extends Comparable<?>> Range<C> create(Cut<C> lowerBound, Cut<C> upperBound) {
        return new Range(lowerBound, upperBound);
    }

    public static <C extends Comparable<?>> Range<C> range(C lower, BoundType lowerType, C upper, BoundType upperType) {
        Cut<C> lowerBound;
        Cut<C> upperBound;
        Preconditions.checkNotNull(lowerType);
        Preconditions.checkNotNull(upperType);
        if (lowerType == BoundType.OPEN) {
            lowerBound = Cut.aboveValue(lower);
        } else {
            lowerBound = Cut.belowValue(lower);
        }
        if (upperType == BoundType.OPEN) {
            upperBound = Cut.belowValue(upper);
        } else {
            upperBound = Cut.aboveValue(upper);
        }
        return create(lowerBound, upperBound);
    }

    public static <C extends Comparable<?>> Range<C> lessThan(C endpoint) {
        return create(Cut.belowAll(), Cut.belowValue(endpoint));
    }

    public static <C extends Comparable<?>> Range<C> atMost(C endpoint) {
        return create(Cut.belowAll(), Cut.aboveValue(endpoint));
    }

    public static <C extends Comparable<?>> Range<C> upTo(C endpoint, BoundType boundType) {
        switch (-getcom-google-common-collect-BoundTypeSwitchesValues()[boundType.ordinal()]) {
            case 1:
                return atMost(endpoint);
            case 2:
                return lessThan(endpoint);
            default:
                throw new AssertionError();
        }
    }

    public static <C extends Comparable<?>> Range<C> greaterThan(C endpoint) {
        return create(Cut.aboveValue(endpoint), Cut.aboveAll());
    }

    public static <C extends Comparable<?>> Range<C> atLeast(C endpoint) {
        return create(Cut.belowValue(endpoint), Cut.aboveAll());
    }

    public static <C extends Comparable<?>> Range<C> downTo(C endpoint, BoundType boundType) {
        switch (-getcom-google-common-collect-BoundTypeSwitchesValues()[boundType.ordinal()]) {
            case 1:
                return atLeast(endpoint);
            case 2:
                return greaterThan(endpoint);
            default:
                throw new AssertionError();
        }
    }

    public static <C extends Comparable<?>> Range<C> all() {
        return ALL;
    }

    private Range(Cut<C> lowerBound, Cut<C> upperBound) {
        if (lowerBound.compareTo((Cut) upperBound) > 0 || lowerBound == Cut.aboveAll() || upperBound == Cut.belowAll()) {
            throw new IllegalArgumentException("Invalid range: " + toString(lowerBound, upperBound));
        }
        this.lowerBound = (Cut) Preconditions.checkNotNull(lowerBound);
        this.upperBound = (Cut) Preconditions.checkNotNull(upperBound);
    }

    public boolean hasLowerBound() {
        return this.lowerBound != Cut.belowAll();
    }

    public C lowerEndpoint() {
        return this.lowerBound.endpoint();
    }

    public boolean hasUpperBound() {
        return this.upperBound != Cut.aboveAll();
    }

    public C upperEndpoint() {
        return this.upperBound.endpoint();
    }

    public boolean contains(C value) {
        Preconditions.checkNotNull(value);
        if (!this.lowerBound.isLessThan(value) || this.upperBound.isLessThan(value)) {
            return false;
        }
        return true;
    }

    @Deprecated
    public boolean apply(C input) {
        return contains(input);
    }

    public boolean isConnected(Range<C> other) {
        if (this.lowerBound.compareTo(other.upperBound) > 0 || other.lowerBound.compareTo(this.upperBound) > 0) {
            return false;
        }
        return true;
    }

    public Range<C> intersection(Range<C> connectedRange) {
        int lowerCmp = this.lowerBound.compareTo(connectedRange.lowerBound);
        int upperCmp = this.upperBound.compareTo(connectedRange.upperBound);
        if (lowerCmp >= 0 && upperCmp <= 0) {
            return this;
        }
        if (lowerCmp <= 0 && upperCmp >= 0) {
            return connectedRange;
        }
        return create(lowerCmp >= 0 ? this.lowerBound : connectedRange.lowerBound, upperCmp <= 0 ? this.upperBound : connectedRange.upperBound);
    }

    public boolean equals(@Nullable Object object) {
        boolean z = false;
        if (!(object instanceof Range)) {
            return false;
        }
        Range<?> other = (Range) object;
        if (this.lowerBound.equals(other.lowerBound)) {
            z = this.upperBound.equals(other.upperBound);
        }
        return z;
    }

    public int hashCode() {
        return (this.lowerBound.hashCode() * 31) + this.upperBound.hashCode();
    }

    public String toString() {
        return toString(this.lowerBound, this.upperBound);
    }

    private static String toString(Cut<?> lowerBound, Cut<?> upperBound) {
        StringBuilder sb = new StringBuilder(16);
        lowerBound.describeAsLowerBound(sb);
        sb.append('‥');
        upperBound.describeAsUpperBound(sb);
        return sb.toString();
    }

    Object readResolve() {
        if (equals(ALL)) {
            return all();
        }
        return this;
    }

    static int compareOrThrow(Comparable left, Comparable right) {
        return left.compareTo(right);
    }
}

package com.google.common.collect;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import javax.annotation.Nullable;

@VisibleForTesting
final class TreeRangeSet$RangesByUpperBound<C extends Comparable<?>> extends AbstractNavigableMap<Cut<C>, Range<C>> {
    private final NavigableMap<Cut<C>, Range<C>> rangesByLowerBound;
    private final Range<Cut<C>> upperBoundWindow;

    private TreeRangeSet$RangesByUpperBound(NavigableMap<Cut<C>, Range<C>> rangesByLowerBound, Range<Cut<C>> upperBoundWindow) {
        this.rangesByLowerBound = rangesByLowerBound;
        this.upperBoundWindow = upperBoundWindow;
    }

    private NavigableMap<Cut<C>, Range<C>> subMap(Range<Cut<C>> window) {
        if (window.isConnected(this.upperBoundWindow)) {
            return new TreeRangeSet$RangesByUpperBound(this.rangesByLowerBound, window.intersection(this.upperBoundWindow));
        }
        return ImmutableSortedMap.of();
    }

    public NavigableMap<Cut<C>, Range<C>> subMap(Cut<C> fromKey, boolean fromInclusive, Cut<C> toKey, boolean toInclusive) {
        return subMap(Range.range(fromKey, BoundType.forBoolean(fromInclusive), toKey, BoundType.forBoolean(toInclusive)));
    }

    public NavigableMap<Cut<C>, Range<C>> headMap(Cut<C> toKey, boolean inclusive) {
        return subMap(Range.upTo(toKey, BoundType.forBoolean(inclusive)));
    }

    public NavigableMap<Cut<C>, Range<C>> tailMap(Cut<C> fromKey, boolean inclusive) {
        return subMap(Range.downTo(fromKey, BoundType.forBoolean(inclusive)));
    }

    public Comparator<? super Cut<C>> comparator() {
        return Ordering.natural();
    }

    public boolean containsKey(@Nullable Object key) {
        return get(key) != null;
    }

    public Range<C> get(@Nullable Object key) {
        if (key instanceof Cut) {
            try {
                Cut<C> cut = (Cut) key;
                if (!this.upperBoundWindow.contains(cut)) {
                    return null;
                }
                Entry<Cut<C>, Range<C>> candidate = this.rangesByLowerBound.lowerEntry(cut);
                if (candidate != null && ((Range) candidate.getValue()).upperBound.equals(cut)) {
                    return (Range) candidate.getValue();
                }
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    Iterator<Entry<Cut<C>, Range<C>>> entryIterator() {
        Iterator<Range<C>> backingItr;
        if (this.upperBoundWindow.hasLowerBound()) {
            Entry<Cut<C>, Range<C>> lowerEntry = this.rangesByLowerBound.lowerEntry((Cut) this.upperBoundWindow.lowerEndpoint());
            if (lowerEntry == null) {
                backingItr = this.rangesByLowerBound.values().iterator();
            } else if (this.upperBoundWindow.lowerBound.isLessThan(((Range) lowerEntry.getValue()).upperBound)) {
                backingItr = this.rangesByLowerBound.tailMap((Cut) lowerEntry.getKey(), true).values().iterator();
            } else {
                backingItr = this.rangesByLowerBound.tailMap((Cut) this.upperBoundWindow.lowerEndpoint(), true).values().iterator();
            }
        } else {
            backingItr = this.rangesByLowerBound.values().iterator();
        }
        return new AbstractIterator<Entry<Cut<C>, Range<C>>>() {
            protected Entry<Cut<C>, Range<C>> computeNext() {
                if (!backingItr.hasNext()) {
                    return (Entry) endOfData();
                }
                Range<C> range = (Range) backingItr.next();
                if (TreeRangeSet$RangesByUpperBound.this.upperBoundWindow.upperBound.isLessThan(range.upperBound)) {
                    return (Entry) endOfData();
                }
                return Maps.immutableEntry(range.upperBound, range);
            }
        };
    }

    Iterator<Entry<Cut<C>, Range<C>>> descendingEntryIterator() {
        Collection<Range<C>> candidates;
        if (this.upperBoundWindow.hasUpperBound()) {
            candidates = this.rangesByLowerBound.headMap((Cut) this.upperBoundWindow.upperEndpoint(), false).descendingMap().values();
        } else {
            candidates = this.rangesByLowerBound.descendingMap().values();
        }
        final PeekingIterator<Range<C>> backingItr = Iterators.peekingIterator(candidates.iterator());
        if (backingItr.hasNext() && this.upperBoundWindow.upperBound.isLessThan(((Range) backingItr.peek()).upperBound)) {
            backingItr.next();
        }
        return new AbstractIterator<Entry<Cut<C>, Range<C>>>() {
            protected Entry<Cut<C>, Range<C>> computeNext() {
                if (!backingItr.hasNext()) {
                    return (Entry) endOfData();
                }
                Entry<Cut<C>, Range<C>> immutableEntry;
                Range<C> range = (Range) backingItr.next();
                if (TreeRangeSet$RangesByUpperBound.this.upperBoundWindow.lowerBound.isLessThan(range.upperBound)) {
                    immutableEntry = Maps.immutableEntry(range.upperBound, range);
                } else {
                    immutableEntry = (Entry) endOfData();
                }
                return immutableEntry;
            }
        };
    }

    public int size() {
        if (this.upperBoundWindow.equals(Range.all())) {
            return this.rangesByLowerBound.size();
        }
        return Iterators.size(entryIterator());
    }

    public boolean isEmpty() {
        if (this.upperBoundWindow.equals(Range.all())) {
            return this.rangesByLowerBound.isEmpty();
        }
        return !entryIterator().hasNext();
    }
}

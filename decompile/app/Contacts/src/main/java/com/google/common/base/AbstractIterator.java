package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;
import java.util.NoSuchElementException;

@GwtCompatible
abstract class AbstractIterator<T> implements Iterator<T> {
    private static final /* synthetic */ int[] -com-google-common-base-AbstractIterator$StateSwitchesValues = null;
    private T next;
    private State state = State.NOT_READY;

    private enum State {
        READY,
        NOT_READY,
        DONE,
        FAILED
    }

    private static /* synthetic */ int[] -getcom-google-common-base-AbstractIterator$StateSwitchesValues() {
        if (-com-google-common-base-AbstractIterator$StateSwitchesValues != null) {
            return -com-google-common-base-AbstractIterator$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.DONE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.FAILED.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.NOT_READY.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.READY.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        -com-google-common-base-AbstractIterator$StateSwitchesValues = iArr;
        return iArr;
    }

    protected abstract T computeNext();

    protected AbstractIterator() {
    }

    protected final T endOfData() {
        this.state = State.DONE;
        return null;
    }

    public final boolean hasNext() {
        Preconditions.checkState(this.state != State.FAILED);
        switch (-getcom-google-common-base-AbstractIterator$StateSwitchesValues()[this.state.ordinal()]) {
            case 1:
                return false;
            case 2:
                return true;
            default:
                return tryToComputeNext();
        }
    }

    private boolean tryToComputeNext() {
        this.state = State.FAILED;
        this.next = computeNext();
        if (this.state == State.DONE) {
            return false;
        }
        this.state = State.READY;
        return true;
    }

    public final T next() {
        if (hasNext()) {
            this.state = State.NOT_READY;
            T result = this.next;
            this.next = null;
            return result;
        }
        throw new NoSuchElementException();
    }

    public final void remove() {
        throw new UnsupportedOperationException();
    }
}

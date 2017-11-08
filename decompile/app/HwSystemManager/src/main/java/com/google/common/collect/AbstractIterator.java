package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.NoSuchElementException;

@GwtCompatible
public abstract class AbstractIterator<T> extends UnmodifiableIterator<T> {
    private static final /* synthetic */ int[] -com-google-common-collect-AbstractIterator$StateSwitchesValues = null;
    private T next;
    private State state = State.NOT_READY;

    private enum State {
        READY,
        NOT_READY,
        DONE,
        FAILED
    }

    private static /* synthetic */ int[] -getcom-google-common-collect-AbstractIterator$StateSwitchesValues() {
        if (-com-google-common-collect-AbstractIterator$StateSwitchesValues != null) {
            return -com-google-common-collect-AbstractIterator$StateSwitchesValues;
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
        -com-google-common-collect-AbstractIterator$StateSwitchesValues = iArr;
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
        switch (-getcom-google-common-collect-AbstractIterator$StateSwitchesValues()[this.state.ordinal()]) {
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
}

package com.huawei.systemmanager.comm.tools;

public abstract class FilterChain<T> {
    private FilterChain<T> mNext;

    public FilterChain(FilterChain<T> next) {
        this.mNext = next;
    }

    public FilterChain<T> addNext(FilterChain<T> next) {
        this.mNext = next;
        return this.mNext;
    }

    public final void prepare() {
        onPrepare();
        if (this.mNext != null) {
            this.mNext.prepare();
        }
    }

    public final void clear() {
        onClear();
        if (this.mNext != null) {
            this.mNext.clear();
        }
    }

    public boolean filter(T t) {
        boolean res = onFilter(t);
        if (!res || this.mNext == null) {
            return res;
        }
        return this.mNext.filter(t);
    }

    protected void onPrepare() {
    }

    protected void onClear() {
    }

    protected boolean onFilter(T t) {
        return true;
    }
}

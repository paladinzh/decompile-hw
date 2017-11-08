package com.android.mms.attachment.datamodel.binding;

import java.util.concurrent.atomic.AtomicLong;

public class Binding<T extends BindableData> extends BindingBase<T> {
    private static AtomicLong sBindingIdx = new AtomicLong(System.currentTimeMillis() * 1000);
    private String mBindingId;
    private T mData;
    private final Object mOwner;
    private boolean mWasBound;

    Binding(Object owner) {
        this.mOwner = owner;
    }

    public T getData() {
        ensureBound();
        return this.mData;
    }

    public boolean isBound() {
        return this.mData != null ? this.mData.isBound(this.mBindingId) : false;
    }

    public void ensureBound() {
        if (!isBound()) {
            throw new IllegalStateException("not bound; wasBound = " + this.mWasBound);
        }
    }

    public void ensureBound(T data) {
        if (!isBound()) {
            throw new IllegalStateException("not bound; wasBound = " + this.mWasBound);
        } else if (data != this.mData) {
            throw new IllegalStateException("not bound to correct data " + data + " vs " + this.mData);
        }
    }

    public String getBindingId() {
        return this.mBindingId;
    }

    public void bind(T data) {
        if (this.mData != null || data.isBound()) {
            throw new IllegalStateException("already bound when binding to " + data);
        }
        this.mBindingId = Long.toHexString(sBindingIdx.getAndIncrement());
        data.bind(this.mBindingId);
        this.mData = data;
        this.mWasBound = true;
    }

    public void unbind() {
        if (this.mData == null || !this.mData.isBound(this.mBindingId)) {
            throw new IllegalStateException("not bound when unbind");
        }
        this.mData.unbind(this.mBindingId);
        this.mData = null;
        this.mBindingId = null;
    }
}

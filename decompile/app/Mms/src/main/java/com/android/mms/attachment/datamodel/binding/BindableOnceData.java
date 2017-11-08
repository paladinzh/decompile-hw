package com.android.mms.attachment.datamodel.binding;

public abstract class BindableOnceData extends BindableData {
    private boolean boundOnce = false;

    public void bind(String bindingId) {
        if (this.boundOnce) {
            throw new IllegalStateException();
        }
        super.bind(bindingId);
        this.boundOnce = true;
    }

    public boolean isBound() {
        return super.isBound();
    }
}

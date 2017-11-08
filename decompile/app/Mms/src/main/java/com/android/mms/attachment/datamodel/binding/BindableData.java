package com.android.mms.attachment.datamodel.binding;

public abstract class BindableData {
    private String mBindingId;

    protected abstract void unregisterListeners();

    public void bind(String bindingId) {
        if (isBound() || bindingId == null) {
            throw new IllegalStateException();
        }
        this.mBindingId = bindingId;
    }

    public void unbind(String bindingId) {
        if (isBound(bindingId)) {
            unregisterListeners();
            this.mBindingId = null;
            return;
        }
        throw new IllegalStateException();
    }

    protected boolean isBound() {
        return this.mBindingId != null;
    }

    public boolean isBound(String bindingId) {
        return bindingId.equals(this.mBindingId);
    }
}

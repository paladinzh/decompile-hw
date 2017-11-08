package com.android.mms.attachment.datamodel.binding;

public class ImmutableBindingRef<T extends BindableData> extends BindingBase<T> {
    private final BindingBase<T> mBinding;

    ImmutableBindingRef(BindingBase<T> binding) {
        this.mBinding = resolveBinding(binding);
    }

    public T getData() {
        return this.mBinding.getData();
    }

    public void ensureBound(T data) {
        this.mBinding.ensureBound(data);
    }

    public String getBindingId() {
        return this.mBinding.getBindingId();
    }

    private BindingBase<T> resolveBinding(BindingBase<T> binding) {
        BindingBase<T> resolvedBinding = binding;
        while (resolvedBinding instanceof ImmutableBindingRef) {
            resolvedBinding = ((ImmutableBindingRef) resolvedBinding).mBinding;
        }
        return resolvedBinding;
    }
}

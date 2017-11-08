package com.android.mms.attachment.datamodel.binding;

public abstract class BindingBase<T extends BindableData> {
    public abstract void ensureBound(T t);

    public abstract String getBindingId();

    public abstract T getData();

    public static <T extends BindableData> Binding<T> createBinding(Object owner) {
        return new Binding(owner);
    }

    public static <T extends BindableData> ImmutableBindingRef<T> createBindingReference(BindingBase<T> srcBinding) {
        return new ImmutableBindingRef(srcBinding);
    }
}

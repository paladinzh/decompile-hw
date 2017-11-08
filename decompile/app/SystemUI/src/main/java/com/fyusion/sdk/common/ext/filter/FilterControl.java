package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;

/* compiled from: Unknown */
public abstract class FilterControl implements Cloneable {
    private String a;
    private float b = a();

    public FilterControl(String str) {
        this.a = str;
    }

    abstract float a();

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public a createFilter(float f) {
        ReflectiveOperationException reflectiveOperationException;
        setValue(f);
        a aVar = null;
        try {
            a aVar2 = (a) getImplementationClass().newInstance();
            try {
                aVar2.a(this);
                return aVar2;
            } catch (ReflectiveOperationException e) {
                ReflectiveOperationException reflectiveOperationException2 = e;
                aVar = aVar2;
                reflectiveOperationException = reflectiveOperationException2;
            }
        } catch (InstantiationException e2) {
            reflectiveOperationException = e2;
            reflectiveOperationException.printStackTrace();
            return aVar;
        }
    }

    public abstract Class<? extends a> getImplementationClass();

    public String getName() {
        return this.a;
    }

    public float getValue() {
        return this.b;
    }

    public void setValue(float f) {
        this.b = f;
    }
}

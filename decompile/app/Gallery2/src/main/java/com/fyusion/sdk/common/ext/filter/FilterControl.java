package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public abstract class FilterControl implements Cloneable {
    private String a;
    private float b = a();

    public FilterControl(String str) {
        this.a = str;
    }

    private float b(float f) {
        return (f - b()) / (c() - b());
    }

    abstract float a();

    protected float a(float f) {
        if (f >= 0.0f || f <= WMElement.CAMERASIZEVALUE1B1) {
            return ((c() - b()) * f) + b();
        }
        throw new IllegalArgumentException("Value must be between 0.0 and 1.0");
    }

    public void adjustTo(float f) {
        this.b = a(f);
    }

    abstract float b();

    abstract float c();

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

    public float getNormalizedValue() {
        return b(this.b);
    }

    public float getValue() {
        return this.b;
    }

    public void setToDefault() {
        this.b = a();
    }

    public void setValue(float f) {
        this.b = f;
    }
}

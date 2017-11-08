package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.p;

/* compiled from: Unknown */
public interface BlockFilter extends PerPixelFilter {
    int getHeight();

    int getWidth();

    void setImageSize(int i, int i2);

    void setTextureContainer(p pVar);
}

package com.android.gallery3d.ui;

import android.graphics.RectF;
import javax.microedition.khronos.opengles.GL11;

public interface GLCanvas {
    void beginRenderTarget(RawTexture rawTexture);

    void clearBuffer();

    void clearBuffer(float[] fArr);

    void deleteBuffer(int i);

    void deleteRecycledResources();

    void drawLine(float f, float f2, float f3, float f4, GLPaint gLPaint);

    void drawMesh(BasicTexture basicTexture, int i, int i2, int i3, int i4, int i5, int i6);

    void drawMixed(BasicTexture basicTexture, int i, float f, int i2, int i3, int i4, int i5);

    void drawRect(float f, float f2, float f3, float f4, GLPaint gLPaint);

    void drawTexture(BasicTexture basicTexture, int i, int i2, int i3, int i4);

    void drawTexture(BasicTexture basicTexture, RectF rectF, RectF rectF2);

    void drawTexture(BasicTexture basicTexture, float[] fArr, int i, int i2, int i3, int i4);

    void endRenderTarget();

    void fillRect(float f, float f2, float f3, float f4, int i);

    float getAlpha();

    float[] getCropRect();

    int getCustomFlag();

    GL11 getGLInstance();

    int[] getTextureId();

    void invalidateAllTextures();

    void multiplyAlpha(float f);

    void multiplyMatrix(float[] fArr, int i);

    void putTexture(BasicTexture basicTexture);

    void restore();

    void rotate(float f, float f2, float f3, float f4);

    void save();

    void save(int i);

    void scale(float f, float f2, float f3);

    void setAlpha(float f);

    void setCustomFlag(int i);

    void setSize(int i, int i2);

    void translate(float f, float f2);

    void translate(float f, float f2, float f3);

    boolean unloadTexture(BasicTexture basicTexture);

    void yieldAllTextures();
}

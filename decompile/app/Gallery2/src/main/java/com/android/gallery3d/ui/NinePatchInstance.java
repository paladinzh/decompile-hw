package com.android.gallery3d.ui;

import com.android.gallery3d.common.Utils;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL11;

/* compiled from: NinePatchTexture */
class NinePatchInstance {
    private int[] mBufferNames;
    private int mIdxCount;
    private ByteBuffer mIndexBuffer;
    private FloatBuffer mUvBuffer;
    private FloatBuffer mXyBuffer;

    public NinePatchInstance(NinePatchTexture tex, int width, int height) {
        NinePatchChunk chunk = tex.getNinePatchChunk();
        if (width <= 0 || height <= 0) {
            throw new RuntimeException("invalid dimension");
        } else if (chunk.mDivX.length == 2 && chunk.mDivY.length == 2) {
            float[] divX = new float[4];
            float[] divY = new float[4];
            float[] divU = new float[4];
            float[] divV = new float[4];
            prepareVertexData(divX, divY, divU, divV, stretch(divX, divU, chunk.mDivX, tex.getWidth(), width), stretch(divY, divV, chunk.mDivY, tex.getHeight(), height), chunk.mColor);
        } else {
            throw new RuntimeException("unsupported nine patch");
        }
    }

    private static int stretch(float[] x, float[] u, int[] div, int source, int target) {
        int i;
        int textureSize = Utils.nextPowerOf2(source);
        float textureBound = ((float) source) / ((float) textureSize);
        float stretch = 0.0f;
        for (i = 0; i < div.length; i += 2) {
            stretch += (float) (div[i + 1] - div[i]);
        }
        float remaining = ((float) (target - source)) + stretch;
        float lastX = 0.0f;
        float lastU = 0.0f;
        x[0] = 0.0f;
        u[0] = 0.0f;
        int n = div.length;
        for (i = 0; i < n; i += 2) {
            x[i + 1] = ((((float) div[i]) - lastU) + lastX) + 0.5f;
            u[i + 1] = Math.min((((float) div[i]) + 0.5f) / ((float) textureSize), textureBound);
            float partU = (float) (div[i + 1] - div[i]);
            float partX = (remaining * partU) / stretch;
            remaining -= partX;
            stretch -= partU;
            lastX = x[i + 1] + partX;
            lastU = (float) div[i + 1];
            x[i + 2] = lastX - 0.5f;
            u[i + 2] = Math.min((lastU - 0.5f) / ((float) textureSize), textureBound);
        }
        x[div.length + 1] = (float) target;
        u[div.length + 1] = textureBound;
        int last = 0;
        n = div.length + 2;
        for (i = 1; i < n; i++) {
            if (x[i] - x[last] >= WMElement.CAMERASIZEVALUE1B1) {
                last++;
                x[last] = x[i];
                u[last] = u[i];
            }
        }
        return last + 1;
    }

    private void prepareVertexData(float[] x, float[] y, float[] u, float[] v, int nx, int ny, int[] color) {
        int pntCount = 0;
        float[] xy = new float[32];
        float[] uv = new float[32];
        int j = 0;
        while (j < ny) {
            int i = 0;
            int pntCount2 = pntCount;
            while (i < nx) {
                pntCount = pntCount2 + 1;
                int xIndex = pntCount2 << 1;
                int yIndex = xIndex + 1;
                xy[xIndex] = x[i];
                xy[yIndex] = y[j];
                uv[xIndex] = u[i];
                uv[yIndex] = v[j];
                i++;
                pntCount2 = pntCount;
            }
            j++;
            pntCount = pntCount2;
        }
        int idxCount = 1;
        boolean isForward = false;
        byte[] index = new byte[24];
        for (int row = 0; row < ny - 1; row++) {
            int start;
            int end;
            int inc;
            idxCount--;
            isForward = !isForward;
            if (isForward) {
                start = 0;
                end = nx;
                inc = 1;
            } else {
                start = nx - 1;
                end = -1;
                inc = -1;
            }
            for (int col = start; col != end; col += inc) {
                int idxCount2;
                int k = (row * nx) + col;
                if (col != start) {
                    int colorIdx = ((nx - 1) * row) + col;
                    if (isForward) {
                        colorIdx--;
                    }
                    if (color[colorIdx] == 0) {
                        index[idxCount] = index[idxCount - 1];
                        idxCount++;
                        idxCount2 = idxCount + 1;
                        index[idxCount] = (byte) k;
                        idxCount = idxCount2;
                    }
                }
                idxCount2 = idxCount + 1;
                index[idxCount] = (byte) k;
                idxCount = idxCount2 + 1;
                index[idxCount2] = (byte) (k + nx);
            }
        }
        this.mIdxCount = idxCount;
        int size = (pntCount * 2) * 4;
        this.mXyBuffer = allocateDirectNativeOrderBuffer(size).asFloatBuffer();
        this.mUvBuffer = allocateDirectNativeOrderBuffer(size).asFloatBuffer();
        this.mIndexBuffer = allocateDirectNativeOrderBuffer(this.mIdxCount);
        this.mXyBuffer.put(xy, 0, pntCount * 2).position(0);
        this.mUvBuffer.put(uv, 0, pntCount * 2).position(0);
        this.mIndexBuffer.put(index, 0, idxCount).position(0);
    }

    private static ByteBuffer allocateDirectNativeOrderBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    private void prepareBuffers(GLCanvas canvas) {
        this.mBufferNames = new int[3];
        GL11 gl = canvas.getGLInstance();
        GLId.glGenBuffers(3, this.mBufferNames, 0);
        gl.glBindBuffer(34962, this.mBufferNames[0]);
        gl.glBufferData(34962, this.mXyBuffer.capacity() * 4, this.mXyBuffer, 35044);
        gl.glBindBuffer(34962, this.mBufferNames[1]);
        gl.glBufferData(34962, this.mUvBuffer.capacity() * 4, this.mUvBuffer, 35044);
        gl.glBindBuffer(34963, this.mBufferNames[2]);
        gl.glBufferData(34963, this.mIndexBuffer.capacity(), this.mIndexBuffer, 35044);
        this.mXyBuffer = null;
        this.mUvBuffer = null;
        this.mIndexBuffer = null;
    }

    public void draw(GLCanvas canvas, NinePatchTexture tex, int x, int y) {
        if (this.mBufferNames == null) {
            prepareBuffers(canvas);
        }
        canvas.drawMesh(tex, x, y, this.mBufferNames[0], this.mBufferNames[1], this.mBufferNames[2], this.mIdxCount);
    }

    public void recycle(GLCanvas canvas) {
        if (this.mBufferNames != null) {
            canvas.deleteBuffer(this.mBufferNames[0]);
            canvas.deleteBuffer(this.mBufferNames[1]);
            canvas.deleteBuffer(this.mBufferNames[2]);
            this.mBufferNames = null;
        }
    }
}

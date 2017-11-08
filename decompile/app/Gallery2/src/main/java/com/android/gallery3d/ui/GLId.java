package com.android.gallery3d.ui;

import javax.microedition.khronos.opengles.GL11;

public class GLId {
    static int sNextId = 1;

    public static synchronized void glGenTextures(int n, int[] textures, int offset) {
        synchronized (GLId.class) {
            while (true) {
                int n2 = n - 1;
                if (n > 0) {
                    int i = offset + n2;
                    int i2 = sNextId;
                    sNextId = i2 + 1;
                    textures[i] = i2;
                    n = n2;
                }
            }
        }
    }

    public static synchronized void glGenBuffers(int n, int[] buffers, int offset) {
        synchronized (GLId.class) {
            while (true) {
                int n2 = n - 1;
                if (n > 0) {
                    int i = offset + n2;
                    int i2 = sNextId;
                    sNextId = i2 + 1;
                    buffers[i] = i2;
                    n = n2;
                }
            }
        }
    }

    public static synchronized void glDeleteTextures(GL11 gl, int n, int[] textures, int offset) {
        synchronized (GLId.class) {
            gl.glDeleteTextures(n, textures, offset);
        }
    }

    public static synchronized void glDeleteBuffers(GL11 gl, int n, int[] buffers, int offset) {
        synchronized (GLId.class) {
            gl.glDeleteBuffers(n, buffers, offset);
        }
    }
}

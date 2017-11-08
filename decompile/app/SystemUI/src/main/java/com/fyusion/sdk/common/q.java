package com.fyusion.sdk.common;

import android.opengl.GLES20;
import android.util.Log;
import fyusion.vislib.BuildConfig;

/* compiled from: Unknown */
public class q {
    public static int a(String str, String str2) {
        int glCreateProgram = GLES20.glCreateProgram();
        if (glCreateProgram > 0) {
            int b = b(35633, str);
            int b2 = b(35632, str2);
            GLES20.glAttachShader(glCreateProgram, b);
            a("glAttachShader");
            GLES20.glAttachShader(glCreateProgram, b2);
            a("glAttachShader");
            GLES20.glLinkProgram(glCreateProgram);
            try {
                b(glCreateProgram);
                return glCreateProgram;
            } finally {
                GLES20.glDetachShader(glCreateProgram, b);
                GLES20.glDetachShader(glCreateProgram, b2);
                GLES20.glDeleteShader(b);
                GLES20.glDeleteShader(b2);
            }
        } else {
            throw new RuntimeException("Could not create the program");
        }
    }

    private static void a(int i) {
        int[] iArr = new int[1];
        GLES20.glGetShaderiv(i, 35713, iArr, 0);
        if (iArr[0] != 1) {
            String str = "Could not compile the shader:\n" + GLES20.glGetShaderInfoLog(i);
            GLES20.glDeleteShader(i);
            throw new RuntimeException(str);
        }
    }

    public static void a(int i, String str) {
        if (i < 0) {
            throw new RuntimeException("Unable to locate '" + str + "'");
        }
    }

    public static void a(String str) {
        while (true) {
            int glGetError = GLES20.glGetError();
            if (glGetError != 0) {
                Log.e("OpenGLUtils", str + ": glError " + glGetError);
            } else {
                return;
            }
        }
    }

    public static boolean a() {
        String str = BuildConfig.FLAVOR;
        while (GLES20.glGetError() != 0) {
            str = str + String.format(" 0x%4s", new Object[]{Integer.toHexString(GLES20.glGetError())}).replace(' ', '0');
        }
        return str.isEmpty();
    }

    private static int b(int i, String str) {
        int glCreateShader = GLES20.glCreateShader(i);
        a("glCreateShader type=" + i);
        GLES20.glShaderSource(glCreateShader, str);
        a("glShaderSource");
        GLES20.glCompileShader(glCreateShader);
        a("glCompileShader");
        a(glCreateShader);
        return glCreateShader;
    }

    public static void b() {
    }

    private static void b(int i) {
        int[] iArr = new int[1];
        GLES20.glGetProgramiv(i, 35714, iArr, 0);
        if (iArr[0] != 1) {
            String str = "Could not link the shader program:\n" + GLES20.glGetProgramInfoLog(i);
            GLES20.glDeleteProgram(i);
            throw new RuntimeException(str);
        }
    }
}

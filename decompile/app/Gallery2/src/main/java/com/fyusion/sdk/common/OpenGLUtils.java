package com.fyusion.sdk.common;

import android.opengl.GLES20;
import android.util.Log;

/* compiled from: Unknown */
public class OpenGLUtils {
    private static int a(int i, String str) {
        int glCreateShader = GLES20.glCreateShader(i);
        checkGlError("glCreateShader type=" + i);
        GLES20.glShaderSource(glCreateShader, str);
        checkGlError("glShaderSource");
        GLES20.glCompileShader(glCreateShader);
        checkGlError("glCompileShader");
        a(glCreateShader);
        return glCreateShader;
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

    private static void b(int i) {
        int[] iArr = new int[1];
        GLES20.glGetProgramiv(i, 35714, iArr, 0);
        if (iArr[0] != 1) {
            String str = "Could not link the shader program:\n" + GLES20.glGetProgramInfoLog(i);
            GLES20.glDeleteProgram(i);
            throw new RuntimeException(str);
        }
    }

    public static boolean checkErrors() {
        String str = "";
        while (GLES20.glGetError() != 0) {
            str = str + String.format(" 0x%4s", new Object[]{Integer.toHexString(GLES20.glGetError())}).replace(' ', '0');
        }
        return str.isEmpty();
    }

    public static void checkFramebufferStatus() {
    }

    public static void checkGlError(String str) {
        while (true) {
            int glGetError = GLES20.glGetError();
            if (glGetError != 0) {
                Log.e("OpenGLUtils", str + ": glError " + glGetError);
            } else {
                return;
            }
        }
    }

    public static void checkLocation(int i, String str) {
        if (i < 0) {
            throw new RuntimeException("Unable to locate '" + str + "'");
        }
    }

    public static int createProgram(String str, String str2) {
        int glCreateProgram = GLES20.glCreateProgram();
        if (glCreateProgram > 0) {
            int a = a(35633, str);
            int a2 = a(35632, str2);
            GLES20.glAttachShader(glCreateProgram, a);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(glCreateProgram, a2);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(glCreateProgram);
            try {
                b(glCreateProgram);
                return glCreateProgram;
            } finally {
                GLES20.glDetachShader(glCreateProgram, a);
                GLES20.glDetachShader(glCreateProgram, a2);
                GLES20.glDeleteShader(a);
                GLES20.glDeleteShader(a2);
            }
        } else {
            throw new RuntimeException("Could not create the program");
        }
    }
}

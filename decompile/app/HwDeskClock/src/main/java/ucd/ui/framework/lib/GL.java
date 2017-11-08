package ucd.ui.framework.lib;

import android.opengl.GLES20;
import java.nio.Buffer;
import java.nio.IntBuffer;

public class GL {
    public static void glActiveTexture(int target) {
        GLES20.glActiveTexture(target);
    }

    public static void glAttachShader(int program, int shader) {
        GLES20.glAttachShader(program, shader);
    }

    public static void glBindBuffer(int target, int bufferId) {
        GLES20.glBindBuffer(target, bufferId);
    }

    public static void glBindFramebuffer(int target, int framebufId) {
        GLES20.glBindFramebuffer(target, framebufId);
    }

    public static void glBindTexture(int target, int texId) {
        GLES20.glBindTexture(target, texId);
    }

    public static void glBlendFunc(int sfactor, int dfactor) {
        GLES20.glBlendFunc(sfactor, dfactor);
    }

    public static void glBlendFuncSeparate(int sRGB, int dRGB, int sA, int dA) {
        GLES20.glBlendFuncSeparate(sRGB, dRGB, sA, dA);
    }

    public static void glBufferData(int target, int size, Buffer data, int usage) {
        GLES20.glBufferData(target, size, data, usage);
    }

    public static void glClear(int mask) {
        GLES20.glClear(mask);
    }

    public static void glClearColor(float r, float g, float b, float a) {
        GLES20.glClearColor(r, g, b, a);
    }

    public static void glCompileShader(int shader) {
        GLES20.glCompileShader(shader);
    }

    public static int glCreateProgram() {
        return GLES20.glCreateProgram();
    }

    public static int glCreateShader(int shaderType) {
        return GLES20.glCreateShader(shaderType);
    }

    public static void glDeleteProgram(int program) {
        GLES20.glDeleteProgram(program);
    }

    public static void glDeleteShader(int shader) {
        GLES20.glDeleteShader(shader);
    }

    public static void glDeleteTextures(int n, int[] textures, int offset) {
        GLES20.glDeleteTextures(n, textures, offset);
    }

    public static void glDepthFunc(int func) {
        GLES20.glDepthFunc(func);
    }

    public static void glDrawArrays(int mode, int first, int count) {
        GLES20.glDrawArrays(mode, first, count);
    }

    public static void glEnable(int cap) {
        GLES20.glEnable(cap);
    }

    public static void glEnableVertexAttribArray(int location) {
        GLES20.glEnableVertexAttribArray(location);
    }

    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        GLES20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public static void glFrontFace(int mode) {
        GLES20.glFrontFace(mode);
    }

    public static void glGenBuffers(int n, int[] buffers, int offset) {
        GLES20.glGenBuffers(n, buffers, offset);
    }

    public static void glGenFramebuffers(int n, int[] frameb, int offset) {
        GLES20.glGenFramebuffers(n, frameb, offset);
    }

    public static void glGenRenderbuffers(int n, int[] depthRb, int offset) {
        GLES20.glGenRenderbuffers(n, depthRb, offset);
    }

    public static void glGenTextures(int n, int[] textures, int offset) {
        GLES20.glGenTextures(n, textures, offset);
    }

    public static int glGetAttribLocation(int program, String name) {
        return GLES20.glGetAttribLocation(program, name);
    }

    public static int glGetError() {
        return GLES20.glGetError();
    }

    public static String glGetProgramInfoLog(int program) {
        return GLES20.glGetProgramInfoLog(program);
    }

    public static void glGetProgramiv(int program, int status, int[] linkStatus, int i) {
        GLES20.glGetProgramiv(program, status, linkStatus, i);
    }

    public static String glGetShaderInfoLog(int shader) {
        return GLES20.glGetShaderInfoLog(shader);
    }

    public static void glGetShaderiv(int shader, int glCompileStatus, int[] compiled, int i) {
        GLES20.glGetShaderiv(shader, glCompileStatus, compiled, i);
    }

    public static int glGetUniformLocation(int program, String name) {
        return GLES20.glGetUniformLocation(program, name);
    }

    public static void glLinkProgram(int program) {
        GLES20.glLinkProgram(program);
    }

    public static void glShaderSource(int shader, String source) {
        GLES20.glShaderSource(shader, source);
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, IntBuffer data) {
        GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, data);
    }

    public static void glTexParameterf(int target, int pname, int param) {
        GLES20.glTexParameterf(target, pname, (float) param);
    }

    public static void glTexParameteri(int target, int pname, int param) {
        GLES20.glTexParameteri(target, pname, param);
    }

    public static void glUniform1f(int location, float v) {
        GLES20.glUniform1f(location, v);
    }

    public static void glUniform1i(int location, int v) {
        GLES20.glUniform1i(location, v);
    }

    public static void glUniform3f(int location, float v1, float v2, float v3) {
        GLES20.glUniform3f(location, v1, v2, v3);
    }

    public static void glUniform4f(int location, float v1, float v2, float v3, float v4) {
        GLES20.glUniform4f(location, v1, v2, v3, v4);
    }

    public static void glUniform4fv(int location, int count, float[] value, int offset) {
        GLES20.glUniform4fv(location, count, value, offset);
    }

    public static void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {
        GLES20.glUniformMatrix4fv(location, count, transpose, value, offset);
    }

    public static void glUseProgram(int prog) {
        GLES20.glUseProgram(prog);
    }

    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int pointer) {
        GLES20.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public static void glViewport(int left, int top, int width, int height) {
        GLES20.glViewport(left, top, width, height);
    }
}

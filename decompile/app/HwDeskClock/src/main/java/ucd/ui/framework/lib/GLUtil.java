package ucd.ui.framework.lib;

import android.content.res.Resources;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GLUtil {
    protected static final String TAG = GLUtil.class.getSimpleName();

    public static boolean checkGlError(String op) {
        return checkGlError(null, op);
    }

    public static boolean checkGlError(String tag, String op) {
        if (GL.glGetError() == 0) {
            return true;
        }
        return false;
    }

    public static int loadShader(int shaderType, String source) {
        int shader = GL.glCreateShader(shaderType);
        if (shader == 0) {
            return shader;
        }
        GL.glShaderSource(shader, source);
        GL.glCompileShader(shader);
        int[] compiled = new int[1];
        GL.glGetShaderiv(shader, 35713, compiled, 0);
        if (compiled[0] != 0) {
            return shader;
        }
        Log.e("ES20_ERROR", "Could not compile shader " + shaderType + ":");
        Log.e("ES20_ERROR", GL.glGetShaderInfoLog(shader));
        GL.glDeleteShader(shader);
        return 0;
    }

    public static int initShader(String vertex, String frag) {
        return createProgram(vertex, frag);
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(35633, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(35632, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        int program = GL.glCreateProgram();
        if (program != 0) {
            GL.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GL.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GL.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GL.glGetProgramiv(program, 35714, linkStatus, 0);
            if (linkStatus[0] != 1) {
                Log.e("ES20_ERROR", "Could not link program: ");
                Log.e("ES20_ERROR", GL.glGetProgramInfoLog(program));
                GL.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static String loadFromAssetsFile(String fname, Resources r) {
        IOException e;
        String result = null;
        try {
            InputStream in = r.getAssets().open(fname);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (true) {
                int ch = in.read();
                if (ch == -1) {
                    break;
                }
                baos.write(ch);
            }
            byte[] buff = baos.toByteArray();
            baos.close();
            in.close();
            String result2 = new String(buff, "UTF-8");
            try {
                result = result2.replaceAll("\\r\\n", "\n");
            } catch (IOException e2) {
                e = e2;
                result = result2;
                e.printStackTrace();
                return result;
            }
        } catch (IOException e3) {
            e = e3;
            e.printStackTrace();
            return result;
        }
        return result;
    }
}

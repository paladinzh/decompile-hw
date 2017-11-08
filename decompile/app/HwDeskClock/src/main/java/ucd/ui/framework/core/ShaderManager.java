package ucd.ui.framework.core;

import android.content.Context;
import java.util.HashMap;
import ucd.ui.framework.lib.GL;
import ucd.ui.framework.lib.GLUtil;

public class ShaderManager {
    private Context context;
    private int defaultShader = 0;
    private HashMap<Class<?>, Integer> list = new HashMap();
    protected final int[] sAttrs = new int[5];

    public ShaderManager(Context c) {
        this.context = c;
    }

    public int getDefaultShader() {
        if (this.defaultShader == 0) {
            try {
                this.defaultShader = GLUtil.createProgram(GLUtil.loadFromAssetsFile("shader/mix.vertex.glsl", this.context.getResources()), GLUtil.loadFromAssetsFile("shader/globject.radius.frag.glsl", this.context.getResources()));
                this.sAttrs[0] = GL.glGetAttribLocation(this.defaultShader, "aVertexPosition");
                this.sAttrs[1] = GL.glGetUniformLocation(this.defaultShader, "MVPMatrix");
                this.sAttrs[2] = GL.glGetUniformLocation(this.defaultShader, "agRect");
                this.sAttrs[3] = GL.glGetUniformLocation(this.defaultShader, "paintColor");
                this.sAttrs[4] = GL.glGetUniformLocation(this.defaultShader, "attrs");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.defaultShader;
    }

    public int getShader(Class<?> key, String vertexFile, String fragFile) {
        Integer vProg = (Integer) this.list.get(key);
        if (vProg == null || vProg.intValue() == 0) {
            try {
                vProg = Integer.valueOf(GLUtil.initShader(GLUtil.loadFromAssetsFile(vertexFile, this.context.getResources()), GLUtil.loadFromAssetsFile(fragFile, this.context.getResources())));
                this.list.put(key, vProg);
            } catch (Exception e) {
                e.printStackTrace();
                vProg = Integer.valueOf(0);
            }
        }
        return vProg.intValue();
    }

    public int getShader(Class<?> key, String fragFile) {
        return getShader(key, "shader/mix.vertex.glsl", fragFile);
    }
}

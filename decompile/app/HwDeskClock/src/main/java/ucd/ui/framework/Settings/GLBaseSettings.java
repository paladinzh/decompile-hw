package ucd.ui.framework.Settings;

import android.content.Context;
import ucd.ui.framework.core.BlendManager;
import ucd.ui.framework.core.FBO;
import ucd.ui.framework.core.ShaderManager;
import ucd.ui.framework.core.Spirit;
import ucd.ui.framework.core.TextureManager;
import ucd.ui.framework.lib.GL;
import ucd.ui.framework.lib.Model;
import ucd.ui.framework.lib.ProjectorMatrix;

public class GLBaseSettings {
    public final Config config = new Config();
    public final Env env = new Env();
    public int height;
    public final ProjectorMatrix pm = new ProjectorMatrix();
    public int width;

    public static class Config {
        public float baseZ;
        public float cameraZ = 1500.0f;
        public float far = 2000.0f;
        public float farZ;
        public float near = 1000.0f;
        public float nearZ;
        public float scale;
    }

    public static class Env {
        private static final float[] defaultTexCoords = Model.planeTModel(0.0f, 0.0f, 1.0f, 1.0f);
        protected static final float[] fboTexCoords = Model.planeTModel_un(0.0f, 0.0f, 1.0f, 1.0f);
        public final BlendManager blendManager = new BlendManager();
        public int defaultTexBufferPos = 0;
        public final FBO fbo = new FBO();
        public int fboTexBufferPos = 0;
        public ShaderManager shaderManager;
        public final TextureManager texManager = new TextureManager();

        public static float[] getDefaultTexCoords() {
            float[] copy = new float[defaultTexCoords.length];
            System.arraycopy(defaultTexCoords, 0, copy, 0, defaultTexCoords.length);
            return copy;
        }

        protected void initDefaultTextures() {
            if (this.fboTexBufferPos == 0) {
                int[] buffers = new int[1];
                GL.glGenBuffers(1, buffers, 0);
                this.fboTexBufferPos = buffers[0];
                Spirit.setData2GLBuffer(this.fboTexBufferPos, fboTexCoords);
            }
            if (this.defaultTexBufferPos == 0) {
                buffers = new int[1];
                GL.glGenBuffers(1, buffers, 0);
                this.defaultTexBufferPos = buffers[0];
                Spirit.setData2GLBuffer(this.defaultTexBufferPos, defaultTexCoords);
            }
        }

        protected void initShaderManager(Context c) {
            this.shaderManager = new ShaderManager(c);
            this.shaderManager.getDefaultShader();
        }
    }

    public void initGLEnv(Context c) {
        this.env.initShaderManager(c);
        this.env.initDefaultTextures();
        this.env.fbo.init();
        this.env.blendManager.setDefault();
    }
}

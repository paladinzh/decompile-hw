package ucd.ui.framework.lib;

import android.opengl.Matrix;

public class ProjectorMatrix {
    private static int level = -1;
    private static float[] mStack = new float[160];
    public float[] cameraPos = new float[3];
    private float[] mMVPMatrix = new float[16];
    public float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] normalMatrix = new float[16];

    public void setCamera(float cx, float cy, float cz, float tz, float upx, float upy, float upz) {
        this.cameraPos[0] = cx;
        this.cameraPos[1] = cy;
        this.cameraPos[2] = cz;
        Matrix.setLookAtM(this.mVMatrix, 0, cx, cy, cz, cx, cy, tz, upx, upy, upz);
    }

    public void setProjectFrustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(this.mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public float[] getFinalMatrix(float[] objMatrix) {
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.mVMatrix, 0, objMatrix, 0);
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.mProjMatrix, 0, this.mMVPMatrix, 0);
        float[] copy = new float[16];
        System.arraycopy(this.mMVPMatrix, 0, copy, 0, this.mMVPMatrix.length);
        return copy;
    }
}

package ucd.ui.framework.lib;

import android.opengl.Matrix;

public class ModelMatrix {
    private float[] currMatrix = new float[16];
    private int level = -1;
    private float[] mStack = new float[160];

    public void pushMatrix() {
        this.level++;
        System.arraycopy(this.currMatrix, 0, this.mStack, this.level * this.currMatrix.length, this.currMatrix.length);
    }

    public void popMatrix() {
        System.arraycopy(this.mStack, this.level * this.currMatrix.length, this.currMatrix, 0, this.currMatrix.length);
        this.level--;
    }

    public void setInitStack() {
        if (this.currMatrix == null) {
            this.currMatrix = new float[16];
        }
        Matrix.setIdentityM(this.currMatrix, 0);
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(this.currMatrix, 0, x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(this.currMatrix, 0, angle, x, y, z);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(this.currMatrix, 0, x, y, z);
    }

    public float[] getMMatrix() {
        float[] copy = new float[16];
        System.arraycopy(this.currMatrix, 0, copy, 0, this.currMatrix.length);
        return copy;
    }
}

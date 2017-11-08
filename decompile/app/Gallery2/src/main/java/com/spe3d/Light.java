package com.spe3d;

public class Light {
    public float lightAmbient;
    public float lightDiffuse;
    public float[] lightPos;
    public float lightSpecular;

    public Light() {
        this.lightDiffuse = 0.8f;
        this.lightAmbient = 0.5f;
        this.lightSpecular = 0.2f;
        this.lightPos = new float[]{0.0f, 8.0f, 10.0f};
    }

    public Light(Light light) {
        this.lightDiffuse = light.lightDiffuse;
        this.lightAmbient = light.lightAmbient;
        this.lightSpecular = light.lightSpecular;
        this.lightPos = (float[]) light.lightPos.clone();
    }
}

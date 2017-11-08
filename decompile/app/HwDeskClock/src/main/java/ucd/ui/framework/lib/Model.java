package ucd.ui.framework.lib;

import ucd.ui.framework.lib.models.PlaneModel_Triangle_strip;

public class Model {

    public static class Point2D {
        public static void copyTo(float[] tmp, int index, float x, float y) {
            index *= 2;
            tmp[index + 0] = x;
            tmp[index + 1] = y;
        }
    }

    public static class Point3D {
        public static void copyTo(float[] tmp, int index, float x, float y, float z) {
            index *= 3;
            tmp[index + 0] = x;
            tmp[index + 1] = y;
            tmp[index + 2] = z;
        }
    }

    public static float[] planeModel(float w, float h, float z, float[] tmp) {
        return PlaneModel_Triangle_strip.planeModel(w, h, z, tmp);
    }

    public static float[] planeTModel(float l, float t, float w, float h) {
        return PlaneModel_Triangle_strip.planeTModel(l, t, w, h);
    }

    public static float[] planeTModel_un(float l, float t, float w, float h) {
        return planeTModel_un(l, t, w, h, null);
    }

    public static float[] planeTModel_un(float l, float t, float w, float h, float[] src) {
        return PlaneModel_Triangle_strip.planeTModel_un(t, l, w, h, src);
    }
}

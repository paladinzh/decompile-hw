package ucd.ui.framework.lib.models;

import ucd.ui.framework.lib.Model.Point2D;
import ucd.ui.framework.lib.Model.Point3D;

public class PlaneModel_Triangle_strip {
    public static float[] planeModel(float w, float h, float z, float[] tmp) {
        if (tmp == null || tmp.length != 12) {
            tmp = new float[12];
        }
        Point3D.copyTo(tmp, 0, (-w) / 2.0f, (-h) / 2.0f, z);
        Point3D.copyTo(tmp, 1, (-w) / 2.0f, h / 2.0f, z);
        Point3D.copyTo(tmp, 2, w / 2.0f, (-h) / 2.0f, z);
        Point3D.copyTo(tmp, 3, w / 2.0f, h / 2.0f, z);
        return tmp;
    }

    public static float[] planeTModel(float l, float t, float w, float h) {
        float[] tmp = new float[8];
        Point2D.copyTo(tmp, 0, l, t);
        Point2D.copyTo(tmp, 1, l, t + h);
        Point2D.copyTo(tmp, 2, l + w, t);
        Point2D.copyTo(tmp, 3, l + w, t + h);
        return tmp;
    }

    public static float[] planeTModel_un(float l, float t, float w, float h, float[] tmp) {
        if (tmp == null || tmp.length != 8) {
            tmp = new float[8];
        }
        Point2D.copyTo(tmp, 1, l, t);
        Point2D.copyTo(tmp, 0, l, t + h);
        Point2D.copyTo(tmp, 3, l + w, t);
        Point2D.copyTo(tmp, 2, l + w, t + h);
        return tmp;
    }
}

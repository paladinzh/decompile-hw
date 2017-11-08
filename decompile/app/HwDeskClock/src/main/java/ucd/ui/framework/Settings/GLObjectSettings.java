package ucd.ui.framework.Settings;

import ucd.ui.framework.lib.ModelMatrix;

public class GLObjectSettings {
    public GLObjectConfig GLObjectCfg = new GLObjectConfig();
    public boolean hasInit = false;
    public ImageExConfig imageExCfg;
    public ViewConfig viewcfg = new ViewConfig();

    public static class GLObjectConfig {
        public int attrsHandle = -1;
        public int drawType = 5;
        public int itemSize = 3;
        public int mMVPMatrix = -1;
        public ModelMatrix ml = new ModelMatrix();
        public int numItems = 0;
        public int paintColor_handle = -1;
        public int pos4fHandle = -1;
        public int vertexPositionAttribute = -1;
        public float[] vertices;

        public String toString() {
            String t = "unknown";
            if (this.drawType == 4) {
                t = "GL_TRIANGLES";
            } else if (this.drawType == 1) {
                t = "GL_LINES";
            } else if (this.drawType == 3) {
                t = "GL_LINE_STRIP";
            } else if (this.drawType == 2) {
                t = "GL_LINE_LOOP";
            }
            return "PointsCounter=" + this.numItems + ", itemSize=" + this.itemSize + ", drawType=" + t;
        }
    }

    public static class ImageExConfig {
        public int itemSize = 2;
        public int numItems = 0;
        public int samplerUniform = -1;
        public ScaleType scaleType = ScaleType.fill;
        public int textureCoordAttribute = -1;
        public float[] textureCoords;

        public enum ScaleType {
            repeatX,
            repeatY,
            repeatXY,
            fill
        }
    }

    public static class Location {
        private float height = 0.0f;
        public float left = 0.0f;
        public float top = 0.0f;
        private float width = 0.0f;

        public String toString() {
            return "x:" + this.left + ", y:" + this.top + ",  w:" + this.width + ", h:" + this.height;
        }

        public float getWidth() {
            if (this.width < 0.0f) {
                return 0.0f;
            }
            return this.width;
        }

        public float getHeight() {
            if (this.height < 0.0f) {
                return 0.0f;
            }
            return this.height;
        }

        public void setWidth(float w) {
            this.width = w;
        }

        public void setHeight(float h) {
            this.height = h;
        }
    }

    public static class ViewConfig {
        private static final float[] transparent = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        public float alpha = 1.0f;
        public boolean clickable = false;
        public boolean enabled = true;
        public int mixType = 0;
        public float[] paintColor = null;
        public Location pos = new Location();
        public float radius = 0.0f;
        public float rx;
        public float ry;
        public float rz;
        public float scale = 1.0f;
        public GLObjectType type = GLObjectType.Color;
        public float tz;

        public enum GLObjectType {
            Color,
            Image,
            Canvas
        }

        public static float[] getTransparent() {
            return transparent;
        }
    }
}

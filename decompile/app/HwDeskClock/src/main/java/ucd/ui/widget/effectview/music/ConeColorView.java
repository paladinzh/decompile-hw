package ucd.ui.widget.effectview.music;

import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.core.BlendManager.BlendType;
import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.core.ImageEx;

public class ConeColorView extends LinearColorView {
    private ImageEx lightLayer = null;

    public ConeColorView(GLBase root, int w, int h) {
        super(root, w, h, "shader/widget/alarm_color.frag.glsl");
        this.lightLayer = new ImageEx(root, w, h);
        this.lightLayer.load("data/light_layer.png", null);
        add(this.lightLayer);
    }

    protected void updateColorData() {
    }

    protected void getColorHandles(int shader) {
    }

    protected void _drawChildren(GLBaseSettings info) {
        info.env.blendManager.push(BlendType.Normal);
        this.root.drawGLObject(this.colorLayer, info);
        this.root.drawGLObject(this.lightLayer, info);
        info.env.blendManager.pop();
    }

    @Deprecated
    public void setLinearColor(int vs, int ve) {
    }
}

package ucd.ui.framework.core;

import java.util.Stack;
import ucd.ui.framework.lib.GL;

public class BlendManager {
    private BlendType cur;
    private final Stack<BlendType> list = new Stack();

    public enum BlendType {
        Normal,
        Fusion,
        Light,
        SaveTransparent,
        CutTransparent,
        Cover,
        Stencil,
        Add,
        AlphaGradientStencil
    }

    public final void push(BlendType bt) {
        if (bt == null) {
            bt = BlendType.Normal;
        }
        this.list.push(bt);
        if (this.cur != bt) {
            set(bt);
            this.cur = bt;
        }
    }

    public final void pop() {
        if (!this.list.isEmpty()) {
            this.list.pop();
        }
        BlendType bt = BlendType.Normal;
        if (!this.list.isEmpty()) {
            bt = (BlendType) this.list.get(this.list.size() - 1);
        }
        if (bt != this.cur) {
            set(bt);
            this.cur = bt;
        }
    }

    public final void setDefault() {
        set(BlendType.Normal);
    }

    private void set(BlendType bt) {
        if (bt == BlendType.Normal) {
            GL.glBlendFunc(1, 771);
        } else if (bt == BlendType.Fusion) {
            GL.glBlendFunc(1, 772);
        } else if (bt == BlendType.Light) {
            GL.glBlendFunc(1, 771);
        } else if (bt == BlendType.SaveTransparent) {
            GL.glBlendFunc(0, 770);
        } else if (bt == BlendType.CutTransparent) {
            GL.glBlendFuncSeparate(0, 771, 0, 771);
        } else if (bt == BlendType.Cover) {
            GL.glBlendFunc(1, 0);
        } else if (bt == BlendType.Stencil) {
            GL.glBlendFuncSeparate(772, 771, 772, 772);
        } else if (bt == BlendType.Add) {
            GL.glBlendFunc(1, 1);
        } else if (bt == BlendType.AlphaGradientStencil) {
            GL.glBlendFunc(772, 771);
        }
    }
}

package ucd.ui.framework.core;

import java.util.Stack;
import ucd.ui.framework.lib.GL;

public class FBO {
    private int currentFBO = 0;
    private int[] depthRb;
    private int[] frameb;
    protected int levelCounter = 0;
    protected Stack<Integer> stack = new Stack();

    public void init() {
        if (this.frameb == null) {
            this.frameb = new int[1];
            this.depthRb = new int[1];
            GL.glGenFramebuffers(1, this.frameb, 0);
            GL.glGenRenderbuffers(1, this.depthRb, 0);
        }
    }

    public void setDepth(int w, int h) {
    }

    public int start(int texId) {
        return start(texId, true);
    }

    private int start(int texId, boolean push) {
        if (texId != 0) {
            this.levelCounter++;
            if (this.currentFBO != this.frameb[0]) {
                this.currentFBO = this.frameb[0];
                GL.glBindFramebuffer(36160, this.frameb[0]);
            }
            if (push) {
                this.stack.push(Integer.valueOf(texId));
            }
            GL.glFramebufferTexture2D(36160, 36064, 3553, texId, 0);
            return texId;
        }
        System.out.println("FBO, texId err");
        return 0;
    }

    public void end() {
        this.levelCounter--;
        this.stack.pop();
        if (this.stack.size() > 0) {
            start(((Integer) this.stack.get(this.stack.size() - 1)).intValue(), false);
        } else if (this.currentFBO != 0) {
            GL.glBindFramebuffer(36160, 0);
            this.currentFBO = 0;
        }
    }
}

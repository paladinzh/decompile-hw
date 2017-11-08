package ucd.ui.framework.core;

import java.util.ArrayList;
import ucd.ui.framework.lib.GL;

public class TextureManager {
    private ArrayList<Texture> texList = new ArrayList();

    protected static class Texture {
        public int height;
        public int texId;
        public int width;

        public Texture(int id, int w, int h) {
            this.texId = id;
            this.width = w;
            this.height = h;
        }
    }

    private Texture contains(int texId) {
        for (int i = 0; i < this.texList.size(); i++) {
            if (texId == ((Texture) this.texList.get(i)).texId) {
                return (Texture) this.texList.get(i);
            }
        }
        return null;
    }

    private void removeTexRec(int texId) {
        int i = 0;
        while (i < this.texList.size()) {
            if (texId != ((Texture) this.texList.get(i)).texId) {
                i++;
            } else {
                this.texList.remove(i);
                return;
            }
        }
    }

    public void setTextureParams(int texId, int texW, int texH) {
        GL.glBindTexture(3553, texId);
        GL.glTexParameteri(3553, 10242, 33071);
        GL.glTexParameteri(3553, 10243, 33071);
        GL.glTexParameteri(3553, 10240, 9729);
        GL.glTexParameteri(3553, 10241, 9729);
        Texture item = contains(texId);
        if (item == null) {
            this.texList.add(new Texture(texId, texW, texH));
            GL.glTexImage2D(3553, 0, 6408, texW, texH, 0, 6408, 5121, null);
        } else if (item.width != texW || item.height != texH) {
            item.width = texW;
            item.height = texH;
            GL.glTexImage2D(3553, 0, 6408, texW, texH, 0, 6408, 5121, null);
        }
    }

    public int createTex() {
        int[] textures = new int[1];
        GL.glGenTextures(1, textures, 0);
        return textures[0];
    }

    public void recycleTex(int texId) {
        GL.glDeleteTextures(1, new int[]{texId}, 0);
        removeTexRec(texId);
    }
}

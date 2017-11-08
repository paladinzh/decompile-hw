package ucd.ui.framework.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import ucd.ui.framework.Settings.GLObjectSettings.ViewConfig.GLObjectType;
import ucd.ui.framework.lib.GL;

public class Spirit {
    private static volatile FloatBuffer fb;

    public static void draw(GLObject obj, int vertexBufPos, int texBufPos) {
        draw(obj, vertexBufPos, texBufPos, 0);
    }

    protected static void draw(GLObject obj, int vertexBufPos, int texBufPos, int texId) {
        if (obj.settings.GLObjectCfg.vertices != null) {
            setBuffer(vertexBufPos, obj.settings.GLObjectCfg.vertexPositionAttribute, obj.settings.GLObjectCfg.itemSize);
            GLBase gLBase = obj.root;
            gLBase.drawCounter++;
            GL.glEnableVertexAttribArray(obj.settings.GLObjectCfg.vertexPositionAttribute);
            GL.glUniformMatrix4fv(obj.settings.GLObjectCfg.mMVPMatrix, 1, false, obj.info.pm.getFinalMatrix(obj.settings.GLObjectCfg.ml.getMMatrix()), 0);
            if ((obj instanceof ImageEx) && obj.getType() != GLObjectType.Color) {
                ImageEx cur = (ImageEx) obj;
                int bufhandle = cur.root.settings.env.defaultTexBufferPos;
                if (texBufPos == cur.root.settings.env.fboTexBufferPos) {
                    bufhandle = texBufPos;
                } else if (cur.settings.imageExCfg.textureCoords != null) {
                    bufhandle = texBufPos;
                }
                setBuffer(bufhandle, cur.settings.imageExCfg.textureCoordAttribute, cur.settings.imageExCfg.itemSize);
                GL.glEnableVertexAttribArray(obj.settings.imageExCfg.textureCoordAttribute);
                GL.glActiveTexture(33984);
                if (texId == 0) {
                    texId = cur.texObj;
                }
                GL.glBindTexture(cur.texTarget, texId);
                GL.glUniform1i(cur.settings.imageExCfg.samplerUniform, 0);
            }
            GL.glDrawArrays(obj.settings.GLObjectCfg.drawType, 0, obj.settings.GLObjectCfg.numItems);
            if (obj instanceof ImageEx) {
                GL.glBindTexture(((ImageEx) obj).texTarget, 0);
            }
        }
    }

    public static void setData2GLBuffer(int bufHandle, float[] data) {
        GL.glBindBuffer(34962, bufHandle);
        if (data != null) {
            GL.glBufferData(34962, data.length * 4, arrayToBuffer(data), 35044);
        }
    }

    protected static void setBuffer(int bufHandle, int handle, int itemSize, float[] data) {
        setData2GLBuffer(bufHandle, data);
        GL.glVertexAttribPointer(handle, itemSize, 5126, false, itemSize * 4, 0);
    }

    protected static void setBuffer(int bufHandle, int handle, int itemSize) {
        GL.glBindBuffer(34962, bufHandle);
        GL.glVertexAttribPointer(handle, itemSize, 5126, false, itemSize * 4, 0);
    }

    public static final FloatBuffer arrayToBuffer(float[] vertices) {
        if (vertices == null || vertices.length == 0) {
            return null;
        }
        if (fb == null || fb.capacity() < vertices.length) {
            if (fb != null) {
                fb.clear();
            }
            fb = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        fb.limit(vertices.length);
        fb.put(vertices);
        fb.position(0);
        return fb;
    }
}

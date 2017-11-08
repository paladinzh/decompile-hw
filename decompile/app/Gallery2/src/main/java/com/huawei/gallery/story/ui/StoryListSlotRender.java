package com.huawei.gallery.story.ui;

import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.Texture;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer.RenderDelegate;
import com.huawei.gallery.editor.tools.EditorUtils.RenderDelegateWithTexture;
import com.huawei.gallery.ui.ListSlotRender;
import com.huawei.gallery.ui.ListSlotView;
import com.huawei.watermark.manager.parse.WMElement;

public class StoryListSlotRender extends ListSlotRender {
    Rect mSource = new Rect();
    RectF mSourceRectF = new RectF();
    Rect mTarget = new Rect();
    RectF mTargetRectF = new RectF();

    public StoryListSlotRender(GalleryContext activity, ListSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        super(activity, slotView, selectionManager, placeholderColor);
    }

    protected void drawContent(GLCanvas canvas, Texture content, int width, int height, int rotation, float alpha) {
        canvas.save(-1);
        if (content instanceof BitmapTexture) {
            RenderDelegate renderDelegate = new RenderDelegateWithTexture((BitmapTexture) content, null, width, height, rotation);
            RectComputer.computerRect(renderDelegate, this.mSource, this.mTarget);
            this.mSourceRectF.set(this.mSource);
            this.mTargetRectF.set(this.mTarget);
            if (rotation != 0) {
                canvas.translate(((float) width) / 2.0f, ((float) height) / 2.0f);
                canvas.rotate((float) rotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
                if (renderDelegate.isOverTurn()) {
                    canvas.translate(((float) (-height)) / 2.0f, ((float) (-width)) / 2.0f);
                } else {
                    canvas.translate(((float) (-width)) / 2.0f, ((float) (-height)) / 2.0f);
                }
            }
            canvas.drawTexture((BitmapTexture) content, this.mSourceRectF, this.mTargetRectF);
        } else {
            if (rotation != 0) {
                canvas.translate(((float) width) / 2.0f, ((float) height) / 2.0f);
                canvas.rotate((float) rotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
                canvas.translate(((float) (-width)) / 2.0f, ((float) (-height)) / 2.0f);
            }
            content.draw(canvas, 0, 0);
        }
        canvas.setAlpha(alpha);
        canvas.restore();
    }

    protected boolean needDrawUpLoadIcon() {
        return false;
    }
}

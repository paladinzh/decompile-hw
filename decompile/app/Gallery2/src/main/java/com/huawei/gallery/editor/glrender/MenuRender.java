package com.huawei.gallery.editor.glrender;

import android.graphics.Rect;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.huawei.gallery.editor.glrender.BaseRender.EditorStateDelegate;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer.RenderDelegate;

public class MenuRender extends BaseRender {
    public MenuRender(EditorViewDelegate render, EditorStateDelegate delegate) {
        super(render, delegate);
    }

    public void render(GLCanvas canvas) {
        boolean isValid;
        boolean more = false;
        long animTime = AnimationTime.get();
        if (this.mEditorOpenOrQuitEffect != null) {
            this.mEditorOpenOrQuitEffect.calculate(animTime);
            more = true;
            if (!this.mEditorOpenOrQuitEffect.isActive()) {
                this.mEditorOpenOrQuitEffect = null;
            }
        }
        Rect drawRect = null;
        if (this.mEditorOpenOrQuitEffect != null) {
            isValid = false;
            drawRect = EditorUtils.getDrawRect(this.mEditorOpenOrQuitEffect);
        } else {
            isValid = true;
            if (this.mDelegate.computeRenderTexture() != null) {
                RectComputer.computerRect((RenderDelegate) this, this.mSourceRect, this.mTargetRect);
                drawRect = this.mTargetRect;
            }
        }
        if (drawRect != null) {
            this.mDelegate.getPreviewTexture().draw(canvas, drawRect.left, drawRect.top, drawRect.width(), drawRect.height());
            if (isValid) {
                drawCompareImage(canvas, drawRect);
            }
            this.mDelegate.onRenderFinished(drawRect, isValid);
        }
        if (more) {
            this.mEditorViewDelegate.invalidate();
        }
    }

    public boolean renderOpenAnimation(GLCanvas canvas) {
        long animTime = AnimationTime.get();
        if (this.mEditorOpenOrQuitEffect != null) {
            this.mEditorOpenOrQuitEffect.calculate(animTime);
            if (!this.mEditorOpenOrQuitEffect.isActive()) {
                this.mEditorOpenOrQuitEffect = null;
            }
            this.mEditorViewDelegate.invalidate();
        }
        return renderByOpenAnimation(canvas, this.mEditorOpenOrQuitEffect);
    }
}

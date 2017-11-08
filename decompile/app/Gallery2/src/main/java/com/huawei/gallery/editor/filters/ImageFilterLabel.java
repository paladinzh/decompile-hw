package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.text.StaticLayout;
import android.text.TextPaint;
import com.android.gallery3d.common.BitmapUtils;
import com.huawei.gallery.editor.app.EditorState;
import com.huawei.gallery.editor.app.LabelState;
import com.huawei.gallery.editor.filters.FilterLabelRepresentation.LabelHolder;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.util.ReflectUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.Iterator;

public class ImageFilterLabel extends ImageFilter {
    private FilterLabelRepresentation mParameters;
    private LabelState mState;

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterLabelRepresentation) {
            this.mParameters = (FilterLabelRepresentation) representation;
        }
    }

    public void useEditorState(EditorState state) {
        if (state instanceof LabelState) {
            this.mState = (LabelState) state;
        }
    }

    public Bitmap apply(Bitmap bitmap) {
        FilterLabelRepresentation representation = this.mParameters;
        if (representation == null || representation.isNil()) {
            return bitmap;
        }
        Rect displayBounds = new Rect(representation.getDisplayBounds());
        float scale = Math.min(((float) displayBounds.width()) / ((float) bitmap.getWidth()), ((float) displayBounds.height()) / ((float) bitmap.getHeight()));
        if (scale > WMElement.CAMERASIZEVALUE1B1) {
            bitmap = BitmapUtils.resizeBitmapByScale(bitmap, scale, true);
            scale = WMElement.CAMERASIZEVALUE1B1;
        }
        Canvas canvas = new Canvas(bitmap);
        Iterator<LabelHolder> iterator = representation.getLabelHolderQueue().iterator();
        while (iterator.hasNext()) {
            drawSingleLabelHolder(canvas, (LabelHolder) iterator.next(), scale);
        }
        return bitmap;
    }

    private void drawSingleLabelHolder(Canvas canvas, LabelHolder labelHolder, float scale) {
        if (!labelHolder.isNil()) {
            drawBubble(canvas, labelHolder, scale);
            drawText(canvas, labelHolder, scale);
        }
    }

    private void drawBubble(Canvas canvas, LabelHolder labelHolder, float scale) {
        int iconRes = labelHolder.labelPainData.iconRes;
        if (iconRes != 0) {
            NinePatchDrawable drawable = getEnvironment().getBubbleCache().getBubble(iconRes, this.mState.getSimpleEditorManager().getContext());
            if (drawable != null) {
                int width = canvas.getWidth();
                int height = canvas.getHeight();
                canvas.save();
                canvas.translate(labelHolder.centerPoint.x * ((float) width), labelHolder.centerPoint.y * ((float) height));
                canvas.rotate((-labelHolder.rotatedAngle) * 57.29578f);
                canvas.scale(labelHolder.scale / scale, labelHolder.scale / scale);
                canvas.translate(((float) (-labelHolder.initWidth)) / 2.0f, ((float) (-labelHolder.initHeight)) / 2.0f);
                canvas.translate((float) labelHolder.bubbleInitRect.left, (float) labelHolder.bubbleInitRect.top);
                Bitmap bitmap = getDrawableBitmap(drawable);
                if (bitmap != null) {
                    Rect bubbleRect = new Rect(0, 0, labelHolder.bubbleInitRect.width(), labelHolder.bubbleInitRect.height());
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    canvas.drawBitmap(bitmap, null, bubbleRect, paint);
                }
                canvas.restore();
            }
        }
    }

    private void drawText(Canvas canvas, LabelHolder labelHolder, float scale) {
        TextPaint textPaint = new TextPaint();
        labelHolder.labelPainData.updateTextPaint(textPaint);
        StaticLayout staticLayout = EditorUtils.getStaticLayout(labelHolder.content, textPaint, labelHolder.textInitRect);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.save();
        canvas.translate(labelHolder.centerPoint.x * ((float) width), labelHolder.centerPoint.y * ((float) height));
        canvas.rotate((-labelHolder.rotatedAngle) * 57.29578f);
        canvas.scale(labelHolder.scale / scale, labelHolder.scale / scale);
        canvas.translate(((float) (-labelHolder.initWidth)) / 2.0f, ((float) (-labelHolder.initHeight)) / 2.0f);
        canvas.translate(labelHolder.textInitRect.left + ((labelHolder.textInitRect.width() - ((float) staticLayout.getWidth())) / 2.0f), labelHolder.textInitRect.top + ((labelHolder.textInitRect.height() - ((float) staticLayout.getHeight())) / 2.0f));
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private Bitmap getDrawableBitmap(NinePatchDrawable drawable) {
        if (drawable == null) {
            return null;
        }
        NinePatch obj = ReflectUtils.getFieldValue(drawable, "mNinePatch");
        if (obj == null) {
            obj = ReflectUtils.getFieldValue(ReflectUtils.getFieldValue(drawable, "mNinePatchState"), "mNinePatch");
        }
        if (obj instanceof NinePatch) {
            return obj.getBitmap();
        }
        return null;
    }
}

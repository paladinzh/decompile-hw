package com.huawei.gallery.editor.category;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.gallery3d.R;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.ResourceUtils;

public class CategoryView extends IconView implements OnClickListener {
    protected Action mAction;
    CategoryAdapter mAdapter;
    protected int mSelectedDrawable = R.drawable.pic_frame_selected;
    protected int mUnSelectedDrawable = R.drawable.btn_check_off_pressed_emui_black;

    public CategoryView(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public void onDraw(Canvas canvas) {
        if (this.mAction != null) {
            if (!this.mAction.isDoubleAction()) {
                this.mAction.setImageFrame(new Rect(0, 0, getWidth(), getHeight()), this);
                if (this.mAction.getImage() != null) {
                    setBitmap(this.mAction.getImage());
                }
            } else {
                return;
            }
        }
        drawImage(canvas);
        drawOverlay(canvas);
    }

    private void drawFrame(Canvas canvas) {
        NinePatchDrawable frame;
        if (this.mAdapter.isSelected(this)) {
            frame = (NinePatchDrawable) ColorfulUtils.mappingColorfulDrawableForce(getContext(), this.mSelectedDrawable);
        } else {
            frame = (NinePatchDrawable) ResourceUtils.getDrawable(getResources(), Integer.valueOf(this.mUnSelectedDrawable));
        }
        frame.setBounds(getMargin(), getMargin(), getWidth() - getMargin(), getWidth() - getMargin());
        frame.draw(canvas);
    }

    protected void drawImage(Canvas canvas) {
        super.onDraw(canvas);
    }

    protected void drawOverlay(Canvas canvas) {
        drawFrame(canvas);
        if (this.mNeedMask) {
            drawOutlinedText(canvas, getText());
        }
    }

    public void setAction(Action action, CategoryAdapter adapter) {
        this.mAction = action;
        setText(this.mAction.getName());
        this.mAdapter = adapter;
        setUseOnlyDrawable(false);
        if (this.mAction.getType() == 1) {
            setBitmap(BitmapFactory.decodeResource(getResources(), this.mAction.getRepresentation().getOverlayId()));
            setUseOnlyDrawable(true);
        } else {
            setBitmap(this.mAction.getImage());
        }
        invalidate();
    }

    public void onClick(View view) {
        this.mAction.showRepresentation(this.mAdapter.getEditorStep());
        this.mAdapter.setSelected(this);
    }
}

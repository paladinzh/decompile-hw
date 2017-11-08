package com.huawei.gallery.refocus.wideaperture.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.IconView;
import com.huawei.gallery.refocus.wideaperture.app.WideApertureFilterAction;

public class CategoryView extends IconView implements OnClickListener {
    private WideApertureFilterAction mAction;
    private CategoryAdapter mAdapter;

    public CategoryView(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOverlay(canvas);
    }

    private void drawFrame(Canvas canvas) {
        NinePatchDrawable frame;
        this.mTextColor = getResources().getColor(R.color.filtershow_categoryview_text);
        if (this.mAdapter.isSelected(this)) {
            frame = (NinePatchDrawable) getResources().getDrawable(R.drawable.pic_frame_selected);
        } else {
            frame = (NinePatchDrawable) getResources().getDrawable(R.drawable.btn_check_off_pressed_emui_black);
        }
        frame.setBounds(getMargin(), getMargin(), getWidth() - getMargin(), getWidth() - getMargin());
        frame.draw(canvas);
    }

    protected void drawOverlay(Canvas canvas) {
        drawFrame(canvas);
        if (this.mNeedMask) {
            drawOutlinedText(canvas, getText());
        }
    }

    public void setAction(WideApertureFilterAction action, CategoryAdapter adapter) {
        this.mAction = action;
        setText(this.mAction.getFilterName());
        this.mAdapter = adapter;
        setUseOnlyDrawable(false);
        setBitmap(BitmapFactory.decodeResource(getResources(), this.mAction.getFilterIconID()));
        setUseOnlyDrawable(true);
        invalidate();
    }

    public void onClick(View view) {
        this.mAdapter.setSelected(this);
    }
}

package com.android.mms.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import com.google.android.gms.R;

public class SignView extends View {
    private ShapeDrawable mDrawable;

    public SignView(Context context, boolean isSelected) {
        int color;
        super(context);
        OvalShape circle = new OvalShape();
        float density = this.mContext.getResources().getDisplayMetrics().density;
        circle.resize(5.0f * density, 5.0f * density);
        this.mDrawable = new ShapeDrawable(circle);
        if (isSelected) {
            color = context.getResources().getColor(R.color.signview_selected_color);
        } else {
            color = context.getResources().getColor(R.color.signview_normal_color);
        }
        this.mDrawable.getPaint().setShader(new RadialGradient(12.5f, 2.5f, 15.0f, color, color, TileMode.CLAMP));
    }

    protected void onDraw(Canvas canvas) {
        this.mDrawable.draw(canvas);
    }
}

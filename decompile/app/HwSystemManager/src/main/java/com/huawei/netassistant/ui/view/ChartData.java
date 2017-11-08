package com.huawei.netassistant.ui.view;

import android.graphics.RectF;

public interface ChartData {
    RectF getClickArea();

    String getDate();

    int getId();

    int getPointRadius();

    String getText();

    float getX();

    float getY();

    void setPointRadius(int i);

    void updateClickArea(RectF rectF);

    void updateX(float f);

    void updateY(float f);
}

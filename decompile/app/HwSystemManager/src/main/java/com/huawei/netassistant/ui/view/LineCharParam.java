package com.huawei.netassistant.ui.view;

import android.content.res.TypedArray;

public class LineCharParam {
    private static final int DATE_TEXT_INTERVAL = 14;
    public static final int LINE_CHART_HORLINE_COUNT = 6;
    private static final int LINE_CHART_HOR_LINE_INTERVAL = 32;
    private static final int LINE_CHART_INTERVAL = 120;
    public static final int LINE_CHART_SPLIT_RATIO_PERCENT = 50;
    private static final int LINE_OFFSET = 6;
    public static final int LINE_RIGHT_MARGIN_TEXT = 56;
    public static final int LINK_LINE_WIDTH = 3;
    public static final int NUM_XYLINE_SIZE = 33;
    private static final int POINT_CIRCLE_RADIUS = 2;
    public int dateTextInterval;
    public int lineChartHorLineInterval;
    public int lineChartInterval;
    public int lineOffset;
    public float linkLineWidth;
    public int numXYlineSize;
    public int pointCircleRadius;

    public LineCharParam(TypedArray a) {
        this.lineChartInterval = a.getDimensionPixelSize(0, 120);
        this.lineChartHorLineInterval = a.getDimensionPixelSize(2, 32);
        this.pointCircleRadius = a.getDimensionPixelSize(3, 2);
        this.dateTextInterval = a.getDimensionPixelSize(1, 14);
        this.numXYlineSize = a.getDimensionPixelSize(4, 33);
        this.linkLineWidth = (float) a.getDimensionPixelSize(5, 3);
        this.lineOffset = a.getDimensionPixelSize(6, 6);
    }

    public int getLineChartStartY() {
        return this.dateTextInterval + this.numXYlineSize;
    }
}

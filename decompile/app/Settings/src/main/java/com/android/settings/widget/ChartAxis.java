package com.android.settings.widget;

import android.content.res.Resources;
import android.text.SpannableStringBuilder;

public interface ChartAxis {
    long buildLabel(Resources resources, SpannableStringBuilder spannableStringBuilder, long j);

    float convertToPoint(long j);

    long convertToValue(float f);

    float[] getTickPoints();

    boolean setBounds(long j, long j2);

    boolean setSize(float f);

    int shouldAdjustAxis(long j);
}

package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.util.Log;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.HighlightsFilter;
import com.fyusion.sdk.common.ext.filter.MultiControlsFilter;
import com.fyusion.sdk.common.ext.filter.ShadowsFilter;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class q extends a<FilterControl> implements MultiControlsFilter {
    private float c = 0.0f;
    private float d = WMElement.CAMERASIZEVALUE1B1;
    private int e = -1;
    private int f = -1;
    private Map<String, Float> g = new HashMap();

    public q() {
        super(m.HIGHLIGHTSHADOW.a());
    }

    public void a(float f) {
        this.c = f;
    }

    public void a(int i) {
        this.e = GLES20.glGetUniformLocation(i, "shadows");
        this.f = GLES20.glGetUniformLocation(i, "highlights");
    }

    public void a(@NonNull FilterControl filterControl) {
        super.a(filterControl);
        this.g.put(filterControl.getName(), Float.valueOf(filterControl.getValue()));
        if (filterControl instanceof HighlightsFilter) {
            b(filterControl.getValue());
        } else if (filterControl instanceof ShadowsFilter) {
            a(filterControl.getValue());
        } else {
            Log.w("HighlightShadowFilter", "Unxpected FilterControl class: " + filterControl);
        }
    }

    public void b(float f) {
        this.d = f;
    }

    public String f() {
        return "uniform highp float shadows;uniform highp float highlights;const highp vec3 highlights_shadows_luminance_weighting = vec3 (0.3, 0.3, 0.3);highp float shadowTransferFunction (highp float luminance, highp float strength){  return clamp (pow (luminance, 1.0 / (strength + 1.0)) + (-0.76) * pow (luminance, 2.0 / (strength + 1.0)) - luminance, 0.0, 1.0);}highp float getShadowValue (highp float luminance){  if (shadows >= 0.0)  {    return shadowTransferFunction (luminance, shadows);  }  else  {    return -shadowTransferFunction (luminance, -shadows);  }}highp float getHighlightValue (highp float luminance){  if (highlights >= 0.0)  {    return shadowTransferFunction (1.0 - luminance, highlights);  }  else  {    return -shadowTransferFunction (1.0 - luminance, -highlights);  }}";
    }

    public String g() {
        return "highp float luminance = dot (input_color.rgb, highlights_shadows_luminance_weighting);highp float shadow = getShadowValue (luminance);highp float highlight = getHighlightValue (luminance);highp vec3 result = vec3 (0.0, 0.0, 0.0) + ((luminance + shadow + highlight) - 0.0) * ((input_color.rgb - vec3 (0.0, 0.0, 0.0)) / (luminance - 0.0));return vec4 (result.rgb, input_color.a);";
    }

    public Map<String, Float> getFilterValues() {
        return this.g;
    }

    public void h() {
        GLES20.glUniform1f(this.e, this.c);
        GLES20.glUniform1f(this.f, this.d);
    }
}

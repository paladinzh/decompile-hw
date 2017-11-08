package com.amap.api.trace;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TraceLocation {
    private static DecimalFormat f = new DecimalFormat("0.000000", new DecimalFormatSymbols(Locale.US));
    private double a;
    private double b;
    private float c;
    private float d;
    private long e;

    public TraceLocation(double d, double d2, float f, float f2, long j) {
        this.a = a(d);
        this.b = a(d2);
        this.c = (float) ((int) ((3600.0f * f) / 1000.0f));
        this.d = (float) ((int) f2);
        this.e = j;
    }

    public double getLatitude() {
        return this.a;
    }

    public void setLatitude(double d) {
        this.a = a(d);
    }

    public double getLongitude() {
        return this.b;
    }

    public void setLongitude(double d) {
        this.b = a(d);
    }

    public float getSpeed() {
        return this.c;
    }

    public void setSpeed(float f) {
        this.c = (float) ((int) ((3600.0f * f) / 1000.0f));
    }

    public float getBearing() {
        return this.d;
    }

    public void setBearing(float f) {
        this.d = (float) ((int) f);
    }

    public long getTime() {
        return this.e;
    }

    public void setTime(long j) {
        this.e = j;
    }

    private static double a(double d) {
        return Double.parseDouble(f.format(d));
    }

    public TraceLocation copy() {
        TraceLocation traceLocation = new TraceLocation();
        traceLocation.d = this.d;
        traceLocation.a = this.a;
        traceLocation.b = this.b;
        traceLocation.c = this.c;
        traceLocation.e = this.e;
        return traceLocation;
    }

    public String toString() {
        return this.a + ",longtitude " + this.b + ",speed " + this.c + ",bearing " + this.d + ",time " + this.e;
    }
}

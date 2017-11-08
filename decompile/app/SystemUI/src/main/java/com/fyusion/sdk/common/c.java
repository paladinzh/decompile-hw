package com.fyusion.sdk.common;

import android.opengl.Matrix;

/* compiled from: Unknown */
public class c {

    /* compiled from: Unknown */
    public static class a {
        public double a;
        public double b;
        public double c;
        public double d;
        public double e;
        public double f;
        public double g;
        public double h;
        public double i;
        public double j;
        public double k;
        public double l;
        public double m;
        public double n;
        public double o;
        public double p;
    }

    /* compiled from: Unknown */
    public static class b {
        public double a;
        public double b;
        public double c;
        public double d;
        public double e;
        public double f;
    }

    public static a a() {
        a aVar = new a();
        aVar.a = 1.0d;
        aVar.b = 0.0d;
        aVar.c = 0.0d;
        aVar.d = 0.0d;
        aVar.e = 0.0d;
        aVar.f = 1.0d;
        aVar.g = 0.0d;
        aVar.h = 0.0d;
        aVar.i = 0.0d;
        aVar.j = 0.0d;
        aVar.k = 1.0d;
        aVar.l = 0.0d;
        aVar.m = 0.0d;
        aVar.n = 0.0d;
        aVar.o = 0.0d;
        aVar.p = 1.0d;
        return aVar;
    }

    public static a a(double d, double d2, double d3, double d4) {
        float[] fArr = new float[16];
        Matrix.setRotateM(fArr, 0, (float) d, (float) d2, (float) d3, (float) d4);
        return b(fArr);
    }

    public static a a(a aVar, double d, double d2, double d3) {
        float[] fArr = new float[16];
        a(aVar, fArr);
        Matrix.translateM(fArr, 0, (float) d, (float) d2, (float) d3);
        return b(fArr);
    }

    public static a a(a aVar, double d, double d2, double d3, double d4) {
        float[] fArr = new float[16];
        a(aVar, fArr);
        Matrix.rotateM(fArr, 0, (float) d, (float) d2, (float) d3, (float) d4);
        return b(fArr);
    }

    public static a a(a aVar, a aVar2) {
        float[] fArr = new float[16];
        a(aVar, fArr);
        float[] fArr2 = new float[16];
        a(aVar2, fArr2);
        float[] fArr3 = new float[16];
        Matrix.multiplyMM(fArr3, 0, fArr, 0, fArr2, 0);
        return b(fArr3);
    }

    public static b a(double d) {
        b bVar = new b();
        bVar.a = Math.cos(d);
        bVar.b = Math.sin(d);
        bVar.c = -Math.sin(d);
        bVar.d = Math.cos(d);
        bVar.e = 0.0d;
        bVar.f = 0.0d;
        return bVar;
    }

    public static b a(double d, double d2) {
        b bVar = new b();
        bVar.a = d;
        bVar.b = 0.0d;
        bVar.c = 0.0d;
        bVar.d = d2;
        bVar.e = 0.0d;
        bVar.f = 0.0d;
        return bVar;
    }

    public static b a(b bVar) {
        float[] fArr = new float[16];
        a(bVar, fArr);
        float[] fArr2 = new float[16];
        Matrix.invertM(fArr2, 0, fArr, 0);
        return a(fArr2);
    }

    public static b a(b bVar, b bVar2) {
        b bVar3 = new b();
        bVar3.a = (bVar2.a * bVar.a) + (bVar2.b * bVar.c);
        bVar3.b = (bVar2.a * bVar.b) + (bVar2.b * bVar.d);
        bVar3.e = ((bVar2.a * bVar.e) + (bVar2.b * bVar.f)) + bVar2.e;
        bVar3.c = (bVar2.c * bVar.a) + (bVar2.d * bVar.c);
        bVar3.d = (bVar2.c * bVar.b) + (bVar2.d * bVar.d);
        bVar3.f = ((bVar2.c * bVar.e) + (bVar2.d * bVar.f)) + bVar2.f;
        return bVar3;
    }

    public static b a(float[] fArr) {
        b bVar = new b();
        bVar.a = (double) fArr[0];
        bVar.b = (double) fArr[1];
        bVar.e = (double) fArr[3];
        bVar.c = (double) fArr[4];
        bVar.d = (double) fArr[5];
        bVar.f = (double) fArr[7];
        return bVar;
    }

    public static void a(a aVar, float[] fArr) {
        fArr[0] = (float) aVar.a;
        fArr[1] = (float) aVar.b;
        fArr[2] = (float) aVar.c;
        fArr[3] = (float) aVar.d;
        fArr[4] = (float) aVar.e;
        fArr[5] = (float) aVar.f;
        fArr[6] = (float) aVar.g;
        fArr[7] = (float) aVar.h;
        fArr[8] = (float) aVar.i;
        fArr[9] = (float) aVar.j;
        fArr[10] = (float) aVar.k;
        fArr[11] = (float) aVar.l;
        fArr[12] = (float) aVar.m;
        fArr[13] = (float) aVar.n;
        fArr[14] = (float) aVar.o;
        fArr[15] = (float) aVar.p;
    }

    public static void a(b bVar, float[] fArr) {
        fArr[0] = (float) bVar.a;
        fArr[1] = (float) bVar.b;
        fArr[2] = 0.0f;
        fArr[3] = (float) bVar.e;
        fArr[4] = (float) bVar.c;
        fArr[5] = (float) bVar.d;
        fArr[6] = 0.0f;
        fArr[7] = (float) bVar.f;
        fArr[8] = 0.0f;
        fArr[9] = 0.0f;
        fArr[10] = 1.0f;
        fArr[11] = 0.0f;
        fArr[12] = 0.0f;
        fArr[13] = 0.0f;
        fArr[14] = 0.0f;
        fArr[15] = 1.0f;
    }

    public static a b(b bVar) {
        a aVar = new a();
        aVar.a = bVar.a;
        aVar.b = bVar.c;
        aVar.c = 0.0d;
        aVar.d = 0.0d;
        aVar.e = bVar.b;
        aVar.f = bVar.d;
        aVar.g = 0.0d;
        aVar.h = 0.0d;
        aVar.i = 0.0d;
        aVar.j = 0.0d;
        aVar.k = 1.0d;
        aVar.l = 0.0d;
        aVar.m = bVar.e;
        aVar.n = bVar.f;
        aVar.o = 0.0d;
        aVar.p = 1.0d;
        return aVar;
    }

    public static a b(float[] fArr) {
        a aVar = new a();
        aVar.a = (double) fArr[0];
        aVar.b = (double) fArr[1];
        aVar.c = (double) fArr[2];
        aVar.d = (double) fArr[3];
        aVar.e = (double) fArr[4];
        aVar.f = (double) fArr[5];
        aVar.g = (double) fArr[6];
        aVar.h = (double) fArr[7];
        aVar.i = (double) fArr[8];
        aVar.j = (double) fArr[9];
        aVar.k = (double) fArr[10];
        aVar.l = (double) fArr[11];
        aVar.m = (double) fArr[12];
        aVar.n = (double) fArr[13];
        aVar.o = (double) fArr[14];
        aVar.p = (double) fArr[15];
        return aVar;
    }

    public static b b() {
        b bVar = new b();
        bVar.a = 1.0d;
        bVar.b = 0.0d;
        bVar.c = 0.0d;
        bVar.d = 1.0d;
        bVar.e = 0.0d;
        bVar.f = 0.0d;
        return bVar;
    }

    public static b b(double d, double d2) {
        b b = b();
        b.e = d;
        b.f = d2;
        return b;
    }

    public static void b(a aVar, float[] fArr) {
        fArr[0] = (float) aVar.a;
        fArr[3] = (float) aVar.b;
        fArr[6] = (float) aVar.c;
        fArr[1] = (float) aVar.e;
        fArr[4] = (float) aVar.f;
        fArr[7] = (float) aVar.g;
        fArr[2] = ((float) aVar.i) + ((float) aVar.m);
        fArr[5] = ((float) aVar.j) + ((float) aVar.n);
        fArr[8] = (float) aVar.p;
    }
}

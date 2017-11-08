package com.fyusion.sdk.common.ext.filter.a;

import com.fyusion.sdk.common.ext.Size;
import com.fyusion.sdk.common.p;

/* compiled from: Unknown */
public class n extends x {
    public l a = null;

    public void a() {
        super.a(g(), h());
    }

    protected void a(int i) {
        if (this.a != null) {
            this.a.a(i);
        }
    }

    public void a(l lVar) {
        this.a = lVar;
        this.b = true;
    }

    public void a(p pVar) {
        this.a.a(pVar);
    }

    protected void a(o[] oVarArr, boolean z) {
        if (this.a.c()) {
            this.a.a(oVarArr, z);
            return;
        }
        int i = -99;
        if (!z) {
            i = oVarArr[0].b;
        }
        if (!(oVarArr[1] == null || oVarArr[1].b == r0)) {
            oVarArr[1].b();
        }
        oVarArr[1] = new o(oVarArr[0]);
    }

    public void b() {
        super.k();
        super.a(g(), h());
    }

    public void c() {
        this.b = true;
    }

    protected void d() {
        if (this.a != null) {
            this.a.e();
        }
    }

    protected void e() {
        if (this.a != null) {
            this.a.f();
        }
    }

    protected void f() {
        if (this.a != null) {
            this.a.l();
        }
    }

    protected String g() {
        StringBuilder append = new StringBuilder().append("#version 100\nattribute vec2 quad_vertex;varying vec2 texture_coordinate;");
        String g = (this.a != null && this.a.c()) ? this.a.g() : "";
        append = append.append(g).append("void main ()").append("{").append("    highp float x = quad_vertex.x * 2.0 - 1.0;").append("    highp float y = quad_vertex.y * 2.0 - 1.0;").append("    gl_Position = vec4 (x, y, 0.0, 1.0);").append("    texture_coordinate = quad_vertex;");
        g = (this.a != null && this.a.c()) ? this.a.h() : "";
        return append.append(g).append("}").toString();
    }

    protected String h() {
        StringBuilder append = new StringBuilder().append("#version 100\nprecision highp float;precision highp int;uniform sampler2D texture;varying highp vec2 texture_coordinate;highp vec3 rgb2hsv(highp vec3 c){highp vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);highp vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));highp vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));highp float d = q.x - min(q.w, q.y);highp float e = 1.0e-10;return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);}highp vec3 hsv2rgb(highp vec3 c){highp vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);highp vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);}");
        String str = (this.a != null && this.a.c()) ? this.a.i() + this.a.j() : "";
        append = append.append(str).append("void main ()").append("{").append("    highp vec4 color = texture2D (texture, texture_coordinate);");
        str = (this.a != null && this.a.c()) ? this.a.k() : "";
        return append.append(str).append("    gl_FragColor = color;").append("}").toString();
    }

    protected Size i() {
        return this.a == null ? null : new Size(this.a.b(), this.a.a());
    }
}

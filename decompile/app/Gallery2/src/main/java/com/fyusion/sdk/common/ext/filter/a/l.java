package com.fyusion.sdk.common.ext.filter.a;

import com.fyusion.sdk.common.ext.filter.BlockFilter;
import com.fyusion.sdk.common.ext.filter.EditFilterCollection;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.p;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/* compiled from: Unknown */
public class l {
    s a = new s();
    public boolean b = true;
    public boolean c = true;
    private Map<Integer, ImageFilter> d = new TreeMap();
    private boolean e = true;
    private int f;
    private int g;
    private p h = new p();

    public static l a(ImageFilter imageFilter) {
        l lVar = new l();
        if (imageFilter != null) {
            lVar.d.put(Integer.valueOf(imageFilter.getLayer()), imageFilter);
        }
        return lVar;
    }

    private String a(int i, String str) {
        return "highp vec4 applyFilter" + i + " (highp vec4 input_color, highp vec2 texture_coordinate)" + "{" + str + "}";
    }

    private String a(ImageFilter imageFilter, String str, String str2) {
        return (imageFilter.isEnabled() && str2 != null) ? str + str2 : str;
    }

    private synchronized boolean a(FilterControl filterControl) {
        Iterator it = this.d.values().iterator();
        do {
            if (!it.hasNext()) {
                return false;
            }
        } while (!((ImageFilter) it.next()).getClass().equals(filterControl.getImplementationClass()));
        return true;
    }

    private String b(int i) {
        return "color = applyFilter" + i + " (color, texture_coordinate);";
    }

    public int a() {
        return this.g;
    }

    void a(int i) {
        for (a aVar : this.d.values()) {
            if (aVar.isEnabled()) {
                aVar.a(i);
            }
        }
    }

    public synchronized void a(EditFilterCollection editFilterCollection) {
        if (editFilterCollection.isUpdated()) {
            Iterator it = this.d.values().iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                FilterControl filterControl = editFilterCollection.getFilterControl(aVar.getClass());
                if (filterControl == null) {
                    it.remove();
                } else {
                    aVar.a(filterControl);
                }
            }
            try {
                for (FilterControl filterControl2 : editFilterCollection.getFilterControls()) {
                    if (!a(filterControl2)) {
                        ImageFilter imageFilter = (a) filterControl2.getImplementationClass().newInstance();
                        imageFilter.a(filterControl2);
                        b(imageFilter);
                    }
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            this.b = true;
            a(editFilterCollection.isEnabled());
            editFilterCollection.resetUpdate();
        }
    }

    public void a(p pVar) {
        this.h = pVar;
        this.f = this.h.e();
        this.g = this.h.f();
        for (a aVar : this.d.values()) {
            if (aVar.isEnabled() && (aVar instanceof BlockFilter)) {
                ((BlockFilter) aVar).setTextureContainer(this.h);
            }
        }
    }

    public void a(Collection<ImageFilter> collection) {
        for (ImageFilter imageFilter : collection) {
            this.d.put(Integer.valueOf(imageFilter.getLayer()), imageFilter);
        }
    }

    public synchronized void a(boolean z) {
        this.e = z;
    }

    public void a(o[] oVarArr, boolean z) {
        int i = -99;
        if (!z) {
            i = oVarArr[0].b;
        }
        int i2 = i;
        o oVar = new o(oVarArr[0]);
        if (!(oVarArr[1] == null || oVarArr[1].b == i2)) {
            oVarArr[1].b();
        }
        oVarArr[1] = new o(oVarArr[0]);
        o[] oVarArr2 = new o[]{oVar, oVarArr[1]};
        for (a a : this.d.values()) {
            a.a(oVarArr2, oVarArr2[0].b != i2, this.a);
            oVarArr2[0].a(oVarArr2[1]);
        }
        oVarArr[1].a(oVarArr2[0]);
        if (!(oVarArr2[1] == null || oVarArr2[1].b == i2 || oVarArr2[1].b == oVarArr[1].b)) {
            oVarArr2[1].b();
        }
        if (oVarArr[0].b != i2 && oVarArr[0].b != oVarArr[1].b) {
            oVarArr[0].b();
        }
    }

    public int b() {
        return this.f;
    }

    public void b(ImageFilter imageFilter) {
        if (imageFilter instanceof BlockFilter) {
            ((BlockFilter) imageFilter).setImageSize(this.f, this.g);
            ((BlockFilter) imageFilter).setTextureContainer(this.h);
        }
        this.d.put(Integer.valueOf(imageFilter.getLayer()), imageFilter);
    }

    public synchronized boolean c() {
        return this.e;
    }

    public Collection<ImageFilter> d() {
        return !c() ? Collections.emptyList() : Collections.unmodifiableCollection(this.d.values());
    }

    void e() {
        for (a aVar : this.d.values()) {
            if (aVar.isEnabled()) {
                aVar.a = this.b;
                aVar.b = this.c;
                aVar.a();
            }
        }
        this.b = false;
    }

    void f() {
        for (a aVar : this.d.values()) {
            if (aVar.isEnabled()) {
                aVar.b();
            }
        }
    }

    public String g() {
        String str = "";
        Iterator it = this.d.values().iterator();
        while (true) {
            String str2 = str;
            if (!it.hasNext()) {
                return str2;
            }
            a aVar = (a) it.next();
            str = a(aVar, str2, aVar.d());
        }
    }

    public String h() {
        String str = "";
        Iterator it = this.d.values().iterator();
        while (true) {
            String str2 = str;
            if (!it.hasNext()) {
                return str2;
            }
            a aVar = (a) it.next();
            str = a(aVar, str2, aVar.e());
        }
    }

    public String i() {
        String str = "";
        Iterator it = this.d.values().iterator();
        while (true) {
            String str2 = str;
            if (!it.hasNext()) {
                return str2;
            }
            a aVar = (a) it.next();
            str = a(aVar, str2, aVar.f());
        }
    }

    public String j() {
        String str = "";
        Iterator it = this.d.values().iterator();
        while (true) {
            String str2 = str;
            if (!it.hasNext()) {
                return str2;
            }
            a aVar = (a) it.next();
            str = a(aVar, str2, a(aVar.getLayer() + 1, aVar.g()));
        }
    }

    String k() {
        String str = "";
        Iterator it = this.d.values().iterator();
        while (true) {
            String str2 = str;
            if (!it.hasNext()) {
                return str2;
            }
            a aVar = (a) it.next();
            str = a(aVar, str2, b(aVar.getLayer() + 1));
        }
    }

    public void l() {
        for (a aVar : this.d.values()) {
            if (aVar.isEnabled()) {
                aVar.h();
            }
        }
    }
}

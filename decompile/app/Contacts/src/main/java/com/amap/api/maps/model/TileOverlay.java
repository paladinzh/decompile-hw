package com.amap.api.maps.model;

import com.amap.api.mapcore.ap;

public final class TileOverlay {
    private ap a;

    public TileOverlay(ap apVar) {
        this.a = apVar;
    }

    public void remove() {
        this.a.a();
    }

    public void clearTileCache() {
        this.a.b();
    }

    public String getId() {
        return this.a.c();
    }

    public void setZIndex(float f) {
        this.a.a(f);
    }

    public float getZIndex() {
        return this.a.d();
    }

    public void setVisible(boolean z) {
        this.a.a(z);
    }

    public boolean isVisible() {
        return this.a.e();
    }

    public boolean equals(Object obj) {
        if (obj instanceof TileOverlay) {
            return this.a.a(((TileOverlay) obj).a);
        }
        return false;
    }

    public int hashCode() {
        return this.a.f();
    }
}

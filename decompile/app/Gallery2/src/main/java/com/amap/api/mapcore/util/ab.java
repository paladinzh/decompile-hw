package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.ADGLAnimation;
import com.autonavi.amap.mapcore.ADGLMapAnimGroup;
import com.autonavi.amap.mapcore.MapProjection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: ADGLMapAnimationMgr */
public class ab {
    private List<ADGLAnimation> a = Collections.synchronizedList(new ArrayList());

    public synchronized void a() {
        this.a.clear();
    }

    public synchronized int b() {
        return this.a.size();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(MapProjection mapProjection) {
        if (mapProjection == null) {
            return;
        }
        if (this.a.size() > 0) {
            ADGLAnimation aDGLAnimation = (ADGLAnimation) this.a.get(0);
            if (aDGLAnimation == null) {
                return;
            }
            if (aDGLAnimation.isOver()) {
                this.a.remove(aDGLAnimation);
            } else {
                aDGLAnimation.doAnimation(mapProjection);
            }
        }
    }

    public void a(ADGLAnimation aDGLAnimation) {
        if (aDGLAnimation != null) {
            synchronized (this.a) {
                if (!aDGLAnimation.isOver() && this.a.size() > 0) {
                    ADGLAnimation aDGLAnimation2 = (ADGLAnimation) this.a.get(this.a.size() - 1);
                    if (aDGLAnimation2 != null && (aDGLAnimation instanceof ADGLMapAnimGroup) && (aDGLAnimation2 instanceof ADGLMapAnimGroup) && ((ADGLMapAnimGroup) aDGLAnimation).typeEqueal((ADGLMapAnimGroup) aDGLAnimation2) && !((ADGLMapAnimGroup) aDGLAnimation)._needMove) {
                        this.a.remove(aDGLAnimation2);
                    }
                }
                this.a.add(aDGLAnimation);
            }
        }
    }
}

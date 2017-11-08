package com.android.systemui.statusbar.notification;

import android.graphics.drawable.Icon;
import android.util.Pools.SimplePool;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.R;

public class ImageTransformState extends TransformState {
    private static SimplePool<ImageTransformState> sInstancePool = new SimplePool(40);
    private Icon mIcon;

    public void initFrom(View view) {
        super.initFrom(view);
        if (view instanceof ImageView) {
            this.mIcon = (Icon) view.getTag(R.id.image_icon_tag);
        }
    }

    protected boolean sameAs(TransformState otherState) {
        if (!(otherState instanceof ImageTransformState)) {
            return super.sameAs(otherState);
        }
        return this.mIcon != null ? this.mIcon.sameAs(((ImageTransformState) otherState).getIcon()) : false;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public static ImageTransformState obtain() {
        ImageTransformState instance = (ImageTransformState) sInstancePool.acquire();
        if (instance != null) {
            return instance;
        }
        return new ImageTransformState();
    }

    protected boolean transformScale() {
        return true;
    }

    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    protected void reset() {
        super.reset();
        this.mIcon = null;
    }
}

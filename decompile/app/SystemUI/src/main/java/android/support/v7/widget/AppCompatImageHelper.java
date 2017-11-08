package android.support.v7.widget;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

public class AppCompatImageHelper {
    private final AppCompatDrawableManager mDrawableManager;
    private final ImageView mView;

    public void loadFromAttributes(android.util.AttributeSet r8, int r9) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x003d in list [B:12:0x003a]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r7 = this;
        r6 = -1;
        r0 = 0;
        r3 = r7.mView;	 Catch:{ all -> 0x003e }
        r1 = r3.getDrawable();	 Catch:{ all -> 0x003e }
        if (r1 != 0) goto L_0x0033;	 Catch:{ all -> 0x003e }
    L_0x000a:
        r3 = r7.mView;	 Catch:{ all -> 0x003e }
        r3 = r3.getContext();	 Catch:{ all -> 0x003e }
        r4 = android.support.v7.appcompat.R$styleable.AppCompatImageView;	 Catch:{ all -> 0x003e }
        r5 = 0;	 Catch:{ all -> 0x003e }
        r0 = android.support.v7.widget.TintTypedArray.obtainStyledAttributes(r3, r8, r4, r9, r5);	 Catch:{ all -> 0x003e }
        r3 = android.support.v7.appcompat.R$styleable.AppCompatImageView_srcCompat;	 Catch:{ all -> 0x003e }
        r4 = -1;	 Catch:{ all -> 0x003e }
        r2 = r0.getResourceId(r3, r4);	 Catch:{ all -> 0x003e }
        if (r2 == r6) goto L_0x0033;	 Catch:{ all -> 0x003e }
    L_0x0020:
        r3 = r7.mDrawableManager;	 Catch:{ all -> 0x003e }
        r4 = r7.mView;	 Catch:{ all -> 0x003e }
        r4 = r4.getContext();	 Catch:{ all -> 0x003e }
        r1 = r3.getDrawable(r4, r2);	 Catch:{ all -> 0x003e }
        if (r1 == 0) goto L_0x0033;	 Catch:{ all -> 0x003e }
    L_0x002e:
        r3 = r7.mView;	 Catch:{ all -> 0x003e }
        r3.setImageDrawable(r1);	 Catch:{ all -> 0x003e }
    L_0x0033:
        if (r1 == 0) goto L_0x0038;	 Catch:{ all -> 0x003e }
    L_0x0035:
        android.support.v7.widget.DrawableUtils.fixDrawable(r1);	 Catch:{ all -> 0x003e }
    L_0x0038:
        if (r0 == 0) goto L_0x003d;
    L_0x003a:
        r0.recycle();
    L_0x003d:
        return;
    L_0x003e:
        r3 = move-exception;
        if (r0 == 0) goto L_0x0044;
    L_0x0041:
        r0.recycle();
    L_0x0044:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.widget.AppCompatImageHelper.loadFromAttributes(android.util.AttributeSet, int):void");
    }

    public AppCompatImageHelper(ImageView view, AppCompatDrawableManager drawableManager) {
        this.mView = view;
        this.mDrawableManager = drawableManager;
    }

    public void setImageResource(int resId) {
        if (resId != 0) {
            Drawable d;
            if (this.mDrawableManager != null) {
                d = this.mDrawableManager.getDrawable(this.mView.getContext(), resId);
            } else {
                d = ContextCompat.getDrawable(this.mView.getContext(), resId);
            }
            if (d != null) {
                DrawableUtils.fixDrawable(d);
            }
            this.mView.setImageDrawable(d);
            return;
        }
        this.mView.setImageDrawable(null);
    }

    boolean hasOverlappingRendering() {
        Drawable background = this.mView.getBackground();
        if (VERSION.SDK_INT < 21 || !(background instanceof RippleDrawable)) {
            return true;
        }
        return false;
    }
}

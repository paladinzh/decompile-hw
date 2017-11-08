package com.android.gallery3d.gadget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import com.android.gallery3d.util.GalleryLog;
import java.lang.reflect.Method;

public class HwTransitionReflection {
    private static final String TAG = "HwTransitionReflection";
    public static final String TRANS_TYPE_BOX = "Box";
    public static final String TRANS_TYPE_CYLINDER = "Cylinder";
    public static final String TRANS_TYPE_DEPTH = "Depth";
    public static final String TRANS_TYPE_FLIPOVER = "Flipover";
    public static final String TRANS_TYPE_NORMAL = "Normal";
    public static final String TRANS_TYPE_PAGE = "Page";
    public static final String TRANS_TYPE_PUSH = "Push";
    public static final String TRANS_TYPE_ROTATION = "Rotation";
    public static final String TRANS_TYPE_WINDMILL = "Windmill";
    private Object HwTransitionInstance;
    private boolean bErr;
    private Class<?> clazz;
    private Method mAnimateDispatchDrawMethod;
    private Method mAnimateDrawMethod;
    private Method mIs3DAnimationMethod;
    private Method mSetBackgroundMethod;
    private Method mSetCameraDistanceMethod;
    private Method mSetLayerTransparentMethod;
    private Method mSetTransitionTypeMethod;
    private Method mStartAnimationMethod;
    private Method msetAlphaModeMethod;

    public HwTransitionReflection(View targetView, String type) {
        this(targetView, type, 0);
    }

    HwTransitionReflection(View targetView, String type, int pageSpacing) {
        this.clazz = null;
        this.HwTransitionInstance = null;
        this.mSetTransitionTypeMethod = null;
        this.mAnimateDrawMethod = null;
        this.mStartAnimationMethod = null;
        this.mSetCameraDistanceMethod = null;
        this.mIs3DAnimationMethod = null;
        this.mAnimateDispatchDrawMethod = null;
        this.mSetLayerTransparentMethod = null;
        this.mSetBackgroundMethod = null;
        this.msetAlphaModeMethod = null;
        this.bErr = false;
        this.clazz = WidgetUtils.getHwTransitionClass(targetView.getContext());
        getHwTransitionInstance(targetView, type, pageSpacing);
        initDeclaredMethods();
    }

    public void setTransitionType(String type) {
        if (this.mSetTransitionTypeMethod != null) {
            try {
                this.mSetTransitionTypeMethod.invoke(this.HwTransitionInstance, new Object[]{type});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
    }

    public void setCameraDistance(float z) {
        if (this.mSetCameraDistanceMethod != null) {
            try {
                this.mSetCameraDistanceMethod.invoke(this.HwTransitionInstance, new Object[]{Float.valueOf(z)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
    }

    public boolean startAnimation(View view) {
        boolean rtn = false;
        if (this.mStartAnimationMethod != null) {
            try {
                rtn = ((Boolean) this.mStartAnimationMethod.invoke(this.HwTransitionInstance, new Object[]{view})).booleanValue();
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
        return rtn;
    }

    public boolean animateDraw(Canvas canvas) {
        boolean rtn = false;
        if (this.mAnimateDrawMethod != null) {
            try {
                rtn = ((Boolean) this.mAnimateDrawMethod.invoke(this.HwTransitionInstance, new Object[]{canvas})).booleanValue();
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
        return rtn;
    }

    public boolean is3DAnimation() {
        boolean rtn = false;
        if (this.mIs3DAnimationMethod != null) {
            try {
                rtn = ((Boolean) this.mIs3DAnimationMethod.invoke(this.HwTransitionInstance, new Object[0])).booleanValue();
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
        return rtn;
    }

    public boolean animateDispatchDraw(Canvas canvas, int transitonX, boolean isScrolling) {
        boolean rtn = false;
        if (this.mAnimateDispatchDrawMethod != null) {
            try {
                rtn = ((Boolean) this.mAnimateDispatchDrawMethod.invoke(this.HwTransitionInstance, new Object[]{canvas, Integer.valueOf(transitonX), Boolean.valueOf(isScrolling)})).booleanValue();
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
        return rtn;
    }

    public void setLayerTransparent(boolean isTransparent) {
        if (this.mSetLayerTransparentMethod != null) {
            try {
                this.mSetLayerTransparentMethod.invoke(this.HwTransitionInstance, new Object[]{Boolean.valueOf(isTransparent)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
    }

    public void setBackground(Bitmap background) {
        if (this.mSetBackgroundMethod != null) {
            try {
                this.mSetBackgroundMethod.invoke(this.HwTransitionInstance, new Object[]{background});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
    }

    public void setAlphaMode(boolean useAlpha) {
        if (this.msetAlphaModeMethod != null) {
            try {
                this.msetAlphaModeMethod.invoke(this.HwTransitionInstance, new Object[]{Boolean.valueOf(useAlpha)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    GalleryLog.d(TAG, e.toString());
                }
            }
        }
    }

    private void getHwTransitionInstance(View targetView, String type, int pageSpacing) {
        Class<?>[] mConstructorSignature = new Class[]{View.class, String.class, Integer.TYPE};
        if (this.clazz != null) {
            try {
                this.HwTransitionInstance = this.clazz.getConstructor(mConstructorSignature).newInstance(new Object[]{targetView, type, Integer.valueOf(pageSpacing)});
                GalleryLog.d(TAG, "HwTransitionInstance  = " + this.HwTransitionInstance);
            } catch (Exception e) {
                GalleryLog.d(TAG, "HwTransitionInstance  = " + this.HwTransitionInstance);
            }
        }
    }

    private void initDeclaredMethods() {
        Class<?>[] setTransitionTypeArgs = new Class[]{String.class};
        Class<?>[] animateDrawArgs = new Class[]{Canvas.class};
        Class<?>[] startAnimationArgs = new Class[]{View.class};
        Class<?>[] setCameraDistanceArgs = new Class[]{Float.TYPE};
        Class<?>[] dispatchDrawArgs = new Class[]{Canvas.class, Integer.TYPE, Boolean.TYPE};
        Class<?>[] setLayerTransparentArgs = new Class[]{Boolean.TYPE};
        Class<?>[] setBackgroundArgs = new Class[]{Bitmap.class};
        Class<?>[] setAlphaModeArgs = new Class[]{Boolean.TYPE};
        if (this.clazz != null) {
            try {
                this.mSetTransitionTypeMethod = this.clazz.getDeclaredMethod("setTransitionType", setTransitionTypeArgs);
                this.mAnimateDrawMethod = this.clazz.getDeclaredMethod("animateDraw", animateDrawArgs);
                this.mStartAnimationMethod = this.clazz.getDeclaredMethod("startAnimation", startAnimationArgs);
                this.mSetCameraDistanceMethod = this.clazz.getDeclaredMethod("setCameraDistance", setCameraDistanceArgs);
                this.mIs3DAnimationMethod = this.clazz.getDeclaredMethod("is3DAnimation", new Class[0]);
                this.mAnimateDispatchDrawMethod = this.clazz.getDeclaredMethod("animateDispatchDraw", dispatchDrawArgs);
                this.mSetLayerTransparentMethod = this.clazz.getDeclaredMethod("setLayerTransparent", setLayerTransparentArgs);
                this.mSetBackgroundMethod = this.clazz.getDeclaredMethod("setBackground", setBackgroundArgs);
                this.msetAlphaModeMethod = this.clazz.getDeclaredMethod("setAlphaMode", setAlphaModeArgs);
            } catch (NoSuchMethodException e) {
                GalleryLog.i(TAG, "No such method error!");
            }
        }
    }
}

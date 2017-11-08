package com.huawei.gallery.ui;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.Wrapper;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import java.lang.reflect.InvocationTargetException;

public class PhotoPageLoadingManager {
    private static final int PADDING = GalleryUtils.dpToPixel(20);
    public static final ReflectCaller sReflectCaller = new ReflectCaller() {
        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            para[0].getClass().getMethod("setColor", new Class[]{Integer.TYPE}).invoke(para[0], new Object[]{Integer.valueOf(-1)});
            return null;
        }
    };
    private ViewGroup mStubLoadingProgressNeigh;
    private ViewGroup mStubLoadingProgressOri;
    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (msg.obj instanceof Float) {
                        PhotoPageLoadingManager.this.mStubLoadingProgressOri.setTranslationX(((Float) msg.obj).floatValue());
                        PhotoPageLoadingManager.this.mStubLoadingProgressOri.setVisibility(msg.arg2);
                        return;
                    }
                    return;
                case 2:
                    if (msg.obj instanceof Float) {
                        PhotoPageLoadingManager.this.mStubLoadingProgressNeigh.setTranslationX(((Float) msg.obj).floatValue());
                        PhotoPageLoadingManager.this.mStubLoadingProgressNeigh.setVisibility(msg.arg2);
                        return;
                    }
                    return;
                case 3:
                    ViewGroup temp = PhotoPageLoadingManager.this.mStubLoadingProgressNeigh;
                    PhotoPageLoadingManager.this.mStubLoadingProgressNeigh = PhotoPageLoadingManager.this.mStubLoadingProgressOri;
                    PhotoPageLoadingManager.this.mStubLoadingProgressOri = temp;
                    return;
                default:
                    return;
            }
        }
    };

    public PhotoPageLoadingManager(Activity activity) {
        this.mStubLoadingProgressOri = (ViewGroup) activity.findViewById(R.id.progress_stub_ori);
        this.mStubLoadingProgressNeigh = (ViewGroup) activity.findViewById(R.id.progress_stub_neighbor);
        this.mStubLoadingProgressOri.removeAllViews();
        this.mStubLoadingProgressNeigh.removeAllViews();
        this.mStubLoadingProgressOri.addView(createProgress(activity));
        this.mStubLoadingProgressNeigh.addView(createProgress(activity));
    }

    private ProgressBar createProgress(Activity activity) {
        ProgressBar bar = new ProgressBar(activity);
        Wrapper.runCaller(sReflectCaller, bar.getIndeterminateDrawable());
        return bar;
    }

    public void onResume() {
        setVisible(8, 1);
        this.mStubLoadingProgressOri.setTranslationX(0.0f);
        this.mStubLoadingProgressNeigh.setTranslationY(0.0f);
    }

    public void onPause() {
        this.mUIHandler.removeCallbacksAndMessages(null);
        this.mStubLoadingProgressOri.setTranslationX(0.0f);
        this.mStubLoadingProgressNeigh.setTranslationY(0.0f);
        setVisible(8, 0);
        setVisible(8, 1);
    }

    public void onPhotoTranslationChange(float x, int index, boolean visible) {
        if ((index == 0 ? this.mStubLoadingProgressOri : this.mStubLoadingProgressNeigh).getVisibility() == 0 || visible) {
            Message message = Message.obtain(this.mUIHandler, index == 0 ? 1 : 2, Float.valueOf(x));
            message.arg1 = index;
            message.arg2 = visible ? 0 : 8;
            this.mUIHandler.sendMessage(message);
        }
    }

    public void onSlidePicture() {
        this.mUIHandler.removeCallbacksAndMessages(null);
        this.mUIHandler.sendEmptyMessage(3);
    }

    public void setVisible(int visible, int index) {
        if (index == 0) {
            this.mStubLoadingProgressOri.setVisibility(visible);
        } else {
            this.mStubLoadingProgressNeigh.setVisibility(visible);
        }
    }

    public void onConfigurationChanged() {
        this.mStubLoadingProgressOri.setVisibility(8);
        this.mStubLoadingProgressNeigh.setVisibility(8);
    }
}

package com.android.keyguard.hwlockscreen;

import android.content.Context;
import android.util.AttributeSet;
import com.android.huawei.slideunlock.HwCustSlideUnlockScreen;
import com.android.keyguard.HwCustManager;
import java.lang.reflect.InvocationTargetException;

public class HwSlideUnlockScreen extends SlideUnlockScreen {
    private HwCustSlideUnlockScreen mCust;

    public HwSlideUnlockScreen(Context context) {
        super(context);
    }

    public HwSlideUnlockScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mCust = (HwCustSlideUnlockScreen) getObject(HwCustSlideUnlockScreen.class, getContext());
        if (this.mCust != null) {
            this.mCust.init();
            this.mCust.setUnlockText(this.mUnlockTip);
            this.mCust.showTalkBackTips();
            this.mCust.addListener();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mCust != null) {
            this.mCust.removeListener();
        }
        this.mCust = null;
    }

    private Object getObject(Class<?> classClass, Object... args) {
        try {
            return Class.forName(HwCustManager.CLASS_HWCUSTUTIL).getMethod(HwCustManager.METHOD_CREATEOBJ, new Class[]{Class.class, Object[].class}).invoke(null, new Object[]{classClass, args});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SecurityException e2) {
            e2.printStackTrace();
            return null;
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
            return null;
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
            return null;
        } catch (IllegalAccessException e5) {
            e5.printStackTrace();
            return null;
        } catch (InvocationTargetException e6) {
            e6.printStackTrace();
            return null;
        }
    }
}

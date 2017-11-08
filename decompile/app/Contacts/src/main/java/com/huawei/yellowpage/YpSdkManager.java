package com.huawei.yellowpage;

import android.content.Context;
import android.util.Log;
import android.view.View;
import java.lang.reflect.Constructor;

public class YpSdkManager implements IYpSdkMgr {
    private static IYpSdkMgr mInstance = null;
    private Class<?> cls = null;
    private Constructor<?> constructor = null;
    private Object ypSdkManager = null;

    public static IYpSdkMgr getInstance() {
        if (mInstance == null) {
            mInstance = new YpSdkManager();
        }
        return mInstance;
    }

    public boolean initPlug(Context context) {
        try {
            Context plugContext = loadCalss(context);
            return ((Boolean) this.cls.getMethod("initPlug", new Class[]{Context.class, Context.class}).invoke(this.ypSdkManager, new Object[]{context, plugContext})).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public View getView(Context context) {
        try {
            if (this.cls == null) {
                Context mcontext = loadCalss(context);
            }
            return (View) this.cls.getMethod("getView", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Context loadCalss(Context context) throws Exception {
        Context mPlugContext = context.createPackageContext("com.huawei.yellowpage", 3);
        this.cls = null;
        this.constructor = null;
        this.ypSdkManager = null;
        this.cls = mPlugContext.getClassLoader().loadClass("com.huawei.plug.yp.sdk.YpSdkPlugHW");
        Log.i("YpSdkMgr", "cls:" + this.cls.hashCode());
        this.constructor = this.cls.getConstructor(new Class[0]);
        this.ypSdkManager = this.constructor.newInstance(new Object[0]);
        return mPlugContext;
    }

    public void onPlugIn(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onPlugIn", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPlugOut(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onPlugOut", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPlugStart(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onPlugStart", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPlugResume(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onPlugResume", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPlugPause(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onPlugPause", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("YpSdkMgr", e.getMessage());
        }
    }

    public void onPlugStop(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onPlugStop", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("YpSdkMgr", e.getMessage());
        }
    }

    public void onPlugDestory(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onPlugDestory", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("YpSdkMgr", e.getMessage());
        }
    }

    public void onPageSelected(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onPageSelected", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onConfigurationChanged(Context context) {
        try {
            if (this.cls == null) {
                loadCalss(context);
            }
            this.cls.getMethod("onConfigurationChanged", new Class[]{Context.class}).invoke(this.ypSdkManager, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

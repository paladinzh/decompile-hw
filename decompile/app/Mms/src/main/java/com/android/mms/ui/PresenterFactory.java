package com.android.mms.ui;

import android.content.Context;
import com.android.mms.model.Model;
import com.huawei.cspcommon.MLog;
import java.lang.reflect.InvocationTargetException;

public class PresenterFactory {
    public static Presenter getPresenter(String className, Context context, ViewInterface view, Model model) {
        try {
            if (className.indexOf(".") == -1) {
                className = "com.android.mms.ui." + className;
            }
            return (Presenter) Class.forName(className).getConstructor(new Class[]{Context.class, ViewInterface.class, Model.class}).newInstance(new Object[]{context, view, model});
        } catch (ClassNotFoundException e) {
            MLog.e("PresenterFactory", "Type not found: " + className, (Throwable) e);
            return null;
        } catch (NoSuchMethodException e2) {
            MLog.e("PresenterFactory", "No such constructor.", (Throwable) e2);
            return null;
        } catch (InvocationTargetException e3) {
            MLog.e("PresenterFactory", "Unexpected InvocationTargetException", (Throwable) e3);
            return null;
        } catch (IllegalAccessException e4) {
            MLog.e("PresenterFactory", "Unexpected IllegalAccessException", (Throwable) e4);
            return null;
        } catch (InstantiationException e5) {
            MLog.e("PresenterFactory", "Unexpected InstantiationException", (Throwable) e5);
            return null;
        }
    }
}

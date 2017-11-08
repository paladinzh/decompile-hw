package com.android.gallery3d.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Wrapper {

    public interface ReflectCaller {
        Object run(Object[] objArr) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException;
    }

    public static class InstanceMethodCaller implements ReflectCaller {
        private Method mInstanceMethod;

        public InstanceMethodCaller(Method instanceMethod) {
            this.mInstanceMethod = instanceMethod;
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            return this.mInstanceMethod.invoke(para[0], new Object[]{para[1], para[2]});
        }
    }

    public static class StaticMethodCaller implements ReflectCaller {
        private Method mStaticMethod;

        public StaticMethodCaller(Method staticMethod) {
            this.mStaticMethod = staticMethod;
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            return this.mStaticMethod.invoke(null, new Object[]{para[0]});
        }
    }

    public static Object runCaller(ReflectCaller caller, Object... para) {
        try {
            return caller.run(para);
        } catch (IllegalArgumentException e) {
            GalleryLog.e("Wrapper", "IllegalArgumentException " + e.getMessage());
            return null;
        } catch (InstantiationException e2) {
            GalleryLog.e("Wrapper", "InstantiationException " + e2.getMessage());
            return null;
        } catch (InvocationTargetException e3) {
            GalleryLog.e("Wrapper", "InvocationTargetException " + e3.getMessage());
            return null;
        } catch (IllegalAccessException e4) {
            GalleryLog.e("Wrapper", "IllegalAccessException " + e4.getMessage());
            return null;
        } catch (NoSuchMethodException e5) {
            GalleryLog.e("Wrapper", "NoSuchMethodException " + e5.getMessage());
            return null;
        } catch (NullPointerException e6) {
            GalleryLog.e("Wrapper", "NullPointerException " + e6.getMessage());
            return null;
        } catch (Exception e7) {
            GalleryLog.e("Wrapper", "unknow exception " + e7.getMessage());
            return null;
        }
    }
}

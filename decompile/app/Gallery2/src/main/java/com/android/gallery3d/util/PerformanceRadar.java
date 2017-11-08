package com.android.gallery3d.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PerformanceRadar {
    private static int REPORT_STATE_ENDED = 3;
    private static int REPORT_STATE_INIT = 1;
    private static int REPORT_STATE_STARTED = 2;
    private static Map<String, Field> fields = new HashMap(3);
    private static String[] names = new String[]{"JLID_CAMERA_SEE_TO_REVIEW_BEGIN", "JLID_CAMERA_SEE_TO_REVIEW_END"};
    private static boolean sIsInit;
    private static Method sLogMethod;
    private static Method sLogMethodWithParam;

    public enum Reporter {
        CAMERA_SEE_TO_REVIEW("JLID_CAMERA_SEE_TO_REVIEW_BEGIN", "JLID_CAMERA_SEE_TO_REVIEW_END");
        
        private String mEnd;
        private String mStart;
        private int mState;

        private Reporter(String start, String end) {
            this.mState = PerformanceRadar.REPORT_STATE_INIT;
            this.mStart = start;
            this.mEnd = end;
        }

        public void start(String body) {
            if (this.mState == PerformanceRadar.REPORT_STATE_INIT || this.mState == PerformanceRadar.REPORT_STATE_ENDED) {
                String str = this.mStart;
                if (body == null) {
                    body = this.mStart;
                }
                PerformanceRadar.report(str, body);
                this.mState = PerformanceRadar.REPORT_STATE_STARTED;
                GalleryLog.d("PerformanceRadar$Reporter", this.mStart + " start reporter, state change to " + this.mState);
                return;
            }
            GalleryLog.d("PerformanceRadar$Reporter", this.mStart + " Can't start reporter, wrong state " + this.mState);
        }

        public void end(String body) {
            if (this.mState == PerformanceRadar.REPORT_STATE_STARTED) {
                String str = this.mEnd;
                if (body == null) {
                    body = this.mEnd;
                }
                PerformanceRadar.report(str, body);
                this.mState = PerformanceRadar.REPORT_STATE_ENDED;
                GalleryLog.d("PerformanceRadar$Reporter", this.mEnd + " end reporter, state change to " + this.mState);
                return;
            }
            GalleryLog.d("PerformanceRadar$Reporter", this.mEnd + " Can't end reporter, wrong state " + this.mState);
        }
    }

    static {
        sIsInit = false;
        try {
            Class<?> clazz = Class.forName("android.util.Jlog");
            sLogMethod = clazz.getDeclaredMethod("d", new Class[]{Integer.TYPE, String.class});
            sLogMethodWithParam = clazz.getDeclaredMethod("d", new Class[]{Integer.TYPE, Integer.TYPE, String.class});
            clazz = Class.forName("android.util.JlogConstants");
            for (String name : names) {
                fields.put(name, clazz.getDeclaredField(name));
            }
            sIsInit = true;
        } catch (NoSuchMethodException e) {
            GalleryLog.e("PerformanceRadar", "NoSuchMethodException android.util.JLog Not supported~:" + e);
            sIsInit = false;
        } catch (ClassNotFoundException e2) {
            GalleryLog.e("PerformanceRadar", "ClassNotFoundException android.util.JLog Not supported~:" + e2);
            sIsInit = false;
        } catch (NoSuchFieldException e3) {
            GalleryLog.e("PerformanceRadar", "NoSuchFieldException android.util.JLogConstants Not supported~:" + e3);
            sIsInit = false;
        }
    }

    public static void report(String bugType, String body) {
        if (sIsInit) {
            try {
                if (((Field) fields.get(bugType)) == null) {
                    GalleryLog.e("PerformanceRadar", "no such field " + bugType);
                    return;
                }
                sLogMethod.invoke(null, new Object[]{Integer.valueOf(field.getInt(null)), body});
            } catch (IllegalAccessException e) {
                GalleryLog.e("PerformanceRadar", "IllegalAccessException when reportPerformanceRadar:" + e);
            } catch (InvocationTargetException e2) {
                GalleryLog.e("PerformanceRadar", "InvocationTargetException when reportPerformanceRadar:" + e2);
            }
        }
    }
}

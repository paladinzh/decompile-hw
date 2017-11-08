package android.support.v13.view;

import android.content.ClipData;
import android.support.v4.os.BuildCompat;
import android.view.View;
import android.view.View.DragShadowBuilder;

public class ViewCompat extends android.support.v4.view.ViewCompat {
    static ViewCompatImpl IMPL;

    interface ViewCompatImpl {
        void cancelDragAndDrop(View view);

        boolean startDragAndDrop(View view, ClipData clipData, DragShadowBuilder dragShadowBuilder, Object obj, int i);

        void updateDragShadow(View view, DragShadowBuilder dragShadowBuilder);
    }

    private static class Api24ViewCompatImpl implements ViewCompatImpl {
        private Api24ViewCompatImpl() {
        }

        public boolean startDragAndDrop(View v, ClipData data, DragShadowBuilder shadowBuilder, Object localState, int flags) {
            return ViewCompatApi24.startDragAndDrop(v, data, shadowBuilder, localState, flags);
        }

        public void cancelDragAndDrop(View v) {
            ViewCompatApi24.cancelDragAndDrop(v);
        }

        public void updateDragShadow(View v, DragShadowBuilder shadowBuilder) {
            ViewCompatApi24.updateDragShadow(v, shadowBuilder);
        }
    }

    private static class BaseViewCompatImpl implements ViewCompatImpl {
        private BaseViewCompatImpl() {
        }

        public boolean startDragAndDrop(View v, ClipData data, DragShadowBuilder shadowBuilder, Object localState, int flags) {
            return v.startDrag(data, shadowBuilder, localState, flags);
        }

        public void cancelDragAndDrop(View v) {
        }

        public void updateDragShadow(View v, DragShadowBuilder shadowBuilder) {
        }
    }

    static {
        if (BuildCompat.isAtLeastN()) {
            IMPL = new Api24ViewCompatImpl();
        } else {
            IMPL = new BaseViewCompatImpl();
        }
    }

    public static boolean startDragAndDrop(View v, ClipData data, DragShadowBuilder shadowBuilder, Object localState, int flags) {
        return IMPL.startDragAndDrop(v, data, shadowBuilder, localState, flags);
    }

    public static void cancelDragAndDrop(View v) {
        IMPL.cancelDragAndDrop(v);
    }

    public static void updateDragShadow(View v, DragShadowBuilder shadowBuilder) {
        IMPL.updateDragShadow(v, shadowBuilder);
    }

    private ViewCompat() {
    }
}

package android.os;

import android.app.Activity;
import android.os.FreezeScreenScene.MonitorHelper;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import com.android.internal.policy.PhoneWindow;
import java.lang.reflect.Field;

public class FreezeScreenApplicationMonitor implements IFreezeScreenApplicationMonitor {
    public static final String TAG = "FreezeScreenApplicationMonitor";
    private static FreezeScreenApplicationMonitor mAppMonitor = null;
    private ArrayMap<String, Integer> mSceneMap;
    private TransparentActivityScene mTransparentActivityScene;

    public static final class TransparentActivityScene extends FreezeScreenScene {
        private static final int CHECK_FREEZE_SCREEN_DELAY_TIME = 5000;
        private static final String CHILDREN_ARRAY_FIELD = "mChildren";
        private static final int MAX_CHILDREN_VIEW_IN_FIRST_HIERARCHY = 1;
        private static final int MAX_CHILDREN_VIEW_IN_SECOND_HIERARCHY = 0;
        private static final String PARENT_CONTENT_FIELD = "mContentParent";
        private static final String PHONE_WINDOW_CLASS = "com.android.internal.policy.PhoneWindow";
        private static final String VIEW_GROUP_CLASS = "android.view.ViewGroup";
        private static final int[] mCheckStanderd = new int[]{1, 0};
        private Activity mCurCheckActivity = null;
        private Activity mLastCheckActivity = null;

        public boolean checkParamsValid(ArrayMap<String, Object> params) {
            if (params != null && (params.get(FreezeScreenScene.LOOPER_PARAM) instanceof Looper) && (params.get(FreezeScreenScene.TOKEN_PARAM) instanceof IBinder) && (params.get(FreezeScreenScene.LAYOUT_PARAM) instanceof LayoutParams) && (params.get(FreezeScreenScene.ACTIVITY_PARAM) instanceof Activity)) {
                return true;
            }
            return false;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                this.mCurCheckActivity = (Activity) params.get(FreezeScreenScene.ACTIVITY_PARAM);
                if (this.mCurCheckActivity != null && !this.mCurCheckActivity.getComponentName().equals(this.mLastCheckActivity)) {
                    if (isWinMayCauseFreezeScreen((IBinder) params.get(FreezeScreenScene.TOKEN_PARAM), (LayoutParams) params.get(FreezeScreenScene.LAYOUT_PARAM))) {
                        String window = this.mCurCheckActivity.getWindow() == null ? null : this.mCurCheckActivity.getWindow().toString();
                        Log.i(FreezeScreenApplicationMonitor.TAG, "TransparentActivityScene find freezeScreen,mCurCheckActivity.getWindow():" + window);
                        ArrayMap<String, Object> paramsRadar = new ArrayMap();
                        paramsRadar.put(FreezeScreenScene.CHECK_TYPE_PARAM, Integer.valueOf(FreezeScreenScene.TRANSPARENT_ACTIVITY_SCENE));
                        paramsRadar.put(FreezeScreenScene.WINDOW_PARAM, window);
                    }
                    this.mLastCheckActivity = this.mCurCheckActivity;
                }
            }
        }

        private final boolean isWinMayCauseFreezeScreen(IBinder wtoken, LayoutParams l) {
            boolean z = false;
            if (l == null) {
                return false;
            }
            if (MonitorHelper.isFullWindow(l)) {
                z = checkViewNumIfMeetStanderd();
            }
            return z;
        }

        private boolean checkViewNumIfMeetStanderd() {
            if (this.mCurCheckActivity != null && (this.mCurCheckActivity.getWindow() instanceof PhoneWindow)) {
                Field parentContentField = MonitorHelper.getReflectPrivateField(PHONE_WINDOW_CLASS, PARENT_CONTENT_FIELD);
                if (parentContentField != null) {
                    try {
                        return checkViewNum((ViewGroup) parentContentField.get(this.mCurCheckActivity.getWindow()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }

        private final boolean checkViewNum(ViewGroup parentContent) {
            boolean z = true;
            if (parentContent != null && parentContent.getChildCount() == 1) {
                View[] childrenArray = getViewChild(parentContent);
                if (childrenArray != null && (childrenArray[0] instanceof ViewGroup)) {
                    if (childrenArray[0].getChildCount() != 0) {
                        z = false;
                    }
                    return z;
                }
            }
            return false;
        }

        private View[] getViewChild(ViewGroup parentContent) {
            Object childrenArray = null;
            Field childrenArrayField = MonitorHelper.getReflectPrivateField(VIEW_GROUP_CLASS, CHILDREN_ARRAY_FIELD);
            if (childrenArrayField != null) {
                try {
                    childrenArray = childrenArrayField.get(parentContent);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (childrenArray instanceof View[]) {
                    return (View[]) childrenArray;
                }
            }
            return null;
        }
    }

    public static synchronized FreezeScreenApplicationMonitor getInstance() {
        FreezeScreenApplicationMonitor freezeScreenApplicationMonitor;
        synchronized (FreezeScreenApplicationMonitor.class) {
            if (mAppMonitor == null) {
                mAppMonitor = new FreezeScreenApplicationMonitor();
            }
            freezeScreenApplicationMonitor = mAppMonitor;
        }
        return freezeScreenApplicationMonitor;
    }

    private FreezeScreenApplicationMonitor() {
        initScene();
        initSceneMap();
    }

    private void initScene() {
        this.mTransparentActivityScene = new TransparentActivityScene();
    }

    private void initSceneMap() {
        this.mSceneMap = new ArrayMap();
        this.mSceneMap.put("TransparentActivityScene", Integer.valueOf(FreezeScreenScene.TRANSPARENT_ACTIVITY_SCENE));
    }

    public void checkFreezeScreen(ArrayMap<String, Object> params) {
        if (params != null && (params.get(FreezeScreenScene.CHECK_TYPE_PARAM) instanceof String)) {
            switch (((Integer) this.mSceneMap.get((String) params.get(FreezeScreenScene.CHECK_TYPE_PARAM))).intValue()) {
                case FreezeScreenScene.TRANSPARENT_ACTIVITY_SCENE /*907400009*/:
                    this.mTransparentActivityScene.checkFreezeScreen(params);
                    break;
            }
        }
    }
}

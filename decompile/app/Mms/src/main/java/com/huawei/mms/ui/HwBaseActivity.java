package com.huawei.mms.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.messaging.util.BugleActivityUtil;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ActivityExWrapper;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MmsScaleSupport.MmsScaleHandler;
import com.huawei.mms.util.MmsScaleSupport.SacleListener;

public class HwBaseActivity extends Activity {
    private static final String TAG = "Mms_View";
    private final String mComponentName = (getClass().getSimpleName() + " @" + String.valueOf(hashCode()) + "  ");
    private boolean mHasSmsPermissionsForUser;
    protected int mOritation;
    protected MmsScaleHandler mScaleHandler;
    protected ActivityExWrapper mWrapper;

    protected void onCreate(Bundle savedInstanceState) {
        MLog.d(TAG, this.mComponentName + "lifecycle onCreate");
        super.onCreate(savedInstanceState);
        if (!BugleActivityUtil.onActivityCreate(this)) {
            this.mWrapper = new ActivityExWrapper(this);
            MmsConfig.checkSimpleUi();
            if (!MmsConfig.isInSimpleUI() || MmsConfig.getMmsBoolConfig("enableEasyModeAutoRotate")) {
                setRequestedOrientation(-1);
            } else {
                MLog.d(TAG, "Easy Mode - only portrait support");
                setRequestedOrientation(1);
            }
            this.mOritation = getResources().getConfiguration().orientation;
            if ("startPeekActivity".equals(getIntent().getStringExtra("android.intent.action.START_PEEK_ACTIVITY")) && ActivityExWrapper.IS_PRESS_SUPPORT) {
                this.mWrapper.run("shrinkContent");
            }
        }
    }

    public void setSupportScale(SacleListener listener) {
        if (MmsConfig.isEnableZoomWhenView()) {
            this.mScaleHandler = MmsScaleHandler.create(this, listener);
        }
    }

    public void removeSupportScale() {
        if (this.mScaleHandler != null) {
            this.mScaleHandler = null;
        }
    }

    public void setFontScale(float scale) {
        if (this.mScaleHandler != null) {
            this.mScaleHandler.setFontScale(scale);
        }
    }

    public float getFontScale() {
        if (this.mScaleHandler == null) {
            return ContentUtil.FONT_SIZE_NORMAL;
        }
        return this.mScaleHandler.getFontScale();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mScaleHandler != null && ev.getPointerCount() > 1) {
            this.mScaleHandler.onTouchEvent(ev);
            ev.setAction(3);
        }
        return super.dispatchTouchEvent(ev);
    }

    protected void onStart() {
        HwMessageUtils.disableFrameRandar(this.mComponentName + " onStart");
        MLog.d(TAG, this.mComponentName + "lifecycle onStart");
        super.onStart();
    }

    public void onUserInteraction() {
        HwMessageUtils.enableFrameRandar(this.mComponentName + " onUserInteraction");
        super.onUserInteraction();
    }

    protected void onNewIntent(Intent intent) {
        MLog.d(TAG, this.mComponentName + "lifecycle onNewIntent");
        super.onNewIntent(intent);
    }

    protected void onRestart() {
        MLog.d(TAG, this.mComponentName + "lifecycle onRestart");
        super.onRestart();
    }

    protected void onResume() {
        MLog.d(TAG, this.mComponentName + "lifecycle onResume");
        super.onResume();
        this.mHasSmsPermissionsForUser = BugleActivityUtil.onActivityResume(this, this);
    }

    protected void onPause() {
        MLog.d(TAG, this.mComponentName + "lifecycle onPause");
        super.onPause();
    }

    protected void onStop() {
        MLog.d(TAG, this.mComponentName + "lifecycle onStop");
        super.onStop();
    }

    public void onLowMemory() {
        MLog.d(TAG, this.mComponentName + "lifecycle onLowMemory");
        super.onLowMemory();
    }

    protected void onDestroy() {
        MLog.d(TAG, this.mComponentName + "lifecycle onDestroy");
        super.onDestroy();
    }

    public void onBackPressed() {
        MLog.d(TAG, this.mComponentName + "lifecycle interaction-onBackPressed");
        super.onBackPressed();
    }

    protected void setBackgroundColor(int colorId) {
        getWindow().setBackgroundDrawable(getResources().getDrawable(colorId));
    }

    protected void setBackgroundRes(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        if (drawable != null && (drawable instanceof BitmapDrawable)) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            bitmapDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            bitmapDrawable.setDither(true);
            getWindow().setBackgroundDrawable(bitmapDrawable);
        }
    }

    public static <T> T newInstance(ClassLoader loader, String className, Class<?>[] constructorSignature, Object[] arguments) {
        try {
            return loader.loadClass(className).getConstructor(constructorSignature).newInstance(arguments);
        } catch (Exception e) {
            MLog.w(TAG, "Cannot instantiate class: " + className, e);
            return null;
        }
    }

    public static final boolean isCurosrValide(Cursor cursor) {
        if (cursor != null && !cursor.isClosed() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
            return true;
        }
        MLog.e(TAG, "Bad cursor.", new RuntimeException());
        return false;
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            MessageUtils.shwNoAppDialog(this);
            MLog.e(TAG, "start activity failed >> " + e + " itent: " + (intent != null ? intent.getAction() : "is null"));
        } catch (SecurityException e2) {
            if (!BugleActivityUtil.redirectToPermissionCheckIfNeeded(this)) {
                MLog.e(TAG, "startActivityForResult fail for " + intent.getAction(), (Throwable) e2);
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isFinishing()) {
            MLog.e(TAG, this.mComponentName + "lifecycle onConfigurationChanged. Error activity is finished");
            return;
        }
        MLog.d(TAG, this.mComponentName + "lifecycle onConfigurationChanged");
        MmsConfig.checkSimpleUi();
        if (this.mOritation != newConfig.orientation) {
            onRotationChanged(this.mOritation, newConfig.orientation);
            this.mOritation = newConfig.orientation;
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        MLog.d(TAG, this.mComponentName + "lifecycle onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        MLog.d(TAG, this.mComponentName + "lifecycle onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void onRotationChanged(int oldOritation, int newOritation) {
        MLog.d(TAG, this.mComponentName + "lifecycle onRotationChanged. from " + oldOritation + " to " + newOritation);
    }

    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MessageUtils.shwNoAppDialog(this);
            MLog.e(TAG, "start activity failed >> " + e + " itent: " + (intent != null ? intent.getAction() : "is null"));
        } catch (SecurityException e2) {
            if (!BugleActivityUtil.redirectToPermissionCheckIfNeeded(this)) {
                MLog.e(TAG, "startActivityForResult fail for " + intent.getAction(), (Throwable) e2);
            }
        }
    }

    public static void startMmsActivity(Activity context, Class activity, Bundle bundle, boolean showAnimation) {
        startMmsActivity(context, activity, bundle);
        if (!showAnimation) {
            context.overridePendingTransition(0, 0);
        }
    }

    public static void startMmsActivity(Context context, Class activity, Bundle bundle) {
        Intent intent = new Intent(context, activity);
        intent.setFlags(872415232);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MLog.e(TAG, "start Mms activity failed >> " + activity);
        } catch (SecurityException e2) {
            if (!BugleActivityUtil.checkPermissionIfNeeded(context, null)) {
                MLog.e(TAG, "startActivityForResult fail for " + activity.getName(), (Throwable) e2);
            }
        }
    }

    public static void startMmsActivity(Context context, Class activity) {
        startMmsActivity(context, activity, null);
    }

    public static void gotoCellBroadCast(Context activity) {
        Intent cellBroadcastIntent = new Intent("android.intent.action.MAIN");
        cellBroadcastIntent.setComponent(new ComponentName("com.android.cellbroadcastreceiver", "com.android.cellbroadcastreceiver.ui.CellBroadcastListActivity"));
        try {
            activity.startActivity(cellBroadcastIntent);
        } catch (ActivityNotFoundException e) {
            MLog.e(TAG, "ActivityNotFoundException for CellBroadcastListActivity");
        } catch (SecurityException e2) {
            if (!BugleActivityUtil.checkPermissionIfNeeded(activity, null)) {
                MLog.e(TAG, "startActivityForResult fail for CellBroadCast");
            }
        }
    }

    public final boolean isInLandscape() {
        return getResources().getConfiguration().orientation == 2;
    }

    public void triggerSearch(String query, Bundle appSearchData) {
    }

    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
    }

    public boolean ismHasSmsPermissionsForUser() {
        return this.mHasSmsPermissionsForUser;
    }

    public void setmHasSmsPermissionsForUser(boolean hasSmsPermissionsForUser) {
        this.mHasSmsPermissionsForUser = hasSmsPermissionsForUser;
    }
}

package com.huawei.gallery.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThumbnailReporter;
import com.android.gallery3d.util.TraceController;
import com.android.gallery3d.util.Wrapper;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import com.autonavi.amap.mapcore.ERROR_CODE;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarMenuManager.OnItemSelectedListener;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.freeshare.FreeShareAdapter;
import com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil;
import com.huawei.gallery.service.MediaMountReceiver;
import com.huawei.gallery.service.MediaMountService;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.MediaSyncerHelper;
import com.huawei.gallery.util.NavigationBarHandler.Listener;
import com.huawei.gallery.util.PermissionManager;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractGalleryActivity extends FragmentActivity implements GalleryContext, OnItemSelectedListener, Listener {
    @SuppressWarnings({"DP_DO_INSIDE_DO_PRIVILEGED"})
    public static final ReflectCaller sReflectCaller = new ReflectCaller() {
        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            Method method = View.class.getDeclaredMethod("onConfigurationChanged", new Class[]{Configuration.class});
            method.setAccessible(true);
            method.invoke(para[0], new Object[]{para[1]});
            return null;
        }
    };
    private GalleryActionBar mActionBar;
    private StorageInfoAlert mAlert;
    protected boolean mCanRequestPermission = true;
    protected AbstractGalleryFragment mContent;
    private FreeShareAdapter mFreeShareAdapter;
    private boolean mHasStoragePermission = true;
    private MediaMountReceiver mMediaMountReceiver;
    private Configuration oldConfig;

    private class StorageInfoAlert extends BroadcastReceiver implements OnClickListener, OnCancelListener {
        private AlertDialog mDialog;
        private final IntentFilter mFilter;

        private StorageInfoAlert() {
            this.mDialog = null;
            this.mFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
        }

        public void onCancel(DialogInterface dialog) {
            AbstractGalleryActivity.this.finish();
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }

        public void onReceive(Context context, Intent intent) {
            if (GalleryUtils.ensureCacherDirOnlyInner(context) != null) {
                onStorageReady();
            }
        }

        private void onStorageReady() {
            if (this.mDialog != null) {
                GalleryUtils.dismissDialogSafely(this.mDialog, AbstractGalleryActivity.this);
                this.mDialog = null;
                AbstractGalleryActivity.this.unregisterReceiver(this);
            }
        }

        private void start() {
            if (GalleryUtils.ensureCacherDirOnlyInner(AbstractGalleryActivity.this.getAndroidContext()) == null && !AbstractGalleryActivity.this.isFromCamera()) {
                Builder builder = new Builder(AbstractGalleryActivity.this.getAndroidContext()).setTitle(R.string.freeshare_helper_title).setMessage(R.string.no_space_please_clean).setNegativeButton(17039360, this).setPositiveButton(R.string.clean, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            AbstractGalleryActivity.this.startActivity(new Intent("huawei.intent.action.HSM_STORAGE_CLEANER"));
                        } catch (ActivityNotFoundException e) {
                            AbstractGalleryActivity.this.startActivity(new Intent("android.settings.INTERNAL_STORAGE_SETTINGS"));
                        }
                    }
                }).setOnCancelListener(this);
                if (ApiHelper.HAS_SET_ICON_ATTRIBUTE) {
                    builder.setIconAttribute(16843605);
                } else {
                    builder.setIcon(17301543);
                }
                this.mDialog = builder.show();
                AbstractGalleryActivity.this.registerReceiver(this, this.mFilter);
            }
        }

        private void stop() {
            if (this.mDialog != null) {
                AbstractGalleryActivity.this.unregisterReceiver(this);
                this.mDialog.dismiss();
                this.mDialog = null;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        TraceController.beginSection("AbstractGalleryActivity.onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mHasStoragePermission = savedInstanceState.getBoolean("key-has-storage-permission");
        }
        new Thread(new Runnable() {
            public void run() {
                MediaSyncerHelper.registerMediaObserver(AbstractGalleryActivity.this.getAndroidContext());
            }
        }).start();
        this.mAlert = new StorageInfoAlert();
        this.oldConfig = new Configuration(getResources().getConfiguration());
        GalleryUtils.checkLayoutRTL(getApplicationContext());
        GalleryUtils.checkLanguageChanged(this, false);
        TraceController.endSection();
    }

    protected void onStart() {
        TraceController.beginSection("AbstractGalleryActivity.onStart");
        super.onStart();
        GalleryUtils.updatesCVAAMode(getApplicationContext());
        LayoutHelper.getNavigationBarHandler().addListener(this);
        this.mAlert.start();
        if (!isFromCamera() && isInMultiWindowMode()) {
            MultiWindowStatusHolder.updateMultiWindowMode(this);
            multiWindowModeChangedInternal(true);
        }
        TraceController.endSection();
    }

    protected void onResume() {
        TraceController.beginSection("AbstractGalleryActivity.onResume");
        super.onResume();
        if (DisplayEngineUtils.isDisplayEngineEnable()) {
            DisplayEngineUtils.updateEffectActivityOnResume();
        }
        ThumbnailReporter.updateCallingStack(this);
        checkPermissionsAtResume();
        notifyChangeIfStoragePermissionGranted(this);
        if (ApiHelper.HAS_MULTI_USER_STORAGE) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
            intentFilter.addAction("android.intent.action.MEDIA_EJECT");
            intentFilter.addDataScheme("file");
            if (this.mMediaMountReceiver == null) {
                this.mMediaMountReceiver = new MediaMountReceiver();
            }
            registerReceiver(this.mMediaMountReceiver, intentFilter);
            startService(new Intent(getApplicationContext(), MediaMountService.class));
        }
        TraceController.endSection();
    }

    protected void onPause() {
        super.onPause();
        if (ApiHelper.HAS_MULTI_USER_STORAGE) {
            unregisterReceiver(this.mMediaMountReceiver);
        }
        if (DisplayEngineUtils.isDisplayEngineEnable()) {
            DisplayEngineUtils.updateEffectActivityOnPause();
        }
    }

    public void onActionItemClicked(Action action) {
        if (this.mContent != null && this.mContent.isResumed() && this.mContent.getUserVisibleHint()) {
            this.mContent.onActionItemClicked(action);
        }
    }

    public void onBackPressed() {
        if (this.mContent == null || !this.mContent.isResumed() || !this.mContent.getUserVisibleHint() || !this.mContent.onBackPressed()) {
            boolean backAsHome = backAsHome();
            GalleryLog.d("AbstractGalleryActivity", "onBackPressed backAsHome: " + backAsHome);
            if (!backAsHome) {
                super.onBackPressed();
            }
        }
    }

    public boolean backAsHome() {
        return false;
    }

    static {
        new Thread() {
            public void run() {
                AbstractGalleryActivity.loadClass(PhotoPage.class.getName());
                AbstractGalleryActivity.loadClass(WideAperturePhotoUtil.class.getName());
                AbstractGalleryActivity.loadClass(EditorLoadLib.class.getName());
            }
        }.start();
    }

    private View getActionBarView() {
        View v = getWindow().getDecorView();
        if (v == null) {
            return null;
        }
        return v.findViewById(getResources().getIdentifier("action_bar", "id", "android"));
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (GalleryUtils.isTabletProduct(this)) {
            Wrapper.runCaller(sReflectCaller, getActionBarView(), newConfig);
        }
        super.onConfigurationChanged(newConfig);
        if (this.mActionBar != null) {
            this.mActionBar.onConfigurationChanged(this.oldConfig, newConfig);
        }
        this.oldConfig = new Configuration(newConfig);
        LayoutHelper.getNavigationBarHandler().update();
    }

    protected void onStop() {
        super.onStop();
        this.mAlert.stop();
        LayoutHelper.getNavigationBarHandler().removeListener(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mFreeShareAdapter != null) {
            this.mFreeShareAdapter.destroy();
            this.mFreeShareAdapter = null;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1000:
                if (-1 == resultCode && PermissionManager.checkHasPermissions(this, getPermissionsType())) {
                    requestPermissionSuccess(requestCode);
                    return;
                } else {
                    requestPermissionFailure(requestCode);
                    return;
                }
            default:
                if (this.mContent != null) {
                    this.mContent.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                return;
        }
    }

    public void setOrientationForTablet() {
        if (GalleryUtils.isTabletProduct(getApplicationContext())) {
            setRequestedOrientation(4);
        } else {
            setRequestedOrientation(5);
        }
    }

    public DataManager getDataManager() {
        return getGalleryApplication().getDataManager();
    }

    public Context getAndroidContext() {
        return this;
    }

    public Context getActivityContext() {
        return this;
    }

    public ThreadPool getThreadPool() {
        return getGalleryApplication().getThreadPool();
    }

    public GalleryApp getGalleryApplication() {
        return (GalleryApp) getApplication();
    }

    public GLRoot getGLRoot() {
        return null;
    }

    public boolean isActivityActive() {
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mContent != null && this.mContent.isResumed() && this.mContent.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mContent != null && this.mContent.isResumed() && this.mContent.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public GalleryActionBar getGalleryActionBar() {
        if (this.mActionBar == null) {
            this.mActionBar = new GalleryActionBar(this);
        }
        return this.mActionBar;
    }

    public void onNavigationBarChanged(boolean show, int height) {
        if (this.mContent != null) {
            this.mContent.onNavigationBarChanged(show, height);
        }
        if (this.mActionBar != null) {
            this.mActionBar.onNavigationBarChanged(show);
        }
    }

    public FreeShareAdapter getFreeShare() {
        if (this.mFreeShareAdapter == null) {
            this.mFreeShareAdapter = FreeShareAdapter.getInstance(getApplicationContext());
        }
        return this.mFreeShareAdapter;
    }

    private boolean isFromCamera() {
        return getIntent().getBooleanExtra("local-merge-camera-album", false);
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        MultiWindowStatusHolder.updateMultiWindowMode(this);
        multiWindowModeChangedInternal(isInMultiWindowMode);
    }

    private void multiWindowModeChangedInternal(boolean isMultiWinStatus) {
        if (this.mActionBar != null) {
            this.mActionBar.setNavigationMarginMw();
        }
        LayoutHelper.getNavigationBarHandler().update();
        onMultiWinStateModeChangedCallback();
    }

    protected int getPermissionRequestCode() {
        return 1000;
    }

    protected String[] getPermissionsType() {
        return PermissionManager.getPermissionsStorage();
    }

    private void checkPermissionsAtResume() {
        if (shouldRequestPermissions()) {
            List<String> requestPermissionList = PermissionManager.getRequestPermissionList(this, getPermissionsType());
            if (!requestPermissionList.isEmpty()) {
                PermissionManager.getInstance().requestPermissions(this, (String[]) requestPermissionList.toArray(new String[requestPermissionList.size()]), getPermissionRequestCode());
                if (requestPermissionList.contains("android.permission.READ_EXTERNAL_STORAGE")) {
                    this.mHasStoragePermission = false;
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length != 0 && permissions.length != 0) {
            switch (requestCode) {
                case 1002:
                    if (!PermissionManager.isAllGranted(grantResults)) {
                        PermissionManager.showTipsGotoPermissionSettingsIfNeed(this, PermissionManager.getPermissionCloudInviteByContacts(), requestCode);
                        break;
                    } else {
                        requestPermissionSuccess(requestCode);
                        break;
                    }
                case 1003:
                    if (!PermissionManager.isAllGranted(grantResults)) {
                        PermissionManager.showTipsGotoPermissionSettingsIfNeed(this, PermissionManager.getPermissionsLocation(), requestCode);
                        break;
                    }
                    break;
                case ERROR_CODE.CANCEL_ERROR /*1004*/:
                    if (!PermissionManager.isAllGranted(grantResults)) {
                        this.mCanRequestPermission = false;
                        if (!PermissionManager.showTipsGotoPermissionSettingsIfNeed(this, getPermissionsType(), requestCode)) {
                            requestPermissionFailure(requestCode);
                            break;
                        }
                    }
                    requestPermissionSuccess(requestCode);
                    break;
                    break;
                default:
                    if (!PermissionManager.isAllGranted(grantResults)) {
                        PermissionManager.showTipsGotoPermissionSettingsIfNeed(this, getPermissionsType(), requestCode);
                        requestPermissionFailure(requestCode);
                        break;
                    }
                    requestPermissionSuccess(requestCode);
                    break;
            }
        }
    }

    protected void requestPermissionSuccess(int requestCode) {
        getDataManager().notifyChange();
    }

    protected void requestPermissionFailure(int requestCode) {
        this.mCanRequestPermission = false;
    }

    protected boolean shouldRequestPermissions() {
        return this.mCanRequestPermission ? needToRequestPermissions() : false;
    }

    protected boolean needToRequestPermissions() {
        return true;
    }

    private void notifyChangeIfStoragePermissionGranted(Activity activity) {
        if (!this.mHasStoragePermission && PermissionManager.checkHasPermissions(activity, PermissionManager.getPermissionsStorage())) {
            this.mHasStoragePermission = true;
            getDataManager().notifyChange();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("key-has-storage-permission", this.mHasStoragePermission);
    }

    protected void onMultiWinStateModeChangedCallback() {
    }

    private static void loadClass(String clazzName) {
        try {
            Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getGalleryActionBar().onCreateOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        getGalleryActionBar().onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }
}

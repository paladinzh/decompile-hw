package com.huawei.gallery.ui;

import android.os.Handler;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.menuexecutor.MenuEnableCtrller;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import java.util.ArrayList;

public class ActionModeHandler {
    private ActionModeDelegate mActionModeDelegate;
    private final GalleryContext mActivity;
    private final Handler mMainHandler;
    private Future<?> mMenuTask;
    private final SelectionManager mSelectionManager;

    public interface ActionModeDelegate {
        ActionBarStateBase getCurrentActionBarState();

        void hasFoundVirtualFlag(int i);

        void resetVirtualFlag();
    }

    public ActionModeHandler(GalleryContext activity, SelectionManager selectionManager) {
        this.mActivity = activity;
        this.mSelectionManager = (SelectionManager) Utils.checkNotNull(selectionManager);
        this.mMainHandler = new Handler(activity.getMainLooper());
    }

    private ArrayList<MediaObject> getSelectedMediaObjects(JobContext jc) {
        ArrayList<Path> unexpandedPaths = this.mSelectionManager.getSelected(false, jc);
        if (unexpandedPaths.isEmpty()) {
            return null;
        }
        ArrayList<MediaObject> selected = new ArrayList();
        DataManager manager = this.mActivity.getDataManager();
        for (Path path : unexpandedPaths) {
            if (jc.isCancelled()) {
                return null;
            }
            selected.add(manager.getMediaObject(path));
        }
        return selected;
    }

    private int computeMenuOptions(JobContext jc, ArrayList<MediaObject> selected) {
        int operation = -1;
        int type = 0;
        int virtualFlags = 0;
        boolean hasFoundFavorite = false;
        boolean hasFoundLocalCameraVideo = false;
        boolean hasFoundDocRectify = false;
        boolean hasFound3DModelImage = false;
        boolean z = !GalleryUtils.isScreenRecorderExist();
        boolean hasFound3dPanorama = false;
        for (MediaObject mediaObject : selected) {
            if (jc.isCancelled()) {
                break;
            }
            type |= mediaObject.getMediaType();
            operation &= mediaObject.getSupportedOperations();
            if (!(this.mActionModeDelegate == null || (hasFoundFavorite && hasFoundLocalCameraVideo && z && hasFoundDocRectify && hasFound3dPanorama))) {
                virtualFlags |= mediaObject.getVirtualFlags();
                if (!hasFoundFavorite) {
                    hasFoundFavorite = findVirtualFlag(virtualFlags, 1);
                }
                if (!hasFoundLocalCameraVideo) {
                    hasFoundLocalCameraVideo = findVirtualFlag(virtualFlags, 2);
                }
                if (!hasFound3dPanorama) {
                    hasFound3dPanorama = findVirtualFlag(virtualFlags, 16);
                }
                if (!hasFoundDocRectify) {
                    hasFoundDocRectify = findVirtualFlag(virtualFlags, 4);
                }
                if (!hasFound3DModelImage) {
                    hasFound3DModelImage = findVirtualFlag(virtualFlags, 32);
                }
                if (!z) {
                    z = findVirtualFlag(virtualFlags, 8);
                }
            }
        }
        switch (selected.size()) {
            case 1:
                if (GalleryUtils.isEditorAvailable(this.mActivity.getActivityContext(), MenuExecutor.getMimeType(type))) {
                    return operation;
                }
                return operation & -513;
            default:
                return operation & 293733639;
        }
    }

    private boolean findVirtualFlag(int objectVirtualFlags, final int toBeFoundVirtualFlag) {
        if (this.mActionModeDelegate == null) {
            return false;
        }
        boolean hasFound = false;
        if ((objectVirtualFlags & toBeFoundVirtualFlag) != 0) {
            hasFound = true;
            this.mMainHandler.post(new Runnable() {
                public void run() {
                    ActionModeHandler.this.mActionModeDelegate.hasFoundVirtualFlag(toBeFoundVirtualFlag);
                }
            });
        }
        return hasFound;
    }

    public void updateSupportedOperation(final ActionBarStateBase mode) {
        if (this.mActionModeDelegate != null) {
            this.mActionModeDelegate.resetVirtualFlag();
        }
        if (this.mMenuTask != null) {
            this.mMenuTask.cancel();
            this.mMainHandler.removeCallbacksAndMessages(null);
        }
        this.mMenuTask = this.mActivity.getThreadPool().submit(new BaseJob<Void>() {
            public Void run(final JobContext jc) {
                ArrayList<MediaObject> selected = ActionModeHandler.this.getSelectedMediaObjects(jc);
                if (selected == null) {
                    return null;
                }
                final int operation = ActionModeHandler.this.computeMenuOptions(jc, selected);
                if (jc.isCancelled()) {
                    return null;
                }
                Handler -get2 = ActionModeHandler.this.mMainHandler;
                final ActionBarStateBase actionBarStateBase = mode;
                -get2.post(new Runnable() {
                    int supportOperatino = operation;

                    public void run() {
                        if (!jc.isCancelled()) {
                            if (!GalleryUtils.isAnyMapAvailable(ActionModeHandler.this.mActivity.getActivityContext())) {
                                this.supportOperatino &= -17;
                            }
                            MenuEnableCtrller.updateMenuOperation(actionBarStateBase, this.supportOperatino);
                            boolean useMoreEdit = GalleryUtils.hasMoreEditorForPic(ActionModeHandler.this.mActivity.getActivityContext());
                            if ((this.supportOperatino & 512) == 0) {
                                useMoreEdit = false;
                            }
                            actionBarStateBase.setActionEnable(useMoreEdit, Action.ACTION_ID_MORE_EDIT);
                        }
                    }
                });
                return null;
            }

            public String workContent() {
                return "update menu operation ";
            }
        });
    }

    public void pause() {
        if (this.mMenuTask != null) {
            this.mMenuTask.cancel();
            this.mMenuTask = null;
        }
    }

    public void resume() {
        if (this.mActionModeDelegate != null && this.mSelectionManager.inSelectionMode()) {
            updateSupportedOperation(this.mActionModeDelegate.getCurrentActionBarState());
        }
    }

    public void setActionModeDelegate(ActionModeDelegate delegate) {
        this.mActionModeDelegate = delegate;
    }
}

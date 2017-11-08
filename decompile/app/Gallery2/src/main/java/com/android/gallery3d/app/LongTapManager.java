package com.android.gallery3d.app;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Handler;
import android.os.RemoteException;
import com.android.gallery3d.R;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.photoshare.ui.PhotoShareAlertDialogFragment;
import com.huawei.gallery.photoshare.ui.PhotoShareAlertDialogFragment.onDialogButtonClickListener;
import com.huawei.gallery.photoshare.utils.PhotoShareNoHwAccount;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import java.util.ArrayList;

public class LongTapManager {
    private static final int[] AlbumSet_Family_Share_Meun = new int[]{R.string.photoshare_download_short, R.string.photoshare_modify_album_members, R.string.details};
    private static final int[] AlbumSet_Local_AutoUpload_Menu = new int[]{R.string.photoshare_download_short, R.string.delete, R.string.details};
    private static final int[] AlbumSet_Local_Default_Menu = new int[]{R.string.photoshare_download_short, R.string.details};
    private static final int[] AlbumSet_Receive_Items = new int[]{R.string.photoshare_download_short, R.string.photoshare_modify_album_members, R.string.photoshare_cancel_my_receive, R.string.details};
    private static final int[] AlbumSet_Share_Menu = new int[]{R.string.photoshare_add_picture, R.string.photoshare_download_short, R.string.delete, R.string.photoshare_remove_receiver_list, R.string.photoshare_modify_album_members, R.string.rename, R.string.details};
    private static final int[] AlbumSet_Share_No_Members_Menu = new int[]{R.string.photoshare_add_picture, R.string.photoshare_download_short, R.string.delete, R.string.photoshare_modify_album_members, R.string.rename, R.string.details};
    private AbstractGalleryActivity mActivity;
    private AlertDialog mAlertDialog = null;
    private ProgressDialog mDialog;
    private int[] mItemIds;
    private OnItemClickedListener mListener;
    private Path mMediaPath;
    private int mSlotIndex;

    public interface OnItemClickedListener {
        boolean onItemClicked(int i, int i2);
    }

    private class Operation extends BaseJob<Void> {
        private final int mOperation;
        private final Path mPath;

        public Operation(int operation, Path path) {
            this.mOperation = operation;
            this.mPath = path;
        }

        private void cancelShare(Handler handler, Object object, boolean result) {
            if (PhotoShareUtils.isNetworkConnected(LongTapManager.this.mActivity)) {
                if (!(object == null || ((MediaSet) object).getAlbumInfo() == null)) {
                    ShareInfo shareInfo = ((MediaSet) object).getAlbumInfo().getShareInfo();
                    if (shareInfo != null) {
                        try {
                            result = PhotoShareUtils.getServer().modifyShareRecDel(shareInfo, shareInfo.getReceiverList()) == 0;
                        } catch (RemoteException e) {
                            PhotoShareUtils.dealRemoteException(e);
                        }
                        new PhotoShareNoHwAccount(LongTapManager.this.mActivity).delete(shareInfo.getOwnerAcc(), shareInfo.getShareId());
                        if (result) {
                            PhotoShareUtils.notifyPhotoShareFolderChanged(1);
                        } else {
                            handler.post(new Runnable() {
                                public void run() {
                                    ContextedUtils.showToastQuickly(LongTapManager.this.mActivity, LongTapManager.this.mActivity.getString(R.string.photoshare_remove_receiver_list_fail, LongTapManager.this.mActivity.getString(R.string.photoshare_toast_fail_common_Toast)), 0);
                                }
                            });
                        }
                    } else {
                        return;
                    }
                }
                return;
            }
            handler.post(new Runnable() {
                public void run() {
                    ContextedUtils.showToastQuickly(LongTapManager.this.mActivity, (int) R.string.photoshare_toast_nonetwork, 0);
                }
            });
        }

        private void stopReceive(Handler handler, Object object, boolean result) {
            if (object != null) {
                try {
                    result = PhotoShareUtils.getServer().cancelReceiveShare(((MediaSet) object).getAlbumInfo().getId()) == 0;
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                }
            }
            if (!result) {
                handler.post(new Runnable() {
                    public void run() {
                        ContextedUtils.showToastQuickly(LongTapManager.this.mActivity, LongTapManager.this.mActivity.getString(R.string.photoshare_toast_cancel_receive_fail, LongTapManager.this.mActivity.getString(R.string.photoshare_toast_fail_common_Toast)), 0);
                    }
                });
            }
        }

        private void download(Handler handler, Object object) {
            if (object != null) {
                MediaSet set = (MediaSet) object;
                ArrayList<FileInfo> fileInfoArrayList = PhotoShareUtils.getFileInfo(set.getAlbumInfo().getId(), set.getAlbumType(), set);
                if (fileInfoArrayList.size() > 0) {
                    final boolean addDownLoadTaskResult = PhotoShareUtils.addDownLoadTask(fileInfoArrayList, set.getAlbumType()) == 0;
                    handler.post(new Runnable() {
                        public void run() {
                            if (addDownLoadTaskResult) {
                                PhotoShareUtils.enableDownloadStatusBarNotification(true);
                                PhotoShareUtils.refreshStatusBar(true);
                                return;
                            }
                            ContextedUtils.showToastQuickly(LongTapManager.this.mActivity, (int) R.string.photoshare_add_download_task_failed, 0);
                        }
                    });
                }
            }
        }

        public Void run(JobContext jc) {
            if (jc.isCancelled()) {
                return null;
            }
            DataManager manager = LongTapManager.this.mActivity.getDataManager();
            Handler handler = new Handler(LongTapManager.this.mActivity.getMainLooper());
            MediaObject object = manager.getMediaObject(this.mPath);
            switch (this.mOperation) {
                case R.string.delete:
                    if (object != null) {
                        ReportToBigData.reportCloudOperationWithAlbumType(67, (MediaSet) object);
                        ((MediaSet) object).getAlbumInfo().delete();
                        break;
                    }
                    break;
                case R.string.photoshare_download_short:
                    download(handler, object);
                    break;
                case R.string.photoshare_state_owner:
                case R.string.photoshare_edit_my_share:
                    manager.editPhotoShare(LongTapManager.this.mActivity, this.mPath);
                    break;
                case R.string.photoshare_cancel_my_receive:
                    stopReceive(handler, object, false);
                    break;
                case R.string.photoshare_remove_receiver_list:
                    cancelShare(handler, object, false);
                    break;
            }
            if (true) {
                handler.post(new Runnable() {
                    public void run() {
                        GalleryUtils.dismissDialogSafely(LongTapManager.this.mDialog, LongTapManager.this.mActivity);
                        LongTapManager.this.mDialog = null;
                    }
                });
            }
            return null;
        }

        public String workContent() {
            return "longTap";
        }
    }

    public LongTapManager(AbstractGalleryActivity activity) {
        this.mActivity = activity;
    }

    public void show(MediaSet set, int slotIndex) {
        this.mItemIds = loadAlbumSetItems(set);
        this.mMediaPath = set.getPath();
        this.mSlotIndex = slotIndex;
        showDialog(set.getDefaultAlbumName());
    }

    public void showDialog(String albumName) {
        int length = this.mItemIds.length;
        String[] items = new String[length];
        Resources res = this.mActivity.getResources();
        for (int i = 0; i < length; i++) {
            items[i] = res.getString(this.mItemIds[i]);
        }
        this.mAlertDialog = new Builder(this.mActivity).setItems(items, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (LongTapManager.this.mListener != null && !LongTapManager.this.mListener.onItemClicked(LongTapManager.this.mItemIds[which], LongTapManager.this.mSlotIndex)) {
                    LongTapManager.this.doAfterItemClicked(LongTapManager.this.mItemIds[which], LongTapManager.this.mMediaPath);
                }
            }
        }).setTitle(albumName).create();
        this.mAlertDialog.show();
    }

    private void doAfterItemClicked(final int action, final Path path) {
        switch (action) {
            case R.string.delete:
                int strId = R.string.photoshare_cancel_my_share_desc;
                if (RecycleUtils.supportRecycle()) {
                    strId = R.string.dialog_deleteSharedAlbum;
                }
                PhotoShareAlertDialogFragment cancelMyShareDialog = PhotoShareAlertDialogFragment.newInstance(this.mActivity.getString(R.string.photoshare_cancel_my_share), this.mActivity.getString(strId), this.mActivity.getString(R.string.delete), true);
                cancelMyShareDialog.setOnDialogButtonClickListener(new onDialogButtonClickListener() {
                    public void onPositiveClick() {
                        LongTapManager.this.mDialog = LongTapManager.this.createProgressDialog(LongTapManager.this.mActivity, R.string.photoshare_deleting);
                        LongTapManager.this.mDialog.show();
                        LongTapManager.this.execute(action, path);
                    }
                });
                cancelMyShareDialog.show(this.mActivity.getSupportFragmentManager(), "");
                break;
            case R.string.photoshare_download_short:
                ReportToBigData.report(73);
                this.mDialog = createProgressDialog(this.mActivity, R.string.photoshare_add_downloading_task);
                this.mDialog.show();
                execute(action, path);
                break;
            case R.string.photoshare_add_picture:
                break;
            case R.string.photoshare_cancel_my_receive:
                if (!shouldShowNoNetWorkTips()) {
                    PhotoShareAlertDialogFragment cancelMyReceiveDialog = PhotoShareAlertDialogFragment.newInstance(this.mActivity.getString(R.string.photoshare_cancel_my_receive), this.mActivity.getString(R.string.photoshare_cancel_my_receive_desc), this.mActivity.getString(R.string.photoshare_cancel_my_receive), true);
                    cancelMyReceiveDialog.setOnDialogButtonClickListener(new onDialogButtonClickListener() {
                        public void onPositiveClick() {
                            ReportToBigData.report(70);
                            LongTapManager.this.mDialog = LongTapManager.this.createProgressDialog(LongTapManager.this.mActivity, R.string.dialog_gallerycloud_cancelshare);
                            LongTapManager.this.mDialog.show();
                            LongTapManager.this.execute(action, path);
                        }
                    });
                    cancelMyReceiveDialog.show(this.mActivity.getSupportFragmentManager(), "");
                    break;
                }
                return;
            case R.string.photoshare_remove_receiver_list:
                ReportToBigData.report(71);
                this.mDialog = createProgressDialog(this.mActivity, R.string.photoshare_removing_receiver_list);
                this.mDialog.show();
                execute(action, path);
                break;
            default:
                execute(action, path);
                break;
        }
    }

    private void execute(int action, Path path) {
        this.mActivity.getThreadPool().submit(new Operation(action, path));
    }

    private boolean shouldShowNoNetWorkTips() {
        if (PhotoShareUtils.isNetworkConnected(this.mActivity)) {
            return false;
        }
        ContextedUtils.showToastQuickly(this.mActivity, (int) R.string.photoshare_toast_nonetwork, 0);
        return true;
    }

    private ProgressDialog createProgressDialog(Context context, int messageId) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(this.mActivity.getResources().getString(messageId));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        return dialog;
    }

    private int[] loadAlbumSetItems(MediaSet set) {
        switch (set.getAlbumType()) {
            case 1:
                return AlbumSet_Local_Default_Menu;
            case 2:
                if (set.getAlbumInfo().getReceiverCount() == 0) {
                    return AlbumSet_Share_No_Members_Menu;
                }
                return AlbumSet_Share_Menu;
            case 3:
                return AlbumSet_Receive_Items;
            case 7:
                return AlbumSet_Family_Share_Meun;
            case 10:
                return AlbumSet_Local_AutoUpload_Menu;
            default:
                return AlbumSet_Local_Default_Menu;
        }
    }

    public void setListener(OnItemClickedListener listener) {
        this.mListener = listener;
    }
}

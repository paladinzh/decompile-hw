package com.huawei.gallery.util;

import android.content.res.Resources;
import com.android.gallery3d.R;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;

public class DeleteMsgUtil {

    public interface Delegate {
        MediaItem getSelectedItem();
    }

    public static String getDeleteMsg(Resources res, DataManager dataManager, MediaSet mediaSet, int virtualFlags, int selectedCount, Delegate delegate, MediaItem mediaItem) {
        String label = null;
        if (mediaSet != null && mediaSet.isVirtual()) {
            label = mediaSet.getLabel();
        }
        boolean inFavoriteAlbum = "favorite".equalsIgnoreCase(label);
        boolean inCameraVideoAlbum = "camera_video".equalsIgnoreCase(label);
        boolean inDocRectifyAlbum = "doc_rectify".equalsIgnoreCase(label);
        boolean in3DModelAlbum = "3d_model_image".equalsIgnoreCase(label);
        boolean inScreenshotsVideoAlbum = "screenshots_video".equalsIgnoreCase(label);
        boolean in3dPanoramaAlbum = "3d_panorama".equalsIgnoreCase(label);
        if (selectedCount == 1) {
            MediaItem item;
            if (inFavoriteAlbum) {
                if ((virtualFlags & 2) != 0) {
                    return res.getString(R.string.delete_virtual_single_msg_two, new Object[]{res.getString(MediaSetUtils.getCameraAlbumStringId()), res.getString(R.string.camera_video)});
                } else if ((virtualFlags & 8) != 0) {
                    return res.getString(R.string.delete_virtual_single_msg_two, new Object[]{res.getString(MediaSetUtils.getScreenshotsAlbumStringId()), res.getString(R.string.screenshots_video)});
                } else if ((virtualFlags & 16) != 0) {
                    return res.getString(R.string.delete_virtual_single_msg_two, new Object[]{res.getString(MediaSetUtils.getCameraAlbumStringId()), res.getString(R.string.panorama)});
                } else {
                    String defaultSetName = null;
                    item = delegate == null ? mediaItem : delegate.getSelectedItem();
                    if (item != null) {
                        defaultSetName = getMediaSetAlbumName(dataManager, item);
                    }
                    if (defaultSetName != null) {
                        if ((virtualFlags & 4) != 0) {
                            return getDeleteMsgForRelativeAlbumSet(res, defaultSetName, virtualFlags, 4, R.string.folder_doc_rectify);
                        }
                        if ((virtualFlags & 32) != 0) {
                            return getDeleteMsgForRelativeAlbumSet(res, defaultSetName, virtualFlags, 32, R.string.capture_mode_3dcreator);
                        }
                        return res.getString(R.string.delete_virtual_single_msg_one, new Object[]{defaultSetName});
                    } else if (item == null || !item.isBurstCover()) {
                        return null;
                    } else {
                        return res.getQuantityString(R.plurals.delete_burst_cover_selection, dataManager.getMediaSet(item.getBurstSetPath()).getMediaItemCount(), new Object[]{Integer.valueOf(dataManager.getMediaSet(item.getBurstSetPath()).getMediaItemCount())});
                    }
                }
            } else if (inCameraVideoAlbum || in3dPanoramaAlbum) {
                return getDeleteMsgForRelativeAlbumSet(res, res.getString(MediaSetUtils.getCameraAlbumStringId()), virtualFlags, 1, R.string.virtual_folder_my_favorite_general);
            } else if (inScreenshotsVideoAlbum) {
                return getDeleteMsgForRelativeAlbumSet(res, res.getString(MediaSetUtils.getScreenshotsAlbumStringId()), virtualFlags, 1, R.string.virtual_folder_my_favorite);
            } else if (inDocRectifyAlbum) {
                item = delegate == null ? mediaItem : delegate.getSelectedItem();
                if (item == null) {
                    return null;
                }
                return getDeleteMsgForRelativeAlbumSet(res, getMediaSetAlbumName(dataManager, item), virtualFlags, 1, R.string.virtual_folder_my_favorite_general);
            } else if (in3DModelAlbum) {
                item = delegate == null ? mediaItem : delegate.getSelectedItem();
                if (item == null) {
                    return null;
                }
                return getDeleteMsgForRelativeAlbumSet(res, getMediaSetAlbumName(dataManager, item), virtualFlags, 1, R.string.virtual_folder_my_favorite_general);
            } else {
                item = delegate == null ? mediaItem : delegate.getSelectedItem();
                if (virtualFlags != 0) {
                    int kindCount = 0;
                    String favorite = null;
                    String localVideo = null;
                    if ((virtualFlags & 1) != 0) {
                        kindCount = 1;
                        favorite = res.getString(R.string.virtual_folder_my_favorite_general);
                    }
                    if ((virtualFlags & 2) != 0) {
                        kindCount++;
                        localVideo = res.getString(R.string.camera_video);
                    } else if ((virtualFlags & 8) != 0) {
                        kindCount++;
                        localVideo = res.getString(R.string.screenshots_video);
                    }
                    if ((virtualFlags & 4) != 0) {
                        kindCount++;
                        localVideo = res.getString(R.string.folder_doc_rectify);
                    }
                    if ((virtualFlags & 32) != 0) {
                        kindCount++;
                        localVideo = res.getString(R.string.capture_mode_3dcreator);
                    }
                    if ((virtualFlags & 16) != 0) {
                        kindCount++;
                        localVideo = res.getString(R.string.panorama);
                    }
                    if (kindCount == 1) {
                        String[] strArr = new Object[1];
                        if (favorite != null) {
                            localVideo = favorite;
                        }
                        strArr[0] = localVideo;
                        return res.getString(R.string.delete_virtual_single_msg_one, strArr);
                    } else if (kindCount != 2) {
                        return null;
                    } else {
                        return res.getString(R.string.delete_virtual_single_msg_two, new Object[]{favorite, localVideo});
                    }
                } else if (item == null || !item.isBurstCover()) {
                    return null;
                } else {
                    return res.getQuantityString(R.plurals.delete_burst_cover_selection, dataManager.getMediaSet(item.getBurstSetPath()).getMediaItemCount(), new Object[]{Integer.valueOf(dataManager.getMediaSet(item.getBurstSetPath()).getMediaItemCount())});
                }
            }
        } else if (virtualFlags != 0) {
            return res.getString(R.string.delete_virtual_multi_msg);
        } else {
            return null;
        }
    }

    private static String getDeleteMsgForRelativeAlbumSet(Resources res, String defaultSetName, int virtualFlags, int relativeAlbumSetFlag, int relativeAlbumSetNameId) {
        String relativeAlbumSetName = null;
        if ((virtualFlags & relativeAlbumSetFlag) != 0) {
            relativeAlbumSetName = res.getString(relativeAlbumSetNameId);
        }
        if (relativeAlbumSetName == null) {
            return res.getString(R.string.delete_virtual_single_msg_one, new Object[]{defaultSetName});
        }
        return res.getString(R.string.delete_virtual_single_msg_two, new Object[]{defaultSetName, relativeAlbumSetName});
    }

    public static String getDeleteTitle(Resources res, MediaSet mediaSet, int selectedCount, boolean isPhotoPage, boolean isHicloudAlbum, boolean isSyncedAlbum) {
        boolean isCloudOperateTogether = PhotoShareUtils.isHiCloudLoginAndCloudPhotoSwitchOpen();
        String title;
        if (isPhotoPage) {
            if (RecycleUtils.supportRecycle()) {
                if (isCloudOperateTogether) {
                    title = res.getQuantityString(R.plurals.delete_synced_photo_msg, 1, new Object[]{Integer.valueOf(30)});
                } else {
                    title = res.getQuantityString(R.plurals.delete_local_photo_msg, 1, new Object[]{Integer.valueOf(30)});
                }
            } else if (isHicloudAlbum) {
                GalleryLog.d("DeleteMsgUtil", "this is HicloudAlbum");
                title = res.getString(R.string.delete_single_cloud_title);
            } else if (isSyncedAlbum) {
                GalleryLog.d("DeleteMsgUtil", "this is SyncedAlbum");
                title = res.getString(R.string.delete_single_both_places_title);
            } else {
                GalleryLog.d("DeleteMsgUtil", "this is neihter HicloudAlbum nor SyncedAlbum");
                title = res.getString(R.string.delete_single_file_title);
            }
            return title;
        }
        int totalCount = mediaSet.getTotalMediaItemCount();
        if (!RecycleUtils.supportRecycle()) {
            boolean isDeleteAll = selectedCount == totalCount && totalCount > 1;
            if (isHicloudAlbum) {
                GalleryLog.d("DeleteMsgUtil", "this is HicloudAlbum");
                if (isDeleteAll) {
                    title = res.getString(R.string.delete_all_cloud_file);
                } else {
                    title = res.getQuantityString(R.plurals.delete_multi_cloud_title, selectedCount, new Object[]{Integer.valueOf(selectedCount)});
                }
            } else if (isSyncedAlbum) {
                GalleryLog.d("DeleteMsgUtil", "this is SyncedAlbum");
                if (isDeleteAll) {
                    title = res.getString(R.string.delete_all_local_file);
                } else {
                    title = res.getQuantityString(R.plurals.delete_multi_both_places_title, selectedCount, new Object[]{Integer.valueOf(selectedCount)});
                }
            } else {
                if (selectedCount != totalCount || totalCount <= 1) {
                    title = res.getQuantityString(R.plurals.delete_selection_title, selectedCount, new Object[]{Integer.valueOf(selectedCount)});
                } else {
                    title = res.getString(R.string.delete_all_files_title);
                }
                GalleryLog.d("DeleteMsgUtil", "this is neihter HicloudAlbum nor SyncedAlbum");
            }
        } else if (isCloudOperateTogether) {
            title = res.getQuantityString(R.plurals.delete_synced_photo_msg, selectedCount, new Object[]{Integer.valueOf(30)});
        } else {
            title = res.getQuantityString(R.plurals.delete_local_photo_msg, selectedCount, new Object[]{Integer.valueOf(30)});
        }
        return title;
    }

    private static String getMediaSetAlbumName(DataManager dataManager, MediaItem mediaItem) {
        Path albumPath = dataManager.getDefaultSetOf(mediaItem.getPath());
        if (albumPath == null) {
            GalleryLog.d("DeleteMsgUtil", "albumPath is null");
            return null;
        }
        MediaSet mediaSet = dataManager.getMediaSet(albumPath);
        if (mediaSet != null) {
            return mediaSet.getDefaultAlbumName();
        }
        GalleryLog.d("DeleteMsgUtil", "mediaSet is null from " + albumPath);
        return null;
    }
}

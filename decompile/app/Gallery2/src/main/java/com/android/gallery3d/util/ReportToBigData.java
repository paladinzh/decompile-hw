package com.android.gallery3d.util;

import android.content.Context;
import com.amap.api.services.core.AMapException;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.SelectionManager;
import com.huawei.bd.Reporter;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.photoshare.DiscoverHeadDataLoader.AlbumSet;
import com.huawei.gallery.photoshare.DiscoverHeadDataLoader.DiscoverAlbumSet;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class ReportToBigData {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static int NORMAL = 20;
    private static boolean isSupportBigData = true;
    private static Context mContext;

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 28;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 29;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 1;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 30;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 31;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 32;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 3;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 4;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 33;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 5;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 6;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 7;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 8;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 34;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 9;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 10;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 35;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 36;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 37;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 38;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 39;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 40;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 41;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 42;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 11;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 12;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 43;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 44;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 13;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 14;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 45;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 46;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 15;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 47;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 48;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 16;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 49;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 50;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 51;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 52;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 53;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 54;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 55;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 56;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 57;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 58;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 17;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 60;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 61;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 62;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 63;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 64;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 65;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 66;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 67;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 68;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 18;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 69;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 70;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 71;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 72;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 73;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 74;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 19;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 75;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 76;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 77;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 78;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 79;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 80;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 20;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 81;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 21;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 22;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 82;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 83;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 84;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 23;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 85;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 86;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 87;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 88;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 89;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 90;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 91;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 24;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 25;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 26;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 92;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 93;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 27;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 94;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 95;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 96;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 97;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 98;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = 99;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Action.WITHOUT_UPDATE.ordinal()] = 100;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Action.WITH_UPDATE.ordinal()] = 101;
        } catch (NoSuchFieldError e101) {
        }
        -com-huawei-gallery-actionbar-ActionSwitchesValues = iArr;
        return iArr;
    }

    private ReportToBigData() {
    }

    public static void initialize(Context context) {
        mContext = context;
    }

    public static void report(int eventID, String eventMsg) {
        if (isSupportBigData) {
            Reporter.e(mContext, eventID, eventMsg, NORMAL);
        }
    }

    public static void reportBeta(int eventID, String eventMsg) {
        if (isSupportBigData) {
            try {
                Reporter.beta(mContext, eventID, eventMsg);
            } catch (NoSuchMethodError e) {
                GalleryLog.w("ReportToBigData", "reportBeta error, there maybe no beta interface");
            }
        }
    }

    public static void report(int eventID) {
        if (isSupportBigData) {
            Reporter.c(mContext, eventID);
        }
    }

    public static void reportActionForFragment(String fragment, Action action, SelectionManager selectionManager) {
        if (fragment != null) {
            switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
                case 1:
                case 10:
                    report(61);
                    break;
                case 3:
                    report(99);
                    break;
                case 4:
                    report(SmsCheckResult.ESCT_143, String.format("{CollageAction:%s}", new Object[]{fragment}));
                    break;
                case 5:
                    report(22, String.format("{CopyItem:%s}", new Object[]{fragment}));
                    break;
                case 7:
                    report(23, String.format("{DeleteItem:%s}", new Object[]{fragment}));
                    break;
                case 8:
                    report(31, String.format("{DetailItem:%s}", new Object[]{fragment}));
                    break;
                case 9:
                    report(24, String.format("{EditItem:%s}", new Object[]{fragment}));
                    break;
                case 11:
                    report(49, String.format("{MoreEditItem:%s}", new Object[]{fragment}));
                    break;
                case 12:
                    report(21, String.format("{MoveItem:%s}", new Object[]{fragment}));
                    break;
                case 13:
                    report(100, String.format("{MultiScreenAction:%s}", new Object[]{"On"}));
                    break;
                case 14:
                    report(100, String.format("{MultiScreenAction:%s}", new Object[]{"Off"}));
                    break;
                case 15:
                    report(33, String.format("{FavoriteItem:%s}", new Object[]{"CancelFavorite"}));
                    break;
                case 16:
                    report(33, String.format("{FavoriteItem:%s}", new Object[]{"SetFavorite"}));
                    break;
                case 19:
                    String chooseStatus = "Single";
                    if (!"FromPhotoView".equals(fragment)) {
                        chooseStatus = processChosedStatus(selectionManager);
                    }
                    report(25, String.format("{PrintItem:%s,ChooseStatus:%s}", new Object[]{fragment, chooseStatus}));
                    break;
                case 20:
                    report(26, String.format("{RenameItem:%s}", new Object[]{fragment}));
                    break;
                case 21:
                case 22:
                    report(29, String.format("{RotateItem:%s}", new Object[]{fragment}));
                    break;
                case 23:
                    report(27, String.format("{SetAsItem:%s}", new Object[]{fragment}));
                    break;
                case 24:
                    report(34, String.format("{GallerySetting:%s}", new Object[]{fragment}));
                    break;
                case 25:
                    report(20, String.format("{ShareItem:%s}", new Object[]{fragment}));
                    break;
                case AMapException.ERROR_CODE_URL /*26*/:
                    report(30, String.format("{MapItem:%s}", new Object[]{fragment}));
                    break;
                case AMapException.ERROR_CODE_UNKNOW_HOST /*27*/:
                    report(32, String.format("{SlideShowItem:%s}", new Object[]{fragment}));
                    break;
            }
        }
    }

    private static String processChosedStatus(SelectionManager selectManager) {
        if (selectManager == null || !selectManager.inSelectionMode()) {
            return "";
        }
        if (selectManager.getSelectedCount() == 1) {
            return "Single";
        }
        return "Multi";
    }

    public static void reportChooseSourceForKeyguardMagazine(String albumLabel, int bucketId, Context context) {
        String sourceType = "Other";
        if (albumLabel == null) {
            switch (MediaSetUtils.bucketId2ResourceId(bucketId, context)) {
                case R.string.folder_screenshot:
                case R.string.screenshots:
                case R.string.screenshots_folder_only_phone:
                case R.string.screenshots_folder_only_sdcard:
                case R.string.screenshots_folder_multi_sdcard:
                    sourceType = "ScreenShoot";
                    break;
                case R.string.folder_sina_weibo_save:
                    sourceType = "Weibo";
                    break;
                case R.string.folder_qq_weixin:
                    sourceType = "Weixin";
                    break;
                case R.string.preset_pictures:
                    sourceType = "PreLoad";
                    break;
                case R.string.folder_magazine_unlock:
                    sourceType = "UnlockMagazine";
                    break;
                default:
                    break;
            }
        } else if (albumLabel.equalsIgnoreCase("camera")) {
            sourceType = "Camera";
        } else if (albumLabel.equalsIgnoreCase("favorite")) {
            sourceType = "MyFavorite";
        } else if (albumLabel.equalsIgnoreCase("screenshots")) {
            sourceType = "ScreenShoot";
        }
        report(47, String.format("{LocalMagazine:%s}", new Object[]{sourceType}));
    }

    public static void reportBigDataForUsedFilter(int filterIndex) {
        String filterType;
        switch (filterIndex) {
            case 1:
                filterType = "Pencil";
                break;
            case 2:
                filterType = "Comic";
                break;
            case 3:
                filterType = "Monochrome";
                break;
            case 4:
                filterType = "Pin focus";
                break;
            case 5:
                filterType = "Miniature";
                break;
            default:
                return;
        }
        report(44, String.format("{DualCameraFilter:%s}", new Object[]{filterType}));
    }

    public static void reportFreeshareLaunchMode(int launchMode) {
        String mode;
        if (launchMode == 1 || launchMode == 3) {
            mode = "FromMenu";
        } else if (launchMode == 2) {
            mode = "FromPhotoView";
        } else {
            return;
        }
        report(51, String.format("{DoFreeShare:%s}", new Object[]{mode}));
    }

    private static String getCurrentCloudAlbumType(MediaSet mediaSet) {
        String type = "";
        switch (mediaSet.getAlbumType()) {
            case 1:
                return "AutoUpload";
            case 2:
                if (mediaSet.getAlbumInfo().getReceiverCount() > 0) {
                    return "HaveShared";
                }
                return "NoShared";
            case 3:
                return "ReceivedShare";
            default:
                return type;
        }
    }

    public static void reportCloudOperationWithAlbumType(int eventId, MediaSet mediaSet) {
        if (mediaSet != null) {
            report(eventId, String.format("{AlbumType:%s}", new Object[]{getCurrentCloudAlbumType(mediaSet)}));
        }
    }

    public static void reportCloudUploadDownloadPage(String enterType, int pageType) {
        if (enterType == null) {
            enterType = "StatusBar";
        }
        String currentPage = "";
        switch (pageType) {
            case 1:
                currentPage = "Download";
                break;
            case 2:
                currentPage = "Upload";
                break;
        }
        report(77, String.format("{EnterType:%s,PageType:%s}", new Object[]{enterType, currentPage}));
    }

    public static void reportAddCloudPicturesWithCount(int count, boolean isFamilyAlbum) {
        report(74, String.format("{AddPicCount:%s,IsFamilyShare:%s}", new Object[]{Integer.valueOf(count), Boolean.valueOf(isFamilyAlbum)}));
    }

    public static void reportGotoCloudAlbumMember(boolean isFamilyAlbum) {
        report(68, String.format("{IsFamilyShare:%s}", new Object[]{Boolean.valueOf(isFamilyAlbum)}));
    }

    public static void reportCloudSelectActionAtAlbumPage(Action action, int count, boolean isFamilyAlbum) {
        String eventMsg;
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 2:
            case 6:
                eventMsg = String.format("{Action:%s,IsFamilyShare:%s}", new Object[]{action.toString(), Boolean.valueOf(isFamilyAlbum)});
                break;
            case 17:
            case 18:
            case 25:
                eventMsg = String.format("{Action:%s,Count:%s,IsFamilyShare:%s}", new Object[]{action.toString(), Integer.valueOf(count), Boolean.valueOf(isFamilyAlbum)});
                break;
            default:
                return;
        }
        report(90, eventMsg);
    }

    public static void reportCloudActionAtPhotoPage(Action action, boolean isFamilyAlbum) {
        report(91, String.format("{Action:%s,IsFamilyShare:%s}", new Object[]{action.toString(), Boolean.valueOf(isFamilyAlbum)}));
    }

    public static void reportForHiddenAlbumPath(String albumPath) {
        reportBeta(78, String.format("{SetHiddenAlbum:%s}", new Object[]{albumPath}));
    }

    public static void reportForFingerprintToSlidingPictures() {
        report(81);
    }

    public static void reportForAlbumMovePath(String eventMsg) {
        reportBeta(SmsCheckResult.ESCT_CODE, eventMsg);
    }

    public static void reportForDeleteLocalOrAll(int deleteFlag, String callerName) {
        if ((deleteFlag & 1) == 0 || (deleteFlag & 2) == 0) {
            report(153, String.format("{DeleteLocalPhoto:%s}", new Object[]{callerName}));
            GalleryLog.i("ReportToBigData", "only delete photo in local");
            return;
        }
        report(152, String.format("{DeleteAllPhoto:%s}", new Object[]{callerName}));
        GalleryLog.i("ReportToBigData", "delete photo in local and cloud");
    }

    public static void reportForDiscoverAlbumSet(AlbumSet albumSet, int position) {
        if (DiscoverAlbumSet.PEOPLE.equals(albumSet.entry)) {
            report(SmsCheckResult.ESCT_164);
        } else if (DiscoverAlbumSet.PLACE.equals(albumSet.entry)) {
            report(SmsCheckResult.ESCT_165);
        } else if (DiscoverAlbumSet.CATEGORY.equals(albumSet.entry)) {
            Object pathStr = null;
            MediaSet mediaSet = albumSet.getSubMediaSet(position);
            if (mediaSet != null) {
                Path path = mediaSet.getPath();
                if (path != null) {
                    pathStr = path.toString();
                }
            }
            if ("/photoshare/classify/Landscape/-1".equals(pathStr)) {
                report(SmsCheckResult.ESCT_167);
            } else if ("/photoshare/classify/1/-1".equals(pathStr)) {
                report(SmsCheckResult.ESCT_168);
            } else if ("/photoshare/classify/File_document/-1".equals(pathStr)) {
                report(SmsCheckResult.ESCT_171);
            }
        } else if (DiscoverAlbumSet.STORY.equals(albumSet.entry)) {
            report(SmsCheckResult.ESCT_184);
        }
    }

    public static void reportForDiscoverAlbumSet(AlbumSet albumSet) {
        if (DiscoverAlbumSet.STORY.equals(albumSet.entry)) {
            report(SmsCheckResult.ESCT_189);
        }
    }
}

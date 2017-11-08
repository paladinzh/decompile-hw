package com.huawei.gallery.recycle.app;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$CommonAlbumFragment;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.ActionDeleteAndConfirm.ActionDelegate;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.SelectionMode;
import com.huawei.gallery.actionbar.StandardTitleActionMode;
import com.huawei.gallery.app.SlotAlbumPage;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.ui.RecycleAlbumSlotRender;
import com.huawei.gallery.ui.AbstractCommonAlbumSlotRender;
import com.huawei.gallery.ui.SlotView.AbsLayout;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.HashMap;

public class RecycleAlbumPage extends SlotAlbumPage {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private ActionDelegate mActionDelegate = new ActionDelegate();
    private final Action[] mDefaultMenu = new Action[]{Action.RECYCLE_CLEAN_BIN};
    private final Action[] mDefaultSelectMenu = new Action[]{Action.RECYCLE_DELETE, Action.RECYCLE_RECOVERY, Action.ALL};
    private boolean mIsMenuExecutorRunning = false;

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 6;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 7;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 8;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 9;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 11;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 1;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 12;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 13;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 14;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 15;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 16;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 17;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 18;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 19;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 20;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 21;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 22;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 23;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 24;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 25;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 26;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 27;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 28;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 29;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 30;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 31;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 32;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 33;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 34;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 35;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 36;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 37;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 38;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 2;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 39;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 40;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 41;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 42;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 43;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 44;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 45;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 46;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 47;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 48;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 49;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 50;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 51;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 52;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 53;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 54;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 55;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 56;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 57;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 58;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 60;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 61;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 62;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 63;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 64;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 65;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 66;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 67;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 68;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 69;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 70;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 3;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 4;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 5;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 71;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 72;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 73;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 74;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 75;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 76;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 77;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 78;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 79;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 80;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 81;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 82;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 83;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 84;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 85;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 86;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 87;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 88;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 89;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 90;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 91;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 92;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 93;
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

    protected AbstractCommonAlbumSlotRender onCreateSlotRender(Config$CommonAlbumFragment config) {
        return new RecycleAlbumSlotRender(this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, config.placeholderColor);
    }

    protected void onInflateMenu(Menu menu) {
        this.mMenu = this.mDefaultMenu;
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(296);
        if (this.mSelectionManager.inSelectionMode() && !this.mGetContent) {
            return true;
        }
        onInflateMenu(menu);
        this.mActionBar.enterActionMode(false);
        StandardTitleActionMode am = this.mActionBar.enterStandardTitleActionMode(false);
        am.setTitle(this.mMediaSet.getName());
        am.setMenu(Math.min(5, this.mMenu.length), this.mMenu);
        am.setBothAction(Action.NONE, Action.NONE);
        am.show();
        return true;
    }

    protected void enterSelectionMode() {
        SelectionMode sm = this.mActionBar.enterSelectionMode(true);
        sm.setLeftAction(Action.NO);
        sm.setTitle((int) R.string.has_selected);
        sm.setRightAction(Action.NONE);
        this.mMenu = this.mDefaultSelectMenu;
        sm.setMenu(Math.min(5, this.mMenu.length), this.mMenu);
        sm.show();
        this.mHost.requestFeature(296);
        this.mRootPane.requestLayout();
    }

    protected void leaveSelectionMode() {
        this.mActionBar.leaveCurrentMode();
        ActionBarStateBase am = this.mActionBar.getCurrentMode();
        if (am instanceof StandardTitleActionMode) {
            ((StandardTitleActionMode) am).setTitle(this.mMediaSet.getName());
        }
        this.mRootPane.requestLayout();
    }

    protected void onResume() {
        super.onResume();
        PhotoShareUtils.refreshAlbum(2);
        new Thread(new Runnable() {
            public void run() {
                long serverTime = -1;
                try {
                    if (PhotoShareUtils.getServer() != null) {
                        serverTime = PhotoShareUtils.getServer().getServerTime();
                    }
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                }
                if (RecycleAlbumPage.this.mAlbumRender != null && (RecycleAlbumPage.this.mAlbumRender instanceof RecycleAlbumSlotRender)) {
                    ((RecycleAlbumSlotRender) RecycleAlbumPage.this.mAlbumRender).updateDaysTextureIfNeed(serverTime);
                }
            }
        }).start();
    }

    protected boolean setupEmptyButton() {
        RelativeLayout galleryRoot = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        if (galleryRoot == null) {
            return false;
        }
        this.mEmptyAlbumLayout = (LinearLayout) ((LayoutInflater) this.mHost.getActivity().getSystemService("layout_inflater")).inflate(R.layout.empty_album, galleryRoot, false);
        ((TextView) this.mEmptyAlbumLayout.findViewById(R.id.no_picture_name)).setText(R.string.content_nodeletedphotoandvideos);
        updateEmptyLayout(this.mEmptyAlbumLayout);
        galleryRoot.addView(this.mEmptyAlbumLayout);
        return true;
    }

    private void delete() {
        if (this.mIsMenuExecutorRunning) {
            GalleryLog.d("RecycleAlbumPage", "MenuExecutor is Running");
            return;
        }
        final Bundle bundle = new Bundle();
        bundle.putInt("recycle_flag", 3);
        this.mActionDelegate.setParams(this.mHost.getActivity(), this.mHost.getActivity().getResources().getQuantityString(R.plurals.delete_recycled_photo_msg, this.mSelectionManager.getSelectedCount()), this.mHost.getActivity().getString(R.string.photoshare_allow_title), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (RecycleAlbumPage.this.mIsMenuExecutorRunning) {
                    GalleryLog.d("RecycleAlbumPage", "MenuExecutor is Running");
                    return;
                }
                if (which == -1) {
                    RecycleAlbumPage.this.mIsMenuExecutorRunning = RecycleAlbumPage.this.mMenuExecutor.startAction(R.id.action_thorough_delete, R.string.delete, RecycleAlbumPage.this.mSlotDeleteProgressListener, false, true, Style.NORMAL_STYLE, null, bundle, 0);
                }
                RecycleAlbumPage.this.mRootPane.invalidate();
            }
        }, 1);
    }

    private void recovery() {
        if (this.mIsMenuExecutorRunning) {
            GalleryLog.d("RecycleAlbumPage", "MenuExecutor is Running");
            return;
        }
        Bundle bundle = new Bundle();
        int selectedCount = this.mSelectionManager.getSelectedCount();
        String toastString = this.mHost.getGalleryContext().getResources().getQuantityString(R.plurals.toast_restorephoto01, selectedCount, new Object[]{Integer.valueOf(selectedCount)});
        bundle.putInt("recycle_flag", 1);
        this.mSlotDeleteProgressListener.setOnCompleteToastContent(toastString);
        this.mIsMenuExecutorRunning = this.mMenuExecutor.startAction(R.id.action_recovery, R.string.toolbarbutton_recover, this.mSlotDeleteProgressListener, false, true, Style.NORMAL_STYLE, null, bundle, 0);
        this.mRootPane.invalidate();
    }

    protected void onDeleteProgressComplete(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap, AbsLayout layout, int result, String toastContent) {
        super.onDeleteProgressComplete(visiblePathMap, visibleIndexMap, layout, result, toastContent);
        GalleryLog.d("RecycleAlbumPage", "MenuExecutor is done");
        this.mIsMenuExecutorRunning = false;
    }

    private void clearBin() {
        final Bundle bundle = new Bundle();
        bundle.putInt("recycle_flag", 3);
        String confirmMsg = this.mHost.getActivity().getString(R.string.message_deletepopupwindow03);
        String title = this.mHost.getActivity().getString(R.string.photoshare_allow_title);
        this.mSelectionManager.setInverse(true);
        this.mActionDelegate.setDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                RecycleAlbumPage.this.mSelectionManager.setInverse(false);
            }
        });
        this.mActionDelegate.setParams(this.mHost.getActivity(), confirmMsg, title, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    RecycleAlbumPage.this.mMenuExecutor.startAction(R.id.action_thorough_delete, R.string.delete, RecycleAlbumPage.this.mSlotDeleteProgressListener, false, true, Style.NORMAL_STYLE, null, bundle, 0);
                }
                RecycleAlbumPage.this.mRootPane.invalidate();
            }
        }, 2);
    }

    protected boolean onItemSelected(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 2:
                return onBackPressed();
            case 3:
                clearBin();
                break;
            case 4:
                delete();
                break;
            case 5:
                recovery();
                break;
            default:
                return super.onItemSelected(action);
        }
        return true;
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        Resources r = this.mHost.getActivity().getResources();
        TextPaint paint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.time_line_text_size), r.getColor(R.color.photoshare_login_title_color));
        int limitWidth = (right - left) - (r.getDimensionPixelSize(R.dimen.recycle_head_tip_port_margin) * 2);
        this.mSlotView.setHeadCoverHeight((r.getDimensionPixelSize(R.dimen.recycle_head_tip_land_margin) * 2) + new StaticLayout(TextUtils.ellipsize(String.format(r.getString(R.string.text_recentlydeletedtips), new Object[]{Integer.valueOf(30)}), paint, (float) (limitWidth * 5), TruncateAt.END).toString(), paint, limitWidth, Alignment.ALIGN_CENTER, WMElement.CAMERASIZEVALUE1B1, 0.0f, false).getHeight());
        super.onGLRootLayout(left, top, right, bottom);
    }

    protected void showEmptyAlbum() {
        super.showEmptyAlbum();
        this.mActionBar.hideFootActionContainer();
    }

    protected boolean autoFinishWhenNoItems() {
        return false;
    }
}
